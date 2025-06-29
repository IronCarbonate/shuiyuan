
package Process

import Common.API.{API, PlanContext, TraceID}
import Common.DBAPI.{initSchema, writeDB}
import Common.ServiceUtils.schemaName
import Global.ServerConfig
import cats.effect.IO
import io.circe.generic.auto.*
import java.util.UUID
import Global.DBConfig
import Process.ProcessUtils.server2DB
import Global.GlobalVariables

object Init {
  def init(config: ServerConfig): IO[Unit] = {
    given PlanContext = PlanContext(traceID = TraceID(UUID.randomUUID().toString), 0)
    given DBConfig = server2DB(config)

    val program: IO[Unit] = for {
      _ <- IO(GlobalVariables.isTest=config.isTest)
      _ <- API.init(config.maximumClientConnection)
      _ <- Common.DBAPI.SwitchDataSourceMessage(projectName = Global.ServiceCenter.projectName).send
      _ <- initSchema(schemaName)
            /** 评论表，用于存储帖子评论的相关信息
       * comment_id: 评论的唯一ID
       * reply_id: 被回复的评论ID
       * user_id: 评论发布者的用户ID
       * post_id: 评论所属帖子ID
       * content: 评论内容
       * created_at: 评论创建时间
       */
      _ <- writeDB(
        s"""
        CREATE TABLE IF NOT EXISTS "${schemaName}"."comment_table" (
            comment_id VARCHAR NOT NULL PRIMARY KEY,
            reply_id TEXT,
            user_id TEXT NOT NULL,
            post_id TEXT NOT NULL,
            content TEXT NOT NULL,
            created_at TIMESTAMP NOT NULL
        );
         
        """,
        List()
      )
      /** 评论点赞表，记录用户对评论的点赞行为
       * comment_like_id: 评论点赞记录的唯一ID
       * comment_id: 点赞的评论ID
       * user_id: 点赞的用户ID
       * node_path: 节点路径，指向表存储的路径
       */
      _ <- writeDB(
        s"""
        CREATE TABLE IF NOT EXISTS "${schemaName}"."comment_like_table" (
            comment_like_id VARCHAR NOT NULL PRIMARY KEY,
            comment_id TEXT NOT NULL,
            user_id TEXT NOT NULL,
            node_path TEXT NOT NULL DEFAULT 'shuiyuanfull/CommentService/CommentService-TableRoot/CommentLikeTable'
        );
         
        """,
        List()
      )
    } yield ()

    program.handleErrorWith(err => IO {
      println("[Error] Process.Init.init 失败, 请检查 db-manager 是否启动及端口问题")
      err.printStackTrace()
    })
  }
}
    