/**
 * DeletePostMessage
 * desc: 普通用户删除帖子接口
 * @param userToken: String (用户登录令牌，用于验证用户身份)
 * @param postID: String (帖子唯一标识符，指定需要删除的帖子)
 * @return result: String (操作结果，表示删除操作成功或失败的信息)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class DeletePostMessage extends TongWenMessage {
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

