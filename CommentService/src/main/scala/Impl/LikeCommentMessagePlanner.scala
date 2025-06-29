package Impl


import Objects.CommentService.CommentLikeEntry
import Utils.CommentManagementProcess.handleCommentLike
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
import Objects.CommentService.Comment
import Objects.AuthService.Session
import Utils.CommentManagementProcess.validateUserToken

case class LikeCommentMessagePlanner(
                                      userToken: String,
                                      commentID: String,
                                      override val planContext: PlanContext
                                    ) extends Planner[String] {

  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      // Step 1: Validate user token and retrieve user ID
      _ <- IO(logger.info(s"开始验证用户 Token: ${userToken}"))
      userID <- validateUserToken(userToken)

      // Step 2: Handle the like action on the comment
      _ <- IO(logger.info(s"用户 [${userID}] 对评论 [${commentID}] 执行点赞操作"))
      result <- handleCommentLike(userID, commentID, isLike = true)

      // Step 3: Log the result and return
      _ <- IO(logger.info(s"操作结果: ${result}"))
    } yield result
  }

}