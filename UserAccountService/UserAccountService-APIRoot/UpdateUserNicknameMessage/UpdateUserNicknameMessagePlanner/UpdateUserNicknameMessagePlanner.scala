package Impl


import Utils.UserManagementProcess.updateNickname
import Utils.UserPermissionProcess.checkPermission
import Objects.UserAccountService.UserPermission
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
import Objects.UserAccountService.UserPermission
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class UpdateUserNicknameMessagePlanner(
                                             userToken: String,
                                             newNickname: String,
                                             override val planContext: PlanContext
                                           ) extends Planner[String] {
  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[String] = {
    for {
      // Step 1: Verify user permission
      _ <- IO(logger.info(s"[UpdateUserNickname] 验证用户权限，userToken: ${userToken}"))
      permission <- checkPermission(userToken)
      _ <- IO(logger.info(s"[UpdateUserNickname] 验证完成，权限结果: ${permission}"))

      // Step 2: Validate the permission
      _ <- if (permission != UserPermission.Normal && permission != UserPermission.Admin) {
        IO.raiseError(new IllegalStateException(s"Invalid permission: ${permission} for userToken: ${userToken}"))
      } else IO(logger.info(s"[UpdateUserNickname] 用户权限验证通过: ${permission}"))

      // Step 3: Extract user ID from userToken
      userID <- extractUserID(userToken)
      _ <- IO(logger.info(s"[UpdateUserNickname] 提取用户ID成功: ${userID}"))

      // Step 4: Update user nickname
      _ <- IO(logger.info(s"[UpdateUserNickname] 更新用户昵称，userID: ${userID}, newNickname: ${newNickname}"))
      result <- updateNickname(userID, newNickname)
      _ <- IO(logger.info(s"[UpdateUserNickname] 昵称更新成功，结果: ${result}"))
    } yield result
  }

  /**
   * 从userToken中提取用户ID
   */
  private def extractUserID(userToken: String)(using PlanContext): IO[String] = {
    logger.info(s"[ExtractUserID] 开始提取用户ID, userToken: ${userToken}")

    val sqlQuery =
      s"""
        SELECT user_id
        FROM ${schemaName}.user_table
        WHERE user_id = ?;
      """
    logger.info(s"[ExtractUserID] 构建提取用户ID的SQL指令: ${sqlQuery}")

    readDBString(sqlQuery, List(SqlParameter("String", userToken))).handleErrorWith { ex =>
      logger.error(s"[ExtractUserID] 提取用户ID失败, 错误信息: ${ex.getMessage}")
      IO.raiseError(new IllegalStateException("提取用户ID失败"))
    }
  }
}