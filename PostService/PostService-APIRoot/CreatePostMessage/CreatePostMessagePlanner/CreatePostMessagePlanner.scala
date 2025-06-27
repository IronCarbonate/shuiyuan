package Impl


import APIs.UserAccountService.UserLoginMessage
import Utils.PostManagementProcess.createPost
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
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class CreatePostMessagePlanner(
                                     userToken: String,
                                     title: String,
                                     content: String,
                                     override val planContext: PlanContext
                                   ) extends Planner[String] {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[String] = {
    for {
      // Step 1: Validate user token and extract userID
      _ <- IO(logger.info(s"[Step 1] Validating user token: ${userToken}"))
      userID <- validateUserToken(userToken)
      _ <- IO(logger.info(s"[Step 1.2] User token validated successfully. UserID: ${userID}"))

      // Step 2: Create new post and generate unique postID
      _ <- IO(logger.info(s"[Step 2.1] Creating new post for userID: ${userID}"))
      postID <- createPostRecord(userID, title, content)
      _ <- IO(logger.info(s"[Step 2.2] Post created successfully with postID: ${postID}"))

    } yield postID
  }

  private def validateUserToken(userToken: String)(using PlanContext): IO[String] = {
    for {
      _ <- IO(logger.info("[Step 1.1] Sending UserLoginMessage to validate user token"))
      userInfo <- UserLoginMessage(userToken).send
      _ <- IO(logger.info(s"[Step 1.1] User validation returned: ${userInfo}"))
    } yield userInfo
  }

  private def createPostRecord(userID: String, title: String, content: String)(using PlanContext): IO[String] = {
    for {
      // Call `createPost` to generate postID
      _ <- IO(logger.info("[Step 2.2.1] Calling `createPost` to generate unique postID"))
      postID <- createPost(userID, title, content)
      _ <- IO(logger.info(s"[Step 2.2.2] Received generated postID: ${postID}"))

      // Prepare the SQL for database insertion
      createdAt <- IO(DateTime.now())
      insertSQL <- IO {
        s"""
        INSERT INTO ${schemaName}.post_table
        (post_id, user_id, title, content, created_at, comment_count, latest_comment_time)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """
      }
      insertParams <- IO {
        List(
          SqlParameter("String", postID),
          SqlParameter("String", userID),
          SqlParameter("String", title),
          SqlParameter("String", content),
          SqlParameter("DateTime", createdAt.getMillis.toString),
          SqlParameter("Int", "0"), // Comment count initialized to 0
          SqlParameter("DateTime", createdAt.getMillis.toString) // Latest comment time initialized to createdAt
        )
      }
      _ <- IO(logger.info(s"[Step 2.2.3] Inserting post record into the database"))
      _ <- writeDB(insertSQL, insertParams)
      _ <- IO(logger.info(s"[Step 2.2.4] Post record successfully inserted into the database"))
    } yield postID
  }
}