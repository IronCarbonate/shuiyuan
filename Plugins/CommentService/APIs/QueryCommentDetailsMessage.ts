/**
 * QueryCommentDetailsMessage
 * desc: 查询指定评论的详细信息。
 * @param userToken: String (用户的会话令牌，用于验证用户身份。)
 * @param commentID: String (需要查询的评论的唯一标识。)
 * @return commentDetails: Comment:1031 (查询到的评论详细信息，包括内容、点赞数及所有回复。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class QueryCommentDetailsMessage extends TongWenMessage {
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

