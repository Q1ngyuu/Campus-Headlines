<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>审核管理 - 校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
    <link rel="stylesheet" href="${ctx}/static/css/article.css">
</head>
<body>

<div class="topbar">
    <span class="brand">校园头条 · 审核管理</span>
    <a href="${ctx}/">首页</a>
    <span class="spacer"></span>
    <span class="hint" style="margin:0">${loginUser.nickname}（管理员）</span>
</div>

<div class="wrap">
    <div class="panel">
        <h2>待审核资讯</h2>
        <p class="hint">
            通过后状态变为「已通过」并写入发布时间，立即出现在首页；
            驳回必须填写原因，作者可在「我的文章」里看到并修改后重新提交。
        </p>

        <div id="listBox"><div class="empty">加载中...</div></div>
        <div class="pager" id="pager"></div>
    </div>
</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var state = { page: 1, size: 10 };

        function esc(s) { return $('<div>').text(s == null ? '' : s).html(); }

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2400));
        }

        function render(data) {
            var $box = $('#listBox').empty();
            var list = data.records || [];

            if (!list.length) {
                $box.append('<div class="empty">暂无待审核资讯</div>');
            } else {
                var $table = $('<table class="audit-table"></table>');
                $table.append(
                    '<thead><tr>' +
                    '<th style="width:60px">ID</th>' +
                    '<th>标题</th>' +
                    '<th style="width:110px">作者</th>' +
                    '<th style="width:110px">栏目</th>' +
                    '<th style="width:160px">提交时间</th>' +
                    '<th style="width:220px">操作</th>' +
                    '</tr></thead>'
                );

                var $tbody = $('<tbody></tbody>');
                list.forEach(function (a) {
                    var $tr = $('<tr></tr>');

                    var $ops = $('<td><div class="ops"></div></td>');
                    var $opWrap = $ops.find('.ops');

                    var $reason = $('<input class="reason-input" placeholder="驳回原因（驳回时必填）" maxlength="200" style="display:none">');

                    var $pass = $('<button class="btn btn-sm btn-primary">通过</button>')
                        .on('click', function () { audit(a.id, 1, null, this); });

                    var $reject = $('<button class="btn btn-sm btn-danger">驳回</button>')
                        .on('click', function () {
                            // 第一次点展开原因输入框，第二次才真正提交
                            if ($reason.is(':hidden')) {
                                $reason.show().focus();
                                $reject.text('确认驳回');
                                return;
                            }
                            var reason = $.trim($reason.val());
                            if (!reason) { toast('请填写驳回原因', 'error'); $reason.focus(); return; }
                            audit(a.id, 0, reason, this);
                        });

                    $opWrap.append($pass, $reject);

                    $tr.append($('<td></td>').text(a.id))
                        .append($('<td></td>').append($('<a></a>')
                            .attr('href', 'javascript:void(0)')
                            .attr('data-id', a.id)
                            .text(a.title)
                            .on('click', function () { if (a.status === 2) { window.open(ctx + '/article/detail/' + a.id); } })))
                        .append($('<td></td>').text(a.authorName || ''))
                        .append($('<td></td>').text(a.categoryName || '未分类'))
                        .append($('<td></td>').text(a.updateTime || ''))
                        .append($ops);

                    var $trReason = $('<tr></tr>').append($('<td colspan="6"></td>').append($reason));
                    $tbody.append($tr, $trReason);
                });

                $table.append($tbody);
                $box.append($table);
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
            $.ajax({
                url: ctx + '/admin/article/pending',
                type: 'GET',
                dataType: 'json',
                data: { page: state.page, size: state.size },
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
                    if (res.code === 200) {
                        toast(res.msg, 'ok');
                        load(state.page);
                    } else {
                        toast(res.msg || '操作失败', 'error');
                        $btn.prop('disabled', false);
                    }
                },
                error: function (xhr) {
                    var msg = (xhr.responseJSON && xhr.responseJSON.msg) || '操作失败';
                    toast(msg, 'error');
                    $btn.prop('disabled', false);
                }
            });
        }

        load(1);
    })();
</script>
</body>
</html>
