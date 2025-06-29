/**
 * CancelLikeCommentMessage
 * desc: 取消对指定评论的点赞
 * @param userToken: String (用户的会话令牌，用于验证操作的有效性)
 * @param commentID: String (需要取消点赞的评论ID)
 * @return result: String (操作结果，返回操作成功或失败信息)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class CancelLikeCommentMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  commentID: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10013"
    }
}

