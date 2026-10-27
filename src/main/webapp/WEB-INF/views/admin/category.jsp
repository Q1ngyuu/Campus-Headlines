<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="staticVer" value="20261027"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>栏目管理 - 校园头条管理后台</title>
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
</div>

<div class="wrap">
    <div class="panel">
        <h2 id="formTitle">新增栏目</h2>
        <p class="hint">栏目决定首页的筛选分组；停用的栏目不会出现在首页筛选条里。</p>

        <div style="display:flex;gap:12px;flex-wrap:wrap;align-items:flex-end;margin-bottom:8px">
            <div style="flex:0 0 220px">
                <label class="hint" for="name" style="display:block;margin-bottom:6px">栏目名称</label>
                <input type="text" class="input" id="name" maxlength="50" placeholder="如：校园安全">
            </div>
            <div style="flex:0 0 140px">
                <label class="hint" for="sortOrder" style="display:block;margin-bottom:6px">排序值（越小越前）</label>
                <input type="number" class="input" id="sortOrder" value="10">
            </div>
            <div style="flex:0 0 130px">
                <label class="hint" for="status" style="display:block;margin-bottom:6px">状态</label>
                <select class="select" id="status">
                    <option value="1">启用</option>
                    <option value="0">停用</option>
                </select>
            </div>
            <button class="btn btn-primary" id="btnSave" type="button">保存</button>
            <button class="btn" id="btnReset" type="button">清空</button>
        </div>
    </div>

    <div class="panel">
        <h2>栏目列表</h2>
        <div id="listBox"><div class="empty">加载中...</div></div>
    </div>
</div>

<div class="toast" id="toast"></div>

<script src="${ctx}/static/js/jquery.min.js?v=${staticVer}"></script>
<script>
    (function () {
        var ctx = '${ctx}';
        var editingId = null;

        function esc(s) { return $('<div>').text(s == null ? '' : s).html(); }

        function toast(msg, type) {
            var $t = $('#toast').text(msg).removeClass('ok error');
            if (type) { $t.addClass(type); }
            $t.addClass('show');
            clearTimeout($t.data('timer'));
            $t.data('timer', setTimeout(function () { $t.removeClass('show'); }, 2200));
        }

        function resetForm() {
            editingId = null;
            $('#name').val('');
            $('#sortOrder').val(10);
            $('#status').val('1');
            $('#formTitle').text('新增栏目');
        }

        function render(list) {
            var $box = $('#listBox').empty();
            if (!list || !list.length) {
                $box.html('<div class="empty">还没有栏目</div>');
                return;
            }

            var $table = $('<table class="audit-table"></table>').append(
                '<thead><tr><th style="width:70px">ID</th><th>名称</th>' +
                '<th style="width:120px">排序</th><th style="width:100px">状态</th>' +
                '<th style="width:170px">操作</th></tr></thead>'
            );
            var $tbody = $('<tbody></tbody>');

            list.forEach(function (c) {
                var enabled = c.status === 1;
                var $ops = $('<td><div class="ops"></div></td>');
                $ops.find('.ops')
                    .append($('<button class="btn btn-sm">编辑</button>')
                        .on('click', function () {
                            editingId = c.id;
                            $('#name').val(c.name);
                            $('#sortOrder').val(c.sortOrder);
                            $('#status').val(String(c.status));
                            $('#formTitle').text('编辑栏目：' + c.name);
                            window.scrollTo({ top: 0, behavior: 'smooth' });
                        }))
                    .append($('<button class="btn btn-sm btn-danger">删除</button>')
                        .on('click', function () { remove(c.id, c.name, this); }));

                $tbody.append(
                    $('<tr></tr>')
                        .append($('<td></td>').text(c.id))
                        .append($('<td></td>').text(c.name))
                        .append($('<td></td>').text(c.sortOrder))
                        .append($('<td></td>').append($('<span class="badge-status ' + (enabled ? 's2' : 's0') + '"></span>')
                            .text(enabled ? '启用' : '停用')))
                        .append($ops)
                );
            });

            $box.append($table.append($tbody));
        }

        function load() {
            $.ajax({
                url: ctx + '/admin/category/list',
                type: 'GET',
                dataType: 'json',
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

        $('#btnSave').on('click', function () {
            var name = $.trim($('#name').val());
            if (!name) { toast('请填写栏目名称', 'error'); return; }

            var payload = {
                id: editingId,
                name: name,
                sortOrder: Number($('#sortOrder').val() || 0),
                status: Number($('#status').val())
            };

            var $btn = $(this).prop('disabled', true);
            $.ajax({
                url: ctx + '/admin/category/save',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                dataType: 'json',
                data: JSON.stringify(payload),
                success: function (res) {
                    if (res.code === 200) {
                        toast('已保存', 'ok');
                        resetForm();
                        load();
                    } else {
                        toast(res.msg || '保存失败', 'error');
                    }
                },
                error: function (xhr) {
                    toast((xhr.responseJSON && xhr.responseJSON.msg) || '保存失败', 'error');
                },
                complete: function () { $btn.prop('disabled', false); }
            });
        });

        function remove(id, name, btn) {
            if (!window.confirm('确定删除栏目「' + name + '」？')) { return; }
            var $btn = $(btn).prop('disabled', true);
            $.ajax({
                url: ctx + '/admin/category/' + id,
                type: 'DELETE',
                dataType: 'json',
                success: function (res) {
                    if (res.code === 200) { toast('已删除', 'ok'); load(); }
                    else { toast(res.msg || '删除失败', 'error'); $btn.prop('disabled', false); }
                },
                error: function (xhr) {
                    toast((xhr.responseJSON && xhr.responseJSON.msg) || '删除失败', 'error');
                    $btn.prop('disabled', false);
                }
            });
        }

        $('#btnReset').on('click', resetForm);
        load();
    })();
</script>
</body>
</html>
