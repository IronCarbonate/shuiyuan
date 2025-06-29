/**
 * MuteUserMessage
 * desc: 管理员禁言某用户并返回操作结果。
 * @param adminToken: String (管理员的用户令牌，用于验证管理员会话信息。)
 * @param targetUserID: String (需要进行禁言操作的目标用户ID。)
 * @return result: String (操作结果，表示禁言是否成功。)
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
        return "127.0.0.1:10011"
    }
}

