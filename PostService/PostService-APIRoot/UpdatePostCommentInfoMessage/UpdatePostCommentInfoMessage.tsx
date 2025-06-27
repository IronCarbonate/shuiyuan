/**
 * UpdatePostCommentInfoMessage
 * desc: 更新帖子评论信息，增加评论计数并更新最新评论时间。
 * @param postID: String (帖子唯一标识，用于定位具体帖子。)
 * @param commentTime: DateTime (最新评论的时间，用于更新帖子的最新评论时间字段。)
 * @return result: String (操作结果提示信息，例如更新成功或失败。)
 */
import { TongWenMessage } from 'Plugins/TongWenAPI/TongWenMessage'



export class UpdatePostCommentInfoMessage extends TongWenMessage {
    constructor(
        public  postID: string,
        public  commentTime: number
    ) {
        super()
    }
    getAddress(): string {
        return "127.0.0.1:10012"
    }
}

