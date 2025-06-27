/**
 * UserLogoutMessage
 * desc: 用户登出，清除登录状态
 * @param userToken: String (用户的身份凭证，用于标识当前登录用户)
 * @return result: String (用户登出的结果提示信息，通常为登出成功的消息)
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

