package Impl


import Utils.UserDataProcess.validateUserToken
import Utils.UserDataProcess.getNicknameByID
import Common.API.{PlanContext, Planner}
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
import Utils.UserDataProcess.getNicknameByID

case class GetNickNameByIDPlanner(
                                   userID: String,
                                   userToken: String,
                                   override val planContext: PlanContext
                                 ) extends Planner[String] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using context: PlanContext): IO[String] = {
    for {
      // Step 1: Validate userToken and retrieve userID
      _ <- IO(logger.info("验证用户会话Token的有效性"))
      tokenUserID <- validateUserToken(userToken)

      // Validate the provided userID matches the tokenUserID to avoid discrepancies
      _ <- if (tokenUserID != userID) {
        IO.raiseError(new IllegalArgumentException(s"请求的userID[${userID}]与Token验证的userID[${tokenUserID}]不一致"))
      } else {
        IO(logger.info(s"userToken验证成功，userID匹配: ${userID}"))
      }

      // Step 2: Query for the user's nickname
      _ <- IO(logger.info(s"开始查询用户的昵称信息，userID: ${userID}"))
      nickname <- getNicknameByID(userID)

      // Step 3: Return the user's nickname
      _ <- IO(logger.info(s"用户昵称查询完成，nickname: ${nickname}"))
    } yield nickname
  }
}