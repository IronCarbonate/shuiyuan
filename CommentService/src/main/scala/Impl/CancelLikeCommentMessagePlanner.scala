package Impl


import Objects.CommentService.CommentLikeEntry
import Objects.CommentService.Comment
import Utils.CommentManagementProcess.handleCommentLike
import Objects.AuthService.Session
import Utils.CommentManagementProcess.validateUserToken
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.slf4j.LoggerFactory
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
import Utils.CommentManagementProcess.validateUserToken

case class CancelLikeCommentMessagePlanner(
                                            userToken: String,
                                            commentID: String,
                                            override val planContext: PlanContext
                                          ) extends Planner[String] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[String] = {
    for {
      // Step 1: Validate userToken and get userID
      _ <- IO(logger.info(s"开始验证用户Token ${userToken}"))
      userID <- validateUserToken(userToken)
      _ <- IO(logger.info(s"用户Token验证成功，userID=${userID}"))

      // Step 2: Handle the "Unlike" operation
      _ <- IO(logger.info(s"开始调用handleCommentLike方法取消用户点赞"))
      result <- handleUnlike(userID, commentID)
      _ <- IO(logger.info(s"取消点赞操作完成，结果为：${result}"))
    } yield result
  }

  /**
   * Call handleCommentLike to cancel the like operation for a given user and comment.
   */
  private def handleUnlike(userID: String, commentID: String)(using PlanContext): IO[String] = {
    handleCommentLike(userID, commentID, isLike = false) // `false` indicates an "Unlike" operation
  }
}