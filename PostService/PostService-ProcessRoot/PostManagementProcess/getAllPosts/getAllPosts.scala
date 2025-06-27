import Objects.PostService.Post
import Objects.PostService.Comment
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
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
import Objects.PostService.Comment
import Common.API.{PlanContext}
import io.circe._
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

def getAllPosts()(using PlanContext): IO[List[Post]] = {
  val logger = LoggerFactory.getLogger("getAllPosts")
  logger.info("开始从数据库读取所有帖子记录")

  val sql =
    s"""
SELECT post_id, user_id, title, content, created_at, comment_count, latest_comment_time
FROM ${schemaName}.post_table
    """.stripMargin
  logger.info(s"SQL 查询语句: ${sql}")

  for {
    rows <- readDBRows(sql, List.empty)
    _ <- IO(logger.info(s"从数据库中读取到 ${rows.size} 条记录"))

    posts <- IO {
      rows.map { json =>
        val postID = decodeField[String](json, "post_id")
        val userID = decodeField[String](json, "user_id")
        val title = decodeField[String](json, "title")
        val content = decodeField[String](json, "content")
        val createdAt = decodeField[DateTime](json, "created_at")
        val commentCount = decodeField[Int](json, "comment_count")
        val latestCommentTime = decodeField[Option[DateTime]](json, "latest_comment_time")

        logger.debug(s"解析帖子记录: postID=${postID}, title=${title}, commentCount=${commentCount}")

        Post(
          postID = postID,
          userID = userID,
          title = title,
          content = content,
          createdAt = createdAt,
          commentCount = commentCount,
          latestCommentTime = latestCommentTime,
          commentList = None
        )
      }
    }

    _ <- IO(logger.info(s"成功解析所有帖子记录，共计 ${posts.size} 条"))
  } yield posts
}