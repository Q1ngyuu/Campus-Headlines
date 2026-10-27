<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>我的收藏 - 校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css?v=20261027">
    <link rel="stylesheet" href="${ctx}/static/css/article.css?v=20261027">
</head>
<body>

<div class="topbar">
    <span class="brand">校园头条</span>
    <a href="${ctx}/">首页</a>
    <a href="${ctx}/article/publish">发布资讯</a>
    <a href="${ctx}/article/my">我的文章</a>
    <a href="${ctx}/material">素材管理</a>
    <a href="${ctx}/profile">个人中心</a>
    <span class="spacer"></span>
    <span class="hint" style="margin:0">${loginUser.nickname}</span>
</div>

<div class="wrap">
    <div class="panel">
        <h2>我的收藏</h2>
        <p class="hint">只显示已通过审核的文章；文章被撤回或驳回后会自动从列表消失。</p>

        <div id="listBox"><div class="empty">加载中...</div></div>
    </div>
</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js?v=20261027"></script>
<script>
    (function () {
        var ctx = '${ctx}';

        function esc(s) { return $('<div>').text(s == null ? '' : s).html(); }

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2000));
        }

        function render(list) {
            var $box = $('#listBox').empty();
            if (!list || !list.length) {
                $box.append('<div class="empty">还没有收藏任何文章，去<a href="' + ctx + '/">首页</a>逛逛吧</div>');
                return;
            }

            var $wrap = $('<div class="article-list"></div>');
            list.forEach(function (f) {
                var $card = $('<div class="article-card"></div>');

                var $cover = $('<div class="cover"></div>');
                if (f.coverImage) {
                    $cover.append($('<img>').attr('src', ctx + f.coverImage).attr('alt', ''));
                } else {
                    $cover.text('暂无封面');
                }

                var $body = $('<div class="body"></div>');
                var $h3 = $('<h3></h3>');
                if (f.isTop === 1) { $h3.append('<span class="badge-top">置顶</span>'); }
                $h3.append($('<a></a>').attr('href', ctx + '/article/detail/' + f.articleId).text(f.title));

                var $meta = $('<div class="meta"></div>')
                    .append($('<span></span>').text(f.categoryName || '未分类'))
                    .append($('<span></span>').text('发布 ' + (f.publishTime || '')))
                    .append($('<span></span>').text('浏览 ' + (f.viewCount || 0)))
                    .append($('<span></span>').text('收藏于 ' + (f.favoriteTime || '')));

                var $ops = $('<div class="meta" style="gap:8px"></div>');
                $ops.append($('<button class="btn btn-sm btn-danger">取消收藏</button>')
                    .on('click', function () { toggle(f.articleId, this); }));

                $body.append($h3).append($meta).append($ops);
                $card.append($cover, $body);
                $wrap.append($card);
            });
            $box.append($wrap);
        }

        function load() {
            $.ajax({
                url: ctx + '/favorite/list',
                type: 'GET',
                dataType: 'json',
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

        function toggle(articleId, btn) {
            var $btn = $(btn).prop('disabled', true);
            $.ajax({
                url: ctx + '/favorite/toggle?articleId=' + articleId,
                type: 'POST',
                dataType: 'json',
                success: function (res) {
                    if (res.code === 200) {
                        toast(res.msg, 'ok');
                        load();
                    } else {
                        toast(res.msg || '操作失败', 'error');
                        $btn.prop('disabled', false);
                    }
                },
                error: function () {
                    toast('操作失败', 'error');
                    $btn.prop('disabled', false);
                }
            });
        }

        load();
    })();
</script>
</body>
</html>
