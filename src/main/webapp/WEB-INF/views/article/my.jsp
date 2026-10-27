<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>我的文章 - 校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css?v=20261027">
    <link rel="stylesheet" href="${ctx}/static/css/article.css?v=20261027">
</head>
<body>

<div class="topbar">
    <span class="brand">校园头条</span>
    <a href="${ctx}/">首页</a>
    <a href="${ctx}/article/publish">发布资讯</a>
    <a href="${ctx}/favorite">我的收藏</a>
    <a href="${ctx}/material">素材管理</a>
    <span class="spacer"></span>
    <span class="hint" style="margin:0">${loginUser.nickname}</span>
</div>

<div class="wrap">
    <div class="panel">
        <div class="filters">
            <div class="tabs" id="statusTabs">
                <span class="tab active" data-status="">全部</span>
                <span class="tab" data-status="0">草稿</span>
                <span class="tab" data-status="1">待审核</span>
                <span class="tab" data-status="2">已通过</span>
                <span class="tab" data-status="3">已驳回</span>
            </div>
            <div class="search-box">
                <a class="btn btn-primary btn-sm" href="${ctx}/article/publish">写新资讯</a>
            </div>
        </div>

        <div id="listBox"><div class="empty">加载中...</div></div>
        <div class="pager" id="pager"></div>
    </div>
</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js?v=20261027"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var state = { status: '', page: 1, size: 10 };

        var STATUS_TEXT = { 0: '草稿', 1: '待审核', 2: '已通过', 3: '已驳回' };

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
                $box.append('<div class="empty">还没有文章</div>');
            } else {
                var $wrap = $('<div class="article-list"></div>');
                list.forEach(function (a) {
                    var $card = $('<div class="article-card"></div>');

                    var $cover = $('<div class="cover"></div>');
                    if (a.coverImage) {
                        $cover.append($('<img>').attr('src', ctx + a.coverImage).attr('alt', ''));
                    } else {
                        $cover.text('暂无封面');
                    }

                    var $body = $('<div class="body"></div>');
                    var $h3 = $('<h3></h3>').append($('<span></span>').text(a.title));

                    var $meta = $('<div class="meta"></div>')
                        .append($('<span class="badge-status s' + a.status + '"></span>')
                            .text(STATUS_TEXT[a.status] || '未知'))
                        .append($('<span></span>').text(a.categoryName || '未分类'))
                        .append($('<span></span>').text('更新于 ' + (a.updateTime || '')))
                        .append($('<span></span>').text('浏览 ' + (a.viewCount || 0)));

                    $body.append($h3);

                    // 驳回原因单独一行，作者最需要看这个
                    if (a.status === 3 && a.rejectReason) {
                        $body.append($('<p class="summary" style="-webkit-line-clamp:3"></p>')
                            .css('color', '#d03050')
                            .text('驳回原因：' + a.rejectReason));
                    } else {
                        $body.append($('<p class="summary"></p>').text(a.summary || ''));
                    }

                    var $ops = $('<div class="meta" style="gap:8px"></div>');

                    if (a.status === 0 || a.status === 3) {
                        $ops.append($('<a class="btn btn-sm"></a>')
                            .attr('href', ctx + '/article/publish?id=' + a.id).text('编辑'));
                    }
                    if (a.status === 0) {
                        $ops.append($('<button class="btn btn-sm btn-primary">提交审核</button>')
                            .on('click', function () { submit(a.id, this); }));
                    }
                    if (a.status === 2) {
                        $ops.append($('<a class="btn btn-sm"></a>')
                            .attr('href', ctx + '/article/detail/' + a.id).text('查看'));
                    }
                    $body.append($ops);

                    $card.append($cover, $body);
                    $wrap.append($card);
                });
                $box.append($wrap);
            }

            var $pager = $('#pager').empty();
            if (data.pages > 1) {
                $pager.append($('<button class="btn btn-sm">上一页</button>')
                    .prop('disabled', data.page <= 1)
                    .on('click', function () { load(data.page - 1); }));
                $pager.append($('<span></span>').text(' 第 ' + data.page + ' / ' + data.pages + ' 页 '));
                $pager.append($('<button class="btn btn-sm">下一页</button>')
                    .prop('disabled', data.page >= data.pages)
                    .on('click', function () { load(data.page + 1); }));
            }
        }

        function load(page) {
            state.page = page || 1;
            $.ajax({
                url: ctx + '/article/my/list',
                type: 'GET',
                dataType: 'json',
                data: { status: state.status === '' ? null : state.status, page: state.page, size: state.size },
                success: function (res) {
                    if (res.code === 200) { render(res.data); }
                    else { $('#listBox').html('<div class="empty">' + esc(res.msg) + '</div>'); }
                },
                error: function (xhr) {
                    if (xhr.status === 401) { window.location.href = ctx + '/login'; }
                    else { $('#listBox').html('<div class="empty">加载失败</div>'); }
                }
            });
        }

        function submit(articleId, btn) {
            var $btn = $(btn).prop('disabled', true);
            $.post(ctx + '/article/submit/' + articleId, function (res) {
                if (res.code === 200) { toast(res.msg, 'ok'); load(state.page); }
                else { toast(res.msg || '提交失败', 'error'); $btn.prop('disabled', false); }
            }).fail(function (xhr) {
                var msg = (xhr.responseJSON && xhr.responseJSON.msg) || '提交失败';
                toast(msg, 'error');
                $btn.prop('disabled', false);
            });
        }

        $('#statusTabs').on('click', '.tab', function () {
            $(this).addClass('active').siblings().removeClass('active');
            state.status = $(this).data('status');
            load(1);
        });

        load(1);
    })();
</script>
</body>
</html>
