/**
 * AdminLoginMessage
 * desc: 管理员登录，验证账户和密码是否匹配后返回 adminToken。
 * @param accountName: String (管理员账户名称，用于管理员登录时的身份验证。)
 * @param password: String (管理员账户密码，用于管理员登录时的身份验证。)
 * @return adminToken: String (管理员登录成功后返回的唯一标识 token，用于后续操作身份验证。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class AdminLoginMessage extends TongWenMessage {
    constructor(
        public  accountName: string,
        public  password: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10010"
    }
}

