/**
 * Comment
 * desc: 评论信息，包括内容、回复、点赞等
 * @param commentID: String (评论的唯一标识)
 * @param replyID: String (回复的评论ID，可选项)
 * @param userID: String (发表评论的用户ID)
 * @param postID: String (评论所属的帖子ID)
 * @param content: String (评论内容)
 * @param createdAt: DateTime (评论的创建时间)
 * @param likes: CommentLikeEntry:1085 (评论的点赞信息列表)
 * @param replies: String (回复的评论ID列表)
 */
import { Serializable } from 'Plugins/CommonUtils/Send/Serializable'

import { CommentLikeEntry } from 'Plugins/CommentService/Objects/CommentLikeEntry';


export class Comment extends Serializable {
    constructor(
        public  commentID: string,
        public  replyID: string | null,
        public  userID: string,
        public  postID: string,
        public  content: string,
        public  createdAt: number,
        public  likes: CommentLikeEntry[] | null,
        public  replies: string[] | null
    ) {
        super()
    }
}


