# McpMobile —— 一个属于你自己的 MCP 服务器 App

在手机上运行 MCP（Model Context Protocol）工具的轻量服务器。
你可以自由添加工具，让 AI 通过 HTTP 调用你手机上的能力。

## 特性
- 前台服务常驻，稳定不掉线
- 内嵌 NanoHTTPD，端口可配置（默认 8080）
- 标准 MCP 协议：`initialize` / `tools/list` / `tools/call`
- Token 鉴权 + 仅本地访问开关
- 插件式工具框架，加工具 = 加一个文件

## 已内置工具
| 工具名 | 说明 |
|---|---|
| `echo` | 回显文本，连通性测试 |
| `list_dir` | 列出目录内容 |
| `read_text` | 读取文本文件 |
| `write_text` | 写入文本文件 |
| `device_info` | 获取设备信息 |

## 如何添加自己的工具
1. 在 `app/src/main/java/com/nyan/mcpmobile/tool/impl/` 下新建一个类，实现 `McpTool`
2. 在 `ToolRegistry.init()` 里 `register(你的工具())`
3. 重新构建即可

示例：
```kotlin
class MyTool : McpTool {
    override val name = "my_tool"
    override val description = "我的工具"
    override fun inputSchema() = JSONObject("""{"type":"object","properties":{}}""")
    override fun invoke(args: JSONObject): String = "hello from my tool"
}
```

## 编译
推荐用 GitHub Actions 云构建（见 `.github/workflows/build.yml`）：
1. 把整个项目 push 到 GitHub
2. Actions 自动编译
3. 在 Actions 的 Artifacts 里下载 APK

或本地 Termux / AndroidIDE 执行：`gradle assembleRelease`

## 使用
1. 安装 App，点「启动」
2. 在 AI 客户端（如 Operit / Claude Desktop）里配置 MCP 服务器地址：
   `http://<手机IP>:8080/mcp`
3. 记得在 App 里设置 Token，并打开「仅允许本地访问」更安全

> ⚠️ 安全提醒：开启局域网访问时，同网络下任何人都可能访问你的文件，
> 请务必设置 Token，并在不用时关闭服务。
