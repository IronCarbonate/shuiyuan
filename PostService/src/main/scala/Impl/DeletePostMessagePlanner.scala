package Impl


/**
 * API: DeletePostMessage
 * Description: 用户删除自己的帖子
 * 输入: userToken: String, postID: String
 * 输出: deleteResult: String
 */
import APIs.UserService.ValidateUserTokenMessage
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import Utils.PostManagementProcess.deletePost
import Utils.PostValidationProcess.doesPostExist
import Utils.PostValidationProcess.isPostOwner
import cats.effect.IO
import org.slf4j.LoggerFactory
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.joda.time.DateTime
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
import Utils.PostValidationProcess.isPostOwner
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class DeletePostMessagePlanner(
                                     userToken: String,
                                     postID: String,
                                     override val planContext: PlanContext
                                   ) extends Planner[String] {

  // Logger 定义
  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  /**
   * 主方法：实现删除帖子功能
   */
  override def plan(using planContext: PlanContext): IO[String] = {
    for {
      // Step 1: 验证用户Token并解析用户ID
      userID <- validateUserToken(userToken)

      // Step 2: 检查帖子是否存在
      _ <- checkPostExistence(postID)

      // Step 3: 验证当前用户是否是帖子创建者
      _ <- validatePostOwnership(userID, postID)

      // Step 4: 删除帖子
      _ <- performPostDeletion(postID)
    } yield {
      val successMessage = s"帖子删除成功: postID=${postID}"
      logger.info(successMessage)
      successMessage
    }
  }

  /**
   * Step 1: 验证用户Token有效性，并解析用户ID
   */
  private def validateUserToken(userToken: String)(using PlanContext): IO[String] = {
    for {
      _ <- IO(logger.info(s"[Step 1] 验证用户Token: ${userToken}"))
      validationResult <- ValidateUserTokenMessage(userToken).send
      userID <- validationResult match {
        case Some(uid) =>
          IO(logger.info(s"[Step 1.1] 用户Token验证成功, userID=${uid}")) >> IO.pure(uid)
        case None =>
          val errorMessage = s"[Step 1.2] 无效的用户Token: ${userToken}"
          IO(logger.error(errorMessage)) >> IO.raiseError(new IllegalArgumentException(errorMessage))
      }
    } yield userID
  }

  /**
   * Step 2: 检查帖子是否存在
   */
  private def checkPostExistence(postID: String)(using PlanContext): IO[Unit] = {
    for {
      _ <- IO(logger.info(s"[Step 2] 检查帖子是否存在: postID=${postID}"))
      doesExist <- doesPostExist(postID)
      _ <- if (!doesExist) {
        val errorMessage = s"[Step 2.1] 帖子不存在: postID=${postID}"
        IO(logger.error(errorMessage)) >> IO.raiseError(new IllegalArgumentException(errorMessage))
      } else IO.unit
    } yield ()
  }

  /**
   * Step 3: 验证当前用户是否为帖子创建者
   */
  private def validatePostOwnership(userID: String, postID: String)(using PlanContext): IO[Unit] = {
    for {
      _ <- IO(logger.info(s"[Step 3] 验证用户是否是帖子创建者: userID=${userID}, postID=${postID}"))
      isOwner <- isPostOwner(userID, postID)
      _ <- if (!isOwner) {
        val errorMessage = s"[Step 3.1] 用户无权删除该帖子: userID=${userID}, postID=${postID}"
        IO(logger.error(errorMessage)) >> IO.raiseError(new IllegalAccessException(errorMessage))
      } else IO.unit
    } yield ()
  }

  /**
   * Step 4: 删除帖子
   */
  private def performPostDeletion(postID: String)(using PlanContext): IO[Unit] = {
    for {
      _ <- IO(logger.info(s"[Step 4] 开始删除帖子: postID=${postID}"))
      deleteSuccess <- deletePost(postID)
      _ <- if (!deleteSuccess) {
        val errorMessage = s"[Step 4.1] 删除帖子失败: postID=${postID}"
        IO(logger.error(errorMessage)) >> IO.raiseError(new RuntimeException(errorMessage))
      } else {
        IO(logger.info(s"[Step 4.2] 帖子删除成功: postID=${postID}"))
      }
    } yield ()
  }
}