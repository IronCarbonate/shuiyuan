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

case class QueryUnPinnedPostsMessagePlanner(
                                             userToken: String,
                                             override val planContext: PlanContext
                                           ) extends Planner[List[PostSummary]] {

  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[List[PostSummary]] = {
    for {
      // Step 1: Validate the userToken
      userId <- validateUserTokenStep(userToken)

      // Step 2: Query for non-pinned posts
      postsList <- queryNonPinnedPostsStep()

      // Log and return the final result
      _ <- IO(logger.info(s"[QueryUnPinnedPosts] 查询到非置顶帖数量: ${postsList.size}"))
    } yield postsList
  }

  private def validateUserTokenStep(userToken: String)(using PlanContext): IO[String] = {
    logger.info("[QueryUnPinnedPosts] 验证 userToken 的有效性")
    validateUserToken(userToken).map { userID =>
      logger.info(s"[QueryUnPinnedPosts] 验证通过, UserID: ${userID}")
      userID
    }
  }

  private def queryNonPinnedPostsStep()(using PlanContext): IO[List[PostSummary]] = {
    logger.info("[QueryUnPinnedPosts] 查询所有非置顶帖")
    queryPostMessage(pinned = false, tags = None).map { postsList =>
      logger.info(s"[QueryUnPinnedPosts] 查询成功，返回非置顶帖子列表")
      postsList
    }
  }
}