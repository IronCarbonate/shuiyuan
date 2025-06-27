package Utils

//process plan import 预留标志位，不要删除
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import Common.DBAPI._
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import Common.API.{PlanContext, Planner}
import Common.Object.SqlParameter
import cats.effect.IO
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case object PostValidationProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除
  
  
  def doesPostExist(postID: String)(using PlanContext): IO[Boolean] = {
    // Log the start of the operation
    IO(logger.info(s"检查帖子是否存在 - postID: ${postID}")) >>
    IO {
      // Construct the SQL query
      val sqlQuery = s"SELECT post_id FROM ${schemaName}.post_table WHERE post_id = ?;"
      // Define the query parameters
      val queryParameters = List(SqlParameter("String", postID))
      // Log SQL query and parameters
      logger.info(s"执行SQL查询: ${sqlQuery} with parameters: ${queryParameters}")
      (sqlQuery, queryParameters)
    }.flatMap { case (sqlQuery, parameters) =>
      // Execute query and map the results
      readDBJsonOptional(sqlQuery, parameters).flatMap {
        case Some(_) =>
          IO(logger.info(s"帖子存在 - postID: ${postID}")) >>
          IO.pure(true)
        case None =>
          IO(logger.info(s"帖子不存在 - postID: ${postID}")) >>
          IO.pure(false)
      }
    }
  }
  
  
  def isPostOwner(userID: String, postID: String)(using PlanContext): IO[Boolean] = {
  // val logger = LoggerFactory.getLogger("isPostOwner")  // 同文后端处理: logger 统一
    val querySQL =
      s"SELECT user_id FROM ${schemaName}.post_table WHERE post_id = ?"
  
    val dbParameters = List(SqlParameter("String", postID))
  
    for {
      // Log the input parameters
      _ <- IO(logger.info(s"[isPostOwner] 检查用户 ${userID} 是否为帖子 ${postID} 的创建者"))
      // Execute the query and fetch the userID of the post owner
      postOwnerID <- readDBString(querySQL, dbParameters)
      _ <- IO(logger.info(s"[isPostOwner] 数据库返回的帖子创建者ID为: ${postOwnerID}"))
      // Compare the retrieved userID with the provided userID
      isOwner = userID == postOwnerID
      _ <- IO(logger.info(s"[isPostOwner] 用户 ${userID} 是否为帖子 ${postID} 的拥有者: ${isOwner}"))
    } yield isOwner
  }
}
