package Impl


import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import Objects.UserAccountService.UserPermission
import Utils.UserPermissionProcess.checkPermission
import Utils.UserManagementProcess.setMuteStatus
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
import Utils.UserManagementProcess.setMuteStatus
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class MuteUserMessagePlanner(
                                   adminToken: String,
                                   targetUserID: String,
                                   override val planContext: PlanContext
                                 ) extends Planner[String] {

  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      _ <- IO(logger.info(s"开始执行 MuteUserMessagePlanner，管理员Token: ${adminToken}, 目标用户ID: ${targetUserID}"))

      // Step 1: 检查管理员权限
      _ <- IO(logger.info(s"开始执行权限验证"))
      permission <- checkPermission(adminToken)
      _ <- if (permission != UserPermission.Admin) {
        val errorMessage = s"管理员权限验证失败，权限为: ${permission}"
        logger.error(errorMessage)
        IO.raiseError(new IllegalStateException(errorMessage))
      } else IO(logger.info(s"管理员权限验证通过，权限为: ${permission}"))

      // Step 2: 更新用户禁言状态
      _ <- IO(logger.info(s"开始更新禁言状态"))
      updateResult <- setMuteStatus(targetUserID, isMuted = true)
      _ <- IO(logger.info(s"禁言状态更新结果: ${updateResult}"))

    } yield "用户禁言成功"
  }

}