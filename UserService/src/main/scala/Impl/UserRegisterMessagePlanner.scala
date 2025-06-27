package Impl

import Utils.UserValidationProcess.isAccountNameExists
import Objects.UserService.User
import Utils.UserManagementProcess.createUser
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
import cats.implicits.*
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
import Utils.UserManagementProcess.createUser
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class UserRegisterMessagePlanner(
                                       accountName: String,
                                       password: String,
                                       override val planContext: PlanContext
                                     ) extends Planner[User] {

  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + s"_${planContext.traceID.id}")

  override def plan(using planContext: PlanContext): IO[User] = {
    for {
      // Step 1: Validate account existence
      _ <- IO {
        if (accountName.trim.isEmpty || password.trim.isEmpty)
          throw new IllegalArgumentException("账户名和密码不能为空")
        logger.info(s"[Step 1] 开始检查账户名是否已存在: ${accountName}")
      }
      accountExists <- isAccountNameExists(accountName)

      // Step 2: Handle account existence
      _ <- if (accountExists) {
        val errorMessage = s"[Step 2] 账户名称已存在: ${accountName}"
        IO(logger.error(errorMessage)) >> IO.raiseError(new IllegalStateException(errorMessage))
      } else IO(logger.info(s"[Step 2] 账户名 '${accountName}' 不存在，可以继续注册"))

      // Step 3: Create user
      _ <- IO(logger.info(s"[Step 3] 开始创建新用户，账户名: ${accountName}"))
      userID <- createUser(accountName, password)

      // Step 4: Prepare user object for return
      createTime <- IO(DateTime.now().getMillis)
      _ <- IO(logger.info(s"[Step 4] 返回注册用户信息: userID=${userID}, accountName=${accountName}"))
    } yield User(userID, accountName, password, createTime)
  }
}