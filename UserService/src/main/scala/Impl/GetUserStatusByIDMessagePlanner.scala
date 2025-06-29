package Impl


import Common.API.{API, PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import Utils.UserDataProcess.getUserStatusByID
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
import Utils.UserDataProcess.getUserStatusByID

case class GetUserStatusByIDMessage(userID: String) extends API[Boolean]("userManagementServiceCode")

case class GetUserStatusByIDMessagePlanner(userID: String, override val planContext: PlanContext) extends Planner[Boolean] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[Boolean] = {
    for {
      // Step 1: Log the start of user mute status retrieval
      _ <- IO(logger.info(s"开始获取用户 userID [${userID}] 的禁言状态"))

      // Step 2: Invoke utility function to get user status by ID
      status <- getUserStatusByID(userID)

      // Step 3: Log the result
      _ <- IO(logger.info(s"userID [${userID}] 的禁言状态为：${status}"))
    } yield status
  }
}