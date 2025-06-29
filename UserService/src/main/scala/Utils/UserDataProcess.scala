package Utils

//process plan import 预留标志位，不要删除
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import Common.DBAPI._
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import Objects.AuthService.Session
import Common.API.{PlanContext, Planner}
import Common.Object.SqlParameter
import cats.effect.IO
import Common.Serialize.CustomColumnTypes.{decodeDateTime, encodeDateTime}
import cats.implicits._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Common.API.{PlanContext}
import io.circe.Json
import Common.API.PlanContext
import Common.DBAPI.{readDBJsonOptional, decodeField}
import Common.DBAPI.{readDBJsonOptional, writeDB}
import Utils.UserDataProcess.getUserRoleByID
import Objects.UserService.UserRole

case object UserDataProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除
  
  def validateUserToken(userToken: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger("validateUserToken")  // 同文后端处理: logger 统一
  
    for {
      // Step 1: Check if userToken is null or empty
      _ <- if (userToken == null || userToken.trim.isEmpty) {
        IO.raiseError(new IllegalArgumentException("无效会话"))
      } else {
        IO(logger.info(s"验证用户token：${userToken}"))
      }
  
      // Step 2: Query the database for a session matching the userToken
      sqlQuery <- IO {
        s"SELECT user_id, user_token, session_expiry FROM ${schemaName}.session WHERE user_token = ?"
      }
      parameters <- IO {
        List(SqlParameter("String", userToken))
      }
      _ <- IO(logger.info(s"正在执行数据库查询以获取会话信息：${sqlQuery}，参数：[userToken=${userToken}]"))
      sessionOption <- readDBJsonOptional(sqlQuery, parameters)
  
      session <- sessionOption match {
        case Some(json) =>
          // Session found in the database
          IO(logger.info("会话记录已在数据库中找到")) *> IO.pure(json)
        case None =>
          // No matching session found
          IO(logger.warn("未找到匹配的会话记录")) *> IO.raiseError(new IllegalArgumentException("无效会话"))
      }
  
      // Step 3: Decode and validate session
      _ <- IO(logger.info("正在解码和验证会话"))
      userID <- IO {
        decodeField[String](session, "user_id")
      }
      sessionExpiry <- IO {
        decodeField[Long](session, "session_expiry")
      }
      currentTime <- IO {
        DateTime.now().getMillis
      }
  
      _ <- if (currentTime > sessionExpiry) {
        IO(logger.warn(s"会话已过期：userID=${userID}, sessionExpiry=${sessionExpiry}, currentTime=${currentTime}")) *>
          IO.raiseError(new IllegalArgumentException("会话已过期"))
      } else {
        IO(logger.info(s"会话有效：userID=${userID}"))
      }
    } yield userID
  }
  
  
  def getNicknameByID(userID: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger(getClass)  // 同文后端处理: logger 统一
    logger.info(s"[getNicknameByID] 开始获取用户昵称，userID: ${userID}")
  
    for {
      // Step 1: 校验用户是否存在
      checkUserExistsSQL <- IO {
        s"""
           SELECT 1
           FROM ${schemaName}.user_table
           WHERE user_id = ?;
         """.stripMargin
      }
      _ <- IO(logger.info(s"[Step 1] 检查用户是否存在 SQL: $checkUserExistsSQL"))
      userExists <- readDBJsonOptional(
        checkUserExistsSQL,
        List(SqlParameter("String", userID))
      )
      _ <- if (userExists.isEmpty) {
        IO.raiseError(new IllegalArgumentException(s"用户不存在: ${userID}"))
      } else {
        IO(logger.info(s"用户 ${userID} 存在"))
      }
  
      // Step 2: 获取用户昵称
      fetchNicknameSQL <- IO {
        s"""
           SELECT nickname
           FROM ${schemaName}.user_table
           WHERE user_id = ?;
         """.stripMargin
      }
      _ <- IO(logger.info(s"[Step 2] 获取用户昵称 SQL: $fetchNicknameSQL"))
      nickname <- readDBString(
        fetchNicknameSQL,
        List(SqlParameter("String", userID))
      )
      _ <- IO(logger.info(s"[Step 3] 用户昵称获取成功: ${nickname}"))
    } yield nickname
  }
  
  
  def getUserStatusByID(userID: String)(using PlanContext): IO[Boolean] = {
    for {
      // Step 1: Log the start of the process
      _ <- IO(logger.info(s"开始根据userID[${userID}]查询用户的禁言状态"))
  
      // Step 2: Construct the SQL query
      sql <- IO {
        s"""
SELECT is_muted
FROM ${schemaName}.user_table
WHERE user_id = ?
""".stripMargin
      }
  
      _ <- IO(logger.info(s"执行数据库查询，SQL命令为：${sql}"))
  
      // Step 3: Execute the query and fetch the muted status
      isMuted <- readDBBoolean(sql, List(SqlParameter("String", userID)))
  
      // Step 4: Log the result
      _ <- IO(logger.info(s"查询完成，userID[${userID}]的禁言状态为：${isMuted}"))
    } yield isMuted
  }
  
  def getUserTokenByUserID(userID: String)(using PlanContext): IO[Option[String]] = {
    // Logger
  // val logger = LoggerFactory.getLogger("getUserTokenByUserID")  // 同文后端处理: logger 统一
    
    // Step 1: Log beginning of function execution
    logger.info(s"开始获取用户userID=${userID}的会话token")
  
    // Step 2: Construct SQL query and parameters
    val sqlQuery = s"SELECT user_token FROM ${schemaName}.session WHERE user_id = ? AND session_valid = true;"
    val sqlParams = List(SqlParameter("String", userID))
  
    // Step 3: Execute query and process result
    for {
      _ <- IO(logger.info(s"执行查询SQL: ${sqlQuery}, 参数是: ${sqlParams}"))
      sessionResult <- readDBJsonOptional(sqlQuery, sqlParams)
      userToken <- IO {
        sessionResult match {
          case Some(json) =>
            val token = decodeField[String](json, "user_token")
            logger.info(s"成功获取到userID=${userID}的会话token: ${token}")
            Some(token)
          case None =>
            logger.warn(s"未找到userID=${userID}的有效会话记录，用户未登录")
            None
        }
      }
      _ <- IO(logger.info(s"获取会话token完成，结果是: ${userToken}"))
    } yield userToken
  }
  
  
  def verifyAccountPassword(accountName: String, password: String)(using PlanContext): IO[Boolean] = {
    for {
      // Step 1: 查询数据库，检查是否存在与传入的accountName匹配的用户记录
      _ <- IO(logger.info(s"查询账号名称为${accountName}的用户记录"))
      sqlQuery <- IO { s"SELECT password FROM ${schemaName}.user_table WHERE account_name = ?" }
      params <- IO { List(SqlParameter("String", accountName)) }
      dbRecord <- readDBJsonOptional(sqlQuery, params)
  
      // Step 2: 验证数据库中是否存在记录，以及与传入密码比对
      result <- dbRecord match {
        case Some(json) =>
          // 提取数据库中的密码并进行比对
          val storedPassword = decodeField[String](json, "password")
          val isPasswordMatch = storedPassword == password
          if (isPasswordMatch) {
            IO(logger.info(s"账号${accountName}的密码验证成功")) >> IO(true)
          } else {
            IO(logger.info(s"账号${accountName}的密码验证失败")) >> IO(false)
          }
  
        case None =>
          // 如果查询结果为空，返回false
          IO(logger.info(s"账号名称${accountName}在数据库中不存在")) >> IO(false)
      }
    } yield result
  }
  
  
  def getUserByAccountName(accountName: String)(using PlanContext): IO[String] = {
    logger.info(s"开始根据账户名查找用户信息，账户名为: ${accountName}")
  
    // 构造SQL查询语句
    val sqlQuery =
      s"""
        SELECT user_id
        FROM ${schemaName}.user_table
        WHERE account_name = ?;
      """.stripMargin
  
    // 构造SQL参数
    val parameters = List(
      SqlParameter("String", accountName)
    )
  
    for {
      // 打印调试信息
      _ <- IO(logger.info(s"生成的SQL查询为: ${sqlQuery}"))
      _ <- IO(logger.info(s"查询参数为: ${parameters.map(_.value).mkString(", ")}"))
  
      // 执行数据库查询，读取userID
      userIDOption <- readDBJsonOptional(sqlQuery, parameters)
  
      // 检查查询结果是否存在
      userID <- userIDOption match {
        case Some(json) =>
          IO {
            decodeField[String](json, "user_id")
          }
  
        case None =>
          IO.raiseError(new IllegalArgumentException(s"未找到账户名为 ${accountName} 的用户信息"))
      }
  
      // 打印用户ID信息
      _ <- IO(logger.info(s"查询成功，根据账户名 ${accountName} 找到的userID为: ${userID}"))
    } yield userID
  }
  
  
  def updateNickname(userID: String, newNickname: String)(using PlanContext): IO[String] = {
    // Logging the start of the method
  // val logger = LoggerFactory.getLogger(this.getClass)  // 同文后端处理: logger 统一
    logger.info(s"开始执行updateNickname方法，userID=${userID}, newNickname=${newNickname}")
  
    // Step 1.1: Validate userID
    if (userID.isEmpty) {
      val errorMsg = "userID为空，请提供有效的用户ID"
      logger.error(errorMsg)
      IO.raiseError(new IllegalArgumentException(errorMsg))
    }
    // Step 1.2: Validate newNickname
    else if (newNickname.isEmpty) {
      val errorMsg = "newNickname为空，请提供有效的昵称"
      logger.error(errorMsg)
      IO.raiseError(new IllegalArgumentException(errorMsg))
    } else {
      for {
        // Step 2: Check if the user exists by querying UserTable with the given userID
        userOpt <- {
          val sql =
            s"""
             SELECT * FROM ${schemaName}.user_table
             WHERE user_id = ?;
             """.stripMargin
          val parameters = List(SqlParameter("String", userID))
          logger.info(s"执行SQL查询用户：${sql}，参数：userID=${userID}")
          readDBJsonOptional(sql, parameters)
        }
        // Step 2.1: If user does not exist, return an error
        _ <- userOpt match {
          case None =>
            val errorMsg = s"未找到userID=${userID}的用户"
            logger.error(errorMsg)
            IO.raiseError(new IllegalArgumentException(errorMsg))
          case Some(_) =>
            IO(logger.info(s"找到userID=${userID}的用户，开始更新nickname..."))
        }
        // Step 3: Update 'nickname' field to the newNickname in UserTable
        _ <- {
          val updateSQL =
            s"""
             UPDATE ${schemaName}.user_table
             SET nickname = ?
             WHERE user_id = ?;
             """.stripMargin
          val params = List(
            SqlParameter("String", newNickname),
            SqlParameter("String", userID)
          )
          logger.info(s"执行SQL更新用户昵称：${updateSQL}，参数：newNickname=${newNickname}, userID=${userID}")
          writeDB(updateSQL, params).void
        }
        // Step 4: Return operation success result
        result <- IO {
          val successMessage = s"昵称更新成功！userID=${userID}, newNickname=${newNickname}"
          logger.info(successMessage)
          successMessage
        }
      } yield result
    }
  }
  
  def muteTargetUser(targetUserID: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger("muteTargetUser")  // 同文后端处理: logger 统一
  
    val userTable = s"${schemaName}.user_table"
  
    // Step 1: Validate whether the target user exists
    val validateUserSQL =
      s"""
         SELECT *
         FROM $userTable
         WHERE user_id = ?;
       """
    for {
      _ <- IO(logger.info(s"[Step 1] Checking if the target user exists. UserID: ${targetUserID}"))
      userOptional <- readDBJsonOptional(validateUserSQL, List(SqlParameter("String", targetUserID)))
      user <- userOptional match {
        case Some(json) =>
          IO {
            logger.info(s"[Step 1.1] The user exists. UserID: ${targetUserID}")
            json
          }
        case None =>
          val errorMessage = s"[Step 1.2] User does not exist: ${targetUserID}"
          IO(logger.error(errorMessage)) *> IO.pure("用户不存在")
      }
  
      // Step 2: Check if the target user is an administrator
      _ <- IO(logger.info(s"[Step 2] Fetching the user's role to verify if they are an admin"))
      role <- getUserRoleByID(targetUserID)
      _ <- role match {
        case UserRole.Admin =>
          val errorMessage = s"[Step 2.1] User is an admin. Cannot mute. UserID: ${targetUserID}"
          IO(logger.warn(errorMessage)) *> IO.pure("无权限禁言管理员")
        case UserRole.Normal =>
          IO(logger.info(s"[Step 2.1] User is not an admin. Proceeding to mute. UserID: ${targetUserID}"))
      }
  
      // Step 3: Update the user's is_muted field to true
      _ <- IO(logger.info(s"[Step 3] Updating the 'is_muted' field for the user. UserID: ${targetUserID}"))
      updateMuteSQL =
        s"""
           UPDATE $userTable
           SET is_muted = ?
           WHERE user_id = ?;
         """
      _ <- writeDB(updateMuteSQL, List(SqlParameter("Boolean", true.toString), SqlParameter("String", targetUserID)))
      _ <- IO(logger.info(s"[Step 3.1] Successfully updated 'is_muted' to true for UserID: ${targetUserID}"))
  
    } yield "禁言成功！"
  }
  
  def confirmAdminRole(userID: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger("AdminRoleChecker")  // 同文后端处理: logger 统一
    logger.info(s"开始验证用户[userID=${userID}]是否为管理员角色.")
  
    val sql = s"SELECT role FROM ${schemaName}.user_table WHERE user_id = ?"
    val parameters = List(SqlParameter("String", userID))
  
    for {
      _ <- IO(logger.info(s"执行SQL查询: ${sql}, 参数: ${parameters.map(_.value).mkString(", ")}"))
      queryResult <- readDBJsonOptional(sql, parameters)
      result <- queryResult match {
        case Some(json) =>
          for {
            roleStr <- IO(decodeField[String](json, "role"))
            _ <- IO(logger.info(s"从数据库查询到用户[userID=${userID}]角色为: ${roleStr}"))
            response <- IO {
              try {
                val userRole = UserRole.fromString(roleStr)
                if (userRole == UserRole.Admin) {
                  logger.info(s"用户[userID=${userID}]角色为管理员(Admin).")
                  "管理员验证成功！"
                } else {
                  logger.info(s"用户[userID=${userID}]角色不是管理员(Normal).")
                  "无管理员权限！"
                }
              } catch {
                case _: Exception =>
                  logger.error(s"解析用户[userID=${userID}]角色失败: ${roleStr}.")
                  "无管理员权限！"
              }
            }
          } yield response
        case None =>
          for {
            _ <- IO(logger.error(s"[验证失败] 用户ID: ${userID}未找到匹配记录"))
            _ <- IO.raiseError(new IllegalStateException(s"未找到用户 [userID=${userID}]."))
          } yield ""
      }
    } yield result
  }
  
  def getUserRoleByID(userID: String)(using PlanContext): IO[UserRole] = {
  // val logger = LoggerFactory.getLogger("getUserRoleByID")  // 同文后端处理: logger 统一
  
    val sqlValidation =
      s"""
         SELECT 1
         FROM ${schemaName}.user_table
         WHERE user_id = ?;
       """
    val sqlRole =
      s"""
         SELECT role
         FROM ${schemaName}.user_table
         WHERE user_id = ?;
       """
  
    for {
      // Step 1. 校验 userID 是否存在
      _ <- IO(logger.info(s"[Step 1] 开始验证用户是否存在，用户ID: ${userID}"))
      validationResult <- readDBJsonOptional(sqlValidation, List(SqlParameter("String", userID)))
      _ <- validationResult match {
        case Some(_) =>
          IO(logger.info(s"[Step 1.1] 用户存在，用户ID: ${userID}"))
        case None =>
          val errorMessage = s"[Step 1.2] 用户不存在: ${userID}"
          IO(logger.error(errorMessage)) >>
          IO.raiseError(new IllegalArgumentException(errorMessage))
      }
  
      // Step 2. 获取用户角色信息
      _ <- IO(logger.info(s"[Step 2] 开始获取用户角色信息，用户ID: ${userID}"))
      roleString <- readDBString(sqlRole, List(SqlParameter("String", userID)))
      _ <- IO(logger.info(s"[Step 2.1] 从数据库中读取到角色值: ${roleString}"))
  
      // Step 3. 转换角色信息为枚举值
      roleEnum <- IO {
        try {
          UserRole.fromString(roleString)
        } catch {
          case e: Exception =>
            logger.error(s"[Step 3] 无法将角色字符串转换为枚举值, 角色字符串: ${roleString}", e)
            throw new IllegalArgumentException(s"非法的用户角色值: ${roleString}")
        }
      }
      _ <- IO(logger.info(s"[Step 3.1] 成功转换角色信息为枚举值: ${roleEnum}"))
  
    } yield roleEnum
  }
}
