<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="staticVer" value="20261027"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/auth.css?v=${staticVer}">
</head>
<body class="auth-page">

<div class="auth-split">

    <%-- 左：校园实景 + 校徽 --%>
    <aside class="auth-visual">
        <div class="auth-brand">
            <img src="${ctx}/static/img/school-logo.png?v=${staticVer}" alt="湖南科技学院">
            <span class="sep"></span>
            <span class="name">校园头条</span>
        </div>
        <div class="auth-visual-caption">
            <h2>校园头条</h2>
            <p>记录校园里的每一件新鲜事</p>
        </div>
    </aside>

    <%-- 右：登录表单 --%>
    <main class="auth-main">
        <div class="auth-box">

            <div class="auth-tabs">
                <span class="auth-tab is-active">账号密码登录</span>
            </div>

            <form id="loginForm" autocomplete="off">
                <div class="auth-field">
                    <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="8" r="3.6"/>
                        <path d="M4.6 20c1.4-3.6 4.2-5.4 7.4-5.4s6 1.8 7.4 5.4"/>
                    </svg>
                    <input type="text" id="username" name="username" placeholder="请输入账号"
                           maxlength="50" autocomplete="username" required>
                </div>

                <div class="auth-field">
                    <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="4.8" y="10.4" width="14.4" height="9.6" rx="2.4"/>
                        <path d="M8.4 10.4V7.9a3.6 3.6 0 0 1 7.2 0v2.5"/>
                    </svg>
                    <input type="password" id="password" name="password" placeholder="请输入密码"
                           maxlength="32" autocomplete="current-password" required>
                    <button class="auth-eye" type="button" id="eyeBtn" title="显示 / 隐藏密码" aria-label="显示密码">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M2.6 12S6.2 6 12 6s9.4 6 9.4 6-3.6 6-9.4 6-9.4-6-9.4-6z"/>
                            <circle cx="12" cy="12" r="2.9"/>
                            <path id="eyeSlash" d="M4 20 20 4" style="display:none"/>
                        </svg>
                    </button>
                </div>

                <div class="auth-row">
                    <label class="auth-check">
                        <input type="checkbox" id="rememberMe">
                        <span>记住我（7 天内免登录）</span>
                    </label>
                    <a class="auth-link" href="${ctx}/">返回首页</a>
                </div>

                <button class="auth-submit" type="submit" id="submitBtn">登录</button>

                <p class="auth-msg" id="msg"></p>

                <div class="auth-foot-row">
                    <a class="auth-link" href="${ctx}/register">注册新账号</a>
                    <span>忘记密码请联系管理员重置</span>
                </div>
            </form>

            <p class="auth-note">
                建议使用 Chrome / Edge / Firefox 浏览器访问<br>
                <span class="dot"></span>校园头条 · 校园资讯发布与审核平台
            </p>
        </div>
    </main>

</div>

<script src="${ctx}/static/js/jquery.min.js?v=${staticVer}"></script>
<script>
    (function () {
        var ctx = '${ctx}';

        function showMsg(text, ok) {
            $('#msg').text(text).removeClass('ok error').addClass(ok ? 'ok' : 'error');
        }

        // 密码显示 / 隐藏
        $('#eyeBtn').on('click', function () {
            var $pwd = $('#password');
            var hidden = $pwd.attr('type') === 'password';
            $pwd.attr('type', hidden ? 'text' : 'password');
            $('#eyeSlash').toggle(hidden);
        });

        $('#loginForm').on('submit', function (e) {
            e.preventDefault();

            var username = $.trim($('#username').val());
            var password = $('#password').val();
            var rememberMe = $('#rememberMe').is(':checked');

            if (!username) { showMsg('请输入账号', false); return; }
            if (!password) { showMsg('请输入密码', false); return; }

            var $btn = $('#submitBtn').prop('disabled', true).text('登录中...');
            showMsg('', false);

            $.ajax({
                url: ctx + '/user/login',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify({username: username, password: password, rememberMe: rememberMe}),
                success: function (res) {
                    // 统一返回体：HTTP 恒为 200，靠 code 判断
                    if (res.code === 200) {
                        showMsg('登录成功，正在跳转...', true);
                        window.location.href = ctx + '/';
                    } else {
                        showMsg(res.msg || '登录失败', false);
                        $btn.prop('disabled', false).text('登录');
                    }
                },
                error: function (xhr) {
                    showMsg('网络异常（' + xhr.status + '），请稍后再试', false);
                    $btn.prop('disabled', false).text('登录');
                }
            });
        });
    })();
</script>
</body>
</html>
