/**
 * LikeCommentMessage
 * desc: 点赞评论并更新点赞列表。
 * @param userToken: String (用于用户身份验证的会话令牌。)
 * @param commentID: String (被点赞的评论ID。)
 * @return result: String (操作结果，例如'点赞成功！'。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class LikeCommentMessage extends TongWenMessage {
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

