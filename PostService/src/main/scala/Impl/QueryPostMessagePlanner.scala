package Impl


import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import Objects.PostService.PostDetails
import Utils.PostValidationProcess.{doesPostExist}
import Utils.PostQueryProcess.{getPostByID}
import APIs.UserService.ValidateUserTokenMessage
import cats.effect.IO
import org.slf4j.LoggerFactory
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import cats.implicits.*
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
import Utils.PostValidationProcess.doesPostExist
import Utils.PostQueryProcess.getPostByID
import APIs.UserService.ValidateUserTokenMessage
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class QueryPostMessagePlanner(
    userToken: String,
    postID: String,
    override val planContext: PlanContext
) extends Planner[PostDetails] {

  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[PostDetails] = {
    for {
      // Step 1: Validate the user token
      userID <- validateUserToken(userToken)

      // Step 2: Check if the post exists
      _ <- checkPostExists(postID)

      // Step 3: Fetch post details by postID
      postDetails <- fetchPostDetails(postID)
    } yield postDetails
  }

  // Sub-step 1.1: Validate user token and return userID
  private def validateUserToken(userToken: String)(using PlanContext): IO[String] = {
    ValidateUserTokenMessage(userToken).send.flatMap {
      case Some(userID) =>
        IO(logger.info(s"userToken 验证成功，userID = ${userID}")) >> IO.pure(userID)
      case None =>
        val errorMsg = s"userToken 无效或已过期: ${userToken}"
        IO(logger.error(errorMsg)) >> IO.raiseError(new IllegalArgumentException(errorMsg))
    }
  }

  // Sub-step 2.1: Check if post exists
  private def checkPostExists(postID: String)(using PlanContext): IO[Unit] = {
    doesPostExist(postID).flatMap {
      case true =>
        IO(logger.info(s"帖子存在: postID = ${postID}"))
      case false =>
        val errorMsg = s"帖子不存在: postID = ${postID}"
        IO(logger.error(errorMsg)) >> IO.raiseError(new IllegalArgumentException(errorMsg))
    }
  }

  // Sub-step 3.1: Fetch post details
  private def fetchPostDetails(postID: String)(using PlanContext): IO[PostDetails] = {
    getPostByID(postID).flatMap {
      case Some(postDetails) =>
        IO(logger.info(s"成功获取帖子详情: postID = ${postID}, 标题=${postDetails.title}")) >> IO.pure(postDetails)
      case None =>
        val errorMsg = s"帖子详情不存在，postID = ${postID}"
        IO(logger.error(errorMsg)) >> IO.raiseError(new IllegalArgumentException(errorMsg))
    }
  }
}