<%--
  项目入口页：仅用于验证 war 包部署成功、中文编码正常。
  后续接入 Controller 后，可以改成重定向到首页，或直接删掉由 "/" 路由接管。
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>校园头条 - 环境检查</title>
    <style>
        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: "Microsoft YaHei", "PingFang SC", Arial, sans-serif;
            background: #f5f7fa;
            color: #2c3e50;
        }
        .card {
            background: #fff;
            padding: 40px 48px;
            border-radius: 12px;
            box-shadow: 0 6px 24px rgba(0, 0, 0, .08);
            min-width: 460px;
        }
        h1 { margin: 0 0 8px; font-size: 24px; }
        .ok { color: #18a058; font-weight: 600; }
        ul { line-height: 2; padding-left: 20px; margin: 16px 0 0; }
        code { background: #eef2f7; padding: 1px 6px; border-radius: 4px; font-size: 13px; }
        .tip { margin-top: 20px; font-size: 13px; color: #7f8c9a; }
    </style>
</head>
<body>
<div class="card">
    <h1>校园头条 <span class="ok">环境就绪</span></h1>
    <p class="tip">如果你能看到本页，说明 war 已成功部署、JSP 编译与 UTF-8 编码正常。</p>
    <ul>
        <li>上下文路径：<code><%= request.getContextPath() %></code></li>
        <li>服务器信息：<code><%= application.getServerInfo() %></code></li>
        <li>Servlet 版本：<code><%= application.getMajorVersion() %>.<%= application.getMinorVersion() %></code></li>
        <li>当前时间：<code><%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) %></code></li>
    </ul>
    <p class="tip">
        下一步：建库并修改 <code>src/main/resources/properties/jdbc.properties</code>，
        然后按 README 启动 Tomcat 9。
    </p>
</div>
</body>
</html>
