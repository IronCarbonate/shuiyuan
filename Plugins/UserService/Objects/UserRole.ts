export enum UserRole {
    admin = '管理员',
    normal = '普通用户'
}

export const userRoleList = Object.values(UserRole)

export function getUserRole(newType: string): UserRole {
    return userRoleList.filter(t => t === newType)[0]
}
