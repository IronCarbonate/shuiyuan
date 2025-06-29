/**
 * QueryPinnedPostsMessageByTags
 * desc: 查询置顶帖子并按Tag筛选
 * @param userToken: String (用户会话令牌，用于验证用户登录状态。)
 * @param tags: PostTag (帖子所属标签列表，用于筛选指定分类的置顶帖子。)
 * @return postsList: PostSummary:1121 (查询结果，包含所有符合条件的置顶帖列表信息。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'
import { PostTag } from 'Plugins/PostService/Objects/PostTag';


export class QueryPinnedPostsMessageByTags extends TongWenMessage {
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

