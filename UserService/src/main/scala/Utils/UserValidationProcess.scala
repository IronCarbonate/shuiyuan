package Utils

import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.ParameterList
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

def UserValidationProcess()(using PlanContext): IO[Unit] = {
  val logger = LoggerFactory.getLogger("UserValidationProcess")
  
  logger.info("[UserValidationProcess] 开始执行用户验证流程")

  val sqlFetchUsers = s"SELECT user_id, username, last_active FROM ${schemaName}.user WHERE status = ?"
  val fetchParams = List(SqlParameter("String", "active"))

  for {
    // Step 1: 获取所有用户信息
    _ <- IO(logger.info("[Step 1] 正在查询活跃用户"))
    _ <- IO(logger.debug(s"[Step 1] 查询用户SQL: ${sqlFetchUsers}, 参数: ${fetchParams}"))
    users <- readDBRows(sqlFetchUsers, fetchParams)

    // Step 2: 遍历用户信息
    _ <- IO(logger.info("[Step 2] 遍历所有用户并处理活跃状态"))

    _ <- IO {
      users.foreach { userJson =>
        val userID = decodeField[String](userJson, "user_id")
        val username = decodeField[String](userJson, "username")
        val lastActiveTimestamp = decodeField[Long](userJson, "last_active")
        val lastActiveDateTime = new DateTime(lastActiveTimestamp)

        logger.info(s"[Step 2] 用户信息: 用户ID=${userID}, 用户名=${username}, 上次活跃时间=${lastActiveDateTime}")

        // Step 3: 验证用户的最后活跃时间是否在30天以内
        if (lastActiveDateTime.isBefore(DateTime.now().minusDays(30))) {
          // 用户超过30天未活跃，标记为非活跃
          logger.info(s"[Step 3] 用户 ${username} 超过30天未活跃，准备标记为非活跃状态")
        } else {
          // 用户在30天内活跃，保留活跃状态
          logger.info(s"[Step 3] 用户 ${username} 为活跃用户，保留活跃状态")
        }
      }
    }

    _ <- IO(logger.info("[UserValidationProcess] 用户验证流程执行完成"))
  } yield ()
}