/**
 * VerifyAccountPasswordMessage
 * desc: 验证账号名称和密码是否匹配
 * @param accountName: String (用户的账号名称)
 * @param password: String (用户的密码)
 * @return result: Boolean (验证结果，表示账号名称和密码是否匹配)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class VerifyAccountPasswordMessage extends TongWenMessage {
    constructor(
        public  accountName: string,
        public  password: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10011"
    }
}

