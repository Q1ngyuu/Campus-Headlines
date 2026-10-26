<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>素材管理 - 校园头条</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
</head>
<body>

<div class="topbar">
    <span class="brand">校园头条</span>
    <a href="${ctx}/">首页</a>
    <a href="${ctx}/article/publish">发布资讯</a>
    <span class="spacer"></span>
    <span class="hint" id="userName"></span>
</div>

<div class="wrap">

    <!-- 上传区 -->
    <div class="panel">
        <h2>上传素材</h2>
        <p class="hint">
            支持图片 jpg/jpeg/png/gif/webp（≤10MB），视频 mp4/webm/ogg（≤50MB）。
            文件名会重命名为 UUID，原始文件名仅作展示。
        </p>
        <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap">
            <input type="file" id="fileInput" accept="image/jpeg,image/png,image/gif,image/webp,video/mp4,video/webm,video/ogg" style="display:none">
            <button class="btn btn-primary" id="pickBtn">选择文件上传</button>
            <span class="hint" id="uploadTip" style="margin:0">未选择文件</span>
        </div>
    </div>

    <!-- 列表区 -->
    <div class="panel">
        <h2>我的素材 <span class="hint" id="count" style="margin:0"></span></h2>
        <p class="hint">删除前会检查是否已被资讯引用；被引用时需先在资讯里移除。</p>
        <div id="listBox">
            <div class="empty">加载中...</div>
        </div>
    </div>

</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var materials = [];

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2200));
        }

        // 文件名可能包含尖括号，必须转义后再拼 HTML，避免 XSS
        function esc(s) {
            return $('<div>').text(s == null ? '' : s).html();
        }

        // ---------- 列表渲染 ----------
        function render() {
            var $box = $('#listBox').empty();
            $('#count').text(materials.length ? '（共 ' + materials.length + ' 个）' : '');

            if (!materials.length) {
                $box.append('<div class="empty">还没有素材，先上传一个吧</div>');
                return;
            }

            var $grid = $('<div class="grid"></div>');
            materials.forEach(function (m) {
                var $card = $('<div class="card"></div>');
                var $thumb = $('<div class="thumb"></div>');

                if (m.image) {
                    $thumb.append($('<img>').attr('src', ctx + m.filePath).attr('alt', m.fileName));
                } else {
                    $thumb.append($('<div class="tag"></div>').text('视频'));
                }
                $thumb.append($('<span class="badge"></span>').text(m.image ? '图片' : '视频'));

                var $meta = $('<div class="meta"></div>')
                    .append($('<div class="name"></div>').text(m.fileName))
                    .append($('<div></div>').text(m.sizeText));

                var $ops = $('<div class="ops"></div>');
                $ops.append($('<button class="btn btn-sm">查看</button>').on('click', function () {
                    window.open(ctx + m.filePath, '_blank');
                }));
                $ops.append($('<button class="btn btn-sm btn-danger">删除</button>')
                    .attr('data-id', m.id)
                    .on('click', function () { remove(m.id); }));

                $card.append($thumb, $meta, $ops);
                $grid.append($card);
            });
            $box.append($grid);
        }

        function load() {
            $.ajax({
                url: ctx + '/material/list',
                type: 'GET',
                dataType: 'json',
                success: function (res) {
                    if (res.code === 200) {
                        materials = res.data || [];
                        render();
                    } else {
                        $('#listBox').html('<div class="empty">' + esc(res.msg) + '</div>');
                    }
                },
                error: function (xhr) {
                    if (xhr.status === 401 || (xhr.responseJSON && xhr.responseJSON.code === 401)) {
                        window.location.href = ctx + '/login';
                        return;
                    }
                    $('#listBox').html('<div class="empty">加载失败，请刷新重试</div>');
                }
            });
        }

        // ---------- 上传 ----------
        $('#pickBtn').on('click', function () { $('#fileInput').click(); });

        $('#fileInput').on('change', function () {
            var file = this.files && this.files[0];
            if (!file) { return; }
            upload(file);
            this.value = '';   // 允许重复选择同一个文件
        });

        function upload(file) {
            var fd = new FormData();
            fd.append('file', file);

            $('#uploadTip').text('上传中：' + file.name + '（' + (file.size / 1024 / 1024).toFixed(1) + ' MB）');
            $('#pickBtn').prop('disabled', true);

            $.ajax({
                url: ctx + '/material/upload',
                type: 'POST',
                data: fd,
                processData: false,     // 交给浏览器自己生成 multipart boundary
                contentType: false,
                dataType: 'json',
                success: function (res) {
                    if (res.code === 200) {
                        toast('上传成功：' + res.data.fileName, 'ok');
                        $('#uploadTip').text('未选择文件');
                        load();
                    } else {
                        toast(res.msg || '上传失败', 'error');
                        $('#uploadTip').text('上传失败');
                    }
                },
                error: function (xhr) {
                    var msg = '上传失败（' + xhr.status + '）';
                    if (xhr.status === 413) { msg = '文件超过服务器允许的大小'; }
                    toast(msg, 'error');
                    $('#uploadTip').text(msg);
                },
                complete: function () {
                    $('#pickBtn').prop('disabled', false);
                }
            });
        }

        // ---------- 删除 ----------
        function remove(id) {
            if (!window.confirm('确定删除这个素材？磁盘文件会一并删除。')) { return; }
            $.ajax({
                url: ctx + '/material/' + id,
                type: 'DELETE',
                dataType: 'json',
                success: function (res) {
                    if (res.code === 200) {
                        toast('已删除', 'ok');
                        load();
                    } else {
                        toast(res.msg || '删除失败', 'error');
                    }
                },
                error: function () { toast('删除失败，请稍后再试', 'error'); }
            });
        }

        // 顺便显示当前登录人，验证登录态可用
        $.get(ctx + '/user/info', function (res) {
            if (res.code === 200) {
                $('#userName').text('当前用户：' + (res.data.nickname || res.data.username));
            }
        });

        load();
    })();
</script>
</body>
</html>
