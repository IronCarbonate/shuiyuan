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
import Common.DBAPI.{writeDB, readDBJsonOptional, decodeField}
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.joda.time.DateTime
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits.*
import Common.DBAPI._
import Common.API.{PlanContext, Planner}
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

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