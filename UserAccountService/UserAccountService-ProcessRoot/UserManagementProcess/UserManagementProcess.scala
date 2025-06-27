package Utils



//process plan import 预留标志位，不要删除
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import Common.DBAPI._
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import cats.implicits.*
import Common.API.{PlanContext, Planner}
import cats.effect.IO
import Common.Object.SqlParameter
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Common.API.PlanContext
import Common.DBAPI.{writeDB, readDBJsonOptional, decodeField}
import Objects.UserAccountService.UserPermission
import java.util.UUID
import Common.Serialize.CustomColumnTypes.{decodeDateTime, encodeDateTime}
import Common.DBAPI.{readDBJsonOptional, writeDB, decodeField}

case object UserManagementProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除
  
  
  def updateNickname(userID: String, newNickname: String)(using PlanContext): IO[String] = {
    logger.info(s"[updateNickname] 开始处理用户ID: ${userID}, 新昵称: ${newNickname}")
  
    if (userID.isEmpty || newNickname.isEmpty) {
      logger.error(s"[updateNickname] 输入参数错误, userID或newNickname为空")
      IO("更新失败: 参数不能为空")
    } else {
      for {
        // 查询用户是否存在：
        _ <- IO(logger.info(s"[updateNickname] 查询用户 ${userID} 的数据库记录"))
        userRecordOpt <- readDBJsonOptional(
          s"SELECT * FROM ${schemaName}.user_table WHERE user_id = ?",
          List(SqlParameter("String", userID))
        )
  
        result <- userRecordOpt match {
          case None =>
            // 用户不存在：
            logger.error(s"[updateNickname] 用户ID ${userID} 未找到对应的记录")
            IO("更新失败: 用户不存在")
  
          case Some(_) =>
            // 更新昵称操作:
            for {
              updateSQL <- IO(s"UPDATE ${schemaName}.user_table SET nickname = ? WHERE user_id = ?")
              updateParams <- IO(
                List(
                  SqlParameter("String", newNickname),
                  SqlParameter("String", userID)
                )
              )
              _ <- IO(logger.info(s"[updateNickname] 开始更新用户 ${userID} 的昵称为 ${newNickname}"))
              _ <- writeDB(updateSQL, updateParams)
              _ <- IO(logger.info(s"[updateNickname] 用户 ${userID} 的昵称更新成功"))
            } yield "修改昵称成功"
        }
      } yield result
    }
  }
  
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
  
  
  def setMuteStatus(targetUserID: String, isMuted: Boolean)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger(getClass)  // 同文后端处理: logger 统一
  
    logger.info(s"开始运行setMuteStatus方法，输入参数：targetUserID=${targetUserID}, isMuted=${isMuted}")
  
    if (targetUserID.isBlank) {
      val errorMessage = "输入参数错误：用户ID不能为空"
      logger.error(errorMessage)
      IO(errorMessage)
    } else {
      logger.info(s"输入参数targetUserID有效")
  
      val querySQL = s"SELECT * FROM ${schemaName}.user_table WHERE user_id = ?"
      val queryParams = List(SqlParameter("String", targetUserID))
  
      logger.info(s"查询用户信息的SQL命令：${querySQL}, 参数：${queryParams}")
      readDBJsonOptional(querySQL, queryParams).flatMap {
        case None =>
          val errorMessage = s"未查找到用户ID: ${targetUserID}对应的记录"
          logger.error(errorMessage)
          IO("用户不存在")
  
        case Some(json) =>
          logger.info(s"已查找到用户记录，读取数据库信息")
  
          val currentMuteStatus = decodeField[Boolean](json, "is_muted")
          logger.info(s"当前禁言状态为${currentMuteStatus}")
  
          if (currentMuteStatus == isMuted) {
            val infoMessage = s"输入的禁言状态与当前数据库状态一致，无需更新。用户ID: ${targetUserID}, 当前禁言状态: ${currentMuteStatus}"
            logger.info(infoMessage)
            IO("禁言状态未更改，因为状态无变化")
          } else {
            val updateSQL = s"UPDATE ${schemaName}.user_table SET is_muted = ? WHERE user_id = ?"
            val updateParams = List(
              SqlParameter("Boolean", isMuted.toString),
              SqlParameter("String", targetUserID)
            )
  
            logger.info(s"开始更新用户禁言状态：新的禁言状态为${isMuted}, SQL命令：${updateSQL}, 参数：${updateParams}")
            writeDB(updateSQL, updateParams).flatMap { updateResult =>
              logger.info(s"更新禁言状态成功，返回结果：${updateResult}")
              IO("禁言状态更新成功")
            }.handleErrorWith { err =>
              val errorMessage = s"更新禁言状态失败，原因：${err.getMessage}"
              logger.error(errorMessage)
              IO("禁言状态更新失败")
            }
          }
      }
    }
  }
}

