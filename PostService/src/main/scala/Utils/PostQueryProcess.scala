package Utils

//process plan import 预留标志位，不要删除
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import Common.DBAPI._
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import Objects.PostService.PostDetails
import Common.API.{PlanContext, Planner}
import Common.Object.SqlParameter
import cats.effect.IO
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime, encodeDateTime}
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Common.API.{PlanContext}
import Objects.PostService.PostOverview
import cats.implicits._

case object PostQueryProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除
  
  def getPostByID(postID: String)(using PlanContext): IO[Option[PostDetails]] = {
    // Log the start of the query process
    IO(logger.info(s"开始查询帖子信息，postID=${postID}")) >>
      // Validate the input parameter
      (if (postID.trim.isEmpty) {
        IO(logger.warn(s"postID为空，无法进行查询")) >>
          IO.pure(None)
      } else {
        IO(logger.info(s"验证postID有效性通过，开始执行数据库查询")) >>
          // Define SQL query and parameters
          IO {
            val sql =
              s"""
                 SELECT title, content, create_time
                 FROM ${schemaName}.post_table
                 WHERE post_id = ?;
               """
            val params = List(SqlParameter("String", postID))
            (sql, params)
          }.flatMap { case (sql, params) =>
            // Execute the database query
            IO(logger.info(s"准备执行SQL: ${sql} 参数: ${params.map(_.value).mkString(", ")}")) >>
              readDBJsonOptional(sql, params).flatMap {
                case Some(json) =>
                  // Parse the query result
                  IO(logger.info(s"成功查询到结果，开始解析标题、内容和创建时间")) >>
                    IO {
                      val title = decodeField[String](json, "title")
                      val content = decodeField[String](json, "content")
                      val createTimeMillis = decodeField[Long](json, "create_time")
                      val createTime = new DateTime(createTimeMillis)
                      Some(PostDetails(title, content, createTime))
                    }
                case None =>
                  // Handle case where no record is found
                  IO(logger.warn(s"未找到postID=${postID}对应的帖子")) >>
                    IO.pure(None)
              }
          }.flatTap {
            // Log parsed post details
            case Some(details) =>
              IO(logger.info(s"成功返回帖子详情：标题=${details.title}, 创建时间=${details.createTime}"))
            case None =>
              IO(logger.warn(s"帖子信息返回空"))
          }
      })
  }
  
  def listAllPosts()(using PlanContext): IO[List[PostOverview]] = {
  // val logger = LoggerFactory.getLogger("listAllPosts")  // 同文后端处理: logger 统一
  
    for {
      // Step 1: Log the start of the process
      _ <- IO(logger.info("开始获取所有帖子记录"))
  
      // Step 2: Define the SQL query
      sqlQuery <- IO {
        s"""
        SELECT post_id, title, create_time
        FROM ${schemaName}.post_table
        ORDER BY create_time DESC;
        """.stripMargin
      }
      _ <- IO(logger.info(s"生成数据库查询命令：$sqlQuery"))
  
      // Step 3: Execute the query to fetch rows from the database
      rows <- readDBRows(sqlQuery, List.empty)
      _ <- IO(logger.info(s"从数据库获得了${rows.size}条记录"))
  
      // Step 4: Map the rows into PostOverview objects
      postOverviewList <- IO {
        rows.map { json =>
          val postID = decodeField[String](json, "post_id")
          val title = decodeField[String](json, "title")
          val createTimeMillis = decodeField[Long](json, "create_time")
          val createTime = new DateTime(createTimeMillis)
  
          logger.debug(s"解析帖子记录：postID=${postID}, title=${title}, createTime=${createTime}")
          PostOverview(postID, title, createTime)
        }
      }
    } yield postOverviewList
  }
}
