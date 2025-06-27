import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits._
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import cats.implicits.*
import Common.DBAPI._
import Common.API.{PlanContext, Planner}
import cats.effect.IO
import Common.Object.SqlParameter
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Common.ServiceUtils.schemaName

import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

def createComment(postID: String, userID: String, content: String)(using PlanContext): IO[String] = {
  val logger = LoggerFactory.getLogger(getClass)

  for {
    // Step 1: Generate a unique comment ID and gather necessary metadata
    commentID <- IO(java.util.UUID.randomUUID().toString) // Generate a unique comment ID
    currentTime <- IO(DateTime.now()) // Get the current timestamp

    // Log the start of the method and input parameters
    _ <- IO(logger.info(s"[createComment] Start - postID=${postID}, userID=${userID}, content=${content}, currentTime=${currentTime}"))

    // Prepare SQL and parameters to insert the comment
    insertCommentSQL <- IO {
      s"""INSERT INTO ${schemaName}.comment_table (comment_id, post_id, user_id, content, created_at)
         |VALUES (?, ?, ?, ?, ?)"""
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
         |FROM ${schemaName}.post_table
         |WHERE post_id = ?"""
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
         |SET comment_count = ?, latest_comment_time = ?
         |WHERE post_id = ?"""
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