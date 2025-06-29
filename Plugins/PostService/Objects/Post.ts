/**
 * Post
 * desc: 帖子信息，包括标题、内容以及交互信息
 * @param postID: String (帖子的唯一标识符)
 * @param userID: String (发布帖子的用户唯一标识符)
 * @param title: String (帖子的标题)
 * @param content: String (帖子的内容)
 * @param tag: PostTag (帖子的标签，表示所属分类)
 * @param isPinned: Boolean (帖子是否置顶)
 * @param createdAt: DateTime (帖子创建时间)
 * @param updatedAt: DateTime (帖子更新时间)
 * @param likes: PostLikeEntry (帖子的点赞信息列表)
 * @param comments: Comment:1031 (帖子的评论列表)
 */
import { Serializable } from 'Plugins/CommonUtils/Send/Serializable'

import { PostTag } from 'Plugins/PostService/Objects/PostTag';
import { PostLikeEntry } from 'Plugins/PostService/Objects/PostLikeEntry';
import { Comment } from 'Plugins/CommentService/Objects/Comment';


export class Post extends Serializable {
    constructor(
        public  postID: string,
        public  userID: string,
        public  title: string,
        public  content: string,
        public  tag: PostTag,
        public  isPinned: boolean,
        public  createdAt: number,
        public  updatedAt: number,
        public  likes: PostLikeEntry[],
        public  comments: Comment[]
    ) {
        super()
    }
}


