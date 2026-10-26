<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/auth.css">
</head>
<body>
<div class="auth-card">
    <h1>校园头条</h1>
    <p class="subtitle">登录后即可投稿、收藏与管理资讯</p>

    <form id="loginForm" autocomplete="off">
        <div class="field">
            <label for="username">账号</label>
            <input type="text" id="username" name="username" placeholder="请输入用户名" maxlength="50" required>
        </div>
        <div class="field">
            <label for="password">密码</label>
            <input type="password" id="password" name="password" placeholder="请输入密码" maxlength="32" required>
        </div>

        <div class="msg" id="msg"></div>

        <button type="submit" class="btn-primary" id="submitBtn">登 录</button>
    </form>

    <p class="foot">
        还没有账号？<a href="${ctx}/register">立即注册</a>
        <span class="sep">|</span>
        <a href="${ctx}/">返回首页</a>
    </p>
</div>

<script src="${ctx}/static/js/jquery.min.js"></script>
<script>
    (function () {
        var ctx = '${ctx}';

        function showMsg(text, ok) {
            $('#msg').text(text).removeClass('ok error').addClass(ok ? 'ok' : 'error');
        }

        $('#loginForm').on('submit', function (e) {
            e.preventDefault();

            var username = $.trim($('#username').val());
            var password = $('#password').val();

            if (!username) { showMsg('请输入用户名', false); return; }
            if (!password) { showMsg('请输入密码', false); return; }

            var $btn = $('#submitBtn').prop('disabled', true).text('登录中...');
            showMsg('', false);

            $.ajax({
                url: ctx + '/user/login',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify({username: username, password: password}),
                success: function (res) {
                    // 统一返回体：HTTP 恒为 200，靠 code 判断
                    if (res.code === 200) {
                        showMsg('登录成功，正在跳转...', true);
                        window.location.href = ctx + '/';
                    } else {
                        showMsg(res.msg || '登录失败', false);
                        $btn.prop('disabled', false).text('登 录');
                    }
                },
                error: function (xhr) {
                    showMsg('网络异常（' + xhr.status + '），请稍后再试', false);
                    $btn.prop('disabled', false).text('登 录');
                }
            });
        });
    })();
</script>
</body>
</html>
