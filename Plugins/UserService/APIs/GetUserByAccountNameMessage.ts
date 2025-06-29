/**
 * GetUserByAccountNameMessage
 * desc: 根据账号名称查询用户ID。
 * @param accountName: String (用户账号名称，用于唯一标识用户。)
 * @return userID: String (查询到的用户唯一标识ID。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class GetUserByAccountNameMessage extends TongWenMessage {
    constructor(
        public  accountName: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10011"
    }
}

