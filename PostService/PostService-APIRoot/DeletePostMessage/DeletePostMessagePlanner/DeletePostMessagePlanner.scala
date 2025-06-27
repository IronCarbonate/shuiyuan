package Impl


import Utils.PostManagementProcess.deletePost
import Objects.UserAccountService.UserPermission
import Objects.UserAccountService.User
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.slf4j.LoggerFactory
import io.circe._
import org.joda.time.DateTime
import cats.implicits._
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
import Objects.UserAccountService.User
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class DeletePostMessagePlanner(userToken: String, postID: String, override val planContext: PlanContext) extends Planner[String] {

  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      _ <- IO(logger.info(s"【DeletePostMessagePlanner】开始处理：userToken=${userToken}, postID=${postID}"))

      // Step 1: 验证用户的token有效性，并提取userID
      userID <- validateUserToken()

      // Step 2: 验证当前postID是否归属于目标的userID
      _ <- validatePostOwnership(userID, postID)

      // Step 3: 删除指定帖子记录
      result <- deletePostRecord(postID)

    } yield result
  }

  private def validateUserToken()(using planContext: PlanContext): IO[String] = {
    val checkTokenSQL = s"""
      SELECT user_id
      FROM ${schemaName}.user_table
      WHERE user_token = ?;
    """
    logger.info(s"[Step 1] 验证用户token有效性，SQL: ${checkTokenSQL}")

    for {
      userJsonOpt <- readDBJsonOptional(checkTokenSQL, List(SqlParameter("String", userToken)))
      userID <- userJsonOpt match {
        case Some(json) =>
          IO(logger.info(s"[Step 1.1] 用户token有效，提取userID.")) >>
            IO(decodeField[String](json, "user_id"))
        case None =>
          val errMsg = s"[Step 1.2] 用户token无效，终止操作。Token: ${userToken}"
          IO(logger.error(errMsg)) >>
            IO.raiseError(new IllegalStateException(errMsg))
      }
    } yield userID
  }

  private def validatePostOwnership(userID: String, postID: String)(using planContext: PlanContext): IO[Unit] = {
    val checkPostSQL = s"""
      SELECT user_id
      FROM ${schemaName}.post_table
      WHERE post_id = ?;
    """
    logger.info(s"[Step 2] 验证帖子所有权是否归目标用户所有，SQL: ${checkPostSQL}")

    for {
      postOwnerOpt <- readDBJsonOptional(checkPostSQL, List(SqlParameter("String", postID)))
      _ <- postOwnerOpt match {
        case Some(json) =>
          val postOwnerID = decodeField[String](json, "user_id")
          if (postOwnerID == userID) {
            IO(logger.info(s"[Step 2.1] 帖子所有权验证通过，postID：${postID} 属于 userID：${userID}."))
          } else {
            val errMsg = s"[Step 2.2] 帖子所有权验证失败。postID=${postID} 不属于 userID=${userID}."
            IO(logger.error(errMsg)) >>
              IO.raiseError(new IllegalStateException(errMsg))
          }
        case None =>
          val errMsg = s"[Step 2.3] 帖子ID不存在，验证失败。postID=${postID}."
          IO(logger.error(errMsg)) >>
            IO.raiseError(new IllegalStateException(errMsg))
      }
    } yield ()
  }

  private def deletePostRecord(postID: String)(using planContext: PlanContext): IO[String] = {
    for {
      _ <- IO(logger.info(s"[Step 3] 调用deletePost方法删除记录，postID: ${postID}."))
      result <- deletePost(postID)
      _ <- IO(logger.info(s"[Step 3.1] 删除结果: ${result}."))
    } yield result
  }
}