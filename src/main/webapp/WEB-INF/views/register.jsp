<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="staticVer" value="20261027"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册 - 校园头条</title>
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
            <h2>加入我们</h2>
            <p>注册后即可投稿，分享你身边的校园故事</p>
        </div>
    </aside>

    <%-- 右：注册表单 --%>
    <main class="auth-main">
        <div class="auth-box">

            <div class="auth-tabs">
                <span class="auth-tab is-active">注册新账号</span>
            </div>

            <form id="registerForm" autocomplete="off">
                <div class="auth-field">
                    <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="8" r="3.6"/>
                        <path d="M4.6 20c1.4-3.6 4.2-5.4 7.4-5.4s6 1.8 7.4 5.4"/>
                    </svg>
                    <input type="text" id="username" name="username" placeholder="账号（3-50 位字母、数字或下划线）"
                           maxlength="50" autocomplete="username" required>
                </div>

                <div class="auth-field">
                    <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3.6" y="5.4" width="16.8" height="13.2" rx="2.6"/>
                        <circle cx="9.2" cy="10.8" r="1.9"/>
                        <path d="M6.2 16.2c.9-1.6 2.2-2.4 3.6-2.4s2.7.8 3.6 2.4"/>
                        <path d="M15 10.4h3.2M15 13.6h2.2"/>
                    </svg>
                    <input type="text" id="nickname" name="nickname" placeholder="昵称（不填默认使用账号）"
                           maxlength="50" autocomplete="nickname">
                </div>

                <div class="auth-field">
                    <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="4.8" y="10.4" width="14.4" height="9.6" rx="2.4"/>
                        <path d="M8.4 10.4V7.9a3.6 3.6 0 0 1 7.2 0v2.5"/>
                    </svg>
                    <input type="password" id="password" name="password" placeholder="密码（6-32 位）"
                           maxlength="32" autocomplete="new-password" required>
                    <button class="auth-eye" type="button" id="eyeBtn1" title="显示 / 隐藏密码" aria-label="显示密码">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M2.6 12S6.2 6 12 6s9.4 6 9.4 6-3.6 6-9.4 6-9.4-6-9.4-6z"/>
                            <circle cx="12" cy="12" r="2.9"/>
                            <path class="eye-slash" d="M4 20 20 4" style="display:none"/>
                        </svg>
                    </button>
                </div>

                <div class="auth-field">
                    <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="4.8" y="10.4" width="14.4" height="9.6" rx="2.4"/>
                        <path d="M8.4 10.4V7.9a3.6 3.6 0 0 1 7.2 0v2.5"/>
                        <path d="m9.6 15.2 1.6 1.6 3.2-3.2"/>
                    </svg>
                    <input type="password" id="confirmPassword" name="confirmPassword" placeholder="再次输入密码"
                           maxlength="32" autocomplete="new-password" required>
                </div>

                <div class="auth-row">
                    <span>注册后默认为学生身份</span>
                    <a class="auth-link" href="${ctx}/">返回首页</a>
                </div>

                <button class="auth-submit" type="submit" id="submitBtn">注册</button>

                <p class="auth-msg" id="msg"></p>

                <div class="auth-foot-row">
                    <a class="auth-link" href="${ctx}/login">已有账号？去登录</a>
                    <span>账号与昵称后续可在个人中心修改</span>
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

        $('#eyeBtn1').on('click', function () {
            var $pwd = $('#password');
            var hidden = $pwd.attr('type') === 'password';
            $pwd.attr('type', hidden ? 'text' : 'password');
            $(this).find('.eye-slash').toggle(hidden);
        });

        $('#registerForm').on('submit', function (e) {
            e.preventDefault();

            var username = $.trim($('#username').val());
            var nickname = $.trim($('#nickname').val());
            var password = $('#password').val();
            var confirmPassword = $('#confirmPassword').val();

            // 前端只做最基础的校验，真正的规则在 UserServiceImpl 里，前端校验只为体验
            if (!/^[A-Za-z0-9_]{3,50}$/.test(username)) {
                showMsg('账号需为 3-50 位字母、数字或下划线', false);
                return;
            }
            if (password.length < 6 || password.length > 32) {
                showMsg('密码长度需在 6-32 位之间', false);
                return;
            }
            if (password !== confirmPassword) {
                showMsg('两次输入的密码不一致', false);
                return;
            }

            var $btn = $('#submitBtn').prop('disabled', true).text('提交中...');
            showMsg('', false);

            $.ajax({
                url: ctx + '/user/register',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify({
                    username: username,
                    nickname: nickname,
                    password: password,
                    confirmPassword: confirmPassword
                }),
                success: function (res) {
                    if (res.code === 200) {
                        showMsg('注册成功，正在跳转登录页...', true);
                        setTimeout(function () {
                            window.location.href = ctx + '/login';
                        }, 800);
                    } else {
                        showMsg(res.msg || '注册失败', false);
                        $btn.prop('disabled', false).text('注册');
                    }
                },
                error: function (xhr) {
                    showMsg('网络异常（' + xhr.status + '），请稍后再试', false);
                    $btn.prop('disabled', false).text('注册');
                }
            });
        });
    })();
</script>
</body>
</html>
