import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.{SqlParameter, ParameterList}
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
import Common.DBAPI.{readDBJsonOptional, writeDB}
import Common.Object.SqlParameter
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

def invalidateToken(userToken: String)(using PlanContext): IO[String] = {
  val logger = LoggerFactory.getLogger(getClass)
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