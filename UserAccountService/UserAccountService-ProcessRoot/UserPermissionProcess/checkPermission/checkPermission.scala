import Objects.UserAccountService.UserPermission
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
import cats.implicits._
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
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

def checkPermission(userToken: String)(using PlanContext): IO[UserPermission] = {
  val logger = LoggerFactory.getLogger("checkPermission")

  logger.info(s"[checkPermission] 开始验证用户 Token: ${userToken}")

  if (userToken.isEmpty) {
    logger.error(s"[checkPermission] 输入的 userToken 为空")
    IO.raiseError(new IllegalArgumentException("userToken cannot be empty"))
  } else {
    for {
      // Step 2: 调用 validateToken 验证 Token 的有效性
      isValid <- validateToken(userToken).send
      _ <- if (!isValid) {
        logger.error(s"[checkPermission] userToken 无效: ${userToken}")
        IO.raiseError(new IllegalStateException(s"userToken is invalid"))
      } else IO(logger.info(s"[checkPermission] userToken 验证通过: ${userToken}"))

      // Step 3: 根据 userToken 查询用户权限信息
      sql <- IO {
        s"""
           SELECT permission
           FROM ${schemaName}.user_table
           WHERE user_id = ?;
         """
      }
      _ <- IO(logger.debug(s"[checkPermission] 获取用户权限 SQL: ${sql}"))
      permissionString <- readDBString(sql, List(SqlParameter("String", userToken)))

      // Step 4: 转换权限字符串为 UserPermission 枚举并返回
      _ <- IO(logger.info(s"[checkPermission] 数据库返回权限值: ${permissionString}"))
      permission <- IO(UserPermission.fromString(permissionString)).handleErrorWith { ex =>
        logger.error(s"[checkPermission] 用户权限字符串转换失败: ${permissionString}, 错误: ${ex.getMessage}")
        IO.raiseError(ex)
      }
      _ <- IO(logger.info(s"[checkPermission] 转换权限值为 UserPermission 枚举: ${permission}"))
    } yield permission
  }
}