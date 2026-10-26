package com.campus.headlines.service;

import com.campus.headlines.dto.LoginDTO;
import com.campus.headlines.dto.RegisterDTO;
import com.campus.headlines.vo.UserVO;

/**
 * 用户业务接口：注册、登录、查询。
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
}
