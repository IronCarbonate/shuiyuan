export enum PostTag {
    campusLife = '校园生活',
    lifeExperience = '人生经验',
    academicCommunication = '学术交流',
    cultureAndArt = '文化艺术',
    leisureAndEntertainment = '休闲娱乐',
    digitalTechnology = '数码科技',
    announcements = '广而告知'
}

export const postTagList = Object.values(PostTag)

export function getPostTag(newType: string): PostTag {
    return postTagList.filter(t => t === newType)[0]
}
