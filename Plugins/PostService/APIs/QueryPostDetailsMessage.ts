/**
 * QueryPostDetailsMessage
 * desc: 查询指定帖子的详细信息。
 * @param userToken: String (用户会话令牌，用于验证用户身份和权限。)
 * @param postID: String (帖子ID，用于指定需要查询的帖子。)
 * @return postDetails: Post:1120 (包含查询结果的帖子详细信息，包括标题、内容、标签、点赞数、评论列表等。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class QueryPostDetailsMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  postID: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

