package Impl


import Utils.UserDataProcess.getUserTokenByUserID
import Objects.AuthService.Session
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
import Objects.AuthService.Session

case class QueryUserTokenMessagePlanner(
  userID: String,
  override val planContext: PlanContext
) extends Planner[Option[String]] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[Option[String]] = {
    for {
      // Log the start of the process
      _ <- IO(logger.info(s"开始处理获取用户会话token的请求，userID=${userID}"))

      // Step 1: Call getUserTokenByUserID to fetch the user token
      userTokenOption <- getUserToken(userID)

      // Step 2: Log the result and handle errors if the token is not present
      _ <- IO(logger.info(s"获取到的userToken是：${userTokenOption.getOrElse("None")}"))
      _ <- ensureUserLoggedIn(userTokenOption, userID)
    } yield userTokenOption
  }

  /** 
   * Step 1: Fetch the user token using the utility method getUserTokenByUserID 
   */
  private def getUserToken(userID: String)(using PlanContext): IO[Option[String]] = {
    logger.info(s"调用getUserTokenByUserID方法查询userID=${userID}的会话token")
    getUserTokenByUserID(userID)
  }

  /**
   * Step 2: Ensure user is logged in if no token is returned
   */
  private def ensureUserLoggedIn(userTokenOption: Option[String], userID: String)(using PlanContext): IO[Unit] = {
    userTokenOption match {
      case Some(_) =>
        IO(logger.info(s"userID=${userID}已登录，成功获取会话token"))
      case None =>
        IO.raiseError(new IllegalStateException(s"userID=${userID}未登录，因此无法获取会话token"))
    }
  }
}