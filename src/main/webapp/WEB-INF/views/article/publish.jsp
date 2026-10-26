<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>发布资讯 - 校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
</head>
<body>

<div class="topbar">
    <span class="brand">校园头条</span>
    <a href="${ctx}/">首页</a>
    <a href="${ctx}/material">素材管理</a>
    <span class="spacer"></span>
    <span class="hint" id="userName" style="margin:0"></span>
</div>

<div class="wrap">
    <div class="panel">
        <h2>发布资讯</h2>
        <p class="hint">
            保存草稿后只有自己能看见；提交审核后由管理员处理，通过后才会出现在首页。
            封面与内嵌素材都取自“素材管理”里已上传的文件。
        </p>

        <div class="form-row">
            <label for="title">标题</label>
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
            <div class="picker" id="coverPicker">
                <div class="empty">加载素材中...</div>
            </div>
        </div>

        <div class="form-row">
            <label>正文</label>
            <textarea class="textarea" id="content" placeholder="支持 HTML 标签，例如 &lt;p&gt;段落&lt;/p&gt;"></textarea>
        </div>

        <div class="form-row">
            <label>内嵌素材（可多选，会写入 article_material 关联表）</label>
            <div class="picker" id="inlinePicker">
                <div class="empty">加载素材中...</div>
            </div>
        </div>

        <div style="display:flex;gap:12px;align-items:center">
            <button class="btn btn-primary" id="btnSubmit" type="button">提交审核</button>
            <button class="btn" id="btnDraft" type="button">保存草稿</button>
            <span class="hint" id="opTip" style="margin:0"></span>
        </div>
    </div>
</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var materials = [];
        var coverMaterialId = null;       // 封面选中的素材 id
        var inlineIds = {};               // 内嵌素材：id -> true

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2400));
        }

        // ---------- 两个选择器的渲染 ----------
        function renderPickers() {
            if (!materials.length) {
                $('#coverPicker, #inlinePicker').html('<div class="empty">还没有素材，<a href="' + ctx + '/material">先去上传</a></div>');
                return;
            }

            var $cover = $('#coverPicker').empty();
            var $inline = $('#inlinePicker').empty();

            materials.forEach(function (m) {
                // 封面：单选
                var $c = $('<div class="pick"></div>')
                    .append(m.image
                        ? $('<img>').attr('src', ctx + m.filePath).attr('alt', m.fileName)
                        : $('<div class="tag"></div>').text('视频'))
                    .attr('title', m.fileName)
                    .on('click', function () {
                        coverMaterialId = (coverMaterialId === m.id) ? null : m.id;
                        $cover.find('.pick').removeClass('selected');
                        if (coverMaterialId !== null) { $c.addClass('selected'); }
                        $('#coverTip').text(coverMaterialId === null ? '未选择' : m.fileName);
                    });
                if (coverMaterialId === m.id) { $c.addClass('selected'); }
                $cover.append($c);

                // 内嵌：多选
                var $i = $('<div class="pick"></div>')
                    .append(m.image
                        ? $('<img>').attr('src', ctx + m.filePath).attr('alt', m.fileName)
                        : $('<div class="tag"></div>').text('视频'))
                    .attr('title', m.fileName)
                    .on('click', function () {
                        if (inlineIds[m.id]) {
                            delete inlineIds[m.id];
                            $i.removeClass('selected');
                        } else {
                            inlineIds[m.id] = true;
                            $i.addClass('selected');
                        }
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
                    if (res.code === 200) {
                        materials = res.data || [];
                        renderPickers();
                    } else {
                        toast(res.msg || '素材加载失败', 'error');
                    }
                },
                error: function (xhr) {
                    if (xhr.status === 401) { window.location.href = ctx + '/login'; }
                }
            });
        }

        $('#clearCover').on('click', function () {
            coverMaterialId = null;
            $('#coverPicker .pick').removeClass('selected');
            $('#coverTip').text('未选择');
        });

        // ---------- 提交 ----------
        function collect() {
            var title = $.trim($('#title').val());
            if (!title) {
                toast('请填写标题', 'error');
                $('#title').focus();
                return null;
            }

            var ids = [];
            for (var k in inlineIds) {
                if (inlineIds.hasOwnProperty(k)) { ids.push(Number(k)); }
            }
            // 封面素材也算被这篇资讯使用，一并写进关联表
            if (coverMaterialId !== null && ids.indexOf(coverMaterialId) === -1) {
                ids.push(coverMaterialId);
            }

            var coverPath = null;
            if (coverMaterialId !== null) {
                materials.forEach(function (m) {
                    if (m.id === coverMaterialId) { coverPath = m.filePath; }
                });
            }

            var categoryId = $('#category').val();

            return {
                id: null,
                title: title,
                categoryId: categoryId ? Number(categoryId) : null,
                coverImage: coverPath,
                content: $('#content').val(),
                materialIds: ids
            };
        }

        function submit(url, btn, tipText) {
            var payload = collect();
            if (!payload) { return; }

            var $btn = $(btn).prop('disabled', true);
            $('#opTip').text(tipText);

            $.ajax({
                url: ctx + url,
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify(payload),
                success: function (res) {
                    if (res.code === 200) {
                        toast(res.msg + '（ID: ' + res.data + '）', 'ok');
                        $('#opTip').text('');
                        setTimeout(function () { window.location.href = ctx + '/material'; }, 1500);
                    } else {
                        toast(res.msg || '保存失败', 'error');
                        $('#opTip').text('');
                    }
                },
                error: function (xhr) {
                    toast('保存失败（' + xhr.status + '）', 'error');
                    $('#opTip').text('');
                },
                complete: function () { $btn.prop('disabled', false); }
            });
        }

        $('#btnSubmit').on('click', function () { submit('/article/publish', this, '提交中...'); });
        $('#btnDraft').on('click', function () { submit('/article/draft', this, '保存中...'); });

        $.get(ctx + '/user/info', function (res) {
            if (res.code === 200) {
                $('#userName').text('当前用户：' + (res.data.nickname || res.data.username));
            }
        });

        loadMaterials();
    })();
</script>
</body>
</html>
