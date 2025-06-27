      /** 评论表，包含评论的基本信息
       * comment_id: 评论的唯一ID
       * post_id: 所属帖子的Post ID
       * user_id: 评论发布者的用户ID
       * content: 评论的内容
       * created_at: 评论创建时间
       */
      _ <- writeDB(
        s"""
        CREATE TABLE IF NOT EXISTS "${schemaName}"."comment_table" (
            comment_id VARCHAR NOT NULL PRIMARY KEY,
            post_id TEXT NOT NULL,
            user_id TEXT NOT NULL,
            content TEXT NOT NULL,
            created_at TIMESTAMP NOT NULL
        );
         
        """,
        List()
      )