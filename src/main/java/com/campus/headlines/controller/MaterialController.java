package com.campus.headlines.controller;

import com.campus.headlines.common.Result;
import com.campus.headlines.service.MaterialService;
import com.campus.headlines.util.SessionUtil;
import com.campus.headlines.vo.MaterialVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 素材接口：上传、我的素材列表、删除。
 *
 * <p>三个接口都在 {@code LoginInterceptor} 的保护范围内（/material/**），
 * 这里仍然通过 {@link SessionUtil} 取一次用户，既拿到 userId 也做了兜底校验。</p>
 */
@RestController
@RequestMapping("/material")
public class MaterialController {

    @Resource
    private MaterialService materialService;

    /**
     * 上传素材。
     *
     * <pre>
     * POST /material/upload   （multipart/form-data）
     * 字段名：file
     * </pre>
     *
     * <p>表单里不要手动设置 Content-Type，浏览器会自动带上 boundary。</p>
     */
    @PostMapping("/upload")
    public Result<MaterialVO> upload(@RequestParam("file") MultipartFile file,
                                     HttpSession session) {
        Long userId = SessionUtil.requireUserId(session);
        return Result.success("上传成功", materialService.upload(file, userId));
    }

    /**
     * 当前用户的素材列表，按上传时间倒序。
     */
    @GetMapping("/list")
    public Result<List<MaterialVO>> list(HttpSession session) {
        Long userId = SessionUtil.requireUserId(session);
        return Result.success(materialService.listByUser(userId));
    }

    /**
     * 删除素材（物理删除，含磁盘文件）。
     *
     * <p>DELETE 请求需要前端用 AJAX 发，表单的 method 只支持 GET/POST；
     * 本项目已在 web.xml 配了 {@code HiddenHttpMethodFilter}，也可以用
     * POST + {@code _method=delete} 的方式。</p>
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id, HttpSession session) {
        Long userId = SessionUtil.requireUserId(session);
        materialService.delete(id, userId);
        return Result.success("删除成功", null);
    }
}
