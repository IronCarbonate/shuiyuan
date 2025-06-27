package Impl


import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import Objects.UserAccountService.UserPermission
import cats.effect.IO
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import io.circe.syntax.*
import Utils.UserManagementProcess.createUser
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
import Objects.UserAccountService.UserPermission
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class UserRegisterMessagePlanner(accountName: String, password: String) extends Planner[String] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      // Step 1: 调用createUser方法生成用户ID
      _ <- IO(logger.info(s"[Step 1] 调用createUser方法生成用户记录，传入参数 accountName=${accountName}"))
      userID <- createUser(accountName, password, "未命名用户", UserPermission.Normal)
      _ <- IO(logger.info(s"[Step 1] createUser方法调用完成，新创建的userID为: ${userID}"))

      // Step 2: 插入用户记录到UserTable
      _ <- IO(logger.info(s"[Step 2] 开始准备插入用户记录到数据库。"))
      currentTime <- IO(DateTime.now())
      insertSQL <- IO {
        s"""
        INSERT INTO ${schemaName}.user_table 
        (user_id, account_name, password, nickname, is_muted, created_at, permission) 
        VALUES (?, ?, ?, ?, FALSE, ?, ?);
      """
        .stripMargin
      }
      _ <- writeDB(
        insertSQL,
        List(
          SqlParameter("String", userID),
          SqlParameter("String", accountName),
          SqlParameter("String", password),
          SqlParameter("String", "未命名用户"),
          SqlParameter("DateTime", currentTime.getMillis.toString),
          SqlParameter("String", UserPermission.Normal.toString)
        )
      )
      _ <- IO(logger.info(s"[Step 2] 用户记录插入成功，userID: ${userID}, accountName: ${accountName}"))

      // Step 3: 返回生成的userID
      _ <- IO(logger.info(s"[Step 3] 返回新生成的userID: ${userID} 给调用方"))
    } yield userID
  }
}