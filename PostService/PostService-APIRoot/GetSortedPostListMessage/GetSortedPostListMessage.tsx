/**
 * GetSortedPostListMessage
 * desc: 获取按最新评论时间排序的帖子列表。
 * @param userToken: String (用户登录令牌，用于验证用户身份有效性。)
 * @return posts: PostInfo:1072 (返回的帖子摘要信息列表，包括帖子标题、评论数和postID。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class GetSortedPostListMessage extends TongWenMessage {
    constructor(
        public  userToken: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

