export enum UserPermission {
    normal = '普通用户',
    admin = '管理员'
}

export const userPermissionList = Object.values(UserPermission)

export function getUserPermission(newType: string): UserPermission {
    return userPermissionList.filter(t => t === newType)[0]
}
