package Impl


import Objects.UserAccountService.UserPermission
import Utils.UserManagementProcess.createUser
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.joda.time.DateTime
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
import Utils.UserManagementProcess.createUser
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class AdminRegisterMessage(accountName: String, password: String) extends Planner[String] {
  override def plan(using PlanContext): IO[String] = {
    AdminRegisterMessagePlanner(accountName, password).plan
  }
}

case class AdminRegisterMessagePlanner(accountName: String, password: String)(using PlanContext) extends Planner[String] {
  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + summon[PlanContext].traceID.id)

  override def plan(using PlanContext): IO[String] = {
    for {
      // Step 1: 验证输入参数的有效性
      _ <- IO(logger.info(s"Start validating parameters for AdminRegisterMessage. Account: ${accountName}"))
      _ <- IO {
        if (accountName.isEmpty || password.isEmpty) {
          throw new IllegalArgumentException("Account name and password cannot be null or empty.")
        }
      }

      // Step 2: 调用createUser方法，生成用户ID
      _ <- IO(logger.info(s"调用createUser方法, 创建管理员账户. Account Name: ${accountName}"))
      adminID <- createUserAsAdmin(accountName, password)

      // Step 3: 在AdminTable中插入管理员记录
      _ <- IO(logger.info(s"开始插入管理员记录到AdminTable, AdminID: ${adminID}, Account Name: ${accountName}"))
      _ <- insertAdminRecord(adminID, accountName, password)

      // Step 4: 返回生成的adminID
      _ <- IO(logger.info(s"管理员注册成功, 生成的AdminID: ${adminID}"))
    } yield adminID
  }

  private def createUserAsAdmin(accountName: String, password: String)(using PlanContext): IO[String] = {
    for {
      _ <- IO(logger.info(s"调用createUser方法，为账户 ${accountName} 分配管理员权限"))
      adminID <- createUser(accountName, password, "未命名用户", UserPermission.Admin)
    } yield adminID
  }

  private def insertAdminRecord(adminID: String, accountName: String, password: String)(using PlanContext): IO[Unit] = {
    val currentTime = DateTime.now()
    val sql =
      s"""
INSERT INTO ${schemaName}.admin_table (admin_id, account_name, password, created_at)
VALUES (?, ?, ?, ?);
         """.stripMargin

    for {
      _ <- IO(logger.info(s"插入管理员记录到AdminTable, AdminID: ${adminID}, Account Name: ${accountName}"))
      _ <- writeDB(
        sql,
        List(
          SqlParameter("String", adminID),
          SqlParameter("String", accountName),
          SqlParameter("String", password),
          SqlParameter("DateTime", currentTime.getMillis.toString)
        )
      ).map(_ => logger.info(s"管理员记录 AdminID: ${adminID} 插入成功"))
    } yield ()
  }
}