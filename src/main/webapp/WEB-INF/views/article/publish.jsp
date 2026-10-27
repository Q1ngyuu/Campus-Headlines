<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${empty articleId ? '发布资讯' : '编辑资讯'} - 校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css?v=20261027">
    <link rel="stylesheet" href="${ctx}/static/css/article.css?v=20261027">
</head>
<body>

<div class="topbar">
    <span class="brand">校园头条</span>
    <a href="${ctx}/">首页</a>
    <a href="${ctx}/article/my">我的文章</a>
    <a href="${ctx}/material">素材管理</a>
    <span class="spacer"></span>
    <span class="hint" style="margin:0">${loginUser.nickname}</span>
</div>

<div class="wrap">
    <div class="panel">
        <h2 id="pageTitle">${empty articleId ? '发布资讯' : '编辑资讯'}</h2>
        <p class="hint">
            流程：保存草稿 → 提交审核 → 管理员通过后才会出现在首页。
            驳回的稿件编辑保存后会回到草稿状态，可再次提交。
        </p>

        <div id="rejectBanner" style="display:none;margin-bottom:16px;padding:10px 14px;border-radius:8px;background:#fdecef;color:#d03050;font-size:13px"></div>

        <div class="form-row">
            <label for="title">标题 <span style="color:#d03050">*</span></label>
            <input type="text" class="input" id="title" maxlength="200" placeholder="一句话说清楚这条资讯">
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
            <label>封面图（单选，可不选）</label>
            <div style="margin-bottom:8px">
                <button class="btn btn-sm" id="clearCover" type="button">不使用封面</button>
                <span class="hint" id="coverTip" style="margin-left:8px">未选择</span>
            </div>
            <div class="picker" id="coverPicker"><div class="empty">加载素材中...</div></div>
        </div>

        <div class="form-row">
            <label>正文 <span class="muted">（支持 HTML，例如 &lt;p&gt;段落&lt;/p&gt;）</span></label>
            <textarea class="textarea" id="content" placeholder="在这里写正文…"></textarea>
        </div>

        <div class="form-row">
            <label>内嵌素材（可多选，会写入 article_material 关联表）</label>
            <div class="picker" id="inlinePicker"><div class="empty">加载素材中...</div></div>
        </div>

        <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
            <button class="btn btn-primary" id="btnSubmit" type="button">提交审核</button>
            <button class="btn" id="btnDraft" type="button">保存草稿</button>
            <span class="hint" id="opTip" style="margin:0"></span>
        </div>
    </div>
