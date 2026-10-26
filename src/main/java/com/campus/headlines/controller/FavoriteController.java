package com.campus.headlines.controller;

import com.campus.headlines.common.Result;
import com.campus.headlines.service.FavoriteService;
import com.campus.headlines.util.SessionUtil;
import com.campus.headlines.vo.FavoriteVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * 收藏接口。
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    /**
     * 切换收藏。
     *
     * <pre>
     * POST /favorite/toggle?articleId=12
     * 或 POST /favorite/toggle  body: {"articleId": 12}
     * </pre>
     *
     * @return data 为 true 表示已收藏，false 表示已取消
     */
    @PostMapping("/toggle")
    public Result<Boolean> toggle(@RequestParam(value = "articleId", required = false) Long articleId,
                                  @RequestBody(required = false) Map<String, Object> body,
                                  HttpSession session) {
        // 同时兼容 query 参数与 JSON body 两种传法，前端怎么方便怎么来
        if (articleId == null && body != null && body.get("articleId") != null) {
            articleId = Long.valueOf(String.valueOf(body.get("articleId")));
        }
        Long userId = SessionUtil.requireUserId(session);
        boolean favorite = favoriteService.toggle(userId, articleId);
        return Result.success(favorite ? "已收藏" : "已取消收藏", favorite);
    }

    /**
     * 我的收藏列表（只含已通过的文章）。
     */
    @GetMapping("/list")
    public Result<List<FavoriteVO>> list(HttpSession session) {
        Long userId = SessionUtil.requireUserId(session);
        return Result.success(favoriteService.listByUser(userId));
    }
}
