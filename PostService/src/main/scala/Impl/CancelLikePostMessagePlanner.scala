package Impl


import Objects.CommentService.Comment
import Utils.PostManagementProcess.queryPostDetails
import Utils.PostManagementProcess.handlePostLike
import Objects.AuthService.Session
import Objects.PostService.PostTag
import Objects.PostService.Post
import Objects.PostService.PostLikeEntry
import Utils.PostManagementProcess.validateUserToken
import Objects.CommentService.CommentLikeEntry
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
import org.joda.time.DateTime
import cats.implicits.*
import Common.DBAPI._
import Common.API.{PlanContext, Planner}
import cats.effect.IO
import Common.Object.SqlParameter
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Common.ServiceUtils.schemaName
import Objects.CommentService.CommentLikeEntry

case class CancelLikePostMessagePlanner(
                                         userToken: String,
                                         postID: String,
                                         override val planContext: PlanContext
                                       ) extends Planner[String] {

  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[String] = {
    for {
      // Step 1: Validate the user token
      _ <- IO(logger.info(s"开始验证用户Token：${userToken}"))
      userID <- validateUserToken(userToken)

      // Step 2: Handle the cancel like operation
      _ <- IO(logger.info(s"开始取消帖子点赞，用户ID=${userID}，帖子ID=${postID}"))
      result <- handleCancelLike(userID, postID)

      // Step 3: Log and return the result
      _ <- IO(logger.info(s"取消点赞操作结果：${result}"))
    } yield result
  }

  private def handleCancelLike(userID: String, postID: String)(using PlanContext): IO[String] = {
    handlePostLike(userID, postID, isLike = false)
  }
}