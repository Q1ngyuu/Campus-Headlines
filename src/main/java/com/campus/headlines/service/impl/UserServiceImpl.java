package com.campus.headlines.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.dto.LoginDTO;
import com.campus.headlines.dto.RegisterDTO;
import com.campus.headlines.entity.User;
import com.campus.headlines.mapper.UserMapper;
import com.campus.headlines.service.UserService;
import com.campus.headlines.util.AdminGuard;
import com.campus.headlines.vo.PageVO;
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

    @Resource
    private AdminGuard adminGuard;

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

    // =====================================================================
    //  管理员：账号管理
    // =====================================================================

    @Override
    public PageVO<UserVO> listForAdmin(String keyword, Integer role, long page, long size) {
        long current = page < 1 ? 1 : page;
        long pageSize = size < 1 ? 10 : Math.min(size, 50);

        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        String kw = trimToNull(keyword);
        if (kw != null) {
            query.and(w -> w.like(User::getUsername, kw).or().like(User::getNickname, kw));
        }
        if (role != null) {
            query.eq(User::getRole, role);
        }
        query.orderByDesc(User::getId);

        Page<User> result = userMapper.selectPage(new Page<>(current, pageSize), query);
        return PageVO.of(result, UserVO::from);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, Integer status, Long adminUserId) {
        adminGuard.require(adminUserId, "修改账号状态");
        if (status == null || (status != 0 && status != STATUS_NORMAL)) {
            throw new BusinessException("账号状态不合法");
        }
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (userId.equals(adminUserId) && status == 0) {
            throw new BusinessException("不能禁用自己的账号");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "用户不存在");
        }
        User update = new User();
        update.setId(userId);
        update.setStatus(status);
        userMapper.updateById(update);
        log.info("账号状态变更: userId={}, username={}, status={}, adminId={}",
                userId, user.getUsername(), status, adminUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword, Long adminUserId) {
        adminGuard.require(adminUserId, "重置密码");
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (newPassword == null || newPassword.length() < PASSWORD_MIN || newPassword.length() > PASSWORD_MAX) {
            throw new BusinessException("新密码长度需在 " + PASSWORD_MIN + "-" + PASSWORD_MAX + " 位之间");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "用户不存在");
        }
        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(update);
        log.info("重置密码: userId={}, username={}, adminId={}", userId, user.getUsername(), adminUserId);
    }

    /** 去掉首尾空白，空串归一为 null */
    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long userId, Integer role, Long adminUserId) {
        adminGuard.require(adminUserId, "修改用户角色");
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (role == null || (role != 0 && role != 1)) {
            throw new BusinessException("角色参数不合法");
        }
        if (userId.equals(adminUserId)) {
            throw new BusinessException("不能修改自己的角色");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "用户不存在");
        }
        // 降级最后一个管理员会让系统再也没人能进后台
        if (role == 0 && user.getRole() != null && user.getRole() == 1) {
            Long adminCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getRole, 1));
            if (adminCount != null && adminCount <= 1) {
                throw new BusinessException("系统至少需要保留一个管理员");
            }
        }
        User update = new User();
        update.setId(userId);
        update.setRole(role);
        userMapper.updateById(update);
        log.info("角色变更: userId={}, username={}, role={}, adminId={}",
                userId, user.getUsername(), role, adminUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(Long userId, String nickname, String avatar) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "用户不存在");
        }
        String name = trimToNull(nickname);
        if (name == null) {
            throw new BusinessException("昵称不能为空");
        }
        if (name.length() > 50) {
            throw new BusinessException("昵称不能超过 50 个字符");
        }
        String avatarPath = trimToNull(avatar);
        if (avatarPath != null && avatarPath.length() > 500) {
            throw new BusinessException("头像路径过长");
        }

        User update = new User();
        update.setId(userId);
        update.setNickname(name);
        update.setAvatar(avatarPath);
        userMapper.updateById(update);

        log.info("资料已更新: userId={}, nickname={}", userId, name);
        return UserVO.from(userMapper.selectById(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "用户不存在");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new BusinessException("请输入原密码");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (newPassword == null || newPassword.length() < PASSWORD_MIN || newPassword.length() > PASSWORD_MAX) {
            throw new BusinessException("新密码长度需在 " + PASSWORD_MIN + "-" + PASSWORD_MAX + " 位之间");
        }
        if (newPassword.equals(oldPassword)) {
            throw new BusinessException("新密码不能与原密码相同");
        }

        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(update);
        log.info("用户修改密码: userId={}, username={}", userId, user.getUsername());
    }
}
