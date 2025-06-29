
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
            /** 用户表，包含用户的基本信息
       * user_id: 用户的唯一ID，自动递增主键
       * account_name: 用户账号名称，唯一标识
       * password: 用户密码
       * nickname: 用户昵称
       * role: 用户角色（Admin 或 Normal）
       * is_muted: 是否被禁言
       * created_at: 用户注册时间
       */
      _ <- writeDB(
        s"""
        CREATE TABLE IF NOT EXISTS "${schemaName}"."user_table" (
            user_id VARCHAR NOT NULL PRIMARY KEY,
            account_name TEXT NOT NULL,
            password TEXT NOT NULL,
            nickname TEXT,
            role TEXT NOT NULL DEFAULT 'Normal',
            is_muted BOOLEAN NOT NULL DEFAULT false,
            created_at TIMESTAMP NOT NULL
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
    