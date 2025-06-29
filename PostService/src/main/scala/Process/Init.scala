
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
            /** 帖子表，包含帖子相关的基本信息
       * post_id: 帖子唯一ID
       * user_id: 帖子发布者用户ID
       * title: 帖子标题
       * content: 帖子内容
       * tag: 帖子所属标签
       * is_pinned: 是否置顶
       * created_at: 帖子创建时间
       * updated_at: 帖子最后更新的时间
       */
      _ <- writeDB(
        s"""
        CREATE TABLE IF NOT EXISTS "${schemaName}"."post_table" (
            post_id VARCHAR NOT NULL PRIMARY KEY,
            user_id TEXT NOT NULL,
            title TEXT NOT NULL,
            content TEXT NOT NULL,
            tag TEXT NOT NULL,
            is_pinned BOOLEAN NOT NULL DEFAULT false,
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP NOT NULL
        );
         
        """,
        List()
      )
      /** 存储帖子的点赞信息。
       * post_like_id: 点赞记录的唯一ID
       * post_id: 点赞的帖子ID
       * user_id: 点赞的用户ID
       */
      _ <- writeDB(
        s"""
        CREATE TABLE IF NOT EXISTS "${schemaName}"."post_like_table" (
            post_like_id VARCHAR NOT NULL PRIMARY KEY,
            post_id TEXT NOT NULL,
            user_id TEXT NOT NULL
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
    