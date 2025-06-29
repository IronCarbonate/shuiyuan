/**
 * GetUserRoleByIDMessage
 * desc: 查询指定用户的角色。
 * @param userToken: String (用户令牌，用于验证会话有效性。)
 * @param userID: String (目标用户的唯一标识。)
 * @return role: UserRole:1105 (目标用户的角色类型，枚举值包括Admin和Normal。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class GetUserRoleByIDMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  userID: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10011"
    }
}

