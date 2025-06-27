import Objects.PostService.Comment
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import io.circe.Json
import io.circe.generic.auto._
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import cats.implicits._
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
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

def getCommentsByPostID(postID: String)(using PlanContext): IO[List[Comment]] = {
  val logger = LoggerFactory.getLogger(getClass)

  if (postID.trim.isEmpty) 
    IO.raiseError(new IllegalArgumentException("postID不能为空"))
  else {
    logger.info(s"开始检索 postID=${postID} 所关联的评论记录")

    val sqlQuery = s"""
      SELECT comment_id, post_id, user_id, content, created_at
      FROM ${schemaName}.comment_table
      WHERE post_id = ?;
    """

    val parameters = List(SqlParameter("String", postID))

    for {
      _ <- IO(logger.info(s"执行查询评论记录的 SQL: ${sqlQuery}"))
      rows <- readDBRows(sqlQuery, parameters)

      comments <- IO {
        rows.map { row =>
          val commentID = decodeField[String](row, "comment_id")
          val postID = decodeField[String](row, "post_id")
          val userID = decodeField[String](row, "user_id")
          val content = decodeField[String](row, "content")
          val createdAt = new DateTime(decodeField[Long](row, "created_at"))

          Comment(
            commentID = commentID,
            postID = postID,
            userID = userID,
            content = content,
            createdAt = createdAt
          )
        }
      }

      _ <- IO(logger.info(s"成功检索到 ${comments.size} 条评论记录"))
    } yield comments
  }
}