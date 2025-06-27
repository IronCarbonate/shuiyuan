package Impl


import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import Objects.UserService.LoginResult
import Utils.UserValidationProcess.validateUser
import Utils.UserValidationProcess.validateUserTokenMessage
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import cats.effect.IO
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits._
import io.circe._
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
import Utils.UserValidationProcess.validateUser
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class UserLoginMessagePlanner(
                                    accountName: String,
                                    password: String,
                                    override val planContext: PlanContext
                                  ) extends Planner[LoginResult] {

  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[LoginResult] = {
    for {
      _ <- IO(logger.info(s"[UserLoginMessagePlanner] 接收到请求，账户名: ${accountName}"))

      // Step 1: Validate user credentials
      loginResultOpt <- validateUser(accountName, password)
      _ <- IO(logger.info(s"[UserLoginMessagePlanner] 用户账户校验结果为: ${loginResultOpt}"))

      // Step 2: Proceed only if validation is successful
      finalResult <- loginResultOpt match {
        case Some(validResult) =>
          generateTokenAndExpireAt(validResult)
        case None =>
          IO.pure(LoginResult(None, -1)) // Return empty LoginResult if validation fails
      }
      _ <- IO(logger.info(s"[UserLoginMessagePlanner] 最终登录结果为：${finalResult}"))
    } yield finalResult
  }

  /**
   * 子方法，用于生成 JWT Token 和过期时间并返回最终的 LoginResult。
   *
   * @param result 已验证为有效的用户登录结果
   * @return 包含 token 和其过期时间的完整登录结果
   */
  private def generateTokenAndExpireAt(result: LoginResult)(using PlanContext): IO[LoginResult] = {
    for {
      // Step 2.1: Extract token from the result
      userToken = result.token.getOrElse("")
      _ <- IO(logger.info(s"[generateTokenAndExpireAt] 提取到的用户Token: ${userToken}"))

      // Step 2.2: Validate token using validateUserTokenMessage
      tokenValidationResult <- validateUserTokenMessage(userToken)
      _ <- IO(logger.info(s"[generateTokenAndExpireAt] 用户Token验证结果为: ${tokenValidationResult}"))

      // Step 2.3: Set token expiration time and return final LoginResult
      validatedResult <- IO {
        val currentTime = DateTime.now().getMillis
        val expireAt = currentTime + 2 * 60 * 60 * 1000 // Token expires in 2 hours

        tokenValidationResult match {
          case Some(_) =>
            LoginResult(Some(userToken), expireAt) // Valid token
          case None =>
            LoginResult(None, -1) // Invalid token validation
        }
      }

      _ <- IO(logger.info(s"[generateTokenAndExpireAt] 返回最终结果：${validatedResult}"))
    } yield validatedResult
  }
}