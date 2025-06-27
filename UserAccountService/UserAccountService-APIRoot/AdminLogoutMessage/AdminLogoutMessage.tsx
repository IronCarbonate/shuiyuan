/**
 * AdminLogoutMessage
 * desc: 管理员登出，清除登录状态。
 * @param adminToken: String (管理员登录的唯一标识，用于验证登出请求的合法性。)
 * @return result: String (登出结果提示信息，例如“登出成功”。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class AdminLogoutMessage extends TongWenMessage {
    constructor(
        public  adminToken: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10010"
    }
}

