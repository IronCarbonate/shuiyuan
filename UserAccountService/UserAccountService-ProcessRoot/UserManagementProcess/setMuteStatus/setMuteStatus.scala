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
import org.joda.time.DateTime
import cats.implicits.*
import Common.DBAPI._
import Common.API.{PlanContext, Planner}
import cats.effect.IO
import Common.Object.SqlParameter
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Common.ServiceUtils.schemaName

import Common.API.PlanContext
import Common.DBAPI.{readDBJsonOptional, writeDB, decodeField}
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

def setMuteStatus(targetUserID: String, isMuted: Boolean)(using PlanContext): IO[String] = {
  val logger = LoggerFactory.getLogger(getClass)

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