package Impl


import Utils.PostManagementProcess.getAllPosts
import Utils.PostManagementProcess.sortPostsByLatestComment
import Objects.PostService.PostInfo
import Objects.PostService.Post
import Objects.PostService.Comment
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import org.joda.time.DateTime
import cats.effect.IO
import cats.implicits.*
import org.slf4j.LoggerFactory
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
import Objects.PostService.Comment
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class GetSortedPostListMessagePlanner(
    userToken: String,
    override val planContext: PlanContext
) extends Planner[List[PostInfo]] {

  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using planContext: PlanContext): IO[List[PostInfo]] = {
    for {
      // Step 1: Validate userToken
      _ <- IO(logger.info("Step 1: 开始验证用户令牌的有效性"))
      isValidUserToken <- validateUserToken(userToken)
      _ <- if (isValidUserToken) IO(logger.info("用户令牌验证成功")) else {
        val message = s"用户令牌无效: ${userToken}"
        IO(logger.error(message)) >> IO.raiseError(new IllegalArgumentException(message))
      }

      // Step 2: Retrieve all posts
      _ <- IO(logger.info("Step 2: 获取所有帖子数据"))
      posts <- getAllPosts()

      // Step 3: Sort posts by the latest comment time
      _ <- IO(logger.info("Step 3: 开始按最新评论时间对帖子数据进行排序"))
      sortedPosts <- IO(sortPostsByLatestComment(posts))

      // Step 4: Convert sorted posts into PostInfo format
      _ <- IO(logger.info("Step 4: 将排序后的帖子列表封装为 PostInfo 数据结构"))
      postInfos <- IO {
        sortedPosts.map { post =>
          PostInfo(
            postID = post.postID,
            title = post.title,
            userID = post.userID,
            createdAt = post.createdAt,
            latestCommentTime = post.latestCommentTime,
            commentCount = post.commentCount
          )
        }
      }

      _ <- IO(logger.info(s"返回结果包含 ${postInfos.size} 条帖子数据"))
    } yield postInfos
  }

  /** 验证用户令牌
    *
    * @param userToken 用户令牌
    * @return 用户令牌是否有效
    */
  private def validateUserToken(userToken: String)(using PlanContext): IO[Boolean] = {
    val sql =
      s"""
      SELECT EXISTS (
        SELECT 1 FROM ${schemaName}.user_tokens WHERE token = ?
      ) AS is_valid
      """
    logger.info(s"验证用户令牌的 SQL 查询: ${sql}")

    readDBBoolean(sql, List(SqlParameter("String", userToken)))
  }
}