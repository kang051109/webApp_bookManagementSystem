# 图书管理系统 — 综合评价与改进方案（完整版）

> 评价日期：2026-06-10 | 项目：Spring Boot 3.4 + Vue 3 + Vue Router 4 + Vite 5 + MySQL 8.0
>
> 项目根目录：`D:\27658\java_code\webApp_project`

---

## 一、总体评分

| 维度 | 得分 | 评语 |
|------|------|------|
| 代码架构 | 85/100 | 事务设计出色，手工 JDBC 增加维护成本 |
| 功能完整性 | 80/100 | 核心闭环完整，缺辅助功能（修改密码、分页、用户管理） |
| UI/UX 设计 | 95/100 | 同类项目中顶级水准，"Warm Archive" 设计语言 |
| 安全性 | 70/100 | BCrypt 到位，缺 CSRF 防护 + 配置硬编码 |
| 可维护性 | 75/100 | 代码清晰，但手工单例 + 无测试 |
| **综合** | **81/100** | **优秀的课程/毕设项目，前端设计达专业级水准** |

---

## 二、项目亮点

### 2.1 后端工程亮点

| 亮点 | 位置 | 说明 |
|------|------|------|
| 原子防竞态借阅 | `BorrowService.java:144-149` | `UPDATE books SET available_copies = available_copies + ? WHERE id=? AND available_copies + ? >= 0`，数据库层面防止超借 |
| 事务内二次检查 | `BorrowService.java:52-59` | 同一事务连接内复查逾期和重复借阅，消除并发窗口 |
| TOCTOU 防护 | `BookRepository.java:117-134` | 同一连接内完成"检查未还记录→删除图书"，消除时间差漏洞 |
| 安全脱敏 | `User.java:56-66` | `User.safeUser()` 确保 API 响应永不包含 `passwordHash` |
| 全局异常处理 | `GlobalExceptionHandler.java` | 400/401/403/404/409/500 六种状态码的语义化映射 |
| 库存智能计算 | `BookService.java:47-73` | 编辑图书时自动处理 `borrowed → totalCopies → availableCopies` 三角约束 |
| 种子数据 | `schema.sql` + `seed_books.sql` | 37 本真实图书 + 5 个分类 + 管理员账号 |

### 2.2 前端设计亮点 — "Warm Archive" 暖调档案馆

这是本项目**最出色的部分**，设计水准远超同级别课程项目。

**设计系统：**

```
色彩系统: copper(#C4562B) / denim(#4A6A8A) / ochre(#C49B3A) / warm-black(#2C2416)
           warm-gray(#8A7E72) / warm-border(#E2D8CC) / paper(#F2EDE6) / surface(#FAF8F4)
字体系统: DM Serif Display(标题) + Source Sans 3(正文) + PingFang SC(中文回退)
间距系统: 4px → 8px → 16px → 24px → 32px → 48px 六阶韵律
```

**细节表现：**

| 细节 | 实现方式 |
|------|---------|
| 骨架屏 | 5 处 `@keyframes skeleton-pulse` 动画，消除加载白屏闪烁 |
| 空状态 | 4 套手绘 SVG 插画 + 引导文案 + CTA 按钮（浏览图书、新增第一本） |
| 表格装饰 | 左上角 3px 铜色竖线 + hover 行 4% 铜色透明背景 |
| 状态标签 | 借阅中(牛仔蓝) / 已归还(绿色) / 已逾期(红土色)，语义色编码 |
| Modal | `box-shadow: 8px 8px 0` 硬阴影模拟档案馆卡片叠放 |
| 侧边栏 | 3px 赭石色 active 指示条，移动端 slide-in + 半透明 overlay |
| 路由过渡 | `<transition name="fade" mode="out-in">` 0.2s 淡入淡出 |
| 按钮反馈 | hover 时 `transform: scale(0.97)` 微缩放 |
| Toast | 手写发布订阅模式（`toast.js` 仅 35 行），零依赖 |
| 字距 | `letter-spacing: 0.02em~0.15em` 精细化视觉节奏控制 |
| 响应式 | 768px 断点：汉堡菜单 + 单列表单 + 网格重排 |
| 噪声纹理 | `body::before` 伪元素 SVG feTurbulence 噪点叠加（opacity 0.03） |

---

## 三、Bug 及问题汇总（共 9 项）

### 🔴 P0 — 高优先级：影响安全或功能

#### Bug #1：数据库密码硬编码

