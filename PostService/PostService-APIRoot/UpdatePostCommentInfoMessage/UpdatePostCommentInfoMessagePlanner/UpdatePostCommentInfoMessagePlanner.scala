package Impl


import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import Objects.PostService.Post
import org.joda.time.DateTime
import cats.effect.IO
import org.slf4j.LoggerFactory
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime, encodeDateTime}
import Objects.PostService.Comment
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
import Objects.PostService.Comment
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class UpdatePostCommentInfoMessagePlanner(
                                                postID: String,
                                                commentTime: DateTime,
                                                override val planContext: PlanContext
                                              ) extends Planner[String] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      // Step 1: Validate if the post exists
      _ <- IO(logger.info(s"验证帖子是否存在: postID=${postID}"))
      postExists <- validatePostExists(postID)

      // If the post doesn't exist, raise an error and log the failure
      _ <- if (!postExists) {
        IO(logger.error(s"Post with ID ${postID} 不存在")) >>
        IO.raiseError(new IllegalStateException(s"Post with ID ${postID} does not exist."))
      } else IO.unit

      // Step 2: Update the comment count and latest comment time
      _ <- IO(logger.info(s"更新帖子评论计数和最新评论时间: postID=${postID}, commentTime=${commentTime}"))
      _ <- updatePostCommentInfo(postID, commentTime)

      // Step 3: Return the operation result
      result <- IO {
        val successMessage = s"Post with ID ${postID} successfully updated."
        logger.info(successMessage)
        successMessage
      }
    } yield result
  }

  // Step 1.1: Check if the post exists in the PostTable
  private def validatePostExists(postID: String)(using PlanContext): IO[Boolean] = {
    logger.info(s"检查帖子是否存在数据库中: postID=${postID}")
    val sql =
      s"""
SELECT EXISTS(
  SELECT 1
  FROM ${schemaName}.post_table
  WHERE post_id = ?);
""".stripMargin

    readDBBoolean(sql, List(SqlParameter("String", postID)))
  }

  // Step 2: Update the PostTable with new comment count and latest comment time
  private def updatePostCommentInfo(postID: String, commentTime: DateTime)(using PlanContext): IO[Unit] = {
    logger.info(s"准备更新帖子: postID=${postID}, 更新评论计数和最新评论时间")
    val sql =
      s"""
UPDATE ${schemaName}.post_table
SET comment_count = comment_count + 1,
    latest_comment_time = ?
WHERE post_id = ?;
""".stripMargin
    writeDB(
      sql,
      List(
        SqlParameter("DateTime", commentTime.getMillis.toString),
        SqlParameter("String", postID)
      )
    ).void
  }
}