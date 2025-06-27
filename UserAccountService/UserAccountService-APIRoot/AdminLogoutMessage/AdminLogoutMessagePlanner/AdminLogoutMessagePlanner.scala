package Impl


import Utils.TokenProcess.invalidateToken
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
import Utils.TokenProcess.invalidateToken
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class AdminLogoutMessagePlanner(
                                      adminToken: String,
                                      override val planContext: PlanContext
                                    ) extends Planner[String] {
  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  // 主计划方法
  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      _ <- IO(logger.info(s"[AdminLogoutMessagePlanner] 开始处理管理员登出请求，Token: ${adminToken}"))
      // 调用具体的子方法以使管理员Token失效
      result <- invalidateAdminToken(adminToken)
      _ <- IO(logger.info(s"[AdminLogoutMessagePlanner] 操作完成，返回结果: ${result}"))
    } yield result
  }

  private def invalidateAdminToken(token: String)(using PlanContext): IO[String] = {
    for {
      _ <- IO(logger.info(s"[invalidateAdminToken] 准备调用 invalidateToken 方法以清除登录状态，Token: ${token}"))
      result <- invalidateToken(token)
      _ <- IO(logger.info(s"[invalidateAdminToken] 调用结果: ${result}"))
    } yield result
  }
}