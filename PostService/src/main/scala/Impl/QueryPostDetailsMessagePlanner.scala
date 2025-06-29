package Impl


import Objects.CommentService.Comment
import Objects.CommentService.CommentLikeEntry
import Objects.PostService.Post
import Objects.PostService.PostLikeEntry
import Objects.PostService.PostTag
import Utils.PostManagementProcess.queryPostDetails
import Utils.PostManagementProcess.validateUserToken
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.joda.time.DateTime
import io.circe.syntax._
import io.circe.generic.auto._
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
import Objects.AuthService.Session
import Objects.CommentService.CommentLikeEntry

case class QueryPostDetailsMessagePlanner(
                                           userToken: String,
                                           postID: String,
                                           override val planContext: PlanContext
                                         ) extends Planner[Post] {

  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[Post] = {
    for {
      // Step 1: Validate user token
      _ <- IO(logger.info(s"调用 validateUserToken 验证用户会话，userToken: ${userToken}"))
      userID <- validateUserToken(userToken)

      // Step 2: Query post details
      _ <- IO(logger.info(s"调用 queryPostDetails 查询帖子详细信息，postID: ${postID}"))
      postOpt <- queryPostDetails(postID)
      post <- postOpt match {
        case Some(post) => 
          IO.pure(post)
        case None => 
          IO.raiseError(new Exception(s"未找到 postID=${postID} 的帖子信息"))
      }
    } yield post
  }
}