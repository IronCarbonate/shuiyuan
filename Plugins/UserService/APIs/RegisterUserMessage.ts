/**
 * RegisterUserMessage
 * desc: 用户注册，创建新用户信息并返回用户ID。
 * @param accountName: String (用户账号名称，用于唯一标识用户账户。)
 * @param password: String (用户密码，用于账户登录验证。)
 * @param nickname: String (用户昵称，用于显示在平台中。)
 * @param role: UserRole:1105 (用户角色类型，包括管理员和普通用户。)
 * @return userID: String (生成的用户唯一标识。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'
import { UserRole } from 'Plugins/UserService/Objects/UserRole';


export class RegisterUserMessage extends TongWenMessage {
    constructor(
        public  accountName: string,
        public  password: string,
        public  nickname: string,
        public  role: UserRole = Normal
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10011"
    }
}

