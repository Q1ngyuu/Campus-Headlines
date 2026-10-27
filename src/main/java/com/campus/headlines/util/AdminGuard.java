package com.campus.headlines.util;

import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.entity.User;
import com.campus.headlines.mapper.UserMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 管理员校验。
 *
 * <p>拦截器 {@code AdminInterceptor} 只信任 Session 里的角色快照，
 * 用户被降权后不会立刻失效；真正要拦的写操作都应该在服务层再查一次库。
 * 抽成组件是为了让 Article / Category / User 三个服务复用同一段判断。</p>
 */
@Component
public class AdminGuard {

    /** 管理员角色值 */
    private static final int ROLE_ADMIN = 1;

    @Resource
    private UserMapper userMapper;

    /**
     * 要求调用者是管理员，否则抛 401 / 403。
     *
     * @param userId 当前登录用户 id
     * @param action 用于报错的操作名，如“删除栏目”
     */
    public void require(Long userId, String action) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null || user.getRole() == null || user.getRole() != ROLE_ADMIN) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "仅管理员可以" + action);
        }
    }
}
