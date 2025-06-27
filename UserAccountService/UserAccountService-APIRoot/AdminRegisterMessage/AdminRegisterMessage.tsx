/**
 * AdminRegisterMessage
 * desc: 管理员注册，生成 adminID，保存账户名称、密码，权限为管理员。
 * @param accountName: String (管理员账户名称，用于注册时输入的唯一标识。)
 * @param password: String (管理员账户密码，用于注册时输入，需加密存储。)
 * @return adminID: String (管理员唯一ID，注册成功后返回。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class AdminRegisterMessage extends TongWenMessage {
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

