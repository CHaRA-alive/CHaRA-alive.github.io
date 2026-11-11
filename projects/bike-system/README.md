# 校园共享单车管理系统

基于 Spring Boot、Spring Data JPA 和 MySQL 的校园共享单车管理系统。项目提供用户、管理员、运维人员和安全人员等角色入口，覆盖单车管理、骑行订单、账单、围栏规则、维修工单和安全记录等业务。

## 技术栈

- Java 17
- Spring Boot 3.5
- Spring Data JPA
- MySQL
- HTML / CSS / JavaScript

## 本地运行

1. 在 MySQL 中创建数据库 `SUC`。
2. 在 PowerShell 中设置数据库连接环境变量：

   ```powershell
   $env:DB_URL="jdbc:mysql://localhost:3306/SUC?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true"
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="你的数据库密码"
   ```

3. 启动项目：

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. 浏览器访问 <http://localhost:8080>。

数据库密码通过 `DB_PASSWORD` 环境变量传入，不会保存在仓库中。

## 目录结构

```text
src/main/java/com/cjy/bikesystem/
├── controller/   # REST 接口与页面入口
├── entity/       # 数据实体
├── repository/   # JPA 数据访问
└── service/      # 业务逻辑

src/main/resources/
├── application.properties
├── static/       # 登录页与各角色控制台
└── templates/
```

## GitHub Pages

项目源码托管在个人主页仓库的 `projects/bike-system` 目录中。GitHub Pages 只能托管静态页面，完整的 Spring Boot 和 MySQL 应用需要单独部署到支持 Java 的环境。
