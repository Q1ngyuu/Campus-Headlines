<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="staticVer" value="20261027"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>编辑文章 - 校园头条管理后台</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css?v=${staticVer}">
    <link rel="stylesheet" href="${ctx}/static/css/article.css?v=${staticVer}">
</head>
<body>

<div class="topbar">
    <span class="brand">校园头条 · 管理后台</span>
    <a href="${ctx}/admin/article/page">返回文章管理</a>
    <span class="spacer"></span>
    <span class="hint" style="margin:0">${loginUser.nickname}（管理员）</span>
</div>

<div class="wrap">
    <div class="panel">
        <h2>编辑文章 <span class="muted" id="statusTag"></span></h2>
        <p class="hint">
            管理员可以修改标题、栏目、封面与正文。<b>审核状态和发布时间不会变</b>——
            要让待审核的稿子上线，请回到文章管理页点「通过」。
        </p>

        <div class="form-row">
            <label for="title">标题 <span style="color:#d03050">*</span></label>
            <input type="text" class="input" id="title" maxlength="200" placeholder="标题">
        </div>

        <div class="form-row">
            <label for="category">栏目</label>
            <select class="select" id="category">
                <option value="">未分类</option>
                <c:forEach items="${categories}" var="c">
                    <option value="${c.id}">${c.name}</option>
                </c:forEach>
            </select>
        </div>

        <div class="form-row">
            <label>封面图</label>
            <div style="margin-bottom:8px">
                <button class="btn btn-sm" id="clearCover" type="button">不使用封面</button>
                <span class="hint" id="coverTip" style="margin-left:8px">加载中...</span>
            </div>
            <div class="picker" id="coverPicker"><div class="empty">加载素材中...</div></div>
        </div>

        <div class="form-row">
            <label for="content">正文 <span class="muted">（支持 HTML）</span></label>
            <textarea class="textarea" id="content" style="min-height:320px"></textarea>
        </div>

        <div style="display:flex;gap:12px;align-items:center">
            <button class="btn btn-primary" id="btnSave" type="button">保存修改</button>
            <a class="btn" href="${ctx}/admin/article/page">取消</a>
            <span class="hint" id="opTip" style="margin:0"></span>
        </div>
    </div>
</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js?v=${staticVer}"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var articleId = ${empty articleId ? 'null' : articleId};
        var materials = [];
        var coverPath = null;

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2400));
        }

        var STATUS_TEXT = { 0: '草稿', 1: '待审核', 2: '已通过', 3: '已驳回' };

        function renderPickers() {
            var $box = $('#coverPicker').empty();
            if (!materials.length) {
                $box.html('<div class="empty">该作者还没有上传素材，可直接保留原封面或不用封面</div>');
                return;
            }
            materials.forEach(function (m) {
                var $c = $('<div class="pick"></div>')
                    .append(m.image ? $('<img>').attr('src', ctx + m.filePath).attr('alt', m.fileName)
                        : $('<div class="tag"></div>').text('视频'))
                    .attr('title', m.fileName)
                    .on('click', function () {
                        coverPath = (coverPath === m.filePath) ? null : m.filePath;
                        $box.find('.pick').removeClass('selected');
                        if (coverPath) { $c.addClass('selected'); }
                        $('#coverTip').text(coverPath ? m.fileName : '未选择');
                    });
                if (m.filePath === coverPath) { $c.addClass('selected'); }
                $box.append($c);
            });
        }

        function load() {
            if (!articleId) {
                toast('缺少文章ID', 'error');
                return;
            }
            $.ajax({
                url: ctx + '/admin/article/edit-data/' + articleId,
                type: 'GET',
                dataType: 'json',
                success: function (res) {
                    if (res.code !== 200) { toast(res.msg || '加载失败', 'error'); return; }
                    var a = res.data;
                    $('#title').val(a.title || '');
                    $('#category').val(a.categoryId || '');
                    $('#content').val(a.content || '');
                    coverPath = a.coverImage || null;
                    $('#coverTip').text(coverPath ? coverPath : '未选择');
                    $('#statusTag').text('（当前状态：' + (STATUS_TEXT[a.status] || '未知') + '）');
                    renderPickers();
                },
                error: function (xhr) {
                    toast((xhr.responseJSON && xhr.responseJSON.msg) || '加载失败', 'error');
                }
            });

            // 该作者的素材，用于换封面
            $.ajax({
                url: ctx + '/admin/article/materials/' + articleId,
                type: 'GET',
                dataType: 'json',
                success: function (res) {
                    if (res.code === 200) { materials = res.data || []; renderPickers(); }
                }
            });
        }

        $('#clearCover').on('click', function () {
            coverPath = null;
            $('#coverPicker .pick').removeClass('selected');
            $('#coverTip').text('未选择');
        });

        $('#btnSave').on('click', function () {
            var title = $.trim($('#title').val());
            if (!title) { toast('标题不能为空', 'error'); $('#title').focus(); return; }

            var categoryId = $('#category').val();
            var payload = {
                id: articleId,
                title: title,
                categoryId: categoryId ? Number(categoryId) : null,
                coverImage: coverPath,
                content: $('#content').val()
            };

            var $btn = $(this).prop('disabled', true);
            $('#opTip').text('保存中...');

            $.ajax({
                url: ctx + '/admin/article/update',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify(payload),
                success: function (res) {
                    if (res.code === 200) {
                        toast('已保存', 'ok');
                        $('#opTip').text('');
                        setTimeout(function () { window.location.href = ctx + '/admin/article/page'; }, 900);
                    } else {
                        toast(res.msg || '保存失败', 'error');
                        $('#opTip').text('');
                    }
                },
                error: function (xhr) {
                    toast((xhr.responseJSON && xhr.responseJSON.msg) || '保存失败', 'error');
                    $('#opTip').text('');
                },
                complete: function () { $btn.prop('disabled', false); }
            });
        });

        load();
    })();
</script>
</body>
</html>
