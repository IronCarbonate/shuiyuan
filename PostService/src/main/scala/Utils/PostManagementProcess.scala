package Utils

//process plan import 预留标志位，不要删除
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import Common.DBAPI._
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import Objects.PostService.{PostSummary, PostTag}
import Common.API.{PlanContext, Planner}
import Common.Object.SqlParameter
import cats.effect.IO
import cats.implicits._
import Common.Serialize.CustomColumnTypes.{decodeDateTime, encodeDateTime}
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Objects.PostService.PostSummary
import Objects.PostService.PostTag
import Objects.AuthService.Session
import Common.API.PlanContext
import Common.DBAPI.{readDBJsonOptional, decodeField, writeDB}
import Objects.CommentService.CommentLikeEntry
import Objects.CommentService.Comment
import Utils.PostManagementProcess.queryPostDetails
import Objects.PostService.Post
import Objects.PostService.PostLikeEntry
import APIs.UserService.GetUserRoleByIDMessage
import Objects.UserService.UserRole
import Utils.PostManagementProcess.validateUserToken
import Common.Object.ParameterList
import Common.DBAPI.{writeDB, readDBJsonOptional, decodeField}
import io.circe.Json

case object PostManagementProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除
  
  def queryPostMessage(
    pinned: Boolean,
    tags: Option[List[PostTag]]
  )(using PlanContext): IO[List[PostSummary]] = {
  // val logger = LoggerFactory.getLogger("queryPostMessage")  // 同文后端处理: logger 统一
    logger.info(s"[queryPostMessage] 开始查询帖子信息，Pinned = ${pinned}，Tags = ${tags}")
  
    val postFilterSql = IO {
      val tagsCondition = tags match {
        case Some(tagList) => s"AND tag = ANY(?)"
        case None => ""
      }
      val pinnedCondition = if (pinned) "AND is_pinned = TRUE" else "AND is_pinned = FALSE"
  
      s"""
        SELECT post_id, user_id, title, tag, is_pinned, updated_at
        FROM ${schemaName}.post_table
        WHERE 1 = 1
        ${pinnedCondition}
        ${tagsCondition}
      """
    }
  
    val postFilterParams = IO {
      val pinnedParam = SqlParameter("Boolean", pinned.toString)
      val tagsParam = tags.map(tagList => SqlParameter("Array[String]", tagList.map(_.toString).asJson.noSpaces))
      List(Some(pinnedParam), tagsParam).flatten
    }
  
    for {
      postSql <- postFilterSql
      postParams <- postFilterParams
      _ <- IO(logger.info(s"[queryPostMessage] 执行帖子筛选 SQL: ${postSql}，参数=${postParams}"))
      postRecords <- readDBRows(postSql, postParams)
  
      postWithLatestComments <- IO {
        postRecords.map { postJson =>
          val postId = decodeField[String](postJson, "post_id")
          val updatedAt = decodeField[DateTime](postJson, "updated_at")
          logger.debug(s"[queryPostMessage] 帖子 ID=${postId}, 最后更新时间=${updatedAt}")
          postId -> updatedAt
        }
      }
  
      sortedPostIds <- IO {
        postWithLatestComments.sortBy(_._2.getMillis)(Ordering[Long].reverse).map(_._1)
      }
  
      postsList <- IO {
        val postsSummary = sortedPostIds.flatMap { postId =>
          val postJsonOpt = postRecords.find(json => decodeField[String](json, "post_id") == postId)
          postJsonOpt.map { postJson =>
            val postId = decodeField[String](postJson, "post_id")
            val userId = decodeField[String](postJson, "user_id")
            val title = decodeField[String](postJson, "title")
            val updateTime = decodeField[DateTime](postJson, "updated_at").toString
            val numComment = 0 // 假设评论数为预置值，这里可以之后通过查询其他表更新
            PostSummary(postId, userId, title, "", numComment, updateTime)
          }
        }
        logger.info(s"[queryPostMessage] 查询帖子数量为 ${postsSummary.size}")
        postsSummary
      }
    } yield postsList
  }
  
  def validateUserToken(userToken: String)(using PlanContext): IO[String] = {
    logger.info(s"Validating userToken: ${userToken}")
    
    // Step 1. Query the session_table to verify the userToken.
    val sqlQuery = 
      s"""
        SELECT user_id, user_token, expiration_date
        FROM ${schemaName}.session_table
        WHERE user_token = ?
      """
  
    val parameters = List(SqlParameter("String", userToken))
    
    for {
      // Attempt to find a matching session.
      sessionOpt <- readDBJsonOptional(sqlQuery, parameters)
      userID <- sessionOpt match {
        case Some(sessionJson) =>
          // Parse userID and expirationDate from the session record
          val userID = decodeField[String](sessionJson, "user_id")
          val expirationDate = decodeField[DateTime](sessionJson, "expiration_date")
  
          if (expirationDate.isBeforeNow) {
            // If the session has expired, raise an error
            logger.info(s"The session for userToken=${userToken} has expired")
            IO.raiseError(new Exception("无效会话，请重新登录"))
          } else {
            // If the session is valid, return the userID
            logger.info(s"Session is valid for userID=${userID}")
            IO.pure(userID)
          }
  
        case None =>
          // If no matching session is found, raise an error
          logger.info(s"No matching session found for userToken=${userToken}")
          IO.raiseError(new Exception("无效会话，请重新登录"))
      }
    } yield userID
  }
  
  
  def updatePinnedStatus(postID: String, isPinned: Boolean)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger("updatePinnedStatus")  // 同文后端处理: logger 统一
    val fetchSql =
      s"""
         SELECT * 
         FROM ${schemaName}.post_table 
         WHERE post_id = ?
       """.stripMargin
  
    val updateSql =
      s"""
         UPDATE ${schemaName}.post_table 
         SET is_pinned = ?, updated_at = ? 
         WHERE post_id = ?
       """.stripMargin
  
    for {
      // Step 1: 检查 postID 是否存在
      _ <- IO(logger.info(s"开始检查 postID 是否有效: ${postID}"))
      existingPostOpt <- readDBJsonOptional(fetchSql, List(SqlParameter("String", postID)))
      _ <- existingPostOpt match {
        case Some(_) =>
          IO(logger.info(s"postID: ${postID} 已找到，可以进行更新"))
        case None =>
          val errorMessage = s"postID: ${postID} 不存在"
          IO(logger.error(errorMessage)) >>
            IO.raiseError(new IllegalArgumentException(errorMessage))
      }
  
      // Step 2: 更新帖子置顶状态
      currentTime <- IO(DateTime.now())
      updateParams <- IO(
        List(
          SqlParameter("Boolean", isPinned.toString),
          SqlParameter("DateTime", currentTime.getMillis.toString),
          SqlParameter("String", postID)
        )
      )
      _ <- IO(logger.info(s"准备更新帖子置顶状态，SQL: ${updateSql}, 参数: ${updateParams.mkString(", ")}"))
      _ <- writeDB(updateSql, updateParams)
  
      // Step 3: 返回操作结果
      _ <- IO(logger.info("帖子置顶状态更新完成"))
    } yield "状态更新成功！"
  }
  
  def handlePostLike(
      userID: String,
      postID: String,
      isLike: Boolean
  )(using PlanContext): IO[String] = {
    
    if (userID.isEmpty) {
      IO.raiseError(new IllegalArgumentException("用户ID不能为空"))
    } else if (postID.isEmpty) {
      IO.raiseError(new IllegalArgumentException("帖子ID不能为空"))
    } else {
      for {
        _ <- IO(logger.info(s"处理帖子点赞信息: 用户ID=${userID}, 帖子ID=${postID}, 点赞操作=${if (isLike) "点赞" else "取消点赞"}"))
  
        // 查询贴子详细信息
        postDetailsOpt <- queryPostDetails(postID)
        postDetails <- postDetailsOpt match {
          case Some(details) => IO(details)
          case None =>
            IO.raiseError(new IllegalArgumentException(s"帖子ID[${postID}]不存在"))
        }
  
        currentLikes <- IO(postDetails.likes)
  
        updatedLikes <- IO {
          if (isLike) {
            // 如果点赞，增加点赞用户信息
            if (!currentLikes.exists(_.userID == userID)) {
              currentLikes :+ PostLikeEntry(userID)
            } else {
              currentLikes
            }
          } else {
            // 如果取消点赞，移除点赞用户信息
            currentLikes.filterNot(_.userID == userID)
          }
        }
  
        _ <- if (isLike) {
          val insertSQL =
            s"""
            INSERT INTO ${schemaName}.post_like_table (post_like_id, post_id, user_id)
            VALUES (?, ?, ?)
            """
          val insertParams = List(
            SqlParameter("String", java.util.UUID.randomUUID().toString),
            SqlParameter("String", postID),
            SqlParameter("String", userID)
          )
          IO(logger.info(s"插入点赞记录 SQL: ${insertSQL}, 参数: ${insertParams.map(_.value).mkString(", ")}")) >>
          writeDB(insertSQL, insertParams)
        } else {
          val deleteSQL =
            s"""
            DELETE FROM ${schemaName}.post_like_table
            WHERE post_id = ? AND user_id = ?
            """
          val deleteParams = List(
            SqlParameter("String", postID),
            SqlParameter("String", userID)
          )
          IO(logger.info(s"删除点赞记录 SQL: ${deleteSQL}, 参数: ${deleteParams.map(_.value).mkString(", ")}")) >>
          writeDB(deleteSQL, deleteParams)
        }
  
        updatePostSQL <- IO {
          s"""
          UPDATE ${schemaName}.post_table
          SET likes = ?
          WHERE post_id = ?
          """
        }
        serializedLikes <- IO(updatedLikes.asJson.noSpaces)
        updateParams <- IO {
          List(
            SqlParameter("String", serializedLikes),
            SqlParameter("String", postID)
          )
        }
  
        _ <- IO(logger.info(s"更新帖子点赞列表 SQL: ${updatePostSQL}, 参数: ${updateParams.map(_.value).mkString(", ")}")) >>
        writeDB(updatePostSQL, updateParams)
  
        result <- IO(if (isLike) "点赞成功！" else "取消点赞成功！")
        _ <- IO(logger.info(s"操作结果: ${result}"))
  
      } yield result
    }
  }
  
  def deletePost(userToken: String, userID: String, postID: String)(using PlanContext): IO[String] = {
    logger.info(s"开始处理帖子删除请求，userID = ${userID}, postID = ${postID}")
  
    for {
      // Step 1: 查询帖子详情
      postDetailsOpt <- queryPostDetails(postID)
      postDetails <- postDetailsOpt match {
        case Some(post) => IO.pure(post)
        case None =>
          logger.info(s"未找到对应的帖子，postID = ${postID}")
          IO.raiseError(new Exception("帖子不存在"))
      }
  
      // Step 2: 检查删除权限（拥有该帖子或为管理员）
      _ <- if (postDetails.userID != userID) {
        logger.info(s"当前用户不是该帖子的作者，检查用户角色")
        for {
          role <- GetUserRoleByIDMessage(userToken, userID).send
          _ <- if (role.toString != UserRole.Admin.toString) {
            logger.info(
              s"用户权限不足，不能删除帖子。用户角色为${role.toString}"
            )
            IO.raiseError(
              new Exception("权限不足，只有作者或管理员可以删除帖子")
            )
          } else {
            logger.info(s"用户是管理员，权限验证通过")
            IO.unit
          }
        } yield ()
      } else {
        logger.info(s"用户是帖子作者，权限验证通过")
        IO.unit
      }
  
      // Step 3: 删除帖子记录
      deletePostSQL = s"DELETE FROM ${schemaName}.post_table WHERE post_id = ?"
      deleteParameters = List(SqlParameter("String", postID))
      _ <- writeDB(deletePostSQL, deleteParameters)
  
      // 返回成功结果
      resultMessage = "帖子删除成功！"
      _ <- IO(logger.info(s"已成功删除帖子，postID = ${postID}"))
    } yield resultMessage
  }
  
  def createPost(userID: String, title: String, content: String, tag: PostTag)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger("createPost")  // 同文后端处理: logger 统一
  
    for {
      // Step 1: Validate input parameters
      _ <- IO {
        if (userID.isEmpty) {
          val msg = "用户ID不能为空"
          logger.error(msg)
          throw new IllegalArgumentException(msg)
        }
        if (title.isEmpty || content.isEmpty) {
          val msg = "帖子标题或内容不能为空"
          logger.error(msg)
          throw new IllegalArgumentException(msg)
        }
      }
      _ <- IO {
        try {
          PostTag.fromString(tag.toString) // Ensures tag is a valid PostTag enum
        } catch {
          case _: Exception =>
            val msg = s"非法的标签值：${tag}"
            logger.error(msg)
            throw new IllegalArgumentException(msg)
        }
      }
  
      // Step 2.1: Generate unique postID, createdAt, and updatedAt
      postID <- IO(java.util.UUID.randomUUID().toString)
      createdAt <- IO(DateTime.now())
      updatedAt <- IO(createdAt)
  
      // Step 2.2: SQL query and insert operation
      sqlInsert <- IO {
        s"""
           INSERT INTO ${schemaName}.post_table (post_id, user_id, title, content, tag, is_pinned, created_at, updated_at)
           VALUES (?, ?, ?, ?, ?, ?, ?, ?);
         """
      }
      insertParameters <- IO {
        List(
          SqlParameter("String", postID),
          SqlParameter("String", userID),
          SqlParameter("String", title),
          SqlParameter("String", content),
          SqlParameter("String", tag.toString),
          SqlParameter("Boolean", "false"), // Default is not pinned
          SqlParameter("DateTime", createdAt.getMillis.toString),
          SqlParameter("DateTime", updatedAt.getMillis.toString)
        )
      }
  
      _ <- IO(logger.info(s"即将写入帖子信息到数据库。SQL语句：${sqlInsert}，参数：${insertParameters.map(_.value).mkString(", ")}"))
      _ <- writeDB(sqlInsert, insertParameters)
  
      // Step 3: Return the generated postID
      _ <- IO(logger.info(s"帖子创建成功，生成的帖子ID为：${postID}"))
    } yield postID
  }
  
  def queryPostDetails(postID: String)(using PlanContext): IO[Option[Post]] = {
    logger.info(s"开始查询帖子详情，postID = ${postID}")
  
    // 验证帖子是否存在
    val validateQuery =
      s"SELECT post_id, user_id, title, content, tag, is_pinned, created_at, updated_at FROM ${schemaName}.post_table WHERE post_id = ?;"
    val validateParams = List(SqlParameter("String", postID))
  
    for {
      postJsonOpt <- readDBJsonOptional(validateQuery, validateParams)
      postOpt <- postJsonOpt match {
        case Some(postJson) =>
          val postID = decodeField[String](postJson, "post_id")
          val userID = decodeField[String](postJson, "user_id")
          val title = decodeField[String](postJson, "title")
          val content = decodeField[String](postJson, "content")
          val tag = decodeField[String](postJson, "tag")
          val isPinned = decodeField[Boolean](postJson, "is_pinned")
          val createdAt = decodeField[DateTime](postJson, "created_at")
          val updatedAt = decodeField[DateTime](postJson, "updated_at")
  
          logger.info(s"帖子基本信息已成功读取，postID = ${postID}, title = ${title}, userID = ${userID}")
  
          // 将标签从字符串解析为 PostTag 枚举
          val postTag = PostTag.fromString(tag)
  
          // 获取帖子点赞数
          val likeCountQuery =
            s"SELECT COUNT(*) FROM ${schemaName}.post_like_table WHERE post_id = ?;"
          val likeCountParams = validateParams
          val likesIO = readDBInt(likeCountQuery, likeCountParams)
          val likesListIO = likesIO.map { likeCount =>
            List.fill(likeCount)(PostLikeEntry(userID)) // 假设点赞用户ID未知，用模拟形式填充
          }
  
          // 查询帖子评论
          val commentQuery =
            s"SELECT comment_id, reply_id, user_id, post_id, content, created_at FROM ${schemaName}.comments_table WHERE post_id = ?;"
          val commentParams = validateParams
          val commentsIO = readDBRows(commentQuery, commentParams).map { rows =>
            rows.map { row =>
              val commentID = decodeField[String](row, "comment_id")
              val replyIDOpt = decodeField[Option[String]](row, "reply_id")
              val commentUserID = decodeField[String](row, "user_id")
              val commentPostID = decodeField[String](row, "post_id")
              val commentContent = decodeField[String](row, "content")
              val commentCreatedAt = decodeField[DateTime](row, "created_at")
  
              Comment(
                commentID = commentID,
                replyID = replyIDOpt,
                userID = commentUserID,
                postID = commentPostID,
                content = commentContent,
                createdAt = commentCreatedAt,
                likes = List.empty[Option[CommentLikeEntry]], // 评论点赞信息未知，设置为None
                replies = List.empty[Option[String]] // 评论回复信息未知，设置为None
              )
            }
          }
  
          for {
            likes <- likesListIO
            comments <- commentsIO
          } yield {
            Some(
              Post(
                postID = postID,
                userID = userID,
                title = title,
                content = content,
                tag = postTag,
                isPinned = isPinned,
                createdAt = createdAt,
                updatedAt = updatedAt,
                likes = likes,
                comments = comments
              )
            )
          }
  
        case None =>
          logger.info(s"未找到对应的帖子记录，postID = ${postID}")
          IO.pure(None)
      }
    } yield postOpt
  }
}
