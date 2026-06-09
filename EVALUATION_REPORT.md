# 图书管理系统 — 综合评价与改进方案

> 评价日期：2026-06-09 | 项目：Spring Boot 3.4 + Vue 3 + MySQL

---

## 一、总体评分

| 维度 | 得分 | 评语 |
|------|------|------|
| 代码架构 | 85/100 | 事务设计出色，DI 使用可改进 |
| 功能完整性 | 80/100 | 核心闭环完整，缺辅助功能 |
| UI/UX 设计 | 95/100 | 同类项目中顶级水准 |
| 安全性 | 70/100 | BCrypt 到位，缺 CSRF + 配置安全 |
| 可维护性 | 75/100 | 代码清晰，手工 JDBC 增加维护成本 |
| **综合** | **81/100** | 优秀的课程/毕设项目 |

---

## 二、项目亮点 ✨

| 亮点 | 详情 |
|------|------|
| 事务设计 | `BorrowService` 手工事务 + 原子 SQL（`WHERE available_copies + ? >= 0`）防竞态超借 |
| TOCTOU 防护 | `BookRepository.delete()` / `CategoryRepository.delete()` 同一连接内检查+删除 |
| 安全脱敏 | `User.safeUser()` 确保 API 永不泄露密码哈希 |
| 骨架屏 | 5 处 skeleton-pulse 动画，完美消除加载闪烁 |
| 空状态 | 4 套自定义 SVG 插画 + 引导文案 + CTA 按钮 |
| 色彩系统 | copper / denim / ochre / warm-black 六色调和板 |
| 字体搭配 | DM Serif Display（标题） + Source Sans 3（正文） |
| 响应式 | 768px 汉堡菜单 + slide-in 侧边栏 |
| 种子数据 | 37 本真实图书 + 5 个分类 |

---

## 三、需要修复的问题 🔧

### 🔴 高优先级

| # | 问题 | 位置 | 修复方案 |
|---|------|------|---------|
| 1 | **数据库密码硬编码** | `DatabaseUtil.java:20` | 移到 `application.properties`，用 `spring.datasource.*` 配置 |
| 2 | **数据库连接配置硬编码** | `DatabaseUtil.java` | URL、用户名、密码全部硬编码，应走 Spring Boot 自动配置 |
| 3 | **jbcrypt 版本过老** | `pom.xml` | jbcrypt 0.4（2013年），建议升级到 Spring Security 内置 BCrypt |

**修复 #1 和 #2 的代码：**

`DatabaseUtil.java` 当前问题代码：
```java
// ❌ 硬编码的密码和连接信息
config.setJdbcUrl("jdbc:mysql://localhost:3306/book_management?...");
config.setUsername("root");
config.setPassword("mysql123");
```

推荐方案 — 在 `application.properties` 中配置：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/book_management?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:mysql123}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

然后 `DatabaseUtil` 改为注入 Spring 管理的 `DataSource`，而非自己创建 HikariCP。

### 🟡 中优先级

| # | 问题 | 位置 | 修复方案 |
|---|------|------|---------|
| 4 | **无 CSRF 防护** | 全局 | Session 认证应配合 CSRF Token |
| 5 | **session.timeout 配置无效** | `application.properties` | `server.servlet.session.timeout=60m` 仅控制 Tomcat 默认值，`AuthService` 又手动设 3600 秒 |
| 6 | **launch.json 配置错误** | `.claude/launch.json` | 配置了 Python HTTP 服务器，但本项目是 Spring Boot，应用 Vite dev server |

### 🟢 低优先级

| # | 问题 | 位置 | 修复方案 |
|---|------|------|---------|
| 7 | **单例模式替代 Spring DI** | 全部 Service/Repository | 用 `@Service` + `@Repository` + `@Autowired` 替代手工单例 |
| 8 | **JSON 序列化无配置** | 全局 | `JsonResponse` 手工构建，缺少 Jackson 全局配置（如日期格式化） |
| 9 | **Register.vue 缺少 `showToast` 注入** | `Register.vue` | 模板中未渲染 Toast 组件，`showToast('注册成功')` 可能不显示 |

---

## 四、可以添加的功能 🚀

### 🔴 高优先级（核心闭环缺失）

#### 1. 修改密码

**现状：** `UserRepository.updatePassword()` 方法已实现但无 API 暴露。
**需要做：**
- 后端：`PUT /api/auth/password` 端点（需验证旧密码）
- 前端：设置页面 + 修改密码 Modal

