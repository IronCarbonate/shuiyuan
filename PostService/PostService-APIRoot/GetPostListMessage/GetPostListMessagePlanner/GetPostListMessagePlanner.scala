package Impl


import Utils.PostManagementProcess.getAllPosts
import Objects.PostService.PostInfo
import Objects.PostService.Post
import Objects.PostService.Comment
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
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
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

case class GetPostListMessagePlanner(
                                      userToken: String,
                                      override val planContext: PlanContext
                                    ) extends Planner[List[PostInfo]] {

  // Logger实例，附带traceID
  private val logger = LoggerFactory.getLogger(this.getClass.getSimpleName + "_" + planContext.traceID.id)

  override def plan(using PlanContext): IO[List[PostInfo]] = {
    for {
      // Step 1: Validate user token
      _ <- IO(logger.info(s"开始验证 userToken=${userToken} 的有效性"))
      isValid <- validateUserToken
      _ <- IO {
        if (!isValid) {
          val errorMessage = "用户登录状态无效，userToken无效"
          logger.error(errorMessage)
          throw new IllegalStateException(errorMessage)
        }
      }

      // Step 2: Retrieve all post records
      _ <- IO(logger.info("开始从数据库中获取所有帖子记录"))
      posts <- getAllPosts()
      _ <- IO(logger.info(s"从数据库成功获取到 ${posts.size} 条帖子记录"))

      // Step 3: Convert Post records into PostInfo list
      postInfos <- IO {
        val infoList = posts.map { post =>
          PostInfo(
            title = post.title,
            commentCount = post.commentCount,
            postID = post.postID
          )
        }
        logger.info(s"成功封装为 ${infoList.size} 个 PostInfo")
        infoList
      }
    } yield postInfos
  }

  private def validateUserToken(using PlanContext): IO[Boolean] = {
    logger.info(s"验证 userToken=${userToken} 是否有效")
    val sql =
      s"""
         |SELECT is_valid
         |FROM ${schemaName}.user_session
         |WHERE token = ?
       """.stripMargin
    logger.info(s"SQL查询: ${sql}")

    readDBBoolean(
      sql,
      List(SqlParameter("String", userToken)) // 确保 userToken 作为字符串传入
    )
  }
}