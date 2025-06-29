/**
 * QueryPinnedPostsMessage
 * desc: 返回所有置顶帖列表。
 * @param userToken: String (用户会话令牌，用于验证登录状态。)
 * @return postsList: PostSummary:1121 (置顶帖子的列表信息，包含标题、标签和评论数。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class QueryPinnedPostsMessage extends TongWenMessage {
    constructor(
        public  userToken: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

