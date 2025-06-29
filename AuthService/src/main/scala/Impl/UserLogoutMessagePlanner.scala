package Impl


import Utils.SessionManagementProcess.{validateSession, removeSession}
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
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
import Utils.SessionManagementProcess.validateSession
import Utils.SessionManagementProcess.removeSession

case class UserLogoutMessagePlanner(
  userToken: String,
  override val planContext: PlanContext
) extends Planner[String] {
  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[String] = {
    for {
      // Step 1: Validate session
      _ <- IO(logger.info(s"开始验证会话信息，用户令牌为：${userToken}"))
      validationResult <- validateSession(userToken)
      _ <- if (validationResult != "会话有效") {
        IO.raiseError(new IllegalStateException("无效会话，请重新登录"))
      } else IO.unit
      _ <- IO(logger.info(s"会话验证成功，用户令牌为：${userToken}"))

      // Step 2: Remove user session
      _ <- IO(logger.info(s"开始清除会话信息，用户令牌为：${userToken}"))
      logoutResult <- removeSession(userToken)
      _ <- IO(logger.info(s"会话清除结果：${logoutResult}"))

      // Step 3: Return success message
      finalResult <- IO {
        logger.info("用户登出成功！")
        "用户登出成功！"
      }
    } yield finalResult
  }
}