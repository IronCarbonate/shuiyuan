import Objects.PostService.Post
import Objects.PostService.Comment
import Common.API.{PlanContext, Planner}
import Common.DBAPI._
import Common.Object.ParameterList
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import cats.effect.IO
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
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
import cats.implicits.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

def sortPostsByLatestComment(posts: List[Post])(using PlanContext): List[Post] = {
  val logger = LoggerFactory.getLogger(this.getClass)
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