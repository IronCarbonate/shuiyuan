/**
 * CreatePostMessage
 * desc: 创建帖子并保存到数据库。
 * @param userToken: String (用户登录会话令牌，用于验证用户身份。)
 * @param title: String (帖子标题，简要描述帖子内容。)
 * @param content: String (帖子正文内容。)
 * @param tag: PostTag:1045 (帖子标签，用于标记帖子分类。)
 * @return postID: String (生成的帖子唯一标识ID。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'
import { PostTag } from 'Plugins/PostService/Objects/PostTag';


export class CreatePostMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  title: string,
        public  content: string,
        public  tag: PostTag
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

