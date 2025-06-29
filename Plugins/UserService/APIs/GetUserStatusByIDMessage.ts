/**
 * GetUserStatusByIDMessage
 * desc: 查询用户禁言状态
 * @param userID: String (用户的唯一标识，用于查询禁言状态)
 * @return status: Boolean (用户禁言状态，true表示已禁言，false表示未禁言)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class GetUserStatusByIDMessage extends TongWenMessage {
    constructor(
        public  userID: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10011"
    }
}

