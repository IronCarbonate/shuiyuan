package Impl


import Utils.UserDataProcess.{validateUserToken, updateNickname}
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
import Utils.UserDataProcess.validateUserToken
import Objects.AuthService.Session
import Utils.UserDataProcess.updateNickname

case class UpdateUserNicknameMessagePlanner(
                                             userToken: String,
                                             newNickname: String,
                                             override val planContext: PlanContext
                                           ) extends Planner[String] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      // Step 1: Validate user token and retrieve userID
      _ <- IO(logger.info(s"调用validateUserToken以验证用户token：${userToken}"))
      userID <- validateUserToken(userToken)
      _ <- IO(logger.info(s"验证成功，获得userID：${userID}"))

      // Step 2: Update the nickname for the user
      _ <- IO(logger.info(s"调用updateNickname以更新用户昵称，userID=${userID}，newNickname=${newNickname}"))
      result <- updateNickname(userID, newNickname)
      _ <- IO(logger.info(s"昵称更新操作结果：${result}"))
    } yield result
  }
}