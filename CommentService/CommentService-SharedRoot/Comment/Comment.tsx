/**
 * Comment
 * desc: 评论信息，包括评论的ID、所属帖子、作者ID、内容以及创建时间
 * @param commentID: String (评论的唯一ID)
 * @param postID: String (所属帖子的ID)
 * @param userID: String (发布者的用户ID)
 * @param content: String (评论内容)
 * @param createdAt: DateTime (评论创建时间)
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


