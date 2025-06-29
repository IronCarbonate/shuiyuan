/**
 * PostLikeEntry
 * desc: 帖子点赞信息
 * @param userID: String (用户的唯一ID)
 */
import { Serializable } from 'Plugins/CommonUtils/Send/Serializable'




export class PostLikeEntry extends Serializable {
    constructor(
        public  userID: string
    ) {
        super()
    }
}


