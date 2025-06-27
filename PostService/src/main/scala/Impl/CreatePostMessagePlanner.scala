package Impl


import Objects.PostService.PostCreateResult
import APIs.UserService.ValidateUserTokenMessage
import Utils.PostManagementProcess.createPost
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import cats.effect.IO
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
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
import Utils.PostManagementProcess.createPost
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class CreatePostMessagePlanner(
                                     userToken: String,
                                     title: String,
                                     content: String,
                                     override val planContext: PlanContext
                                   ) extends Planner[PostCreateResult] {

  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  /** 主执行体 */
  override def plan(using PlanContext): IO[PostCreateResult] = {
    for {
      _ <- IO(logger.info(s"开始执行 CreatePostMessagePlanner，参数为 userToken=${userToken}, title=${title}, content=${content}"))
      userID <- validateTokenAndExtractUserID(userToken)
      postCreateResult <- createAndStorePost(userID, title, content)
      _ <- IO(logger.info(s"帖子创建成功，结果为 postID=${postCreateResult.postID}, createTime=${postCreateResult.createTime}"))
    } yield postCreateResult
  }

  /** 验证用户Token并提取userID */
  private def validateTokenAndExtractUserID(token: String)(using PlanContext): IO[String] = {
    for {
      _ <- IO(logger.info(s"[Step 1] 验证用户 token 有效性"))
      userIDOption <- ValidateUserTokenMessage(token).send
      userID <- userIDOption match {
        case Some(id) =>
          IO {
            logger.info(s"[Step 1.1] Token 验证通过，解析到有效的 userID=${id}")
            id
          }
        case None =>
          val errorMessage = s"userToken[${token}] 无效"
          IO(logger.error(errorMessage)) >> IO.raiseError(new IllegalStateException(errorMessage))
      }
    } yield userID
  }

  /** 创建并存储帖子 */
  private def createAndStorePost(userID: String, postTitle: String, postContent: String)(using PlanContext): IO[PostCreateResult] = {
    for {
      _ <- IO(logger.info(s"[Step 2] 调用 createPost 创建帖子"))
      postCreateResult <- createPost(userID, postTitle, postContent)
      _ <- IO(logger.info(s"[Step 2.1] 帖子创建完成，postID=${postCreateResult.postID}"))
    } yield postCreateResult
  }
}