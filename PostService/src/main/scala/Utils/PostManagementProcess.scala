package Utils

//process plan import 预留标志位，不要删除
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import Common.DBAPI._
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import Objects.PostService.PostCreateResult
import Common.API.{PlanContext, Planner}
import Common.Object.SqlParameter
import cats.effect.IO
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime, encodeDateTime}
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import java.util.UUID
import Utils.PostValidationProcess.doesPostExist
import cats.implicits._
import Common.API.PlanContext

case object PostManagementProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除
  
  def createPost(userID: String, title: String, content: String)(using PlanContext): IO[PostCreateResult] = {
  // val logger = LoggerFactory.getLogger(getClass)  // 同文后端处理: logger 统一
  
    logger.info(s"[CreatePost] 开始创建帖子，用户ID: ${userID}, 标题: ${title}")
    
    // 验证输入参数的有效性
    if (userID.isEmpty || title.isEmpty || content.isEmpty) {
      IO.raiseError(new IllegalArgumentException("[CreatePost] 参数信息不完整，userID, title, 或 content 为空"))
    } else {
      logger.info(s"[CreatePost] 输入参数验证通过")
      
      for {
        // 生成帖子ID
        postID <- IO(UUID.randomUUID().toString)
        _ <- IO(logger.info(s"[CreatePost] 生成唯一帖子ID: ${postID}"))
        
        // 记录创建时间
        createTime <- IO(DateTime.now())
        createTimeMillis = createTime.getMillis.toString
        _ <- IO(logger.info(s"[CreatePost] 当前帖子创建时间: ${createTime}"))
  
        // 构造插入 SQL 和参数
        insertSQL <- IO {
          s"""
             INSERT INTO ${schemaName}.post_table (post_id, user_id, title, content, create_time)
             VALUES (?, ?, ?, ?, ?)
           """.stripMargin
        }
        params <- IO {
          List(
            SqlParameter("String", postID),
            SqlParameter("String", userID),
            SqlParameter("String", title),
            SqlParameter("String", content),
            SqlParameter("Long", createTimeMillis)
          )
        }
  
        _ <- IO(logger.info(s"[CreatePost] 执行帖子插入 SQL"))
        _ <- writeDB(insertSQL, params)
        _ <- IO(logger.info(s"[CreatePost] 帖子插入数据库成功"))
  
        // 返回帖子创建结果
        postCreateResult <- IO(PostCreateResult(postID, createTime))
        _ <- IO(logger.info(s"[CreatePost] 返回帖子创建结果: postID=${postCreateResult.postID}, createTime=${postCreateResult.createTime}"))
      } yield postCreateResult
    }
  }
  
  def deletePost(postID: String)(using PlanContext): IO[Boolean] = {
    logger.info(s"[Delete Post] 开始删除帖子，postID: ${postID}")
  
    for {
      // Step 1: 检查帖子是否存在
      doesExist <- doesPostExist(postID)
      _ <- if (!doesExist) {
        val errorMsg = s"[Delete Post] 帖子ID: ${postID}不存在，无法进行删除操作"
        IO(logger.warn(errorMsg)) *> IO.pure(false) // 如果不存在，则记录日志并返回false
      } else IO.unit
  
      isDeleted <- if (doesExist) {
        // Step 2: 删除帖子
        val sqlDelete = s"DELETE FROM ${schemaName}.post_table WHERE post_id = ?"
        val deleteParams = List(SqlParameter("String", postID))
        for {
          _ <- IO(logger.info(s"[Delete Post] 执行删除语句: ${sqlDelete}"))
          deleteResult <- writeDB(sqlDelete, deleteParams) // 删除记录
          _ <- IO(logger.info(s"[Delete Post] 数据库返回结果: ${deleteResult}"))
  
          // Step 2.2: 记录删除日志
          logMsg <- IO {
            s"[Delete Post] 帖子ID: ${postID}已成功删除，删除时间: ${DateTime.now}"
          }
          _ <- IO(logger.info(logMsg))
        } yield {
          logger.info(s"[Delete Post] 帖子ID: ${postID}删除操作完成，返回结果: Success")
          true
        }
      } else IO.pure(false)
    } yield isDeleted
  }
}
