/**
 * CommentLikeEntry
 * desc: 评论点赞信息，包含点赞用户的ID
 * @param userID: String (点赞用户的唯一ID)
 */
import { Serializable } from 'Plugins/CommonUtils/Send/Serializable'




export class CommentLikeEntry extends Serializable {
    constructor(
        public  userID: string
    ) {
        super()
    }
}


