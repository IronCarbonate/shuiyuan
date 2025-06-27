package Impl


import Utils.TokenProcess.generateToken
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.slf4j.LoggerFactory
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits._
import org.joda.time.DateTime
import io.circe._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Utils.TokenProcess.generateToken
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.slf4j.LoggerFactory
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits._
import org.joda.time.DateTime
import io.circe._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class UserLoginMessage(accountName: String, password: String)

case class UserLoginMessagePlanner(
  accountName: String,
  password: String,
  override val planContext: PlanContext
) extends Planner[String] {
  
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      // Step 1: Validate user credentials by querying the UserTable
      _ <- IO(logger.info(s"Attempting to fetch user from database for accountName: ${accountName}"))
      userJsonOpt <- fetchUserByAccountAndPassword(accountName, password)
      
      userJson <- IO(userJsonOpt.getOrElse(throw new IllegalStateException(s"Invalid accountName or password for accountName [${accountName}]")))
      userID <- IO { decodeField[String](userJson, "user_id") }
      
      _ <- IO(logger.info(s"User successfully authenticated for accountName: ${accountName}. User ID: ${userID}"))
      
      // Step 2: Generate user token
      userToken <- generateToken(userID)
      _ <- IO(logger.info(s"Token generated successfully for userID: ${userID}. Token: ${userToken}"))
    } yield userToken
  }
  
  // Function to fetch user by accountName and password from the database
  private def fetchUserByAccountAndPassword(accountName: String, password: String)(using PlanContext): IO[Option[Json]] = {
    logger.info(s"Executing database query to authenticate user for accountName: ${accountName}")

    val sql =
      s"""
         SELECT *
         FROM ${schemaName}.user_table
         WHERE account_name = ? AND password = ?;
       """

    val parameters = List(
      SqlParameter("String", accountName),
      SqlParameter("String", password)
    )

    readDBJsonOptional(sql, parameters).map { optJson =>
      logger.info(s"Database query result for accountName: ${accountName} -> ${optJson}")
      optJson
    }
  }
}