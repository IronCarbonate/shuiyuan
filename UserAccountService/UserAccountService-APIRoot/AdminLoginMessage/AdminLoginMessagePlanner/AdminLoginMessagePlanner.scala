package Impl


import Utils.TokenProcess.generateToken
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits._
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
import Utils.TokenProcess.generateToken
import Common.DBAPI.{readDBJsonOptional, decodeField}
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class AdminLoginMessagePlanner(
  accountName: String,
  password: String,
  override val planContext: PlanContext
) extends Planner[String] {

  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      // Step 1: Validate admin credentials
      _ <- IO(logger.info(s"[Step 1] Validating admin credentials for accountName: ${accountName}"))
      adminRecord <- validateAdminCredentials(accountName, password)

      // Step 2: Generate adminToken
      _ <- IO(logger.info(s"[Step 2] Generating adminToken for admin_id: ${adminRecord._1}"))
      adminToken <- generateToken(adminRecord._1)

      _ <- IO(logger.info(s"[Step 3] Returning the generated adminToken: ${adminToken}"))
    } yield adminToken
  }

  private def validateAdminCredentials(accountName: String, password: String)(using PlanContext): IO[(String, String)] = {
    logger.info("[Step 1.1] Creating SQL query to validate admin credentials from admin_table")
    val sql =
      s"""
      SELECT admin_id, account_name
      FROM ${schemaName}.admin_table
      WHERE account_name = ? AND password = ?;
      """
    logger.info(s"[Step 1.2] SQL Query: ${sql}")
    readDBJsonOptional(
      sql,
      List(
        SqlParameter("String", accountName),
        SqlParameter("String", password)
      )
    ).map {
      case Some(value) =>
        val adminId = decodeField[String](value, "admin_id")
        val matchedAccountName = decodeField[String](value, "account_name")
        logger.info(s"[Step 1.3] Admin credentials matched: adminId=${adminId}, accountName=${matchedAccountName}")
        (adminId, matchedAccountName)
      case None =>
        val errorMsg = s"[Step 1.4] Invalid admin credentials for accountName: ${accountName}"
        logger.error(errorMsg)
        throw new IllegalArgumentException(errorMsg)
    }
  }
}