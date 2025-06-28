User





userID: String



accountName: String



password: String



nickname: String



role: UserRole (Enum)



isMuted: Boolean

UserRole (Enum)





Admin: 管理员



Normal: 普通用户

Post





postID: String



userID: String



title: String



content: String



tags: PostTag (Enum)



isPinned: Boolean



createdAt: DateTime



updatedAt: DateTime



likes: List[PostLikeEntry]



comments: List[Comment]

PostTag (Enum)





CampusLife: 校园生活



LifeExperience: 人生经验



AcademicCommunication: 学术交流



CultureAndArt: 文化艺术



LeisureAndEntertainment: 休闲娱乐



DigitalTechnology: 数码科技



Announcements: 广而告知

PostLikeEntry





userID: String



likedAt: DateTime

Comment





commentID: String



replyID: Optional[String]



userID: String



postID: String



content: String



createdAt: DateTime



likes: List[CommentLikeEntry]



replies: List[replyID]

CommentLikeEntry





userID: String



likedAt: DateTime
