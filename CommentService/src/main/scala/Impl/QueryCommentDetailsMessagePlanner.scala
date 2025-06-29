package Impl


import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.slf4j.LoggerFactory
import Objects.AuthService.Session
import Objects.CommentService.{Comment, CommentLikeEntry}
import Utils.CommentManagementProcess.{queryCommentDetails, validateUserToken}
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
import Objects.CommentService.Comment
import Utils.CommentManagementProcess.queryCommentDetails
import Utils.CommentManagementProcess.validateUserToken

case class QueryCommentDetailsMessagePlanner(
                                              userToken: String,
                                              commentID: String,
                                              override val planContext: PlanContext
                                            ) extends Planner[Comment] {
  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[Comment] = {
    for {
      // Step 1: Validate user token and fetch user information
      _ <- IO(logger.info(s"开始验证用户Token：${userToken}"))
      userID <- validateUserToken(userToken)

      // Step 2: Query comment details
      _ <- IO(logger.info(s"验证成功，用户ID：${userID}，正在查询评论详情，评论ID：${commentID}"))
      commentDetailsOpt <- queryCommentDetails(commentID)

      // Step 3: Handle the result of the query
      _ <- commentDetailsOpt match {
        case Some(comment) =>
          IO(logger.info(s"评论详情查询成功，评论ID：${comment.commentID}，内容：${comment.content}"))
        case None =>
          val errorMessage = s"找不到评论ID：${commentID}的详细信息，请确认评论是否存在。"
          IO(logger.error(errorMessage)) >>
            IO.raiseError(new IllegalArgumentException(errorMessage))
      }
    } yield commentDetailsOpt.get // The type contract ensures a non-empty Option at this point
  }
}