```java
// AuthController.java 新增端点
@PutMapping("/password")
public JsonResponse<Void> changePassword(@RequestBody Map<String, String> request,
                                          HttpServletRequest req) {
    Long userId = authService.getCurrentUserId(req);
    if (userId == null) return JsonResponse.unauthorized("请先登录");
    String oldPassword = request.get("oldPassword");
    String newPassword = request.get("newPassword");
    // 验证旧密码正确 → 更新为新密码
    authService.changePassword(userId, oldPassword, newPassword);
    return JsonResponse.success("密码已修改", null);
}
```

#### 2. 借阅记录分页

**现状：** `MyBorrows.vue` 和 `AllBorrows.vue` 全量加载所有记录。
**需要做：**
- 后端：`BorrowRecordRepository` 添加 `findPage()` 方法
- 前端：添加分页组件

#### 3. 路由守卫（当前缺失！）

**现状：** 每个 Layout 子页面仅靠 `Layout.vue` 的 `mounted()` 发 `/auth/me` 请求判断登录状态。如果网络慢，页面会先渲染再跳转。
**需要做：**

```javascript
// router/index.js 添加全局前置守卫
router.beforeEach(async (to, from, next) => {
  const publicPages = ['/login', '/register']
  if (publicPages.includes(to.path)) return next()
  
  try {
    const res = await api.get('/auth/me')
    if (res.code === 200) {
      authStore.currentUser = res.data.user
      authStore.isAdmin = res.data.user.role === 'admin'
      next()
    } else {
      next('/login')
    }
  } catch {
    next('/login')
  }
})
```

### 🟡 中优先级

| # | 功能 | 后端现状 | 需要做 |
|---|------|---------|--------|
| 4 | **用户管理（管理员）** | `UserRepository.findAll()` 已就绪 | 新增 `GET /api/admin/users` + 前端用户列表页 |
| 5 | **图书封面上传** | 无 | Book 模型加 `image_url`，前端加文件上传组件 |
| 6 | **逾期自动提醒** | 仪表板有统计数 | 加定时任务（`@Scheduled`），扫描逾期记录发通知 |
| 7 | **个人设置页面** | 无 | 用户修改个人信息（姓名、邮箱） |
| 8 | **记住我** | 无 | 登录加 "记住我" checkbox，延长 session |
| 9 | **Dashboard 最近借阅** | 无 | 仪表板加最近借阅动态列表 |

### 🟢 低优先级

| # | 功能 | 说明 |
|---|------|------|
| 10 | **图书预约** | 库存为 0 时排队等待，归还后自动通知 |
| 11 | **导出功能** | CSV/Excel 导出图书列表、借阅记录 |
| 12 | **忘记密码** | 邮箱验证码 + 重置链接 |
| 13 | **操作日志** | 审计谁在什么时候做了什么操作 |
| 14 | **图书评分/评论** | 用户阅读后可打分写短评 |
| 15 | **借阅统计图表** | ECharts 柱状图/饼图展示借阅趋势 |
| 16 | **批量导入图书** | Excel 批量导入 |
| 17 | **图书标签系统** | 跨分类的多标签 |
| 18 | **邮件通知** | 借阅到期提醒 + 逾期警告 |
| 19 | **API 文档** | SpringDoc OpenAPI (Swagger UI) |
| 20 | **单元测试** | 当前 0 测试用例 |

---

## 五、网站设计改进建议 🎨

### 5.1 导航体验

| # | 改进 | 当前状态 | 建议 |
|---|------|---------|------|
| 1 | **面包屑完整路径** | `MyBorrows` 显示 "首页 > 我的借阅" | 所有子页面保持一致的 breadcrumb 结构 |
| 2 | **侧边栏折叠状态记忆** | 每次刷新重置 | localStorage 持久化 `sidebarOpen` |
| 3 | **顶部搜索栏** | 无 | Header 加全局图书搜索框 |
| 4 | **当前用户头像** | 只显示文字姓名 | 加 avatar 占位图（首字母圆形） |

### 5.2 交互细节

| # | 改进 | 当前状态 | 建议 |
|---|------|---------|------|
| 5 | **键盘快捷键** | 无 | `Ctrl+K` 全局搜索，`Esc` 关闭 Modal |
| 6 | **表单自动保存** | 无 | 编辑图书时 localStorage 暂存草稿 |
| 7 | **批量操作** | 图书管理无批量删除 | 表格加 checkbox + 批量操作栏 |
| 8 | **列排序** | 表格不支持排序 | 点击表头按该列排序 |
| 9 | **加载进度条** | 只有骨架屏 | 路由切换时顶部 NProgress 进度条 |

### 5.3 视觉增强