| 属性 | 内容 |
|------|------|
| **位置** | `src/main/java/.../util/DatabaseUtil.java` 第 20 行 |
| **问题** | `config.setPassword("mysql123")` 明文硬编码在代码中 |
| **影响** | 任何人拿到源码即可获取数据库密码；推送代码仓库时密码泄露 |
| **修复** | 移到 `application.properties`，用 `${DB_PASSWORD:mysql123}` 环境变量默认值 |

#### Bug #2：数据库连接配置全部硬编码

| 属性 | 内容 |
|------|------|
| **位置** | `src/main/java/.../util/DatabaseUtil.java` 第 18-31 行 |
| **问题** | JDBC URL、用户名、密码、HikariCP 全部参数写死在 Java 代码中 |
| **影响** | 切换环境（开发/测试/生产）需要改代码重新编译 |
| **修复** | 删除手工 HikariCP 初始化，用 `spring.datasource.*` 走 Spring Boot 自动配置 |

#### Bug #3：缺失路由守卫

| 属性 | 内容 |
|------|------|
| **位置** | `frontend/src/router/index.js` |
| **问题** | 登录验证仅靠 `Layout.vue` 的 `mounted()` 发 `/auth/me` 请求 |
| **影响** | 网络慢时页面会先渲染内容再猛然跳到登录页，体验极差 |
| **修复** | 添加 `router.beforeEach` 全局前置守卫，在路由解析前验证登录状态 |

---

### 🟡 P1 — 中优先级：影响健壮性

#### Bug #4：未登录用户访问受保护页面时先渲染再跳转

| 属性 | 内容 |
|------|------|
| **位置** | `frontend/src/views/Layout.vue` 第 59-68 行 |
| **问题** | Layout 的 `mounted()` 是异步的，子路由组件可能在 `/auth/me` 返回前就已经开始渲染 |
| **影响** | 闪烁体验，且子组件的 API 调用可能失败产生多余错误日志 |
| **修复** | 配合 Bug #3 的路由守卫，Layout 组件中移除重复的登录检查 |

#### Bug #5：Register.vue 中 Toast 不显示

| 属性 | 内容 |
|------|------|
| **位置** | `frontend/src/views/Register.vue` 第 41 行 |
| **问题** | `handleRegister()` 调用了 `showToast('注册成功')`，但模板中没有 `<Toast ref="toast"/>` 组件 |
| **影响** | 注册成功后用户看不到 Toast 提示 |
| **修复** | 在 Register.vue 模板中添加 `<Toast ref="toast"/>` |

#### Bug #6：session 超时配置不一致

| 属性 | 内容 |
|------|------|
| **位置** | `application.properties` 设 `server.servlet.session.timeout=60m`，`AuthService.java:71` 又手动设 `session.setMaxInactiveInterval(3600)` |
| **问题** | 两处配置冲突，实际生效的是 3600 秒（60 分钟），但维护者可能误以为改 properties 就能改超时 |
| **修复** | 统一用 Spring Boot 配置，`AuthService` 读取 `server.servlet.session.timeout` |

---

### 🟢 P2 — 低优先级：代码质量

#### Bug #7：launch.json 配置错误

| 属性 | 内容 |
|------|------|
| **位置** | `.claude/launch.json` |
| **问题** | 配置了 `python3 -m http.server 3000`，但本项目是 Spring Boot + Vite 前端 |
| **影响** | 无法用 `preview_start` 启动项目 |
| **修复** | 改为 Spring Boot 启动命令和 Vite dev server 配置 |

#### Bug #8：全局使用 System.out.println 打印日志

| 属性 | 内容 |
|------|------|
| **位置** | `AuthService.java:37,47,49` 等 |
| **问题** | 用 `System.out.println` / `System.err.println` 而非日志框架 |
| **影响** | 无日志级别、无文件输出、无法按环境控制 |
| **修复** | 引入 SLF4J + Logback，用 `log.info()` / `log.error()` |

#### Bug #9：手工单例模式替代 Spring 依赖注入

| 属性 | 内容 |
|------|------|
| **位置** | 全部 Service 类和 Repository 类 |
| **问题** | `private static final INSTANCE = new Xxx()` + `getInstance()` 替代了 Spring 的 `@Service` + `@Autowired` |
| **影响** | 丧失 IoC 优势，难以做单元测试 Mock，无法利用 AOP |
| **修复** | 改为 `@Service` / `@Repository` 注解，用 `@Autowired` 注入 |

---

## 四、可添加功能列表（共 20 项）

### 🔴 P0 — 核心闭环缺失

