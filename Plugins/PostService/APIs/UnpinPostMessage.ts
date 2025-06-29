/**
 * UnpinPostMessage
 * desc: 取消帖子置顶状态并返回操作成功结果。
 * @param adminToken: String (管理员身份令牌，用户验证管理员权限。)
 * @param postID: String (帖子ID，用于定位需要更新置顶状态的帖子。)
 * @return result: String (操作结果消息，返回置顶状态更新的成功信息。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class UnpinPostMessage extends TongWenMessage {
    constructor(
        public  adminToken: string,
        public  postID: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

