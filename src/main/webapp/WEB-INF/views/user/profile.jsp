<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="staticVer" value="20261027"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>个人中心 - 校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css?v=${staticVer}">
    <link rel="stylesheet" href="${ctx}/static/css/article.css?v=${staticVer}">
</head>
<body>

<div class="topbar">
    <span class="brand">校园头条</span>
    <a href="${ctx}/">首页</a>
    <a href="${ctx}/article/publish">发布资讯</a>
    <a href="${ctx}/article/my">我的文章</a>
    <a href="${ctx}/favorite">我的收藏</a>
    <a href="${ctx}/material">素材管理</a>
    <span class="spacer"></span>
    <span class="hint" style="margin:0">${loginUser.nickname}</span>
</div>

<div class="wrap">
    <div style="display:flex;gap:20px;align-items:flex-start;flex-wrap:wrap">

        <!-- 基本资料 -->
        <div class="panel" style="flex:1 1 420px">
            <h2>基本资料</h2>
            <p class="hint">账号与角色由管理员维护，这里只能改昵称和头像。</p>

            <div class="form-row">
                <label>账号</label>
                <input type="text" class="input" id="username" disabled>
            </div>

            <div class="form-row">
                <label>角色</label>
                <input type="text" class="input" id="roleText" disabled>
            </div>

            <div class="form-row">
                <label for="nickname">昵称 <span style="color:#d03050">*</span></label>
                <input type="text" class="input" id="nickname" maxlength="50" placeholder="展示给其他读者的名字">
            </div>

            <div class="form-row">
                <label>头像</label>
                <div style="margin-bottom:8px;display:flex;gap:10px;align-items:center">
                    <img id="avatarPreview" alt="头像"
                         style="width:56px;height:56px;border-radius:50%;object-fit:cover;background:#eef3f9;display:none">
                    <button class="btn btn-sm" id="clearAvatar" type="button">清空头像</button>
                    <span class="hint" id="avatarTip" style="margin:0">未设置</span>
                </div>
                <div class="picker" id="avatarPicker"><div class="empty">加载素材中...</div></div>
                <p class="hint" style="margin:8px 0 0">头像从你上传的素材里选，建议用正方形图片。</p>
            </div>

            <button class="btn btn-primary" id="btnSaveProfile" type="button">保存资料</button>
        </div>

        <!-- 修改密码 -->
        <div class="panel" style="flex:1 1 320px">
            <h2>修改密码</h2>
            <p class="hint">修改后当前登录仍然有效，下次登录用新密码。</p>

            <div class="form-row">
                <label for="oldPassword">原密码</label>
                <input type="password" class="input" id="oldPassword" maxlength="32" autocomplete="current-password">
            </div>

            <div class="form-row">
                <label for="newPassword">新密码</label>
                <input type="password" class="input" id="newPassword" maxlength="32" autocomplete="new-password"
                       placeholder="6-32 位">
            </div>

            <div class="form-row">
                <label for="confirmPassword">确认新密码</label>
                <input type="password" class="input" id="confirmPassword" maxlength="32" autocomplete="new-password">
            </div>

            <button class="btn btn-primary" id="btnChangePwd" type="button">修改密码</button>
        </div>

    </div>
</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js?v=${staticVer}"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var materials = [];
        var avatarPath = null;
        var profile = null;

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2200));
        }

        function renderAvatar() {
            if (avatarPath) {
                $('#avatarPreview').attr('src', ctx + avatarPath).show();
                $('#avatarTip').text(avatarPath);
            } else {
                $('#avatarPreview').hide();
                $('#avatarTip').text('未设置');
            }
            $('#avatarPicker .pick').each(function () {
                var p = $(this).attr('data-path');
                $(this).toggleClass('selected', p === avatarPath);
            });
        }

        function renderPicker() {
            var $box = $('#avatarPicker').empty();
            var images = materials.filter(function (m) { return m.image; });
            if (!images.length) {
                $box.html('<div class="empty">还没有图片素材，<a href="' + ctx + '/material">去上传</a></div>');
                return;
            }
            images.forEach(function (m) {
                $('<div class="pick"></div>')
                    .attr('data-path', m.filePath)
                    .attr('title', m.fileName)
                    .append($('<img>').attr('src', ctx + m.filePath).attr('alt', m.fileName))
                    .on('click', function () {
                        avatarPath = (avatarPath === m.filePath) ? null : m.filePath;
                        renderAvatar();
                    })
                    .appendTo($box);
            });
            renderAvatar();
        }

        function load() {
            $.ajax({
                url: ctx + '/user/profile',
                type: 'GET',
                dataType: 'json',
                success: function (res) {
                    if (res.code !== 200) { toast(res.msg || '加载失败', 'error'); return; }
                    profile = res.data;
                    $('#username').val(profile.username);
                    $('#nickname').val(profile.nickname || '');
                    $('#roleText').val(profile.role === 1 ? '管理员' : '学生');
                    avatarPath = profile.avatar || null;
                    renderAvatar();
                },
                error: function (xhr) {
                    if (xhr.status === 401) { window.location.href = ctx + '/login'; }
                }
            });

            $.ajax({
                url: ctx + '/material/list',
                type: 'GET',
                dataType: 'json',
                success: function (res) {
                    if (res.code === 200) { materials = res.data || []; renderPicker(); }
                }
            });
        }

        $('#clearAvatar').on('click', function () {
            avatarPath = null;
            renderAvatar();
        });

        $('#btnSaveProfile').on('click', function () {
            var nickname = $.trim($('#nickname').val());
            if (!nickname) { toast('昵称不能为空', 'error'); $('#nickname').focus(); return; }

            var $btn = $(this).prop('disabled', true);
            $.ajax({
                url: ctx + '/user/profile',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify({ nickname: nickname, avatar: avatarPath }),
                success: function (res) {
                    if (res.code === 200) {
                        toast('资料已更新', 'ok');
                        $('.topbar .hint').text(res.data.nickname);
                    } else {
                        toast(res.msg || '保存失败', 'error');
                    }
                },
                error: function (xhr) {
                    toast((xhr.responseJSON && xhr.responseJSON.msg) || '保存失败', 'error');
                },
                complete: function () { $btn.prop('disabled', false); }
            });
        });

        $('#btnChangePwd').on('click', function () {
            var oldPwd = $('#oldPassword').val();
            var newPwd = $('#newPassword').val();
            var confirmPwd = $('#confirmPassword').val();

            if (!oldPwd) { toast('请输入原密码', 'error'); return; }
            if (newPwd.length < 6 || newPwd.length > 32) { toast('新密码长度需在 6-32 位之间', 'error'); return; }
            if (newPwd !== confirmPwd) { toast('两次输入的新密码不一致', 'error'); return; }

            var $btn = $(this).prop('disabled', true);
            $.ajax({
                url: ctx + '/user/password',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify({ oldPassword: oldPwd, newPassword: newPwd }),
                success: function (res) {
                    if (res.code === 200) {
                        toast(res.msg, 'ok');
                        $('#oldPassword, #newPassword, #confirmPassword').val('');
                    } else {
                        toast(res.msg || '修改失败', 'error');
                    }
                },
                error: function (xhr) {
                    toast((xhr.responseJSON && xhr.responseJSON.msg) || '修改失败', 'error');
                },
                complete: function () { $btn.prop('disabled', false); }
            });
        });

        load();
    })();
</script>
</body>
</html>
