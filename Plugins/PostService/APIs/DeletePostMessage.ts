/**
 * DeletePostMessage
 * desc: 删除指定帖子并返回操作结果。
 * @param userToken: String (用户的会话令牌，用于验证当前用户身份和权限。)
 * @param postID: String (帖子唯一标识符，用于指定需要删除的帖子。)
 * @return result: String (操作结果字符串，表明帖子删除的成功与否。)
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

