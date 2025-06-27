/**
 * Comment
 * desc: 评论信息类
 * @param commentID: String (评论的唯一ID)
 * @param postID: String (关联的帖子ID)
 * @param userID: String (发布者的唯一ID)
 * @param content: String (评论的具体内容)
 * @param createdAt: DateTime (评论的创建时间)
 */
import { Serializable } from 'Plugins/CommonUtils/Send/Serializable'




export class Comment extends Serializable {
    constructor(
        public  commentID: string,
        public  postID: string,
        public  userID: string,
        public  content: string,
        public  createdAt: number
    ) {
        super()
    }
}


