package Utils

//process plan import 预留标志位，不要删除
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import Common.DBAPI._
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import Common.API.{PlanContext, Planner}
import Common.Object.ParameterList
import Common.Object.SqlParameter
import cats.effect.IO
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Common.API.{PlanContext}
import Common.DBAPI.{writeDB}

case object UserManagementProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除
  
  
  def createUser(accountName: String, password: String)(using PlanContext): IO[String] = {
    for {
      // Step 1: Validate input arguments
      _ <- IO {
        if (accountName.trim.isEmpty)
          throw new IllegalArgumentException("accountName不能为空")
        if (password.trim.isEmpty)
          throw new IllegalArgumentException("password不能为空")
      }
  
      // Step 2: Generate user ID and current timestamp
      userID <- IO(java.util.UUID.randomUUID().toString)
      createTime <- IO(DateTime.now().getMillis)
      
      // Log the generated values
      _ <- IO(logger.info(s"生成的新用户ID: ${userID}, 创建时间戳: ${createTime}"))
  
      // Step 3: Insert user data into the database
      sql <- IO {
        s"""
        INSERT INTO ${schemaName}.user_table (user_id, account_name, password, create_time)
        VALUES (?, ?, ?, ?)""".stripMargin
      }
      
      parameters <- IO {
        List(
          SqlParameter("String", userID),
          SqlParameter("String", accountName),
          SqlParameter("String", password),
          SqlParameter("Long", createTime.toString)
        )
      }
  
      _ <- IO(logger.info(s"准备写入用户数据到数据库: accountName=${accountName}, userID=${userID}"))
      _ <- writeDB(sql, parameters)
  
      // Log success message
      _ <- IO(logger.info(s"用户 ${accountName} 的数据成功写入数据库"))
      
    } yield userID
  }
}
