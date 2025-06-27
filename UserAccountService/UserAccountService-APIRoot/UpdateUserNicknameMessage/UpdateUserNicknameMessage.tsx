/**
 * UpdateUserNicknameMessage
 * desc: 更新用户昵称
 * @param userToken: String (用户令牌，用于验证用户的登录状态和权限)
 * @param newNickname: String (新的用户昵称)
 * @return result: String (更新操作的结果信息，例如“修改昵称成功”)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class UpdateUserNicknameMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  newNickname: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10010"
    }
}

