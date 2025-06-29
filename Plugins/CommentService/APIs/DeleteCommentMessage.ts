/**
 * DeleteCommentMessage
 * desc: 删除指定评论并返回操作结果。
 * @param userToken: String (用户会话令牌，用于验证身份和会话信息。)
 * @param commentID: String (评论的唯一标识，用于定位需要删除的评论。)
 * @return result: String (操作结果，返回是否成功删除评论的信息。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class DeleteCommentMessage extends TongWenMessage {
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

