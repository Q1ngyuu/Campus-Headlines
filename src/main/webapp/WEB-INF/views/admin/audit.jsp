<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="staticVer" value="20261027"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>文章管理 - 校园头条</title>
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
    <span class="hint" style="margin:0">${loginUser.nickname}（管理员）</span>
</div>

<div class="wrap">
    <div class="panel">
        <div class="filters">
            <div class="tabs" id="tabs">
                <span class="tab active" data-tab="pending">待审核</span>
                <span class="tab" data-tab="published">已通过</span>
                <span class="tab" data-tab="all">全部文章</span>
            </div>
        </div>

        <p class="hint" id="tabHint">
            通过后状态变为「已通过」并写入发布时间；驳回必须填写原因，作者可修改后重新提交。
        </p>

        <div id="listBox"><div class="empty">加载中...</div></div>
        <div class="pager" id="pager"></div>
    </div>
</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js?v=${staticVer}"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var state = { tab: 'pending', page: 1, size: 10 };
        var STATUS_TEXT = { 0: '草稿', 1: '待审核', 2: '已通过', 3: '已驳回' };

        function esc(s) { return $('<div>').text(s == null ? '' : s).html(); }

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2400));
        }

        // ---------- 待审核 ----------
        function renderPending(data) {
            var list = data.records || [];
            if (!list.length) { return $('<div class="empty">暂无待审核资讯</div>'); }

            var $table = $('<table class="audit-table"></table>').append(
                '<thead><tr><th style="width:60px">ID</th><th>标题</th>' +
                '<th style="width:100px">作者</th><th style="width:100px">栏目</th>' +
                '<th style="width:150px">提交时间</th><th style="width:260px">操作</th></tr></thead>'
            );
            var $tbody = $('<tbody></tbody>');

            list.forEach(function (a) {
                var $ops = $('<td><div class="ops"></div></td>');
                var $opWrap = $ops.find('.ops');
                var $reason = $('<input class="reason-input" placeholder="驳回原因（驳回时必填）" maxlength="200" style="display:none">');

                var $pass = $('<button class="btn btn-sm btn-primary">通过</button>')
                    .on('click', function () { audit(a.id, 1, null, this); });

                var $reject = $('<button class="btn btn-sm btn-danger">驳回</button>')
                    .on('click', function () {
                        if ($reason.is(':hidden')) {
                            $reason.show().focus();
                            $reject.text('确认驳回');
                            return;
                        }
                        var reason = $.trim($reason.val());
                        if (!reason) { toast('请填写驳回原因', 'error'); $reason.focus(); return; }
                        audit(a.id, 0, reason, this);
                    });

                var $edit = $('<a class="btn btn-sm"></a>')
                    .attr('href', ctx + '/admin/article/edit/' + a.id).text('编辑');

                $opWrap.append($pass, $reject, $edit);

                $tbody.append(
                    $('<tr></tr>')
                        .append($('<td></td>').text(a.id))
                        .append($('<td></td>').text(a.title))
                        .append($('<td></td>').text(a.authorName || ''))
                        .append($('<td></td>').text(a.categoryName || '未分类'))
                        .append($('<td></td>').text(a.updateTime || ''))
                        .append($ops)
                );
                $tbody.append($('<tr></tr>').append($('<td colspan="6"></td>').append($reason)));
            });

            return $table.append($tbody);
        }

        // ---------- 已通过 / 全部 ----------
        function renderList(data, showStatus) {
            var list = data.records || [];
            if (!list.length) { return $('<div class="empty">没有符合条件的文章</div>'); }

            var statusCol = showStatus
                ? '<th style="width:80px">状态</th>'
                : '<th style="width:70px">置顶</th>';

            var $table = $('<table class="audit-table"></table>').append(
                '<thead><tr><th style="width:55px">ID</th><th>标题</th>' +
                '<th style="width:95px">作者</th><th style="width:150px">更新时间</th>' +
                '<th style="width:65px">浏览</th>' + statusCol +
                '<th style="width:220px">操作</th></tr></thead>'
            );
            var $tbody = $('<tbody></tbody>');

            list.forEach(function (a) {
                var top = a.isTop === 1;
                var $ops = $('<td><div class="ops"></div></td>');
                var $wrap = $ops.find('.ops');

                if (a.status === 2) {
                    $wrap.append(
                        $('<button class="btn btn-sm ' + (top ? 'btn-danger' : 'btn-primary') + '"></button>')
                            .text(top ? '取消置顶' : '置顶')
                            .on('click', function () { setTop(a.id, top ? 0 : 1, this); })
                    );
                }
                $wrap.append($('<a class="btn btn-sm"></a>')
                    .attr('href', ctx + '/admin/article/edit/' + a.id).text('编辑'));
                $wrap.append($('<a class="btn btn-sm"></a>')
                    .attr('href', ctx + '/article/detail/' + a.id).attr('target', '_blank').text('查看'));
                $wrap.append($('<button class="btn btn-sm btn-danger">删除</button>')
                    .on('click', function () { remove(a.id, a.title, this); }));

                var $state = showStatus
                    ? $('<td></td>').append($('<span class="badge-status s' + a.status + '"></span>')
                        .text(STATUS_TEXT[a.status] || '未知'))
                    : $('<td></td>').append(top
                        ? $('<span class="badge-status s1"></span>').text('已置顶')
                        : $('<span class="muted"></span>').text('—'));

                $tbody.append(
                    $('<tr></tr>')
                        .append($('<td></td>').text(a.id))
                        .append($('<td></td>').text(a.title))
                        .append($('<td></td>').text(a.authorName || ''))
                        .append($('<td></td>').text(a.updateTime || ''))
                        .append($('<td></td>').text(a.viewCount || 0))
                        .append($state)
                        .append($ops)
                );
            });

            return $table.append($tbody);
        }

        function render(data) {
            var $box = $('#listBox').empty();
            if (state.tab === 'pending') {
                $box.append(renderPending(data));
            } else {
                $box.append(renderList(data, state.tab === 'all'));
            }

            var $pager = $('#pager').empty();
            if (data.pages > 1) {
                $pager.append($('<button class="btn btn-sm">上一页</button>')
                    .prop('disabled', data.page <= 1)
                    .on('click', function () { load(data.page - 1); }));
                $pager.append($('<span></span>').text(' 第 ' + data.page + ' / ' + data.pages + ' 页，共 ' + data.total + ' 条 '));
                $pager.append($('<button class="btn btn-sm">下一页</button>')
                    .prop('disabled', data.page >= data.pages)
                    .on('click', function () { load(data.page + 1); }));
            }
        }

        function load(page) {
            state.page = page || 1;
            var url = '/admin/article/pending';
            var payload = { page: state.page, size: state.size };
            if (state.tab === 'published') { url = '/admin/article/list'; payload.status = 2; }
            if (state.tab === 'all') { url = '/admin/article/list'; }

            $.ajax({
                url: ctx + url,
                type: 'GET',
                dataType: 'json',
                data: payload,
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

        function audit(articleId, pass, reason, btn) {
            var $btn = $(btn).prop('disabled', true);
            $.ajax({
                url: ctx + '/admin/article/audit',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify({ articleId: articleId, pass: pass, rejectReason: reason }),
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

        function setTop(articleId, isTop, btn) {
            var $btn = $(btn).prop('disabled', true);
            $.ajax({
                url: ctx + '/admin/article/top',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify({ articleId: articleId, isTop: isTop }),
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

        function remove(articleId, title, btn) {
            if (!window.confirm('确定删除《' + title + '》？\n该操作不可恢复，关联的素材与收藏会一并清理。')) { return; }
            var $btn = $(btn).prop('disabled', true);
            $.ajax({
                url: ctx + '/admin/article/' + articleId,
                type: 'DELETE',
                dataType: 'json',
                success: function (res) {
                    if (res.code === 200) { toast('已删除', 'ok'); load(state.page); }
                    else { toast(res.msg || '删除失败', 'error'); $btn.prop('disabled', false); }
                },
                error: function (xhr) {
                    toast((xhr.responseJSON && xhr.responseJSON.msg) || '删除失败', 'error');
                    $btn.prop('disabled', false);
                }
            });
        }

        $('#tabs').on('click', '.tab', function () {
            $(this).addClass('active').siblings().removeClass('active');
            state.tab = $(this).data('tab');
            var hints = {
                pending: '通过后状态变为「已通过」并写入发布时间；驳回必须填写原因，作者可修改后重新提交。',
                published: '首页按「置顶 → 发布时间倒序」排序；这里可以置顶、编辑或删除已发布的文章。',
                all: '全部状态的稿件：草稿、待审核、已通过、已驳回。删除后不可恢复。'
            };
            $('#tabHint').text(hints[state.tab]);
            load(1);
        });

        load(1);
    })();
</script>
</body>
</html>
