/**
 * LikePostMessage
 * desc: 为帖子点赞并更新点赞列表
 * @param userToken: String (用户令牌，用于验证用户身份和会话有效性)
 * @param postID: String (帖子的唯一标识符，用于标记需要点赞的帖子)
 * @return result: String (操作结果，表示点赞成功或失败的信息)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class LikePostMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  postID: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