</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js?v=20261027"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var articleId = ${empty articleId ? 'null' : articleId};   // 编辑模式时由后端注入
        var materials = [];
        var coverPath = null;          // 封面图的 filePath
        var inlineIds = {};            // 内嵌素材 id 集合
        var loading = false;

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2400));
        }

        function esc(s) { return $('<div>').text(s == null ? '' : s).html(); }

        // ---------- 选择器 ----------
        function renderPickers() {
            if (!materials.length) {
                $('#coverPicker, #inlinePicker').html('<div class="empty">还没有素材，<a href="' + ctx + '/material">先去上传</a></div>');
                return;
            }
            var $cover = $('#coverPicker').empty();
            var $inline = $('#inlinePicker').empty();

            materials.forEach(function (m) {
                var $c = $('<div class="pick"></div>')
                    .append(m.image ? $('<img>').attr('src', ctx + m.filePath).attr('alt', m.fileName)
                                    : $('<div class="tag"></div>').text('视频'))
                    .attr('title', m.fileName)
                    .on('click', function () {
                        coverPath = (coverPath === m.filePath) ? null : m.filePath;
                        $cover.find('.pick').removeClass('selected');
                        if (coverPath) { $c.addClass('selected'); }
                        $('#coverTip').text(coverPath ? m.fileName : '未选择');
                    });
                if (coverPath === m.filePath) { $c.addClass('selected'); }
                $cover.append($c);

                var $i = $('<div class="pick"></div>')
                    .append(m.image ? $('<img>').attr('src', ctx + m.filePath).attr('alt', m.fileName)
                                    : $('<div class="tag"></div>').text('视频'))
                    .attr('title', m.fileName)
                    .on('click', function () {
                        if (inlineIds[m.id]) { delete inlineIds[m.id]; $i.removeClass('selected'); }
                        else { inlineIds[m.id] = true; $i.addClass('selected'); }
                    });
                if (inlineIds[m.id]) { $i.addClass('selected'); }
                $inline.append($i);
            });
        }

        function loadMaterials() {
            $.ajax({
                url: ctx + '/material/list',
                type: 'GET',
                dataType: 'json',
                success: function (res) {
                    if (res.code === 200) { materials = res.data || []; renderPickers(); }
                    else { toast(res.msg || '素材加载失败', 'error'); }
                },
                error: function (xhr) {
                    if (xhr.status === 401) { window.location.href = ctx + '/login'; }
                }
            });
        }

        // ---------- 编辑回填 ----------
        function loadArticle() {
            if (!articleId) { return; }
            $.ajax({
                url: ctx + '/article/edit/' + articleId,
                type: 'GET',
                dataType: 'json',
                success: function (res) {
                    if (res.code !== 200) { toast(res.msg || '加载失败', 'error'); return; }
                    var a = res.data;
                    $('#title').val(a.title || '');
                    $('#category').val(a.categoryId || '');
                    $('#content').val(a.content || '');
                    coverPath = a.coverImage || null;
                    $('#coverTip').text(coverPath ? '已选择封面' : '未选择');
                    inlineIds = {};
                    (a.materialIds || []).forEach(function (id) { inlineIds[id] = true; });

                    if (a.status === 3) {
                        $('#rejectBanner').show().text('该资讯曾被驳回，修改后保存会回到草稿状态，可重新提交审核。');
                    }
                    renderPickers();
                },
                error: function (xhr) {
                    var msg = (xhr.responseJSON && xhr.responseJSON.msg) || '加载失败';
                    toast(msg, 'error');
                }
            });
        }

        $('#clearCover').on('click', function () {
            coverPath = null;
            $('#coverPicker .pick').removeClass('selected');
            $('#coverTip').text('未选择');
        });

        // ---------- 提交 ----------
        function payload() {
            var title = $.trim($('#title').val());
            if (!title) { toast('请填写标题', 'error'); $('#title').focus(); return null; }

            var ids = [];
            for (var k in inlineIds) {
                if (inlineIds.hasOwnProperty(k)) { ids.push(Number(k)); }
            }
            // 封面素材也算被使用，一并写进关联表
            if (coverPath) {
                materials.forEach(function (m) {
                    if (m.filePath === coverPath && ids.indexOf(m.id) === -1) { ids.push(m.id); }
                });
            }
            var categoryId = $('#category').val();

            return {
                id: articleId,
                title: title,
                categoryId: categoryId ? Number(categoryId) : null,
                coverImage: coverPath,
                content: $('#content').val(),
                materialIds: ids
            };
        }

        function saveDraft(callback) {
            var data = payload();
            if (!data) { return; }
            $.ajax({
                url: ctx + '/article/draft',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify(data),
                success: function (res) {
                    if (res.code === 200) {
                        articleId = res.data;              // 新建后拿到 id，后续提交要用
                        if (callback) { callback(articleId); }
                    } else {
                        toast(res.msg || '保存失败', 'error');
                    }
                },
                error: function (xhr) {
                    var msg = (xhr.responseJSON && xhr.responseJSON.msg) || '保存失败';
                    toast(msg, 'error');
                },
                complete: function () { loading = false; }
            });
        }

        $('#btnDraft').on('click', function () {
            if (loading) { return; }
            loading = true;
            $(this).prop('disabled', true);
            var $self = $(this);
            $('#opTip').text('保存中...');
            saveDraft(function (id) {
                toast('草稿已保存（ID: ' + id + '）', 'ok');
                $('#opTip').text('');
                $self.prop('disabled', false);
            });
        });

        $('#btnSubmit').on('click', function () {
            if (loading) { return; }
            loading = true;
            var $self = $(this).prop('disabled', true);
            $('#opTip').text('提交中...');

            // 先保存（保证改动落库），再提交审核
            saveDraft(function (id) {
                $.ajax({
                    url: ctx + '/article/submit/' + id,
                    type: 'POST',
                    dataType: 'json',
                    success: function (res) {
                        if (res.code === 200) {
                            toast('已提交审核，等待管理员处理', 'ok');
                            setTimeout(function () { window.location.href = ctx + '/article/my'; }, 1200);
                        } else {
                            toast(res.msg || '提交失败', 'error');
                            $self.prop('disabled', false);
                        }
                    },
                    error: function (xhr) {
                        var msg = (xhr.responseJSON && xhr.responseJSON.msg) || '提交失败';
                        toast(msg, 'error');
                        $self.prop('disabled', false);
                    },
                    complete: function () { loading = false; $('#opTip').text(''); }
                });
            });
        });

        loadMaterials();
        loadArticle();
    })();
</script>
</body>
</html>
