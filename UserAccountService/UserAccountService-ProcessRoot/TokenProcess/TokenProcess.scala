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
import Common.DBAPI.{writeDB, SqlParameter}
import Common.Object.SqlParameter
import java.security.SecureRandom
import java.util.Base64
import cats.effect.IO
import io.circe._ // For JSON
import io.circe.syntax._ // For .asJson
import io.circe.generic.auto._ // For generic JSON encoding/decoding
import cats.implicits._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Common.API.PlanContext
import Common.Object.{SqlParameter, ParameterList}
import Common.DBAPI.{readDBJsonOptional, writeDB}

case object TokenProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除
  
  
  def generateToken(userID: String)(using PlanContext): IO[String] = {
    // Logger initialization
  // val logger = LoggerFactory.getLogger(getClass)  // 同文后端处理: logger 统一
  
    // Step 1: Validate the input
    for {
      _ <- if (userID.isEmpty) {
        IO(logger.error("[Step 1] The provided userID is empty.")) >>
        IO.raiseError(new IllegalArgumentException("userID cannot be empty"))
      } else {
        IO(logger.info(s"[Step 1] Validating input parameter userID: ${userID}"))
      }
      
      // Step 2: Generate a secure 32-character token
      userToken <- IO {
        val tokenLengthInBytes = 24 // 24 bytes = 32-character base64 string
        val secureRandom = new SecureRandom()
        val randomBytes = new Array[Byte](tokenLengthInBytes)
        secureRandom.nextBytes(randomBytes)
        Base64.getUrlEncoder.withoutPadding.encodeToString(randomBytes)
      }
      _ <- IO(logger.info(s"[Step 2] Generated a unique token: ${userToken}"))
      
      // Step 3: Save the token in the database
      _ <- {
        val sql = s"INSERT INTO ${schemaName}.user_tokens (user_id, user_token, created_at) VALUES (?, ?, ?)"
        val timestamp = DateTime.now()
        val parameters = List(
          SqlParameter("String", userID),
          SqlParameter("String", userToken),
          SqlParameter("DateTime", timestamp.getMillis.toString)
        )
        writeDB(sql, parameters).flatMap { result =>
          IO(logger.info(s"[Step 3] Token successfully saved for userID: ${userID}, Result: ${result}"))
        }
      }
    } yield userToken
  }
  
  
  def invalidateToken(userToken: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger(getClass)  // 同文后端处理: logger 统一
    logger.info(s"[invalidateToken] 开始使 token 失效：${userToken}")
  
    if (userToken.trim.isEmpty) {
      logger.info("[invalidateToken] 输入的 userToken 为空，返回失败信息")
      IO("登出失败，token不能为空")
    } else {
      logger.info("[invalidateToken] 验证 token 在数据库中是否存在")
  
      // 检查 token 是否存在
      val fetchSql = s"""
        SELECT user_id
        FROM ${schemaName}.user_table
        WHERE user_id = ?;
      """.stripMargin
      val fetchParams = List(SqlParameter("String", userToken))
  
      for {
        tokenExistsOpt <- readDBJsonOptional(fetchSql, fetchParams)
        result <- tokenExistsOpt match {
          case Some(_) =>
            logger.info("[invalidateToken] token 存在，继续清除状态")
  
            // 删除该 token 的记录
            val deleteSql = s"""
              DELETE FROM ${schemaName}.user_table
              WHERE user_id = ?;
            """.stripMargin
            val deleteParams = List(SqlParameter("String", userToken))
  
            for {
              _ <- IO(logger.info("[invalidateToken] 执行删除 token 的操作"))
              deleteResult <- writeDB(deleteSql, deleteParams)
              _ <- IO(logger.info(s"[invalidateToken] 数据库返回信息：${deleteResult}"))
            } yield "登出成功"
  
          case None =>
            logger.info("[invalidateToken] token 不存在，无法登出")
            IO("登出失败，token不存在")
        }
      } yield result
    }
  }
}

