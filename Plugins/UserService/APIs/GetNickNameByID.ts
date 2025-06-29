/**
 * GetNickNameByID
 * desc: 
 * @param userID: String (用户唯一的标识)
 * @param userToken: String (用户令牌，用于验证会话有效性)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class GetNickNameByID extends TongWenMessage {
    constructor(
        public  userID: string,
        public  userToken: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10011"
    }
}

