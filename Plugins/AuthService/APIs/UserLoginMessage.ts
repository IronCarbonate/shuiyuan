/**
 * UserLoginMessage
 * desc: 用户登录，验证账号名称和密码是否匹配，并创建会话返回会话令牌。
 * @param accountName: String (用户账号名称，用于身份验证。)
 * @param password: String (用户密码，用于身份验证。)
 * @return userToken: String (用户登录成功后返回的会话令牌。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class UserLoginMessage extends TongWenMessage {
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

