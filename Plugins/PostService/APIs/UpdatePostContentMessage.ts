/**
 * UpdatePostContentMessage
 * desc: 更新帖子内容并记录修改历史。
 * @param userToken: String (用户的登录会话令牌，用于验证用户身份。)
 * @param postID: String (帖子ID，用于定位需要更新的帖子。)
 * @param newContent: String (更新后的帖子内容。)
 * @return result: String (操作结果，如“帖子内容更新成功！”。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class UpdatePostContentMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  postID: string,
        public  newContent: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

