<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册 - 校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/auth.css">
</head>
<body>
<div class="auth-card">
    <h1>注册账号</h1>
    <p class="subtitle">注册后默认为学生身份，可投稿与收藏</p>

    <form id="registerForm" autocomplete="off">
        <div class="field">
            <label for="username">账号</label>
            <input type="text" id="username" name="username" placeholder="3-50 位字母、数字或下划线" maxlength="50" required>
        </div>
        <div class="field">
            <label for="nickname">昵称</label>
            <input type="text" id="nickname" name="nickname" placeholder="不填则默认使用账号" maxlength="50">
        </div>
        <div class="field">
            <label for="password">密码</label>
            <input type="password" id="password" name="password" placeholder="6-32 位" maxlength="32" required>
        </div>
        <div class="field">
            <label for="confirmPassword">确认密码</label>
            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="再次输入密码" maxlength="32" required>
        </div>

        <div class="msg" id="msg"></div>

        <button type="submit" class="btn-primary" id="submitBtn">注 册</button>
    </form>

    <p class="foot">
        已有账号？<a href="${ctx}/login">去登录</a>
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
                data: JSON.stringify({username: username, password: password, nickname: nickname}),
                success: function (res) {
                    if (res.code === 200) {
                        showMsg('注册成功，即将跳转登录页...', true);
                        setTimeout(function () {
                            window.location.href = ctx + '/login';
                        }, 1200);
                    } else {
                        showMsg(res.msg || '注册失败', false);
                        $btn.prop('disabled', false).text('注 册');
                    }
                },
                error: function (xhr) {
                    showMsg('网络异常（' + xhr.status + '），请稍后再试', false);
                    $btn.prop('disabled', false).text('注 册');
                }
            });
        });
    })();
</script>
</body>
</html>