| # | 功能 | 现状 | 需要做 |
|---|------|------|--------|
| **F01** | **修改密码** | `UserRepository.updatePassword()` 已写好但无 API 暴露 | 后端 `PUT /api/auth/password`（验证旧密码→设置新密码）；前端增加设置页 Modal |
| **F02** | **借阅记录分页** | `MyBorrows.vue` 和 `AllBorrows.vue` 全量加载所有记录 | `BorrowRecordRepository` 加 `findPage(page, size)` 方法 + 前端分页组件 |
| **F03** | **用户管理** | `UserRepository.findAll()` 已就绪，但无 controller 端点 | 管理员专属 `GET /api/admin/users` 用户列表页（查看/禁用/改角色） |

### 🟡 P1 — 体验提升

| # | 功能 | 现状 | 需要做 |
|---|------|------|--------|
| **F04** | **图书封面上传** | Book 模型无图片字段 | Book 表加 `image_url` 列；前端用 `<input type="file">` + `FormData` 上传 |
| **F05** | **逾期自动提醒** | 仪表板仅显示逾期数量 | `@Scheduled` 定时任务扫描逾期记录 → 系统内消息通知 |
| **F06** | **个人设置页面** | 用户无法修改个人信息 | `/profile` 路由 + 修改姓名/邮箱表单 |
| **F07** | **记住我** | 登录无"记住我"选项 | 登录表单加 checkbox，勾选后 session 超时延长至 7 天 |
| **F08** | **Dashboard 最近动态** | 仪表板只有 5 个统计数字 | 在统计卡片下方加"最近借阅"动态时间线列表 |
| **F09** | **借阅统计图表** | 无图表 | 用 ECharts 展示近 7 天/30 天借阅趋势柱状图 + 分类饼图 |

### 🟢 P2 — 扩展功能

| # | 功能 | 说明 |
|---|------|------|
| **F10** | **图书预约排队** | 库存为 0 时用户可排队，归还后自动通知第一位排队者 |
| **F11** | **数据导出** | 导出图书列表 / 借阅记录为 CSV 或 Excel |
| **F12** | **忘记密码** | 输入邮箱 → 发送重置链接 → 设置新密码 |
| **F13** | **操作审计日志** | 记录谁在什么时间做了什么操作（增删改查） |
| **F14** | **图书评分与评论** | 用户归还后可打分（1-5 星）+ 写短评 |
| **F15** | **批量导入图书** | 上传 Excel 批量导入图书数据 |
| **F16** | **多标签系统** | 一本书可打多个标签，跨分类检索 |
| **F17** | **邮件通知** | 借阅到期前 3 天自动发邮件提醒；逾期发警告 |
| **F18** | **API 文档** | 集成 SpringDoc OpenAPI，访问 `/swagger-ui.html` |
| **F19** | **单元测试** | JUnit 5 + MockMvc 覆盖核心业务逻辑，当前 0 测试 |
| **F20** | **深色模式** | CSS 变量体系已就绪，加一个 toggle 开关即可 |

---

## 五、网站设计改进建议（20 项）

### 5.1 导航体验

| # | 改进项 | 当前状态 | 建议 |
|---|--------|---------|------|
| **D01** | **全局搜索框** | 无 | Header 右侧加搜索 input，回车跳转 `/books?keyword=xxx` |
| **D02** | **侧边栏折叠记忆** | 每次刷新重置为关闭 | `localStorage.setItem('sidebarOpen', true/false)` |
| **D03** | **面包屑路径补全** | 部分页面 breadcrumb 不完整 | 三级以上嵌套页面保持 "首页 > 图书管理 > 编辑" 完整路径 |
| **D04** | **用户头像** | 只显示纯文字姓名 | 圆形首字母 avatar（`background: copper; color: white`） |

### 5.2 交互细节

| # | 改进项 | 当前状态 | 建议 |
|---|--------|---------|------|
| **D05** | **NProgress 进度条** | 路由切换无加载指示 | `npm install nprogress`，`router.beforeEach` 中 start，`afterEach` 中 done |
| **D06** | **表单草稿保存** | 编辑图书时关掉页面丢失 | `beforeunload` 事件用 `localStorage` 暂存表单数据 |
| **D07** | **批量操作** | 图书管理无批量删除 | 表格加 checkbox 列 + 顶部"批量删除"按钮 |
| **D08** | **列表排序** | 表格不支持点击排序 | 表头加排序图标，点击按该列升/降序排列 |
| **D09** | **键盘快捷键** | 无 | `Ctrl+K` 聚焦搜索；`Esc` 关闭 Modal；`?` 显示快捷键面板 |
| **D10** | **确认对话框统一** | 只有删除用 Modal 确认 | 归还、批量操作等破坏性操作也加确认 |

