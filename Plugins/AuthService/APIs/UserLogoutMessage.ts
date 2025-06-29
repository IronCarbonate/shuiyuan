/**
 * UserLogoutMessage
 * desc: 用户登出，清除会话信息并确认登出状态。
 * @param userToken: String (会话令牌，用于标识当前用户的会话。)
 * @return result: String (操作结果，确认登出状态。返回成功或失败信息。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class UserLogoutMessage extends TongWenMessage {
    constructor(
        public  userToken: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10010"
    }
}

