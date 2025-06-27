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
import cats.implicits._
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Objects.PostService.Post
import Objects.PostService.Comment
import io.circe.Json
import Common.API.{PlanContext}
import Common.Object.ParameterList

case object PostManagementProcess {
  private val logger = LoggerFactory.getLogger(getClass)
  //process plan code 预留标志位，不要删除
  
  
  def deletePost(postID: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger(getClass)  // 同文后端处理: logger 统一
  
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
  
  def getAllPosts()(using PlanContext): IO[List[Post]] = {
  // val logger = LoggerFactory.getLogger("getAllPosts")  // 同文后端处理: logger 统一
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
  
  def sortPostsByLatestComment(posts: List[Post])(using PlanContext): List[Post] = {
  // val logger = LoggerFactory.getLogger(this.getClass)  // 同文后端处理: logger 统一
    logger.info(s"开始对帖子列表按照最新评论时间进行排序")
    
    val sortedPosts = if (posts.isEmpty) {
      logger.info(s"帖子列表为空，直接返回空列表")
      List.empty[Post]
    } else {
      posts.sortWith((p1, p2) => {
        val latestTime1 = p1.latestCommentTime.getOrElse(new DateTime(0))
        val latestTime2 = p2.latestCommentTime.getOrElse(new DateTime(0))
        latestTime1.isAfter(latestTime2)
      })
    }
  
    logger.info(s"完成帖子排序，总共返回${sortedPosts.size}条帖子")
    sortedPosts
  }
  
  def createPost(userID: String, title: String, content: String)(using PlanContext): IO[String] = {
  // val logger = LoggerFactory.getLogger("createPost")  // 同文后端处理: logger 统一
    
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
}

