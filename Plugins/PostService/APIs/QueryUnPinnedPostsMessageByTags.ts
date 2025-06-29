/**
 * QueryUnPinnedPostsMessageByTags
 * desc: 查询非置顶帖子并按Tag筛选
 * @param userToken: String (用户登录后的会话令牌)
 * @param tags: PostTag (筛选的帖子标签列表)
 * @return postsList: PostSummary:1121 (符合条件的帖子概要信息列表)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'
import { PostTag } from 'Plugins/PostService/Objects/PostTag';


export class QueryUnPinnedPostsMessageByTags extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  tags: PostTag[] | null
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

