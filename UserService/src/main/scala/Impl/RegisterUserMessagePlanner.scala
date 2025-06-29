package Impl


import Objects.UserService.UserRole
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
import Common.Serialize.CustomColumnTypes.{decodeDateTime, encodeDateTime}
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
import Objects.UserService.UserRole
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class RegisterUserMessagePlanner(
                                       accountName: String,
                                       password: String,
                                       nickname: String,
                                       role: UserRole,
                                       override val planContext: PlanContext
                                     ) extends Planner[String] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[String] = {
    for {
      // Step 1: Check if the account name already exists
      _ <- IO(logger.info(s"检查账号名称是否已存在: ${accountName}"))
      isAccountNameExists <- checkAccountNameExists(accountName)
      _ <- if (isAccountNameExists) 
             IO.raiseError(new IllegalArgumentException("账号名称已存在，请重新输入"))
           else 
             IO(logger.info(s"账号名称 ${accountName} 不存在"))

      // Step 2: Generate a unique user ID
      _ <- IO(logger.info("开始生成唯一的用户ID"))
      userID <- generateUniqueUserID()

      // Step 3: Insert new user data into the UserTable
      _ <- IO(logger.info(s"插入新的用户记录到user_table: userID = ${userID}, accountName = ${accountName}, nickname = ${nickname}, role = ${role.toString}"))
      _ <- insertNewUser(userID, accountName, password, nickname, role)

    } yield userID
  }

  private def checkAccountNameExists(accountName: String)(using PlanContext): IO[Boolean] = {
    logger.info("开始创建检查账号名称是否存在的数据库指令")
    val sql =
      s"""
        SELECT EXISTS(
            SELECT 1
            FROM ${schemaName}.user_table
            WHERE account_name = ?
        ) AS account_exists;
      """
    logger.info(s"指令为: ${sql}")
    logger.info("开始执行检查账号名称是否存在的数据库指令")
    readDBBoolean(sql, List(SqlParameter("String", accountName)))
  }

  private def generateUniqueUserID()(using PlanContext): IO[String] = {
    logger.info("开始生成唯一的用户ID")
    IO(java.util.UUID.randomUUID().toString)
  }

  private def insertNewUser(userID: String, accountName: String, password: String, nickname: String, role: UserRole)(using PlanContext): IO[Unit] = {
    logger.info("开始创建插入新的用户记录的数据库指令")
    val sql =
      s"""
        INSERT INTO ${schemaName}.user_table (
            user_id, account_name, password, nickname, role, is_muted, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?);
      """
    logger.info(s"指令为: ${sql}")
    logger.info("开始执行插入新的用户记录的数据库指令")
    val params = List(
      SqlParameter("String", userID),
      SqlParameter("String", accountName),
      SqlParameter("String", password),
      SqlParameter("String", nickname),
      SqlParameter("String", role.toString),
      SqlParameter("Boolean", "false"),
      SqlParameter("DateTime", DateTime.now.getMillis.toString),
    )
    writeDB(sql, params).void
  }
}