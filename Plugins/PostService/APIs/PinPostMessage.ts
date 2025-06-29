/**
 * PinPostMessage
 * desc: 将帖子置顶并更新置顶状态。
 * @param adminToken: String (管理员身份验证Token，用于确认操作者的管理员权限。)
 * @param postID: String (目标帖子的唯一标识符。)
 * @return result: String (操作结果，表示帖子置顶是否成功。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class PinPostMessage extends TongWenMessage {
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

