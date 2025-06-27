/**
 * CreateCommentMessage
 * desc: 创建评论功能，处理发布新评论的请求
 * @param userToken: String (用户登录令牌，用于验证用户权限和身份)
 * @param postID: String (目标帖子的唯一标识ID，表示评论关联的帖子)
 * @param content: String (评论的详细内容)
 * @return commentID: String (新创建评论的唯一标识ID)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class CreateCommentMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  postID: string,
        public  content: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10011"
    }
}

