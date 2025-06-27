import Objects.PostService.Post
import Objects.PostService.Comment
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
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

def createPost(userID: String, title: String, content: String)(using PlanContext): IO[String] = {
  val logger = LoggerFactory.getLogger("createPost")
  
  for {
    _ <- IO(logger.info("Validating input parameters"))
    
    // Step 1.1: Check empty input parameters
    _ <- if (userID.isEmpty || title.isEmpty || content.isEmpty) 
      IO.raiseError(new IllegalArgumentException("userID, title, and content cannot be empty"))
    else IO.unit
    _ <- IO(logger.info("Input parameters validated successfully"))
    
    // Step 2.1: Generate a new postID
    _ <- IO(logger.info("Generating new postID"))
    postID <- {
      val sql = "SELECT generate_unique_id() AS new_post_id"
      readDBString(sql, List())
    }
    _ <- IO(logger.info(s"Generated new postID: ${postID}"))
    
    // Step 3.1: Initialize the post record
    createdAt <- IO(DateTime.now()) // Current creation time
    newPost <- IO {
      Post(
        postID = postID,
        userID = userID,
        title = title,
        content = content,
        createdAt = createdAt,
        commentCount = 0,
        latestCommentTime = Some(createdAt),
        commentList = None
      )
    }
    _ <- IO(logger.info(s"Initialized new post object: ${newPost}"))

    // Step 4.1: Insert post record into the database
    insertSql <- IO {
      s"""
      INSERT INTO ${schemaName}.post_table
      (post_id, user_id, title, content, created_at, comment_count, latest_comment_time)
      VALUES (?, ?, ?, ?, ?, ?, ?)
      """
    }
    insertParams <- IO {
      List(
        SqlParameter("String", newPost.postID),
        SqlParameter("String", newPost.userID),
        SqlParameter("String", newPost.title),
        SqlParameter("String", newPost.content),
        SqlParameter("DateTime", newPost.createdAt.getMillis.toString),
        SqlParameter("Int", newPost.commentCount.toString),
        SqlParameter("DateTime", newPost.latestCommentTime.map(_.getMillis.toString).getOrElse(""))
      )
    }
    _ <- writeDB(insertSql, insertParams)
    _ <- IO(logger.info(s"Post record successfully inserted into the database"))
    
  } yield postID
}