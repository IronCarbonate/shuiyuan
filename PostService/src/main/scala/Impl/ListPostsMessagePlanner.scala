package Impl


import Objects.PostService.PostOverview
import APIs.UserService.ValidateUserTokenMessage
import Utils.PostQueryProcess.listAllPosts
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import cats.implicits.*
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
import Utils.PostQueryProcess.listAllPosts
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class ListPostsMessagePlanner(userToken: String, override val planContext: PlanContext) extends Planner[List[PostOverview]] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[List[PostOverview]] = {
    for {
      // Step 1: Validate user token
      _ <- IO(logger.info(s"[Step 1] 开始验证用户Token: ${userToken}"))
      userIDOption <- validateUserToken(userToken)
      _ <- IO(logger.info(s"[Step 1] 用户Token验证结果: ${userIDOption}"))

      // Step 1.1: If validation failed
      _ <- if (userIDOption.isEmpty) IO.raiseError(new IllegalArgumentException(s"无效的用户Token: ${userToken}")) else IO.unit

      // Step 2: Fetch all posts from the database
      _ <- IO(logger.info(s"[Step 2] 调用listAllPosts方法获取所有帖子列表"))
      posts <- fetchPostsList()
      _ <- IO(logger.info(s"[Step 2] 成功获取了 ${posts.size} 条帖子记录"))

      // Step 3: Return the posts list
      _ <- IO(logger.info("[Step 3] 返回帖子列表"))
    } yield posts
  }

  private def validateUserToken(token: String)(using PlanContext): IO[Option[String]] = {
    for {
      _ <- IO(logger.info(s"[validateUserToken] 验证用户Token: ${token}"))
      userIDOption <- ValidateUserTokenMessage(token).send
      _ <- IO(logger.info(s"[validateUserToken] 用户ID = ${userIDOption.getOrElse("无效Token")}"))
    } yield userIDOption
  }

  private def fetchPostsList()(using PlanContext): IO[List[PostOverview]] = {
    for {
      _ <- IO(logger.info("[fetchPostsList] 开始从数据库获取帖子列表"))
      postsList <- listAllPosts()
      _ <- IO(logger.info(s"[fetchPostsList] 成功获取了 ${postsList.size} 条帖子记录"))
    } yield postsList
  }
}