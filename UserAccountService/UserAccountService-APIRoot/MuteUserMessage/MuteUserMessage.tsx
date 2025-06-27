/**
 * MuteUserMessage
 * desc: 管理员禁言用户接口
 * @param adminToken: String (管理员登录状态的令牌，用于验证管理员权限)
 * @param targetUserID: String (目标被禁言用户的唯一标识ID)
 * @return result: String (操作结果，返回禁言成功的提示信息)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class MuteUserMessage extends TongWenMessage {
    constructor(
        public  adminToken: string,
        public  targetUserID: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10010"
    }
}

