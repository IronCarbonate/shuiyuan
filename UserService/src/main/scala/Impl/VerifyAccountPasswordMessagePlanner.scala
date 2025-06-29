package Impl


import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.ServiceUtils.schemaName
import Common.Object.SqlParameter
import cats.effect.IO
import org.slf4j.LoggerFactory
import Utils.UserDataProcess.verifyAccountPassword
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
import Utils.UserDataProcess.verifyAccountPassword

case class VerifyAccountPasswordMessagePlanner(
                                                accountName: String,
                                                password: String,
                                                override val planContext: PlanContext
                                              ) extends Planner[Boolean] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[Boolean] = {
    for {
      // Step 1: Log the initiation of the verification process
      _ <- IO(logger.info(s"开始验证账号名称为${accountName}的密码是否匹配"))

      // Step 2: Call the verification utility function
      result <- verifyPassword(accountName, password)

      // Step 3: Log the result of the verification
      _ <- IO(logger.info(s"验证结果为: ${result}"))
    } yield result
  }

  private def verifyPassword(accountName: String, password: String)(using PlanContext): IO[Boolean] = {
    for {
      _ <- IO(logger.info(s"调用verifyAccountPassword方法进行账号密码验证"))
      result <- verifyAccountPassword(accountName, password)
    } yield result
  }

}