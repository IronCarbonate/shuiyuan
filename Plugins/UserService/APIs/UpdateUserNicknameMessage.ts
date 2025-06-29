/**
 * UpdateUserNicknameMessage
 * desc: 修改用户昵称并返回操作结果
 * @param userToken: String (用户会话令牌，用于验证用户是否有效登录)
 * @param newNickname: String (用户的新昵称，需符合昵称规则)
 * @return result: String (操作结果，返回更新成功消息或错误信息)
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
        return "127.0.0.1:10011"
    }
}

