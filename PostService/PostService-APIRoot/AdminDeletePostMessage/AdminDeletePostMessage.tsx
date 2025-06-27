/**
 * AdminDeletePostMessage
 * desc: 管理员删除帖子接口
 * @param adminToken: String (管理员Token，用于权限验证)
 * @param postID: String (帖子的唯一标识，用于表示目标需要删除的帖子)
 * @return result: String (操作结果，返回删除成功或失败的消息)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class AdminDeletePostMessage extends TongWenMessage {
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

