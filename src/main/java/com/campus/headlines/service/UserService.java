package com.campus.headlines.service;

import com.campus.headlines.dto.LoginDTO;
import com.campus.headlines.dto.RegisterDTO;
import com.campus.headlines.vo.PageVO;
import com.campus.headlines.vo.UserVO;

/**
 * 用户业务接口：注册、登录、查询，以及管理员侧的账号管理。
 *
 * <p>约定：业务失败一律抛 {@code BusinessException}（带状态码），
 * 由全局异常处理器转成 {@code Result}，本层不返回 Result。</p>
 */
public interface UserService {

    /**
     * 注册新用户。
     *
     * <p>校验规则：用户名 3-50 位且仅含字母/数字/下划线、全表唯一；
     * 密码 6-32 位；昵称留空时默认取用户名。
     * 新用户角色固定为学生（0），状态固定为正常（1）。</p>
     *
     * @param dto 注册参数
     * @return 注册成功的用户信息（不含密码）
     */
    UserVO register(RegisterDTO dto);

    /**
     * 登录校验。
     *
     * <p>依次校验：用户存在 → 状态正常 → 密码匹配。
     * 用户不存在与密码错误返回**同一句提示**，避免被用来枚举账号。</p>
     *
     * @param dto 登录参数
     * @return 登录成功的用户信息（不含密码）
     */
    UserVO login(LoginDTO dto);

    /**
     * 按主键查询用户，不存在时返回 null。
     */
    UserVO getById(Long id);

    // =====================================================================
    //  管理员：账号管理
    // =====================================================================

    /**
     * 用户列表（管理员）。
     *
     * @param keyword 账号或昵称模糊匹配，可为空
     * @param role    角色筛选，null 表示全部
     */
    PageVO<UserVO> listForAdmin(String keyword, Integer role, long page, long size);

    /**
     * 启用 / 禁用账号（管理员）。
     *
     * <p>不允许禁用自己，否则会把自己锁在门外。</p>
     *
     * @param status 0-禁用，1-正常
     */
    void updateStatus(Long userId, Integer status, Long adminUserId);

    /**
     * 重置密码（管理员）。
     *
     * <p>管理员设置新密码后由服务端 BCrypt 加密，数据库里不会出现明文。</p>
     */
    void resetPassword(Long userId, String newPassword, Long adminUserId);
}
