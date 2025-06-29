/**
 * AddCommentMessage
 * desc: 添加评论并将其关联到指定帖子。
 * @param userToken: String (用户的会话令牌，验证用户身份。)
 * @param postID: String (要关联评论的帖子ID。)
 * @param content: String (评论内容文本。)
 * @return commentID: String (生成的评论ID。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class AddCommentMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  postID: string,
        public  content: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10013"
    }
}

