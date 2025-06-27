package Impl


import APIs.UserAccountService.AdminLoginMessage
import Objects.UserAccountService.UserPermission
import Utils.PostManagementProcess.deletePost
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.joda.time.DateTime
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
import Utils.PostManagementProcess.deletePost
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class AdminDeletePostMessagePlanner(
    adminToken: String,
    postID: String,
    override val planContext: PlanContext
) extends Planner[String] {

  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      // Step 1: 验证adminToken的有效性及权限
      _ <- IO(logger.info(s"[Step 1] 开始验证Admin Token，Token：${adminToken}"))
      isValidAdmin <- validateAdminToken(adminToken)
      _ <- if (!isValidAdmin)
        IO {
          val errMsg = s"[Step 1] adminToken无效或没有管理员权限，Token=${adminToken}"
          logger.error(errMsg)
          throw new IllegalStateException(errMsg)
        }
      else IO(logger.info(s"[Step 1] adminToken验证通过，Token=${adminToken}，用户具备管理员权限"))

      // Step 2: 执行删除帖子操作
      _ <- IO(logger.info(s"[Step 2] 准备执行帖子删除操作，PostID=${postID}"))
      deleteResult <- deletePost(postID)
      _ <- IO(logger.info(s"[Step 2] 帖子删除完成，PostID=${postID}, 结果=${deleteResult}"))

      // Step 3: 返回操作结果
      _ <- IO(logger.info(s"[Step 3] 删除成功，返回结果"))
    } yield deleteResult
  }

  /**
   * 验证管理员Token是否有效，并检查是否具备管理员权限
   */
  private def validateAdminToken(adminToken: String)(using PlanContext): IO[Boolean] = {
    for {
      _ <- IO(logger.info(s"[validateAdminToken] 开始验证adminToken, Token=${adminToken}"))
      userPermissionOpt <- AdminLoginMessage(adminToken).send.map { token =>
        try {
          Some(UserPermission.fromString(token))
        } catch {
          case e: Exception =>
            logger.error(s"[validateAdminToken] adminToken解析失败, Token=${token}, Error=${e.getMessage}")
            None
        }
      }
      isAdmin <- userPermissionOpt match {
        case Some(UserPermission.Admin) =>
          IO(logger.info(s"[validateAdminToken] adminToken验证成功，具备管理员权限，Token=${adminToken}")) >> IO.pure(true)
        case Some(_) =>
          IO(logger.warn(s"[validateAdminToken] adminToken验证通过，但不是管理员权限，Token=${adminToken}")) >> IO.pure(false)
        case None =>
          IO(logger.warn(s"[validateAdminToken] adminToken验证失败，Token解析失败或无效，Token=${adminToken}")) >> IO.pure(false)
      }
    } yield isAdmin
  }
}