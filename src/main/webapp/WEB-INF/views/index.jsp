<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
    <link rel="stylesheet" href="${ctx}/static/css/article.css">
</head>
<body>

<div class="topbar">
    <span class="brand">校园头条</span>
    <a href="${ctx}/">首页</a>
    <c:if test="${not empty loginUser}">
        <a href="${ctx}/article/publish">发布资讯</a>
        <a href="${ctx}/article/my">我的文章</a>
        <a href="${ctx}/material">素材管理</a>
    </c:if>
    <c:if test="${not empty loginUser and loginUser.role == 1}">
        <a href="${ctx}/admin/article/page">审核管理</a>
    </c:if>
    <span class="spacer"></span>
    <c:choose>
        <c:when test="${not empty loginUser}">
            <span class="hint" style="margin:0">你好，${loginUser.nickname}</span>
            <button class="btn btn-sm" id="logoutBtn" type="button">退出</button>
        </c:when>
        <c:otherwise>
            <a href="${ctx}/login">登录</a>
            <a href="${ctx}/register">注册</a>
        </c:otherwise>
    </c:choose>
</div>

<div class="wrap">

    <div class="panel">
        <div class="filters">
            <div class="tabs" id="categoryTabs">
                <span class="tab active" data-id="">全部</span>
                <c:forEach items="${categories}" var="c">
                    <span class="tab" data-id="${c.id}">${c.name}</span>
                </c:forEach>
            </div>
            <div class="search-box">
                <input type="text" id="keyword" placeholder="搜索标题或正文…" maxlength="50">
                <button class="btn btn-primary btn-sm" id="searchBtn" type="button">搜索</button>
            </div>
        </div>

        <div id="listBox">
            <div class="empty">加载中...</div>
        </div>

        <div class="pager" id="pager"></div>
    </div>

</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var state = { categoryId: '', keyword: '', page: 1, size: 8 };

        function esc(s) {
            return $('<div>').text(s == null ? '' : s).html();
        }

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2200));
        }

        function timeText(t) {
            // 后端已格式化为 yyyy-MM-dd HH:mm:ss，这里只取到日
            return t ? String(t).substring(0, 16) : '';
        }

        function render(data) {
            var $box = $('#listBox').empty();
            var list = data.records || [];

            if (!list.length) {
                $box.append('<div class="empty">没有找到相关资讯</div>');
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
                    var $h3 = $('<h3></h3>');
                    if (a.isTop === 1) { $h3.append('<span class="badge-top">置顶</span>'); }
                    $h3.append($('<a></a>').attr('href', ctx + '/article/detail/' + a.id).text(a.title));

                    var $meta = $('<div class="meta"></div>')
                        .append($('<span></span>').text(a.categoryName || '未分类'))
                        .append($('<span></span>').text(timeText(a.publishTime)))
                        .append($('<span></span>').text('浏览 ' + (a.viewCount || 0)))
                        .append($('<span></span>').text('作者 ' + (a.authorName || '匿名')));

                    $body.append($h3)
                        .append($('<p class="summary"></p>').text(a.summary || ''))
                        .append($meta);
                    $card.append($cover, $body);
                    $wrap.append($card);
                });
                $box.append($wrap);
            }

            // 分页
            var $pager = $('#pager').empty();
            if (data.pages > 1) {
                $pager.append($('<button class="btn btn-sm">上一页</button>')
                    .prop('disabled', data.page <= 1)
                    .on('click', function () { load(data.page - 1); }));
                $pager.append($('<span></span>').text(' 第 ' + data.page + ' / ' + data.pages + ' 页，共 ' + data.total + ' 条 '));
                $pager.append($('<button class="btn btn-sm">下一页</button>')
                    .prop('disabled', data.page >= data.pages)
                    .on('click', function () { load(data.page + 1); }));
            } else if (data.total > 0) {
                $pager.append($('<span></span>').text('共 ' + data.total + ' 条'));
            }
        }

        function load(page) {
            state.page = page || 1;
            $.ajax({
                url: ctx + '/article/list',
                type: 'GET',
                dataType: 'json',
                data: {
                    categoryId: state.categoryId || null,
                    keyword: state.keyword || null,
                    page: state.page,
                    size: state.size
                },
                success: function (res) {
                    if (res.code === 200) { render(res.data); }
                    else { $('#listBox').html('<div class="empty">' + esc(res.msg) + '</div>'); }
                },
                error: function () { $('#listBox').html('<div class="empty">加载失败，请刷新重试</div>'); }
            });
        }

        // 栏目切换
        $('#categoryTabs').on('click', '.tab', function () {
            $(this).addClass('active').siblings().removeClass('active');
            state.categoryId = $(this).data('id');
            load(1);
        });

        // 搜索
        $('#searchBtn').on('click', function () {
            state.keyword = $.trim($('#keyword').val());
            load(1);
        });
        $('#keyword').on('keydown', function (e) {
            if (e.keyCode === 13) { $('#searchBtn').click(); }
        });

        // 退出
        $('#logoutBtn').on('click', function () {
            $.post(ctx + '/user/logout', function () {
                toast('已退出', 'ok');
                setTimeout(function () { location.reload(); }, 600);
            });
        });

        load(1);
    })();
</script>
</body>
</html>
