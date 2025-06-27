import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
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

import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

def deletePost(postID: String)(using PlanContext): IO[String] = {
  val logger = LoggerFactory.getLogger(getClass)

  logger.info(s"[deletePost] 开始处理删除操作，postID：${postID}")

  for {
    // Step 1: 检查postID的存在性与合法性
    checkPostSQL <- IO {
      s"""
      SELECT post_id 
      FROM ${schemaName}.post_table 
      WHERE post_id = ?;
      """.stripMargin
    }
    _ <- IO(logger.info(s"[Step 1] 检查postID的存在性和合法性，SQL: ${checkPostSQL}"))
    postExists <- readDBJsonOptional(checkPostSQL, List(SqlParameter("String", postID)))

    _ <- postExists match {
      case Some(_) => IO(logger.info(s"[Step 1] postID：${postID} 存在，合法。"))
      case None =>
        val errMsg = s"[deletePost] postID：${postID} 不存在，终止操作。"
        IO(logger.error(errMsg)) >>
          IO.raiseError(new IllegalStateException(errMsg))
    }

    // Step 2: 执行删除操作
    // 2.1 删除帖子记录
    deletePostSQL <- IO {
      s"""
      DELETE FROM ${schemaName}.post_table 
      WHERE post_id = ?;
      """.stripMargin
    }
    _ <- IO(logger.info(s"[Step 2.1] 删除post_table表中的记录，SQL: ${deletePostSQL}"))
    _ <- writeDB(deletePostSQL, List(SqlParameter("String", postID)))

    // 2.2 删除关联的评论记录
    deleteCommentsSQL <- IO {
      s"""
      DELETE FROM ${schemaName}.comment_table 
      WHERE post_id = ?;
      """.stripMargin
    }
    _ <- IO(logger.info(s"[Step 2.2] 删除comment_table表中与postID相关联的评论记录，SQL: ${deleteCommentsSQL}"))
    _ <- writeDB(deleteCommentsSQL, List(SqlParameter("String", postID)))

    // 2.3 若有其他引用字段或关联数据需要清理，可在此扩展清理逻辑
    _ <- IO(logger.info(s"[Step 2.3] 若有其他引用字段或关联数据需要清理，后续逻辑可扩展。"))

    // Step 3: 返回结果
    result <- IO {
      logger.info(s"[Step 3] postID：${postID} 删除成功。")
      "Post deleted successfully"
    }
  } yield result
}