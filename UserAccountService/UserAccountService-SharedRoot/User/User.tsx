/**
 * User
 * desc: 用户信息实体类，包含用户的基本信息和权限
 * @param userID: String (用户的唯一标识ID)
 * @param accountName: String (用户的账户名)
 * @param password: String (用户的密码)
 * @param nickName: String (用户的昵称)
 * @param isMuted: Boolean (表示用户是否被禁言)
 * @param createdAt: DateTime (用户账户创建时间)
 * @param permission: UserPermission:1055 (用户的权限信息)
 */
import { Serializable } from 'Plugins/CommonUtils/Send/Serializable'

import { UserPermission } from 'Plugins/UserAccountService/Objects/UserPermission';


export class User extends Serializable {
    constructor(
        public  userID: string,
        public  accountName: string,
        public  password: string,
        public  nickName: string,
        public  isMuted: boolean,
        public  createdAt: number,
        public  permission: UserPermission
    ) {
        super()
    }
}


