<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="staticVer" value="20261027"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户管理 - 校园头条管理后台</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css?v=${staticVer}">
    <link rel="stylesheet" href="${ctx}/static/css/article.css?v=${staticVer}">
</head>
<body>

<div class="topbar">
    <span class="brand">校园头条 · 管理后台</span>
    <a href="${ctx}/admin/article/page">文章管理</a>
    <a href="${ctx}/admin/category/page">栏目管理</a>
    <a href="${ctx}/admin/user/page">用户管理</a>
    <span class="spacer"></span>
    <a href="${ctx}/">返回首页</a>
</div>

<div class="wrap">
    <div class="panel">
        <div class="filters">
            <div class="tabs" id="roleTabs">
                <span class="tab active" data-role="">全部</span>
                <span class="tab" data-role="0">学生</span>
                <span class="tab" data-role="1">管理员</span>
            </div>
            <div class="search-box">
                <input type="text" id="keyword" placeholder="搜索账号或昵称…" maxlength="50">
                <button class="btn btn-primary btn-sm" id="searchBtn" type="button">搜索</button>
            </div>
        </div>

        <div id="listBox"><div class="empty">加载中...</div></div>
        <div class="pager" id="pager"></div>
    </div>
</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js?v=${staticVer}"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var state = { role: '', keyword: '', page: 1, size: 10 };
        var currentUserId = ${empty loginUser ? 'null' : loginUser.id};

        function esc(s) { return $('<div>').text(s == null ? '' : s).html(); }

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2200));
        }

        function render(data) {
            var $box = $('#listBox').empty();
            var list = data.records || [];
            if (!list.length) {
                $box.html('<div class="empty">没有匹配的用户</div>');
                return;
            }

            var $table = $('<table class="audit-table"></table>').append(
                '<thead><tr><th style="width:60px">ID</th><th>账号</th><th>昵称</th>' +
                '<th style="width:90px">角色</th><th style="width:90px">状态</th>' +
                '<th style="width:170px">注册时间</th><th style="width:230px">操作</th></tr></thead>'
            );
            var $tbody = $('<tbody></tbody>');

            list.forEach(function (u) {
                var enabled = u.status === 1;
                var isAdmin = u.role === 1;
                var isSelf = currentUserId !== null && u.id === currentUserId;

                var $ops = $('<td><div class="ops"></div></td>');
                var $wrap = $ops.find('.ops');

                $wrap.append($('<button class="btn btn-sm ' + (enabled ? 'btn-danger' : 'btn-primary') + '"></button>')
                    .text(enabled ? '禁用' : '启用')
                    .prop('disabled', isSelf && enabled)
                    .on('click', function () { setStatus(u.id, enabled ? 0 : 1, this); }));

                $wrap.append($('<button class="btn btn-sm">重置密码</button>')
                    .on('click', function () { resetPassword(u.id, u.username); }));

                if (isSelf) { $wrap.append($('<span class="muted"></span>').text('（当前账号）')); }

                $tbody.append(
                    $('<tr></tr>')
                        .append($('<td></td>').text(u.id))
                        .append($('<td></td>').text(u.username))
                        .append($('<td></td>').text(u.nickname || ''))
                        .append($('<td></td>').append($('<span class="badge-status ' + (isAdmin ? 's1' : 's0') + '"></span>')
                            .text(isAdmin ? '管理员' : '学生')))
                        .append($('<td></td>').append($('<span class="badge-status ' + (enabled ? 's2' : 's3') + '"></span>')
                            .text(enabled ? '正常' : '禁用')))
                        .append($('<td></td>').text(u.createTime || ''))
                        .append($ops)
                );
            });

            $box.append($table.append($tbody));

            var $pager = $('#pager').empty();
            if (data.pages > 1) {
                $pager.append($('<button class="btn btn-sm">上一页</button>')
                    .prop('disabled', data.page <= 1)
                    .on('click', function () { load(data.page - 1); }));
                $pager.append($('<span></span>').text(' 第 ' + data.page + ' / ' + data.pages + ' 页，共 ' + data.total + ' 人 '));
                $pager.append($('<button class="btn btn-sm">下一页</button>')
                    .prop('disabled', data.page >= data.pages)
                    .on('click', function () { load(data.page + 1); }));
            }
        }

        function load(page) {
            state.page = page || 1;
            $.ajax({
                url: ctx + '/admin/user/list',
                type: 'GET',
                dataType: 'json',
                data: {
                    keyword: state.keyword || null,
                    role: state.role === '' ? null : state.role,
                    page: state.page,
                    size: state.size
                },
                success: function (res) {
                    if (res.code === 200) { render(res.data); }
                    else { $('#listBox').html('<div class="empty">' + esc(res.msg) + '</div>'); }
                },
                error: function (xhr) {
                    if (xhr.status === 403 || xhr.status === 401) {
                        $('#listBox').html('<div class="empty">需要管理员权限</div>');
                    } else {
                        $('#listBox').html('<div class="empty">加载失败</div>');
                    }
                }
            });
        }

        function setStatus(userId, status, btn) {
            var $btn = $(btn).prop('disabled', true);
            $.ajax({
                url: ctx + '/admin/user/status',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify({ userId: userId, status: status }),
                success: function (res) {
                    if (res.code === 200) { toast(res.msg, 'ok'); load(state.page); }
                    else { toast(res.msg || '操作失败', 'error'); $btn.prop('disabled', false); }
                },
                error: function (xhr) {
                    toast((xhr.responseJSON && xhr.responseJSON.msg) || '操作失败', 'error');
                    $btn.prop('disabled', false);
                }
            });
        }

        function resetPassword(userId, username) {
            var pwd = window.prompt('为「' + username + '」设置新密码（6-32 位）：', '');
            if (pwd === null) { return; }
            pwd = $.trim(pwd);
            if (pwd.length < 6 || pwd.length > 32) { toast('密码长度需在 6-32 位之间', 'error'); return; }

            $.ajax({
                url: ctx + '/admin/user/password',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify({ userId: userId, password: pwd }),
                success: function (res) {
                    if (res.code === 200) { toast('密码已重置', 'ok'); }
                    else { toast(res.msg || '操作失败', 'error'); }
                },
                error: function (xhr) {
                    toast((xhr.responseJSON && xhr.responseJSON.msg) || '操作失败', 'error');
                }
            });
        }

        $('#roleTabs').on('click', '.tab', function () {
            $(this).addClass('active').siblings().removeClass('active');
            state.role = $(this).data('role');
            load(1);
        });

        $('#searchBtn').on('click', function () {
            state.keyword = $.trim($('#keyword').val());
            load(1);
        });
        $('#keyword').on('keydown', function (e) {
            if (e.keyCode === 13) { $('#searchBtn').click(); }
        });

        load(1);
    })();
</script>
</body>
</html>