| # | 改进 | 当前状态 | 建议 |
|---|------|---------|------|
| 10 | **深色模式** | CSS 变量已就绪 | 加 toggle 开关，用 `prefers-color-scheme` 自动检测 |
| 11 | **打印样式** | 无 | 借阅记录加 `@media print` 样式 |
| 12 | **favicon** | 无 | 添加自定义 favicon |
| 13 | **Dashboard 趋势图标** | 纯数字 | 借阅数旁加 ↑↓ 趋势箭头（环比） |
| 14 | **图书卡片视图** | 只有表格视图 | 加列表/卡片切换按钮 |

### 5.4 无障碍

| # | 改进 | 当前状态 | 建议 |
|---|------|---------|------|
| 15 | **焦点样式** | 仅输入框有 | 所有可交互元素加 `:focus-visible` 轮廓 |
| 16 | **ARIA 标签** | 缺失 | 侧边栏导航加 `aria-label`，Modal 加 `role="dialog"` |
| 17 | **键盘导航** | Modal 无法 Escape 关闭 | `@keydown.escape` 关闭 Modal |

### 5.5 性能

| # | 改进 | 当前状态 | 建议 |
|---|------|---------|------|
| 18 | **代码分割** | 部分页面已用 `() => import()` | 所有路由组件统一懒加载 |
| 19 | **请求缓存** | 每次切换页面重新请求分类列表 | 分类列表用 `sessionStorage` 缓存 5 分钟 |
| 20 | **图片懒加载** | 登录页背景图直接引入 | `loading="lazy"` + 渐进式加载 |

---

## 六、后端架构改进

### 6.1 从手工 JDBC 迁移到 Spring Data JPA（可选）

当前全部 Repository 手写 JDBC。如果团队规模扩大，建议迁移：
- `BookRepository` → `interface BookRepository extends JpaRepository<Book, Long>`
- 减少 90% 样板代码，获得自动分页、审计、缓存

### 6.2 引入 DTO 层

当前直接用 Model 接收前端请求（如 `@RequestBody Book book`），缺少：
- `CreateBookRequest` / `UpdateBookRequest` — 区分创建和更新时的必填字段
- `LoginRequest` / `RegisterRequest` — 替代 `Map<String, String>`

### 6.3 日志框架

当前用 `System.out.println` / `System.err.println`，建议改用 SLF4J + Logback：
```java
private static final Logger log = LoggerFactory.getLogger(AuthService.class);
log.info("[Auth] 管理员账号已就绪");
```

---

## 七、优先实施路线图

### Phase 1 — 修 Bug（1-2天）
```
☐ 数据库密码移到配置文件
☐ 添加路由守卫（beforeEach）
☐ 修复注册页 Toast 不显示
```

### Phase 2 — 补核心功能（3-5天）
```
☐ 修改密码 API + 前端
☐ 借阅记录分页
☐ 用户管理（管理员）
```

### Phase 3 — 设计增强（3-5天）
```
☐ NProgress 路由进度条
☐ 深色模式 toggle
☐ 全局搜索框
☐ 图书卡片视图
```

### Phase 4 — 扩展功能（按需）
```
☐ API 文档 (Swagger)
☐ 图书封面上传
☐ 借阅统计图表
☐ 邮件通知
☐ 单元测试
```

---

## 八、快速上手：立即可以改进的 3 个改动

### 改动 1：路由守卫（最重要！）

在 `frontend/src/router/index.js` 中添加：

```javascript
import { authStore } from '../services/authStore.js'
import api from '../services/api.js'

router.beforeEach(async (to, from, next) => {
  if (to.path === '/login' || to.path === '/register') {
    return next()
  }
  try {
    const res = await api.get('/auth/me')
    if (res.code === 200 && res.data?.user) {
      authStore.currentUser = res.data.user
      authStore.isAdmin = res.data.user.role === 'admin'
      return next()
    }
  } catch {}
  next('/login')
})
```

### 改动 2：添加 NProgress 进度条

```bash
cd frontend && npm install nprogress
```

在 `main.js` 中：
```javascript
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

router.beforeEach((to, from, next) => {
  NProgress.start()
  next()
})
router.afterEach(() => NProgress.done())
```

### 改动 3：全局搜索

在 `Layout.vue` 的 header 中添加搜索框：
```html
<input 
  v-model="globalSearch" 
  @keyup.enter="$router.push(`/books?keyword=${globalSearch}`)"
  placeholder="搜索图书..."
  class="global-search"
/>
```

---

> 📊 **总结：这是一个架构扎实、设计出色的图书管理系统。核心业务闭环完整且有严谨的事务保护，前端 UI/UX 达到专业水准。主要短板在于配置安全、缺失部分辅助功能（修改密码、路由守卫、分页），以及缺少自动化测试和 API 文档。按照上述路线图逐步改进，可以快速达到生产可用标准。**
