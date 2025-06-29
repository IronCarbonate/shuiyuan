/**
 * QueryUnPinnedPostsMessage
 * desc: 返回所有非置顶帖列表
 * @param userToken: String (用户会话令牌，用于验证用户身份和会话有效性)
 * @return postsList: PostSummary:1121 (帖子列表，包含非置顶帖的简要信息)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class QueryUnPinnedPostsMessage extends TongWenMessage {
    constructor(
        public  userToken: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

