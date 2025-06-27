package Utils



//process plan import 预留标志位，不要删除
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import Common.DBAPI._
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import Common.API.{PlanContext, Planner}
import Common.Object.SqlParameter
import cats.effect.IO
import cats.implicits._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Objects.PostService.Comment
import io.circe.Json

case object CommentManagementProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除
  
  
  def createComment(postID: String, userID: String, content: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger(getClass)  // 同文后端处理: logger 统一
  
    for {
      // Step 1: Generate a unique comment ID and gather necessary metadata
      commentID <- IO(java.util.UUID.randomUUID().toString) // Generate a unique comment ID
      currentTime <- IO(DateTime.now()) // Get the current timestamp
  
      // Log the start of the method and input parameters
      _ <- IO(logger.info(s"[createComment] Start - postID=${postID}, userID=${userID}, content=${content}, currentTime=${currentTime}"))
  
      // Prepare SQL and parameters to insert the comment
      insertCommentSQL <- IO {
        s"""INSERT INTO ${schemaName}.comment_table (comment_id, post_id, user_id, content, created_at)
VALUES (?, ?, ?, ?, ?)"""
          .stripMargin
      }
      insertParams <- IO {
        List(
          SqlParameter("String", commentID),
          SqlParameter("String", postID),
          SqlParameter("String", userID),
          SqlParameter("String", content),
          SqlParameter("DateTime", currentTime.getMillis.toString)
        )
      }
  
      // Insert the comment into the database
      _ <- IO(logger.info(s"[createComment] Inserting comment with commentID=${commentID}"))
      _ <- writeDB(insertCommentSQL, insertParams)
      _ <- IO(logger.info(s"[createComment] Successfully inserted comment with commentID=${commentID}"))
  
      // Step 2: Retrieve the current comment count and latest_comment_time for the post
      fetchPostSQL <- IO {
        s"""SELECT comment_count, latest_comment_time
FROM ${schemaName}.post_table
WHERE post_id = ?"""
          .stripMargin
      }
      fetchPostParams <- IO(List(SqlParameter("String", postID)))
  
      _ <- IO(logger.info(s"[createComment] Fetching comment count and latest comment time for postID=${postID}"))
      postData <- readDBJson(fetchPostSQL, fetchPostParams)
  
      // Decode the current comment count and latest comment time from the post data
      commentCount <- IO(decodeField[Int](postData, "comment_count"))
      latestCommentTime <- IO(decodeField[DateTime](postData, "latest_comment_time"))
  
      _ <- IO(logger.info(s"[createComment] Current post stats for postID=${postID} - commentCount=${commentCount}, latestCommentTime=${latestCommentTime}"))
  
      // Step 3: Update post_table with the incremented comment count and new latest_comment_time
      updatedCommentCount <- IO(commentCount + 1)
      updatePostSQL <- IO {
        s"""UPDATE ${schemaName}.post_table
SET comment_count = ?, latest_comment_time = ?
WHERE post_id = ?"""
          .stripMargin
      }
      updatePostParams <- IO {
        List(
          SqlParameter("Int", updatedCommentCount.toString),
          SqlParameter("DateTime", currentTime.getMillis.toString),
          SqlParameter("String", postID)
        )
      }
  
      _ <- IO(logger.info(s"[createComment] Updating post stats for postID=${postID} - updatedCommentCount=${updatedCommentCount}, latestCommentTime=${currentTime}"))
      _ <- writeDB(updatePostSQL, updatePostParams)
      _ <- IO(logger.info(s"[createComment] Successfully updated post stats for postID=${postID}"))
  
      // Step 4: Log success and return the generated comment ID
      _ <- IO(logger.info(s"[createComment] Returning generated commentID=${commentID}"))
  
    } yield commentID
  }
  
  def getCommentsByPostID(postID: String)(using PlanContext): IO[List[Comment]] = {
  // val logger = LoggerFactory.getLogger(getClass)  // 同文后端处理: logger 统一
  
    if (postID.trim.isEmpty) 
      IO.raiseError(new IllegalArgumentException("postID不能为空"))
    else {
      logger.info(s"开始检索 postID=${postID} 所关联的评论记录")
  
      val sqlQuery = s"""
        SELECT comment_id, post_id, user_id, content, created_at
        FROM ${schemaName}.comment_table
        WHERE post_id = ?;
      """
  
      val parameters = List(SqlParameter("String", postID))
  
      for {
        _ <- IO(logger.info(s"执行查询评论记录的 SQL: ${sqlQuery}"))
        rows <- readDBRows(sqlQuery, parameters)
  
        comments <- IO {
          rows.map { row =>
            val commentID = decodeField[String](row, "comment_id")
            val postID = decodeField[String](row, "post_id")
            val userID = decodeField[String](row, "user_id")
            val content = decodeField[String](row, "content")
            val createdAt = new DateTime(decodeField[Long](row, "created_at"))
  
            Comment(
              commentID = commentID,
              postID = postID,
              userID = userID,
              content = content,
              createdAt = createdAt
            )
          }
        }
  
        _ <- IO(logger.info(s"成功检索到 ${comments.size} 条评论记录"))
      } yield comments
    }
  }
}

