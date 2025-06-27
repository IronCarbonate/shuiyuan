/**
 * GetPostListMessage
 * desc: 获取全部帖子列表功能接口
 * @param userToken: String (用户身份令牌，用于验证用户登录状态和权限)
 * @return posts: PostInfo:1072 (帖子摘要列表，包含帖子标题、评论数和帖子ID)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class GetPostListMessage extends TongWenMessage {
    constructor(
        public  userToken: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

