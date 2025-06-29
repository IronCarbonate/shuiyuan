/**
 * User
 * desc: 用户信息，包括账号、昵称、角色等
 * @param userID: String (用户的唯一ID)
 * @param accountName: String (用户账号名称)
 * @param password: String (用户账号密码)
 * @param nickname: String (用户昵称)
 * @param role: UserRole:1105 (用户角色，枚举类型)
 * @param isMuted: Boolean (是否被禁言)
 * @param createdAt: DateTime (创建时间)
 */
import { Serializable } from 'Plugins/CommonUtils/Send/Serializable'

import { UserRole } from 'Plugins/UserService/Objects/UserRole';


export class User extends Serializable {
    constructor(
        public  userID: string,
        public  accountName: string,
        public  password: string,
        public  nickname: string,
        public  role: UserRole,
        public  isMuted: boolean,
        public  createdAt: number
    ) {
        super()
    }
}


