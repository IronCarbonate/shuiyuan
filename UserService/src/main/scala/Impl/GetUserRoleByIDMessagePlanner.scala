package Impl


import Utils.UserDataProcess.{validateUserToken, getUserRoleByID}
import Objects.UserService.UserRole
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
import Utils.UserDataProcess.validateUserToken
import Objects.AuthService.Session
import Utils.UserDataProcess.getUserRoleByID

case class GetUserRoleByIDMessagePlanner(
                                          userToken: String,
                                          userID: String,
                                          override val planContext: PlanContext
                                        ) extends Planner[UserRole] {
  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[UserRole] = {
    for {
      // Step 1: Validate userToken
      _ <- IO(logger.info(s"开始验证用户会话有效性，userToken: ${userToken}"))
      validatedUserID <- validateUserToken(userToken)
      _ <- IO(logger.info(s"会话验证通过，返回的userID: ${validatedUserID}"))

      // Step 2: Check if the userID matches the requested user
      _ <- if (validatedUserID != userID) {
        IO(logger.error(s"会话验证失败：请求的userID(${userID})与验证返回的userID(${validatedUserID})不一致"))
        IO.raiseError(new IllegalArgumentException("会话信息不正确，无法操作其他用户信息"))
      } else {
        IO(logger.info(s"会话信息匹配成功，userID: ${userID}"))
      }

      // Step 3: Query user's role
      _ <- IO(logger.info(s"开始查询用户角色信息，userID: ${userID}"))
      role <- getUserRoleByID(userID)
      _ <- IO(logger.info(s"用户角色查询成功，userID: ${userID}，角色: ${role}"))
    } yield role
  }
}