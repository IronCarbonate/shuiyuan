/**
 * Session
 * desc: 用户会话信息映射
 * @param userID: String (用户的唯一标识)
 * @param userToken: String (用户的会话 token)
 */
import { Serializable } from 'Plugins/CommonUtils/Send/Serializable'




export class Session extends Serializable {
    constructor(
        public  userID: string,
        public  userToken: string
    ) {
        super()
    }
}


