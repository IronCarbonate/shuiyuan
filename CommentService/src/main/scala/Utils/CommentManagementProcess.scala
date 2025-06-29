package Utils

//process plan import 预留标志位，不要删除
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import Common.DBAPI._
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import cats.implicits._
import Common.API.PlanContext
import cats.effect.IO
import Common.Object.SqlParameter
import Common.Serialize.CustomColumnTypes.{decodeDateTime, encodeDateTime}
import Objects.CommentService.{CommentLikeEntry, Comment}


case object CommentManagementProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除

  def queryCommentDetails(commentID: String)(using PlanContext): IO[Option[Comment]] = {
    logger.info(s"[queryCommentDetails] 开始查询评论详情，commentID=$commentID")
    if (commentID.isEmpty) IO.pure(None)
    else {
      val verifySQL = s"SELECT * FROM ${schemaName}.comment_table WHERE comment_id = ?"
      readDBJsonOptional(verifySQL, List(SqlParameter("String", commentID))).flatMap {
        case None =>
          logger.warn(s"[queryCommentDetails] 找不到指定的评论，commentID=$commentID")
          IO.pure(None)
        case Some(js) =>
          for {
            content   <- IO(decodeField[String](js, "content"))
            createdAt <- IO(decodeField[DateTime](js, "created_at"))
            userID    <- IO(decodeField[String](js, "user_id"))
            postID    <- IO(decodeField[String](js, "post_id"))
            replyID   <- IO(decodeField[Option[String]](js, "reply_id"))
            _         <- IO(logger.info(s"[queryCommentDetails] 基本信息查询成功"))
            // 点赞
            likesJson <- readDBRows(
              s"SELECT user_id FROM ${schemaName}.comment_like_table WHERE comment_id = ?;",
              List(SqlParameter("String", commentID))
            )
            likes = likesJson.map(j => Some(CommentLikeEntry(decodeField[String](j, "user_id"))))
            _     <- IO(logger.info(s"[queryCommentDetails] 查询到点赞 ${likes.size} 条"))
            // 回复
            repliesJson <- readDBRows(
              s"SELECT comment_id FROM ${schemaName}.comment_table WHERE reply_id = ?;",
              List(SqlParameter("String", commentID))
            )
            replies = repliesJson.map(j => Some(decodeField[String](j, "comment_id")))
            _        <- IO(logger.info(s"[queryCommentDetails] 查询到回复 ${replies.size} 条"))
          } yield Some(
            Comment(
              commentID = commentID,
              replyID   = replyID,
              userID    = userID,
              postID    = postID,
              content   = content,
              createdAt = createdAt,
              likes     = likes,
              replies   = replies
            )
          )
      }
    }
  }


  def handleCommentLike(userID: String, commentID: String, isLike: Boolean)(using PlanContext): IO[String] = {
    // 先统一定义更新点赞数的 SQL
    val updateSQL =
      s"""
    UPDATE ${schemaName}.comment
    SET likes_count =
      (SELECT COUNT(*) FROM ${schemaName}.comment_like_table WHERE comment_id = ?)
    WHERE comment_id = ?
    """
    val updateParams = List(
      SqlParameter("String", commentID),
      SqlParameter("String", commentID)
    )

    for {
      _ <- IO(logger.info(s"开始验证输入参数：userID=$userID, commentID=$commentID, isLike=$isLike"))
      _ <- if (userID.isEmpty || commentID.isEmpty)
        IO.raiseError(new IllegalArgumentException("userID和commentID均不能为空！"))
      else IO.unit

      operationResult <- if (isLike) {
        val insertSQL = s"""
        INSERT INTO ${schemaName}.comment_like_table
          (comment_like_id, comment_id, user_id, node_path)
        VALUES (?, ?, ?, ?)
      """
        val params = List(
          SqlParameter("String", java.util.UUID.randomUUID.toString),
          SqlParameter("String", commentID),
          SqlParameter("String", userID),
          SqlParameter("String", "shuiyuanfull/CommentService/.../CommentLikeTable")
        )
        for {
          _ <- IO(logger.info(s"执行点赞，SQL=$insertSQL, 参数=$params"))
          _ <- writeDB(insertSQL, params)
        } yield "点赞成功！"
      } else {
        val deleteSQL =
          s"DELETE FROM ${schemaName}.comment_like_table WHERE comment_id = ? AND user_id = ?"
        val params = List(
          SqlParameter("String", commentID),
          SqlParameter("String", userID)
        )
        for {
          _ <- IO(logger.info(s"执行取消点赞，SQL=$deleteSQL, 参数=$params"))
          _ <- writeDB(deleteSQL, params)
        } yield "取消点赞成功！"
      }

      _ <- IO(logger.info("操作完成，开始更新Comment表中的点赞数"))
      _ <- IO(logger.info(s"更新点赞数，SQL=$updateSQL, 参数=$updateParams")) *> writeDB(updateSQL, updateParams)
    } yield operationResult
  }


  def validateUserToken(userToken: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger(getClass)  // 同文后端处理: logger 统一
  
    for {
      // Step 1: Log the beginning of the token validation process
      _ <- IO(logger.info(s"开始验证用户 Token: ${userToken}"))
  
      // Step 2: Check if the userToken is empty
      _ <- if (userToken.isEmpty) {
        IO(logger.error("用户 Token 为空，验证失败")) >>
        IO.raiseError(new IllegalArgumentException("用户 Token 不能为空"))
      } else IO.unit
  
      // Step 3: Construct the SQL query
      sql <- IO {
        s"""
        SELECT user_id
        FROM ${schemaName}.session
        WHERE user_token = ?
        """
      }
  
      // Step 4: Log the generated SQL for debugging
      _ <- IO(logger.info(s"生成验证用户 Token 的 SQL 查询: ${sql}"))
  
      // Step 5: Execute the SQL to check the session and retrieve the user_id
      sessionOpt <- readDBJsonOptional(sql, List(SqlParameter("String", userToken)))
  
      // Step 6: Validate the query result and extract userID if found
      userID <- sessionOpt match {
        case Some(json) =>
          for {
            extractedUserID <- IO {
              decodeField[String](json, "user_id")
            }
            _ <- IO(logger.info(s"验证成功，提取到的用户 ID: ${extractedUserID}"))
          } yield extractedUserID
  
        case None =>
          val errorMessage = s"用户 Token [${userToken}] 未能匹配到有效的会话记录，验证失败"
          IO(logger.error(errorMessage)) >>
          IO.raiseError(new IllegalArgumentException(s"无效的用户 Token: ${userToken}"))
      }
    } yield userID
  }
}
