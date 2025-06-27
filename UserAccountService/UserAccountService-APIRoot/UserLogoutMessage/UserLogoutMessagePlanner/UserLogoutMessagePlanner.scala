package Impl


import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Utils.TokenProcess.invalidateToken
import cats.effect.IO
import org.slf4j.LoggerFactory
import cats.implicits._
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
import Common.ServiceUtils.schemaName
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class UserLogoutMessagePlanner(
                                      userToken: String,
                                      override val planContext: PlanContext
                                    ) extends Planner[String] {

  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      // Step 1: Invalidate the token
      _ <- IO(logger.info(s"[UserLogoutMessagePlanner] 开始使 userToken 失效：${userToken}"))
      result <- invalidateToken(userToken)(using planContext)
      _ <- IO(logger.info(s"[UserLogoutMessagePlanner] 调用 invalidateToken 完成，结果为：${result}"))
    } yield result
  }
}