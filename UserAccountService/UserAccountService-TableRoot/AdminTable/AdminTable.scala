      /** 管理员表，包含管理员的基础信息
       * admin_id: 管理员的唯一ID
       * account_name: 管理员账户名称
       * password: hash加密的管理员密码
       * created_at: 管理员创建时间
       */
      _ <- writeDB(
        s"""
        CREATE TABLE IF NOT EXISTS "${schemaName}"."admin_table" (
            admin_id VARCHAR NOT NULL PRIMARY KEY,
            account_name TEXT NOT NULL,
            password TEXT NOT NULL,
            created_at TIMESTAMP NOT NULL
        );
         
        """,
        List()
      )