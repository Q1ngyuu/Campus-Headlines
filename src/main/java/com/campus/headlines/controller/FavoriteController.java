package com.campus.headlines.controller;

import com.campus.headlines.common.Result;
import com.campus.headlines.service.FavoriteService;
import com.campus.headlines.util.SessionUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

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
     * @return data 为 true 表示已收藏，false 表示已取消
     */
    @PostMapping("/toggle/{articleId}")
    public Result<Boolean> toggle(@PathVariable("articleId") Long articleId, HttpSession session) {
        Long userId = SessionUtil.requireUserId(session);
        boolean favorite = favoriteService.toggle(articleId, userId);
        return Result.success(favorite ? "已收藏" : "已取消收藏", favorite);
    }
}
