package Impl


import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import Utils.UserDataProcess.getUserByAccountName
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
import Utils.UserDataProcess.getUserByAccountName

case class GetUserByAccountNameMessagePlanner(
                                               accountName: String,
                                               override val planContext: PlanContext
                                             ) extends Planner[Option[String]] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[Option[String]] = {
    IO(logger.info(s"开始执行根据账号名称查询用户ID的流程，账号名称：${accountName}")).flatMap { _ =>
      getUserByAccountName(accountName).attempt.flatMap {
        case Right(id) =>
          IO(logger.info(s"成功通过账号名称 ${accountName} 查询到用户ID: ${id}")) *>
            IO(Some(id))
        case Left(error) =>
          IO(logger.info(s"根据账号名称 ${accountName} 未找到用户信息: ${error.getMessage}")) *>
            IO(None)
      }.flatTap(result =>
        IO(logger.info(s"完成查询操作，根据账号名称 ${accountName} 所得结果为: $result"))
      )
    }
  }
}