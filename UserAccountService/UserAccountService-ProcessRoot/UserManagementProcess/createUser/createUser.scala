import Objects.UserAccountService.UserPermission
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.util.UUID
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits.*
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
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

def createUser(accountName: String, password: String, nickName: String, permission: UserPermission)(using PlanContext): IO[String] = {
  for {
    // Step 1: 验证输入参数的有效性
    _ <- IO {
      if (accountName.isEmpty || password.isEmpty) {
        throw new IllegalArgumentException("Account name and password cannot be null or empty.")
      }
    }
    finalNickName <- IO {
      if (nickName.isEmpty) "未命名用户" else nickName
    }
    _ <- IO {
      if (!List(UserPermission.Normal.toString, UserPermission.Admin.toString).contains(permission.toString)) {
        throw new IllegalArgumentException(s"Invalid permission value: ${permission.toString}")
      }
    }

    // Step 2: 生成全局唯一的userID
    userID <- IO(UUID.randomUUID().toString)
    _ <- IO(logger.info(s"Generated userID: ${userID}"))
    _ <- IO(logger.info(s"AccountName: ${accountName}, NickName: ${finalNickName}, Permission: ${permission.toString}"))

    // Step 3: 插入用户记录
    currentTime <- IO(DateTime.now())
    sql <- IO {
      s"""
INSERT INTO ${schemaName}.user_table (user_id, account_name, password, nickname, is_muted, created_at, permission)
VALUES (?, ?, ?, ?, FALSE, ?, ?)
       """.stripMargin
    }
    _ <- writeDB(
      sql,
      List(
        SqlParameter("String", userID),
        SqlParameter("String", accountName),
        SqlParameter("String", password),
        SqlParameter("String", finalNickName),
        SqlParameter("DateTime", currentTime.getMillis.toString),
        SqlParameter("String", permission.toString)
      )
    ).map(_ => logger.info(s"User record for ${userID} inserted successfully."))

    // Step 4: 返回创建结果
  } yield userID
}