# TestDMAPI
> 接入云小微账号系统及音乐服务相关API的Android demo，使用第三方厂商账号接入

### 须知

默认APP运行时已经初始化SDK，

`accountId`：第三方账号唯一识别符

`firmAcctId`：同`accountId`

`DSN`：`DEFAULT_APP_KEY`使用`_`拼接`firmAcctId`

### 自定义返回参数

```json
{
  -100: "未初始化SDK",
  -99: "缓存失效",
  -1: "解析失败",
  -2: "网络请求失败",
  -3: "参数异常",
  -4: "respBody 空",
  -5: "请重新生成QQ音乐授权二维码",
  -6: "返回数据异常",
  -51: "收藏歌单时，歌单id为空",
}
```

### 初始化

需要在`DM_API_Module/src/main/java/com/jingluo/dm_api_module/constant/Constant.java`将如下常量填写

```java
// 在云小微开放平台生成的appkey
public static final String DEFAULT_APP_KEY = "";

// APP Secret用于厂商账号登录，在云小微开放平台勾选厂商账号登录后，会显示出来
public static final String DEFAULT_APP_SECRET = "";

// 在云小微开放平台生成的access Token
public static final String DEFAULT_APP_ACCESS_TOKEN = "";

// 向QQ音乐部门申请到的AppID *此参数目前没有使用，可不填*
public static final String QQ_MUSIC_APP_ID = "";

// 私钥，用于使用QQ音乐APP进行授权的场景 *此参数目前没有使用，可不填*
public static final String AppPrivateKey = "";

// productId
public static final String ProductId = "";
```

### 使用流程

1. 获取SIG
2. 登录
3. 生成QQ音乐二维码
4. 查询二维码绑定状态
5. QQ音乐服务相关操作

其余方法按需使用。**目前无自动刷新token，账号token有效期为2小时**