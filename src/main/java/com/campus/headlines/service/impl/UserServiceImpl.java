package com.campus.headlines.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.dto.LoginDTO;
import com.campus.headlines.dto.RegisterDTO;
import com.campus.headlines.entity.User;
import com.campus.headlines.mapper.UserMapper;
import com.campus.headlines.service.UserService;
import com.campus.headlines.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 用户业务实现。
 *
 * <p>密码选型：**BCrypt**（而非 MD5 + 盐）。理由见项目 README 的「安全设计」一节，
 * 核心是 BCrypt 自带随机盐 + 可调计算强度，抗彩虹表与 GPU 暴力破解；
 * 而 MD5 无论怎么加盐，单次计算都太快，属于“能存但不能算”的历史方案。</p>
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** 用户名规则：3-50 位，字母、数字、下划线 */
    private static final String USERNAME_PATTERN = "^[A-Za-z0-9_]{3,50}$";

    /** 密码长度下限 */
    private static final int PASSWORD_MIN = 6;

    /** 密码长度上限（BCrypt 只取前 72 字节，超过会被静默截断，所以要限长） */
    private static final int PASSWORD_MAX = 32;

    /** 状态：正常 */
    private static final int STATUS_NORMAL = 1;

    /** 角色：学生 */
    private static final int ROLE_STUDENT = 0;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO dto) {

        // ---------- 1. 参数校验 ----------
        if (dto == null) {
            throw new BusinessException("注册参数不能为空");
        }
        String username = trimToNull(dto.getUsername());
        String rawPassword = dto.getPassword();

        if (username == null) {
            throw new BusinessException("用户名不能为空");
        }
        if (!username.matches(USERNAME_PATTERN)) {
            throw new BusinessException("用户名需为 3-50 位字母、数字或下划线");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        if (rawPassword.length() < PASSWORD_MIN || rawPassword.length() > PASSWORD_MAX) {
            throw new BusinessException("密码长度需在 " + PASSWORD_MIN + "-" + PASSWORD_MAX + " 位之间");
        }

        // ---------- 2. 用户名唯一 ----------
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (exists != null && exists > 0) {
            throw new BusinessException(Result.CODE_CONFLICT, "用户名已被占用");
        }

        // ---------- 3. 落库（密码 BCrypt 加密）----------
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        String nickname = trimToNull(dto.getNickname());
        user.setNickname(nickname == null ? username : nickname);
        user.setRole(ROLE_STUDENT);
        user.setStatus(STATUS_NORMAL);

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发注册时唯一索引兜底，避免“先查后插”的竞态
            throw new BusinessException(Result.CODE_CONFLICT, "用户名已被占用");
        }

        log.info("新用户注册成功: username={}, id={}", username, user.getId());

        // 回读一次，取数据库生成的 create_time
        return UserVO.from(userMapper.selectById(user.getId()));
    }

    @Override
    public UserVO login(LoginDTO dto) {

        if (dto == null) {
            throw new BusinessException("登录参数不能为空");
        }
        String username = trimToNull(dto.getUsername());
        String rawPassword = dto.getPassword();
        if (username == null || rawPassword == null || rawPassword.isEmpty()) {
            throw new BusinessException("用户名和密码不能为空");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 用户不存在与密码错误返回同一提示，防止被用来枚举账号
        if (user == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "用户名或密码错误");
        }
        if (!Integer.valueOf(STATUS_NORMAL).equals(user.getStatus())) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "用户名或密码错误");
        }

        log.info("用户登录成功: username={}, role={}", user.getUsername(), user.getRole());
        return UserVO.from(user);
    }

    @Override
    public UserVO getById(Long id) {
        if (id == null) {
            return null;
        }
        return UserVO.from(userMapper.selectById(id));
    }

    /** 去掉首尾空白，空串归一为 null */
    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
