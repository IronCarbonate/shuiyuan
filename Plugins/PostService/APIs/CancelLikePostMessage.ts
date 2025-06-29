/**
 * CancelLikePostMessage
 * desc: 取消对帖子的点赞。
 * @param userToken: String (用户的会话令牌，用于验证用户身份。)
 * @param postID: String (帖子的唯一标识符。)
 * @return result: String (操作结果，例如“取消点赞成功！”。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class CancelLikePostMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  postID: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

