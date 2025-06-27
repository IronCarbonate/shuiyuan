## UserService

用于用户管理（注册、登录）的微服务

------

### UserRegisterMessage

根据用户传入账户名称和密码，完成用户注册需求。
入参: accountName: String
入参: password: String
返回值: userInfo: User
用于用户注册
**实现细节**

1. 调用`isAccountNameExists`方法，检查传入的`accountName`是否已经存在于数据库中。
   - 如果`accountName`已存在，则返回错误信息（如“账户名称已存在”）。
2. 如果`accountName`不存在，调用`createUser`方法生成新的`userID`，并将账户名称和密码存储到数据库中。
3. 返回包含`userID`和`accountName`的`User`对象，用于用户后续操作。

节点路径: BBSsimple/UserService/UserService-APIRoot/UserRegisterMessage

------

### UserLoginMessage

根据用户输入账户名称和密码，实现登录功能需求。
入参: accountName: String
入参: password: String
返回值: loginResult: LoginResult
用于用户登录
**实现细节**

1. 调用`validateUser`方法，校验传入的`accountName`和`password`是否与数据库记录匹配。
   - 如果不匹配，则返回空的`LoginResult`对象（如 `LoginResult(None, -1)`）作为登录失败标志。
2. 如果匹配，生成用于鉴权的`userToken`（可以通过JWT或类似技术实现），同时设置 Token 的过期时间（如 `expireAt`）。
3. 返回包含`userToken`和`expireAt`的`LoginResult`对象。

节点路径: BBSsimple/UserService/UserService-APIRoot/UserLoginMessage

------

### ValidateUserTokenMessage

根据用户传入的`userToken`，验证其有效性并解析用户ID。
入参: userToken: String
返回值: userID: Option[String]
用于用户身份鉴权
**实现细节**

1. 验证传入的`userToken`是否有效（检查过期时间、签名等）。
   - 如果`userToken`无效，返回`None`。
   - 如果`userToken`有效，解析并返回对应的`userID`。

节点路径: BBSsimple/UserService/UserService-APIRoot/ValidateUserTokenMessage

------

## PostService

用于帖子管理（创建、删除、查询、排序）的微服务

### CreatePostMessage

用户创建新帖子
入参: userToken: String
入参: title: String
入参: content: String
返回值: postCreateResult: PostCreateResult
用于处理帖子创建需求。

**实现细节**

1. 调用`ValidateUserTokenMessage`接口验证`userToken`有效性。
   - 如果`userToken`无效，则返回错误信息。
2. 从`userToken`中解析出`userID`。
3. 调用本服务内部`createPost`方法，传入`userID`、`title`、`content`生成`postID`和`createTime`，并保存到数据库。
4. 返回生成的`PostCreateResult`对象（包含`postID`和`createTime`）。

节点路径: BBSsimple/PostService/PostService-APIRoot/CreatePostMessage

------

------

### DeletePostMessage

用户删除自己的帖子
入参: userToken: String
入参: postID: String
返回值: deleteResult: String
用于删除帖子

**实现细节**

1. 调用`ValidateUserTokenMessage`接口验证`userToken`有效性。
   - 如果`userToken`无效，则返回错误信息。
2. 从`userToken`中解析出`userID`。
3. 调用本服务内部`isPostOwner`方法，校验`postID`对应的用户是否是当前的`userID`。
   - 如果校验不通过，则返回错误信息（如“无权限删除该帖子”）。
4. 如果校验通过，调用本服务内部`deletePost`方法，删除对应的帖子。
5. 返回String(success 或者 failed)

节点路径: BBSsimple/PostService/PostService-APIRoot/DeletePostMessage

------

------

### QueryPostMessage

用户查询帖子内容
入参: userToken: String
入参: postID: String
返回值: postDetails: PostDetails
用于查询帖子详细内容

**实现细节**

1. 调用`ValidateUserTokenMessage`接口验证`userToken`有效性。
   - 如果`userToken`无效，则返回错误信息。
2. 调用本服务内部`doesPostExist`方法检查`postID`是否存在。
   - 如果`postID`不存在，则返回错误信息（如“帖子不存在”）。
3. 调用本服务内部`getPostByID`方法，获取帖子详细信息。
4. 返回`PostDetails`对象（包含`title`、`content`和`createTime`）。

节点路径: BBSsimple/PostService/PostService-APIRoot/QueryPostMessage

------

------

### ListPostsMessage

用户查看所有帖子列表
入参: userToken: String
返回值: postList: List[PostOverview]
用于展示帖子列表（按时间从新到旧排序）

**实现细节**

1. 调用`ValidateUserTokenMessage`接口验证`userToken`有效性。
   - 如果`userToken`无效，则返回错误信息。
2. 调用本服务内部`listAllPosts`方法，查询所有帖子并按创建时间从新到旧排序，返回每个帖子的`postID`、`title`和`createTime`的简要信息。
3. 返回包含简化字段的`List[PostOverview]`对象。

节点路径: BBSsimple/PostService/PostService-APIRoot/ListPostsMessage

------