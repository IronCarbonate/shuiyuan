/**
 * CreatePostMessage
 * desc: 创建帖子接口，用于处理创建帖子功能。
 * @param userToken: String (用户身份验证的令牌，通过它确认用户的有效性。)
 * @param title: String (帖子标题，简要描述帖子的内容或主题。)
 * @param content: String (帖子主要内容，详细描述帖子的具体信息。)
 * @return postID: String (生成的帖子ID，唯一标识帖子。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class CreatePostMessage extends TongWenMessage {
    constructor(
        public  userToken: string,
        public  title: string,
        public  content: string
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

