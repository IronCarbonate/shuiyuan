/**
 * PostSummary
 * desc: 
 * @param postID: String (帖子的唯一标识符)
 * @param userNickName: String (发帖者的昵称)
 * @param title: String (帖子的标题)
 * @param numLiked: String (点赞数)
 * @param numComment: String (评论数)
 * @param updateAt: String (最后一次更新时间)
 */
import { Serializable } from 'Plugins/CommonUtils/Send/Serializable'




export class PostSummary extends Serializable {
    constructor(
        public  postID: string,
        public  userNickName: string,
        public  title: string,
        public  numLiked: string,
        public  numComment: string,
        public  updateAt: string
    ) {
        super()
    }
}


