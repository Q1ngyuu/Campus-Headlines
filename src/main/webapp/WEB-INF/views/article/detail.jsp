<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="staticVer" value="20261027"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${article.title} - 校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css?v=${staticVer}">
    <link rel="stylesheet" href="${ctx}/static/css/article.css?v=${staticVer}">
</head>
<body>

<div class="topbar">
    <span class="brand">校园头条</span>
    <a href="${ctx}/">首页</a>
    <c:if test="${not empty loginUser}">
        <a href="${ctx}/article/publish">发布资讯</a>
        <a href="${ctx}/article/my">我的文章</a>
        <a href="${ctx}/favorite">我的收藏</a>
        <a href="${ctx}/profile">个人中心</a>
        <a href="${ctx}/material">素材管理</a>
    </c:if>
    <span class="spacer"></span>
    <c:choose>
        <c:when test="${not empty loginUser}">
            <span class="hint" style="margin:0">你好，${loginUser.nickname}</span>
        </c:when>
        <c:otherwise>
            <a href="${ctx}/login">登录</a>
            <a href="${ctx}/register">注册</a>
        </c:otherwise>
    </c:choose>
</div>

<div class="wrap">
    <c:choose>
        <c:when test="${not empty errorMsg}">
            <div class="panel">
                <div class="empty">
                    ${errorMsg}
                    <div style="margin-top:16px"><a class="btn" href="${ctx}/">返回首页</a></div>
                </div>
            </div>
        </c:when>
        <c:otherwise>

            <div class="detail-layout">

                <%-- 左：文章主体 --%>
                <div class="panel detail-main">

                    <div class="detail-head">
                        <h1>${article.title}</h1>
                        <div class="detail-meta">
                            <span>作者：${article.authorName}</span>
                            <span>栏目：${article.categoryName != null ? article.categoryName : '未分类'}</span>
                            <span>发布：${fn:substring(fn:replace(article.publishTime, 'T', ' '), 0, 16)}</span>
                            <span>浏览：${article.viewCount}</span>
                        </div>
                    </div>

                    <c:if test="${not empty article.coverImage}">
                        <img class="detail-cover" src="${ctx}${article.coverImage}" alt="封面">
                    </c:if>

                    <%-- 正文是作者提交的富文本，必须原样渲染；生产环境应做 HTML 白名单过滤防 XSS --%>
                    <div class="article-body">
                        <c:out value="${article.content}" escapeXml="false"/>
                    </div>

                    <c:if test="${not empty article.materials}">
                        <h3 style="font-size:15px;margin:26px 0 0">相关素材</h3>
                        <div class="gallery">
                            <c:forEach items="${article.materials}" var="m">
                                <c:choose>
                                    <c:when test="${m.image}">
                                        <a href="${ctx}${m.filePath}" target="_blank" title="${m.fileName}">
                                            <img src="${ctx}${m.filePath}" alt="${m.fileName}">
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${ctx}${m.filePath}" target="_blank" title="${m.fileName}">
                                            <video src="${ctx}${m.filePath}" preload="metadata"></video>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </div>
                    </c:if>

                    <div style="display:flex;gap:12px;align-items:center;margin-top:26px">
                        <button class="btn ${article.favorite ? 'btn-primary' : ''}" id="favBtn" type="button">
                            ${article.favorite ? '已收藏' : '收藏'}
                        </button>
                        <a class="btn" href="${ctx}/">返回首页</a>
                        <span class="hint" id="favTip" style="margin:0"></span>
                    </div>
                </div>

                <%-- 右：相关阅读（竖排小栏，滚动时跟随） --%>
                <aside class="detail-side">
                    <c:if test="${not empty related}">
                        <div class="side-card">
                            <h3>相关阅读</h3>
                            <ul class="side-list">
                                <c:forEach items="${related}" var="r">
                                    <li>
                                        <a class="side-item" href="${ctx}/article/detail/${r.id}">
                                            <span class="side-title">${r.title}</span>
                                            <span class="side-meta">
                                                ${fn:substring(fn:replace(r.publishTime, 'T', ' '), 0, 16)}
                                                · 浏览 ${r.viewCount}
                                            </span>
                                        </a>
                                    </li>
                                </c:forEach>
                            </ul>
                            <c:if test="${not empty article.categoryId}">
                                <a class="side-more" href="${ctx}/?categoryId=${article.categoryId}">
                                    查看全部「${article.categoryName}」资讯 →
                                </a>
                            </c:if>
                        </div>
                    </c:if>

                    <div class="side-card">
                        <h3>快捷入口</h3>
                        <ul class="side-list">
                            <li><a class="side-item" href="${ctx}/"><span class="side-title">返回首页</span></a></li>
                            <li><a class="side-item" href="${ctx}/article/publish"><span class="side-title">我要投稿</span></a></li>
                            <li><a class="side-item" href="${ctx}/favorite"><span class="side-title">我的收藏</span></a></li>
                        </ul>
                    </div>
                </aside>

            </div>

        </c:otherwise>
    </c:choose>
</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js?v=${staticVer}"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var logged = ${not empty loginUser};

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2000));
        }

        $('#favBtn').on('click', function () {
            if (!logged) {
                window.location.href = ctx + '/login';
                return;
            }
            $.ajax({
                url: ctx + '/favorite/toggle?articleId=${article.id}',
                type: 'POST',
                dataType: 'json',
                success: function (res) {
                    if (res.code === 200) {
                        $('#favBtn').text(res.data ? '已收藏' : '收藏')
                            .toggleClass('btn-primary', !!res.data);
                        toast(res.msg, 'ok');
                    } else {
                        toast(res.msg || '操作失败', 'error');
                    }
                },
                error: function (xhr) {
                    if (xhr.status === 401) { window.location.href = ctx + '/login'; }
                    else { toast('操作失败', 'error'); }
                }
            });
        });
    })();
</script>
</body>
</html>
