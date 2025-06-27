/**
 * Post
 * desc: 帖子信息，包含帖子内容、作者和评论相关信息
 * @param postID: String (帖子的唯一ID)
 * @param userID: String (发布者的唯一ID)
 * @param title: String (帖子的标题)
 * @param content: String (帖子的内容)
 * @param createdAt: DateTime (帖子的创建时间)
 * @param commentCount: Int (帖子的评论数量)
 * @param latestCommentTime: DateTime (最近评论的时间)
 * @param commentList: Comment:1054 (帖子的评论列表)
 */
import { Serializable } from 'Plugins/CommonUtils/Send/Serializable'

import { Comment } from 'Plugins/PostService/Objects/Comment';


export class Post extends Serializable {
    constructor(
        public  postID: string,
        public  userID: string,
        public  title: string,
        public  content: string,
        public  createdAt: number,
        public  commentCount: number,
        public  latestCommentTime: number | null,
        public  commentList: Comment[] | null
    ) {
        super()
    }
}


