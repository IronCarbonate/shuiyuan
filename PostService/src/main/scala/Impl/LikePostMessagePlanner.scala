package Impl


import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import Objects.AuthService.Session
import Objects.PostService.PostLikeEntry
import Utils.PostManagementProcess.{validateUserToken, queryPostDetails, handlePostLike}
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
import Objects.CommentService.Comment
import Utils.PostManagementProcess.queryPostDetails
import Utils.PostManagementProcess.handlePostLike
import Objects.PostService.PostTag
import Objects.PostService.Post
import Utils.PostManagementProcess.validateUserToken
import Objects.CommentService.CommentLikeEntry

case class LikePostMessagePlanner(
                                   userToken: String,
                                   postID: String,
                                   override val planContext: PlanContext
                                 ) extends Planner[String] {

  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[String] = {
    for {
      // Step 1: Validate userToken to retrieve userID
      _ <- IO(logger.info(s"开始验证用户 Token：${userToken}"))
      userID <- validateUserToken(userToken)

      // Step 2: Call handlePostLike with userID, postID and isLike=true
      _ <- IO(logger.info(s"调用 handlePostLike 方法更新点赞状态，userID=${userID}, postID=${postID}"))
      result <- handlePostLike(userID, postID, isLike = true)

      _ <- IO(logger.info(s"操作结果：${result}"))
    } yield result
  }
}