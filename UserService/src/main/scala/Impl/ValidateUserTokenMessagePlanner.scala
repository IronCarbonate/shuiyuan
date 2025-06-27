package Impl


import Common.DBAPI._
import Common.API.{PlanContext, Planner}
import Common.Object.SqlParameter
import Common.Serialize.CustomColumnTypes.{decodeDateTime, encodeDateTime}
import Common.ServiceUtils.schemaName
import Utils.UserValidationProcess.validateUserTokenMessage
import cats.effect.IO
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
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
import Utils.UserValidationProcess.validateUserTokenMessage
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class ValidateUserTokenMessagePlanner(
                                            userToken: String,
                                            override val planContext: PlanContext
                                          ) extends Planner[Option[String]] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[Option[String]] = {
    for {
      _ <- IO(logger.info(s"[ValidateUserTokenMessagePlanner] Starting validation for userToken: ${userToken}"))
      userIDOpt <- validateUserToken(userToken)
      _ <- IO(logger.info(s"[ValidateUserTokenMessagePlanner] Validation result for userToken: ${userIDOpt.map(uid => s"Valid userID found: $uid").getOrElse("Invalid or expired token")}"))
    } yield userIDOpt
  }

  /**
   * Validates the user token and extracts userID if the token is valid.
   */
  private def validateUserToken(userToken: String)(using PlanContext): IO[Option[String]] = {
    for {
      _ <- IO(logger.info(s"[ValidateUserTokenMessagePlanner] Invoking validateUserTokenMessage utility for userToken: ${userToken}"))
      userIDOpt <- validateUserTokenMessage(userToken)
      _ <- IO {
        if (userIDOpt.isDefined) {
          logger.info(s"[ValidateUserTokenMessagePlanner] Token is valid. Extracted userID: ${userIDOpt.get}")
        } else {
          logger.info(s"[ValidateUserTokenMessagePlanner] Token is invalid or expired.")
        }
      }
    } yield userIDOpt
  }
}