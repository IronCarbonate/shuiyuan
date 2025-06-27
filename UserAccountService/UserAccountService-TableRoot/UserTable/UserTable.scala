      /** 用户表，用于存储用户的基本账户信息
       * user_id: 用户的唯一ID，使用UUID
       * account_name: 用户账户名称
       * password: 用户密码，采用hash加密
       * nickname: 用户昵称，默认初始值为'未命名用户'
       * is_muted: 用户是否被禁言，默认值为false
       * created_at: 用户创建时间
       * permission: 用户权限，例如'Normal'或者'Admin'
       */
      _ <- writeDB(
        s"""
        CREATE TABLE IF NOT EXISTS "${schemaName}"."user_table" (
            user_id VARCHAR NOT NULL PRIMARY KEY,
            account_name TEXT NOT NULL,
            password TEXT NOT NULL,
            nickname TEXT DEFAULT '未命名用户',
            is_muted BOOLEAN NOT NULL DEFAULT false,
            created_at TIMESTAMP NOT NULL,
            permission TEXT NOT NULL
        );
         
        """,
        List()
      )