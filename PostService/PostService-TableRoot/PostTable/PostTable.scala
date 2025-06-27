      /** 帖子表，包含帖子的基本信息以及互动数据
       * post_id: 帖子的唯一ID
       * user_id: 发布者的用户ID
       * title: 帖子的标题
       * content: 帖子的内容
       * created_at: 帖子的创建时间
       * comment_count: 当前帖子的评论数量
       * latest_comment_time: 帖子最后一条评论的时间
       */
      _ <- writeDB(
        s"""
        CREATE TABLE IF NOT EXISTS "${schemaName}"."post_table" (
            post_id VARCHAR NOT NULL PRIMARY KEY,
            user_id TEXT NOT NULL,
            title TEXT NOT NULL,
            content TEXT NOT NULL,
            created_at TIMESTAMP NOT NULL,
            comment_count INT NOT NULL DEFAULT 0,
            latest_comment_time TIMESTAMP
        );
         
        """,
        List()
      )