### 5.3 视觉增强

| # | 改进项 | 当前状态 | 建议 |
|---|--------|---------|------|
| **D11** | **图书卡片视图** | 只有表格视图 | 列表/卡片切换按钮，卡片视图显示封面+书名+作者+库存徽标 |
| **D12** | **打印样式** | 无 | 借阅记录加 `@media print` 隐藏导航和按钮 |
| **D13** | **favicon** | 无 | 生成 📚 图书图标作为 favicon |
| **D14** | **趋势指示器** | Dashboard 纯数字 | 借阅数字旁加 ↑↓ 箭头 + 百分比（环比昨日/上周） |
| **D15** | **图书状态角标** | 无 | 列表中的已借完图书加红色小角标 "借完" |
| **D16** | **页面标题** | 无 `<title>` 动态更新 | `document.title = '仪表板 - 图书管理系统'` |

### 5.4 无障碍

| # | 改进项 | 当前状态 | 建议 |
|---|--------|---------|------|
| **D17** | **焦点可见性** | 仅输入框有 `:focus` | 所有可交互元素加 `:focus-visible { outline: 2px solid var(--copper) }` |
| **D18** | **ARIA 标签** | 缺失 | 导航加 `aria-label`；Modal 加 `role="dialog"` + `aria-modal="true"` |
| **D19** | **键盘关闭 Modal** | 只能点击关闭 | 添加 `@keydown.escape="closeModal"` |

### 5.5 性能

| # | 改进项 | 当前状态 | 建议 |
|---|--------|---------|------|
| **D20** | **分类列表缓存** | 每次切换页面重新请求 `/api/categories` | 用 `Map` 内存缓存 5 分钟，或 `sessionStorage` |

---

## 六、后端架构改进建议

### 6.1 数据库配置去硬编码

**当前问题代码**（`DatabaseUtil.java`）：
```java
// ❌ 密码、URL、连接池参数全部硬编码
config.setJdbcUrl("jdbc:mysql://localhost:3306/book_management?...");
config.setUsername("root");
config.setPassword("mysql123");
```

**推荐修复方案** — 在 `application.properties` 中统一管理：
```properties
server.port=8080
server.servlet.session.timeout=60m

# 数据库配置（Spring Boot 自动配置 HikariCP）
spring.datasource.url=jdbc:mysql://localhost:3306/book_management?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:mysql123}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

然后删除 `DatabaseUtil` 中的手工 HikariCP 初始化，改为注入 Spring 管理的 `DataSource`。

### 6.2 从手工 JDBC 迁移到 Spring Data JPA

当前全部 Repository 手写 SQL。优点是可精确控制，缺点是样板代码多。可选迁移方案：
- `BookRepository` 改为 `interface BookRepository extends JpaRepository<Book, Long>`
- 自动获得：分页（`Pageable`）、审计（`@CreatedDate`）、方法命名查询

### 6.3 引入 DTO 层

当前 Controller 直接接收 Map 和 Model 对象：
```java
// ❌ 类型不安全
public JsonResponse<Map<String, Object>> login(@RequestBody Map<String, String> request, ...)
public JsonResponse<Map<String, Object>> create(@RequestBody Book book, ...)
```

建议引入：
- `LoginRequest { username, password, rememberMe }` — 替代 `Map<String, String>`
- `CreateBookRequest` / `UpdateBookRequest` — 区分创建和更新时的必填字段
- `ChangePasswordRequest { oldPassword, newPassword }` — 修改密码专用

### 6.4 日志框架

```java
// ❌ 当前做法
System.out.println("[Auth] 管理员密码已确保正确 (admin/admin123)");

