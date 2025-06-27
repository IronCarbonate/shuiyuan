/**
 * UserRegisterMessage
 * desc: 用户注册，生成用户ID，保存账户名称、密码和初始昵称，默认权限为普通用户。
 * @param accountName: String (用户账户名称，用于用户登录)
 * @param password: String (用户账号密码，用于验证账户安全性)
 * @return userID: String (新生成的用户ID，用于唯一标识用户)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class UserRegisterMessage extends TongWenMessage {
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

