/**
 * UserLoginMessage
 * desc: 用户登录，验证账户和密码是否匹配后返回 userToken。
 * @param accountName: String (用户账户名称)
 * @param password: String (用户账户密码)
 * @return userToken: String (登录成功后生成的唯一令牌，用于验证用户登录状态)
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