// ✅ 推荐做法
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
private static final Logger log = LoggerFactory.getLogger(AuthService.class);
log.info("管理员账号已就绪");
```

### 6.5 Spring Security 集成

引入 Spring Security 替代手工 Session 管理：
- 自动获得 CSRF 防护
- BCrypt 密码编码器内置（替代 jbcrypt 0.4）
- `@PreAuthorize("hasRole('ADMIN')")` 声明式权限控制
- 登录成功/失败事件监听

---

## 七、优先实施路线图

```
┌─────────────────────────────────────────────────────────────────┐
│  Phase 1 — 紧急修复（1-2天）                                      │
│  ☐ Bug #1 + #2: 数据库密码移到 application.properties           │
│  ☐ Bug #3: 添加路由守卫 beforeEach                              │
│  ☐ Bug #5: Register.vue 补 Toast 组件                            │
├─────────────────────────────────────────────────────────────────┤
│  Phase 2 — 补核心功能（3-5天）                                    │
│  ☐ F01: 修改密码 API + 前端 Modal                               │
│  ☐ F02: 借阅记录分页（后端 LIMIT/OFFSET + 前端分页栏）          │
│  ☐ F03: 管理员用户列表页                                         │
│  ☐ D01: Header 全局搜索框                                       │
├─────────────────────────────────────────────────────────────────┤
│  Phase 3 — 设计增强（3-5天）                                      │
│  ☐ D05: NProgress 路由进度条                                     │
│  ☐ D11: 图书卡片/表格视图切换                                     │
│  ☐ D07: 批量操作（勾选多行删除）                                   │
│  ☐ D08: 表格列排序                                               │
│  ☐ F20: 深色模式 toggle                                          │
├─────────────────────────────────────────────────────────────────┤
│  Phase 4 — 扩展功能（按需推进）                                    │
│  ☐ F18: Swagger API 文档                                        │
│  ☐ F04: 图书封面上传                                             │
│  ☐ F09: 借阅统计图表 (ECharts)                                    │
│  ☐ F17: 邮件逾期通知                                             │
│  ☐ F19: 单元测试覆盖                                             │
│  ☐ 6.5: Spring Security 集成                                     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 八、立即可实施的 3 个改动（复制即用）

### 改动 1：路由守卫（修复 Bug #3，最重要）

在 `frontend/src/router/index.js` 的 `export default router` 之前添加：

```javascript
import api from '../services/api.js'
import { authStore } from '../services/authStore.js'

router.beforeEach(async (to, from, next) => {
  // 公开页面直接放行
  if (to.path === '/login' || to.path === '/register') {
    return next()
  }
  // 验证登录状态
  try {
    const res = await api.get('/auth/me')
    if (res.code === 200 && res.data?.user) {
      authStore.currentUser = res.data.user
      authStore.isAdmin = res.data.user.role === 'admin'
      return next()
    }
  } catch {}
  // 未登录 → 跳转登录页
  next('/login')
})
```

然后在 `Layout.vue` 的 `mounted()` 中删除重复的 `/auth/me` 请求（第 60-68 行），因为路由守卫已经做了。

### 改动 2：Register.vue 补 Toast 组件（修复 Bug #5）

在 `Register.vue` 模板最外层 `<div>` 内的末尾添加：
```html
<Toast ref="toast" />
```

在 `<script>` 中添加 import：
```javascript
import Toast from '../components/Toast.vue'
```

在 `export default` 中添加：
```javascript
components: { Toast }
```

### 改动 3：NProgress 路由进度条（设计改进 D05）

```bash
cd frontend && npm install nprogress
```

在 `frontend/src/main.js` 开头添加：
```javascript
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false, speed: 400, minimum: 0.2 })

router.beforeEach((to, from, next) => {
  if (from.name) NProgress.start()
  next()
})
router.afterEach(() => NProgress.done())
router.onError(() => NProgress.done())
```

---

## 九、完整统计

| 类别 | 数量 |
|------|------|
| Bug 和问题 | 9 项（P0: 3, P1: 3, P2: 3） |
| 可添加功能 | 20 项（P0: 3, P1: 6, P2: 11） |
| 设计改进 | 20 项（导航 4 + 交互 6 + 视觉 6 + 无障碍 3 + 性能 1） |
| 架构改进 | 5 项 |
| **总计** | **54 项改进建议** |

---

> **📊 总结：**
>
> 这是一个架构扎实、前端设计出色的 Spring Boot + Vue 3 图书管理系统。核心业务闭环（注册→登录→浏览→借阅→归还）**功能完整**，借阅模块有严谨的**事务保护和并发控制**。前端 "Warm Archive" 设计语言是最大亮点——骨架屏、空状态插画、色彩系统、字体搭配、响应式布局均达到**专业水准**，远超一般课程设计项目。
>
> 主要短板：**数据库密码硬编码**需立即修复，**缺失路由守卫**导致登录态校验存在闪烁体验，**缺少修改密码/用户管理/借阅分页**三项核心辅助功能。建议按 Phase 1→4 路线图分阶段推进，补齐后可达到**生产可用标准**。
