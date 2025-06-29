package Impl


import Utils.PostManagementProcess.{validateUserToken, queryPostMessage}
import Objects.PostService.{PostSummary, PostTag}
import Common.API.{PlanContext, Planner}
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
import Utils.PostManagementProcess.validateUserToken
import Objects.PostService.PostSummary
import Objects.AuthService.Session
import Objects.PostService.PostTag
import Utils.PostManagementProcess.queryPostMessage

case class QueryUnPinnedPostsMessageByTagsPlanner(
    userToken: String,
    tags: Option[List[PostTag]],
    override val planContext: PlanContext
) extends Planner[List[PostSummary]] {

  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[List[PostSummary]] = {
    for {
      // Step 1: Validate user token and get user ID
      _ <- IO(logger.info(s"Validating user token: ${userToken}"))
      userID <- validateUserToken(userToken)
      _ <- IO(logger.info(s"Token valid for user ID: ${userID}"))
      
      // Step 2: Query posts based on tags and non-pinned status
      _ <- IO(logger.info(s"Querying non-pinned posts with tags: ${tags}"))
      postsList <- queryPostMessage(pinned = false, tags)
      _ <- IO(logger.info(s"Retrieved ${postsList.size} posts"))
      
    } yield postsList
  }
}