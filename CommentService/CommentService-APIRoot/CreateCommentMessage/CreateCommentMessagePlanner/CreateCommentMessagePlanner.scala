package Impl


import Utils.CommentManagementProcess.createComment
import APIs.PostService.UpdatePostCommentInfoMessage
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
import Common.Serialize.CustomColumnTypes.{decodeDateTime, encodeDateTime}
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
import APIs.PostService.UpdatePostCommentInfoMessage
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class CreateCommentMessagePlanner(
  userToken: String,
  postID: String,
  content: String,
  override val planContext: PlanContext
) extends Planner[String] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      // Step 1: Validate user token
      _ <- IO(logger.info(s"开始验证用户Token: ${userToken}"))
      userID <- validateUserToken(userToken)

      // Step 2: Create a new comment using the utility method
      _ <- IO(logger.info(s"用户验证通过，开始创建评论，postID: ${postID}, userID: ${userID}, content: ${content}"))
      commentID <- createAndStoreComment(postID, userID, content)

      // Step 3: Notify PostService to update post comment info with comment count and latest comment time
      _ <- IO(logger.info(s"通知PostService更新帖子评论信息，postID: ${postID}, commentID: ${commentID}"))
      _ <- UpdatePostCommentInfoMessage(postID, DateTime.now()).send
      _ <- IO(logger.info(s"完成通知PostService更新，commentID: ${commentID}"))

    } yield commentID
  }

  private def validateUserToken(token: String)(using PlanContext): IO[String] = {
    val sql =
      s"""
      SELECT user_id
      FROM ${schemaName}.user_table
      WHERE token = ?;
      """
    val parameters = List(SqlParameter("String", token))
    for {
      _ <- IO(logger.info(s"验证Token使用的SQL: ${sql}"))
      userID <- readDBString(sql, parameters)
      _ <- IO(logger.info(s"用户Token验证通过，userID: ${userID}"))
    } yield userID
  }

  private def createAndStoreComment(postID: String, userID: String, content: String)(using PlanContext): IO[String] = {
    for {
      _ <- IO(logger.info(s"开始调用创建评论工具方法，postID: ${postID}, userID: ${userID}, content: ${content}"))
      commentID <- createComment(postID, userID, content)
      _ <- IO(logger.info(s"创建评论成功，生成的commentID: ${commentID}"))
    } yield commentID
  }
}