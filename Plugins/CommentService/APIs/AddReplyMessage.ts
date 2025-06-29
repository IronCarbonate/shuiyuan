/**
 * AddReplyMessage
 * desc: 为指定评论添加回复并记录
 * @param userToken: String (用户身份令牌，用于验证会话有效性)
 * @param commentID: String (目标评论的唯一标识符，表示回复的评论)
 * @param content: String (回复内容的详细文本信息)
 * @return replyCommentID: String (生成的回复评论唯一标识符)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class AddReplyMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  commentID: string,
        public  content: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10013"
    }
}

