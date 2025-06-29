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
import Common.Object.SqlParameter
import cats.effect.IO
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Common.Serialize.CustomColumnTypes.{decodeDateTime, encodeDateTime}
import cats.implicits._
import Common.API.PlanContext
import Common.API.{PlanContext}
import Common.DBAPI.{writeDB}
import java.util.UUID

case object SessionManagementProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除
  
  
  def removeSession(userToken: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger(getClass)  // 同文后端处理: logger 统一
    for {
      // Step 1: Validate input userToken
      _ <- IO(logger.info(s"开始执行 removeSession 方法，输入参数 userToken: ${userToken}"))
      _ <- if (userToken.isEmpty) 
             IO.raiseError(new IllegalArgumentException("userToken 不能为空"))
           else IO.unit
  
      // Step 2: Construct SQL command and parameters
      sql <- IO {
        s"""
        DELETE FROM ${schemaName}.session_table
        WHERE user_token = ?;
        """.stripMargin
      }
      params <- IO(List(SqlParameter("String", userToken)))
  
      // Step 3: Perform database operation
      _ <- IO(logger.info(s"准备删除用户会话，数据库命令: ${sql}"))
      dbResult <- writeDB(sql, params)
      _ <- IO(logger.info(s"数据库操作结果: ${dbResult}"))
  
      // Step 4: Complete process
      _ <- IO(logger.info(s"成功删除用户会话，userToken: ${userToken}"))
    } yield "用户登出成功！"
  }
  
  
  def validateSession(userToken: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger("validateSession")  // 同文后端处理: logger 统一
    logger.info(s"开始验证会话信息，用户令牌为: ${userToken}")
  
    // 1. 构建SQL查询语句和参数
    val querySQL =
      s"""
        SELECT expired_at
        FROM ${schemaName}.session_table
        WHERE user_token = ?
      """
    val queryParams: List[SqlParameter] = List(SqlParameter("String", userToken))
    
    for {
      // 2. 打印查询SQL和参数到日志
      _ <- IO(logger.info(s"查询数据库中的会话信息: SQL=${querySQL}, 参数=${queryParams}"))
      
      // 3. 查询数据库中的会话信息
      maybeSession <- readDBJsonOptional(querySQL, queryParams)
      
      // 4. 处理查询结果
      result <- IO {
        maybeSession match {
          case Some(sessionJson) => 
            val expiredAt = new DateTime(decodeField[Long](sessionJson, "expired_at"))
            val currentTime = DateTime.now()
            
            // 检查会话的有效性
            if (expiredAt.isAfter(currentTime)) {
              logger.info(s"会话有效，用户令牌: ${userToken}, 过期时间: ${expiredAt}")
              "会话有效"
            } else {
              logger.info(s"会话无效，用户令牌: ${userToken}, 过期时间: ${expiredAt}, 当前时间: ${currentTime}")
              "会话无效"
            }
  
          case None => 
            logger.info(s"未找到会话记录，用户令牌: ${userToken}")
            "会话无效"
        }
      }
    } yield result
  }
  
  
  def generateSession(userID: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger("generateSession")  // 同文后端处理: logger 统一
  
    for {
      // Step 1: 生成一个唯一的userToken
      userToken <- IO {
        val token = UUID.randomUUID().toString
        logger.info(s"generateSession: 为用户 ${userID} 生成的userToken为 ${token}")
        token
      }
  
      // Step 2: 创建会话信息，包括创建时间和过期时间
      (createdAt, expiredAt) <- IO {
        val now = DateTime.now()
        val expiry = now.plusHours(2)
        logger.info(s"generateSession: 会话创建时间为 ${now}, 过期时间为 ${expiry}")
        (now, expiry)
      }
  
      // Step 3: 准备插入SQL和参数
      sessionId <- IO {
        val id = UUID.randomUUID().toString
        logger.info(s"generateSession: 生成的 sessionId 为 ${id}")
        id
      }
      sql <- IO {
        s"""
        INSERT INTO ${schemaName}.session_table (session_id, user_id, user_token, created_at, expired_at)
        VALUES (?, ?, ?, ?, ?)
        """.stripMargin
      }
      params <- IO {
        List(
          SqlParameter("String", sessionId),
          SqlParameter("String", userID),
          SqlParameter("String", userToken),
          SqlParameter("DateTime", createdAt.getMillis.toString),
          SqlParameter("DateTime", expiredAt.getMillis.toString)
        )
      }
  
      // Step 4: 将会话信息存储到SessionTable表中
      _ <- IO(logger.info(s"generateSession: 准备执行插入SQL命令，SQL为：${sql}"))
      _ <- writeDB(sql, params)
      _ <- IO(
        logger.info(s"generateSession: 成功将Session信息写入数据库，SessionID: ${sessionId}")
      )
    } yield userToken
  }
}
