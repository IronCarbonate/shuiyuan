/**
 * QueryUserTokenMessage
 * desc: 通过用户ID查询当前用户的会话令牌
 * @param userID: String (用户唯一标识，区分不同用户)
 * @return userToken: String (用户登录会话令牌，用于标识用户的登录状态)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class QueryUserTokenMessage extends TongWenMessage {
    constructor(
        public  userID: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10011"
    }
}

