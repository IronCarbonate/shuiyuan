package Impl


import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import Objects.PostService.{PostSummary, PostTag}
import Utils.PostManagementProcess.{validateUserToken, queryPostMessage}
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

case class QueryPinnedPostsMessagePlanner(
                                           userToken: String,
                                           override val planContext: PlanContext
                                         ) extends Planner[List[PostSummary]] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[List[PostSummary]] = {
    for {
      // Step 1: Validate user token and fetch user ID
      _ <- IO(logger.info(s"开始验证用户token：${userToken}"))
      userId <- validateUserToken(userToken)
      _ <- IO(logger.info(s"验证通过，获取到用户ID：${userId}"))

      // Step 2: Query pinned posts
      _ <- IO(logger.info(s"开始查询所有置顶帖子"))
      pinnedPosts <- queryPinnedPosts()

      _ <- IO(logger.info(s"查询到置顶帖子数量：${pinnedPosts.size}"))
    } yield pinnedPosts
  }

  private def queryPinnedPosts()(using PlanContext): IO[List[PostSummary]] = {
    queryPostMessage(
      pinned = true,
      tags = None // No tag filtering
    )
  }
}