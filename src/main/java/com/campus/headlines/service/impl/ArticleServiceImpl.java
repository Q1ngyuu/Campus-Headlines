package com.campus.headlines.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.dto.ArticleDTO;
import com.campus.headlines.entity.Article;
import com.campus.headlines.entity.ArticleMaterial;
import com.campus.headlines.entity.Category;
import com.campus.headlines.entity.Favorite;
import com.campus.headlines.entity.Material;
import com.campus.headlines.entity.User;
import com.campus.headlines.mapper.ArticleMapper;
import com.campus.headlines.mapper.ArticleMaterialMapper;
import com.campus.headlines.mapper.CategoryMapper;
import com.campus.headlines.mapper.FavoriteMapper;
import com.campus.headlines.mapper.MaterialMapper;
import com.campus.headlines.mapper.UserMapper;
import com.campus.headlines.service.ArticleService;
import com.campus.headlines.vo.ArticleDetailVO;
import com.campus.headlines.vo.ArticleEditVO;
import com.campus.headlines.vo.ArticleListVO;
import com.campus.headlines.vo.MaterialVO;
import com.campus.headlines.vo.PageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资讯业务实现。
 *
 * <p>状态流转全部用「带 where status=旧状态」的条件更新，而不是先查后改：
 * 两个人同时点“通过”时，只有第一条 update 会影响 1 行，第二条影响 0 行，
 * 于是第二个请求得到明确提示，不会把已通过的稿件再改一次。</p>
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleServiceImpl.class);

    /** 标题最大长度，与 article.title varchar(200) 对齐 */
    private static final int TITLE_MAX = 200;

    /** 列表摘要截断长度 */
    private static final int SUMMARY_LEN = 90;

    /** 管理员角色值 */
    private static final int ROLE_ADMIN = 1;

    /** 分页默认与上限 */
    private static final long DEFAULT_SIZE = 10;
    private static final long MAX_SIZE = 50;

    /** 不分页的动态查询最多返回多少条 */
    private static final int MAX_SEARCH_SIZE = 50;

    /** 详情页相关阅读：默认条数与上限 */
    private static final int DEFAULT_RELATED = 6;
    private static final int MAX_RELATED = 10;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ArticleMaterialMapper articleMaterialMapper;

    @Resource
    private MaterialMapper materialMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private FavoriteMapper favoriteMapper;

    // =====================================================================
    //  投稿
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDraft(ArticleDTO dto, Long userId) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        if (dto == null) {
            throw new BusinessException("保存参数不能为空");
        }
        String title = dto.getTitle() == null ? "" : dto.getTitle().trim();
        if (title.isEmpty()) {
            throw new BusinessException("标题不能为空");
        }
        if (title.length() > TITLE_MAX) {
            throw new BusinessException("标题不能超过 " + TITLE_MAX + " 个字符");
        }
        String content = dto.getContent() == null ? "" : dto.getContent();

        Article article;
        boolean wasApproved = false;
        if (dto.getId() == null) {
            article = new Article();
            article.setUserId(userId);
            article.setViewCount(0);
            article.setIsTop(0);
            article.setStatus(Article.STATUS_DRAFT);
        } else {
            article = articleMapper.selectById(dto.getId());
            if (article == null) {
                throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在");
            }
            if (!userId.equals(article.getUserId())) {
                throw new BusinessException(Result.CODE_FORBIDDEN, "只能编辑自己的资讯");
            }
            int status = article.getStatus() == null ? -1 : article.getStatus();
            if (status == Article.STATUS_PENDING) {
                throw new BusinessException("审核中的资讯请先撤回，再修改");
            }
            // 0 草稿 / 3 已驳回 / 2 已通过：统一回到草稿，改完重新提交审核。
            // 已通过的文章被编辑意味着内容要重新过审，所以同时撤下发布时间。
            wasApproved = (status == Article.STATUS_APPROVED);
            article.setStatus(Article.STATUS_DRAFT);
            // 驳回原因保留在库里做历史，重新提交由 audit 覆盖，这里不清空
        }

        article.setCategoryId(dto.getCategoryId());
        article.setTitle(title);
        article.setCoverImage(dto.getCoverImage());
        article.setContent(content);

        if (article.getId() == null) {
            articleMapper.insert(article);
        } else {
            articleMapper.updateById(article);
            if (wasApproved) {
                // MyBatis-Plus 的 updateById 默认忽略 null 字段，
                // 想真正清空 publish_time 必须用 UpdateWrapper 显式 set null
                articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, article.getId())
                        .set(Article::getPublishTime, null));
            }
        }
        Long articleId = article.getId();

        rebuildMaterials(articleId, dto.getMaterialIds(), userId);

        log.info("草稿保存成功: articleId={}, userId={}, 素材 {} 个",
                articleId, userId, dto.getMaterialIds() == null ? 0 : dto.getMaterialIds().size());
        return articleId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(Long articleId, Long userId) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在");
        }
        if (!userId.equals(article.getUserId())) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "只能提交自己的资讯");
        }
        int status = article.getStatus() == null ? -1 : article.getStatus();
        if (status == Article.STATUS_PENDING) {
            throw new BusinessException("该资讯已提交审核，请等待管理员处理");
        }
        if (status == Article.STATUS_APPROVED) {
            throw new BusinessException("该资讯已通过审核，无需重复提交");
        }
        if (status == Article.STATUS_REJECTED) {
            throw new BusinessException("该资讯已被驳回，请修改并保存草稿后再提交");
        }
        if (status != Article.STATUS_DRAFT) {
            throw new BusinessException("当前状态不允许提交审核");
        }
        String content = article.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("提交审核前请填写正文");
        }

        // 条件更新：只有仍是草稿才改成待审核，防止并发重复提交
        int updated = articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, articleId)
                .eq(Article::getUserId, userId)
                .eq(Article::getStatus, Article.STATUS_DRAFT)
                .set(Article::getStatus, Article.STATUS_PENDING));
        if (updated == 0) {
            throw new BusinessException("资讯状态已变更，请刷新后重试");
        }
        log.info("提交审核成功: articleId={}, userId={}", articleId, userId);
    }

    // =====================================================================
    //  审核
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long articleId, Integer pass, String rejectReason, Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        User admin = userMapper.selectById(adminUserId);
        if (admin == null || admin.getRole() == null || admin.getRole() != ROLE_ADMIN) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "仅管理员可以执行审核操作");
        }
        if (pass == null || (pass != 0 && pass != 1)) {
            throw new BusinessException("审核结果参数不合法");
        }
        if (pass == 0 && (rejectReason == null || rejectReason.trim().isEmpty())) {
            throw new BusinessException("驳回时必须填写驳回原因");
        }

        int target = pass == 1 ? Article.STATUS_APPROVED : Article.STATUS_REJECTED;
        LambdaUpdateWrapper<Article> update = new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, articleId)
                // 关键：只允许从“待审核”流转，重复审核会更新 0 行
                .eq(Article::getStatus, Article.STATUS_PENDING)
                .set(Article::getStatus, target);

        if (pass == 1) {
            update.set(Article::getPublishTime, LocalDateTime.now())
                    .set(Article::getRejectReason, null);
        } else {
            update.set(Article::getRejectReason, rejectReason.trim())
                    // 驳回即撤回发布，重新通过时重新计时
                    .set(Article::getPublishTime, null);
        }

        int updated = articleMapper.update(null, update);
        if (updated == 0) {
            throw new BusinessException("该资讯不在待审核状态，可能已被处理");
        }
        log.info("审核完成: articleId={}, pass={}, adminId={}", articleId, pass, adminUserId);
    }

    // =====================================================================
    //  查询
    // =====================================================================

    @Override
    public PageVO<ArticleListVO> listPublished(Long categoryId, String keyword, long page, long size) {
        LambdaQueryWrapper<Article> query = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, Article.STATUS_APPROVED);

        // 动态条件一：栏目可选
        if (categoryId != null) {
            query.eq(Article::getCategoryId, categoryId);
        }
        // 动态条件二：关键词可选，标题或正文命中即可
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            query.and(w -> w.like(Article::getTitle, kw).or().like(Article::getContent, kw));
        }
        // 置顶优先，再按发布时间倒序；id 兜底保证分页稳定
        query.orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getPublishTime)
                .orderByDesc(Article::getId);

        Page<Article> result = articleMapper.selectPage(buildPage(page, size), query);
        return buildPageVO(result);
    }

    @Override
    public PageVO<ArticleListVO> listMyArticles(Long userId, Integer status, long page, long size) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        LambdaQueryWrapper<Article> query = new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId);
        if (status != null) {
            query.eq(Article::getStatus, status);
        }
        query.orderByDesc(Article::getUpdateTime).orderByDesc(Article::getId);

        Page<Article> result = articleMapper.selectPage(buildPage(page, size), query);
        return buildPageVO(result);
    }

    @Override
    public PageVO<ArticleListVO> listPendingAudit(long page, long size) {
        LambdaQueryWrapper<Article> query = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, Article.STATUS_PENDING)
                // 先提交的先审
                .orderByAsc(Article::getUpdateTime)
                .orderByAsc(Article::getId);
        Page<Article> result = articleMapper.selectPage(buildPage(page, size), query);
        return buildPageVO(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleDetailVO getDetail(Long articleId, Long currentUserId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || !Integer.valueOf(Article.STATUS_APPROVED).equals(article.getStatus())) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在或未通过审核");
        }

        // 浏览量原子 +1，避免“先查后写”在并发下互相覆盖
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, articleId)
                .setSql("view_count = view_count + 1"));

        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setCoverImage(article.getCoverImage());
        vo.setContent(article.getContent());
        vo.setCategoryId(article.getCategoryId());
        vo.setAuthorId(article.getUserId());
        vo.setPublishTime(article.getPublishTime());
        vo.setCreateTime(article.getCreateTime());
        int views = article.getViewCount() == null ? 1 : article.getViewCount() + 1;
        vo.setViewCount(views);

        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            vo.setCategoryName(category == null ? null : category.getName());
        }
        if (article.getUserId() != null) {
            User author = userMapper.selectById(article.getUserId());
            if (author != null) {
                vo.setAuthorName(displayName(author));
                vo.setAuthorAvatar(author.getAvatar());
            }
        }

        // 内嵌素材
        List<ArticleMaterial> relations = articleMaterialMapper.selectList(
                new LambdaQueryWrapper<ArticleMaterial>().eq(ArticleMaterial::getArticleId, articleId));
        if (!relations.isEmpty()) {
            List<Long> materialIds = relations.stream()
                    .map(ArticleMaterial::getMaterialId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!materialIds.isEmpty()) {
                List<Material> materials = materialMapper.selectBatchIds(materialIds);
                vo.setMaterials(materials.stream().map(MaterialVO::from).collect(Collectors.toList()));
            }
        }

        // 收藏状态（未登录时不查）
        boolean favorite = false;
        if (currentUserId != null) {
            Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUserId, currentUserId)
                    .eq(Favorite::getArticleId, articleId));
            favorite = count != null && count > 0;
        }
        vo.setFavorite(favorite);

        return vo;
    }

    @Override
    public List<ArticleListVO> searchArticles(Long categoryId, String keyword, Integer status) {
        // 走 resources/mapper/ArticleMapper.xml 里的手写动态 SQL（<where> + <if>）
        List<Article> records = articleMapper.searchArticles(categoryId, keyword, status);
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        // 不分页的接口必须有兜底上限，防止一次把整表拉出来
        if (records.size() > MAX_SEARCH_SIZE) {
            records = records.subList(0, MAX_SEARCH_SIZE);
        }
        return enrich(new ArrayList<>(records));
    }

    @Override
    public PageVO<ArticleListVO> listPublishedForAdmin(long page, long size) {
        // 与首页同样是 status=2，但管理员需要看到置顶状态并调整，排序保持前端一致
        LambdaQueryWrapper<Article> query = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, Article.STATUS_APPROVED)
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getPublishTime)
                .orderByDesc(Article::getId);
        return buildPageVO(articleMapper.selectPage(buildPage(page, size), query));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void top(Long articleId, Integer isTop, Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        User admin = userMapper.selectById(adminUserId);
        if (admin == null || admin.getRole() == null || admin.getRole() != ROLE_ADMIN) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "仅管理员可以操作置顶");
        }
        if (isTop == null || (isTop != 0 && isTop != 1)) {
            throw new BusinessException("置顶参数不合法");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在");
        }
        if (!Integer.valueOf(Article.STATUS_APPROVED).equals(article.getStatus())) {
            throw new BusinessException("只有已通过的资讯可以置顶");
        }

        // 条件更新：带着 status=2 做 where，避免文章刚好被撤下时还被置顶
        int updated = articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, articleId)
                .eq(Article::getStatus, Article.STATUS_APPROVED)
                .set(Article::getIsTop, isTop));
        if (updated == 0) {
            throw new BusinessException("资讯状态已变更，请刷新后重试");
        }
        log.info("置顶状态更新: articleId={}, isTop={}, adminId={}", articleId, isTop, adminUserId);
    }

    @Override
    public List<ArticleListVO> listRelated(Long articleId, Long categoryId, int limit) {
        int max = limit <= 0 ? DEFAULT_RELATED : Math.min(limit, MAX_RELATED);

        LambdaQueryWrapper<Article> query = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, Article.STATUS_APPROVED)
                .ne(Article::getId, articleId)
                .orderByDesc(Article::getPublishTime)
                .orderByDesc(Article::getId)
                // max 是服务端算出来的整数，不存在注入风险
                .last("LIMIT " + max);
        if (categoryId != null) {
            query.eq(Article::getCategoryId, categoryId);
        }
        List<Article> list = articleMapper.selectList(query);

        // 同栏目没有其它文章（例如新栏目只有这一篇）时，退化为全站最新
        if (list.isEmpty() && categoryId != null) {
            list = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                    .eq(Article::getStatus, Article.STATUS_APPROVED)
                    .ne(Article::getId, articleId)
                    .orderByDesc(Article::getPublishTime)
                    .orderByDesc(Article::getId)
                    .last("LIMIT " + max));
        }
        return enrich(list);
    }

    @Override
    public ArticleEditVO getForEdit(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在");
        }
        if (userId == null || !userId.equals(article.getUserId())) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "只能编辑自己的资讯");
        }
        int status = article.getStatus() == null ? -1 : article.getStatus();
        if (status != Article.STATUS_DRAFT && status != Article.STATUS_REJECTED) {
            throw new BusinessException("只有草稿或已驳回的资讯可以编辑");
        }

        ArticleEditVO vo = new ArticleEditVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setCategoryId(article.getCategoryId());
        vo.setCoverImage(article.getCoverImage());
        vo.setContent(article.getContent());
        vo.setStatus(article.getStatus());

        List<ArticleMaterial> relations = articleMaterialMapper.selectList(
                new LambdaQueryWrapper<ArticleMaterial>().eq(ArticleMaterial::getArticleId, articleId));
        vo.setMaterialIds(relations.stream()
                .map(ArticleMaterial::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return vo;
    }

    // =====================================================================
    //  管理员：文章管理
    // =====================================================================

    @Override
    public PageVO<ArticleListVO> listForAdmin(Integer status, long page, long size) {
        LambdaQueryWrapper<Article> query = new LambdaQueryWrapper<>();
        if (status != null) {
            query.eq(Article::getStatus, status);
        }
        // 管理员看的是“最近被改动过的”，所以按 update_time 倒序
        query.orderByDesc(Article::getUpdateTime).orderByDesc(Article::getId);
        return buildPageVO(articleMapper.selectPage(buildPage(page, size), query));
    }

    @Override
    public ArticleEditVO getForAdminEdit(Long articleId, Long adminUserId) {
        requireAdmin(adminUserId, "仅管理员可以编辑他人文章");
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在");
        }
        return toEditVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByAdmin(ArticleDTO dto, Long adminUserId) {
        requireAdmin(adminUserId, "仅管理员可以修改他人文章");
        if (dto == null || dto.getId() == null) {
            throw new BusinessException("缺少文章ID");
        }
        String title = dto.getTitle() == null ? "" : dto.getTitle().trim();
        if (title.isEmpty()) {
            throw new BusinessException("标题不能为空");
        }
        if (title.length() > TITLE_MAX) {
            throw new BusinessException("标题不能超过 " + TITLE_MAX + " 个字符");
        }
        Article article = articleMapper.selectById(dto.getId());
        if (article == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在");
        }

        // 只改内容字段，status / publish_time / 素材关联全部保持原样
        Article update = new Article();
        update.setId(article.getId());
        update.setTitle(title);
        update.setCategoryId(dto.getCategoryId());
        update.setCoverImage(dto.getCoverImage());
        update.setContent(dto.getContent() == null ? "" : dto.getContent());
        articleMapper.updateById(update);

        log.info("管理员修改文章: articleId={}, adminId={}", article.getId(), adminUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByAdmin(Long articleId, Long adminUserId) {
        requireAdmin(adminUserId, "仅管理员可以删除文章");
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在");
        }
        // 外键 ON DELETE CASCADE 会同时清掉 article_material 与 favorite
        articleMapper.deleteById(articleId);
        log.info("管理员删除文章: articleId={}, title={}, adminId={}",
                articleId, article.getTitle(), adminUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long articleId, Long userId) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在");
        }
        if (!userId.equals(article.getUserId())) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "只能撤回自己的资讯");
        }
        if (!Integer.valueOf(Article.STATUS_PENDING).equals(article.getStatus())) {
            throw new BusinessException("只有待审核的资讯可以撤回");
        }
        // 条件更新：仍是待审核才允许撤回
        int updated = articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, articleId)
                .eq(Article::getUserId, userId)
                .eq(Article::getStatus, Article.STATUS_PENDING)
                .set(Article::getStatus, Article.STATUS_DRAFT));
        if (updated == 0) {
            throw new BusinessException("资讯状态已变更，请刷新后重试");
        }
        log.info("作者撤回稿件: articleId={}, userId={}", articleId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByAuthor(Long articleId, Long userId) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在");
        }
        if (!userId.equals(article.getUserId())) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "只能删除自己的资讯");
        }
        articleMapper.deleteById(articleId);
        log.info("作者删除稿件: articleId={}, title={}, userId={}",
                articleId, article.getTitle(), userId);
    }

    /** 校验调用者是否为管理员，否则抛 403 */
    private void requireAdmin(Long adminUserId, String message) {
        if (adminUserId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        User admin = userMapper.selectById(adminUserId);
        if (admin == null || admin.getRole() == null || admin.getRole() != ROLE_ADMIN) {
            throw new BusinessException(Result.CODE_FORBIDDEN, message);
        }
    }

    @Override
    public List<MaterialVO> listAuthorMaterials(Long articleId, Long adminUserId) {        requireAdmin(adminUserId, "仅管理员可以查看他人素材");
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "资讯不存在");
        }
        List<Material> list = materialMapper.selectList(new LambdaQueryWrapper<Material>()
                .eq(Material::getUserId, article.getUserId())
                .orderByDesc(Material::getCreateTime)
                .orderByDesc(Material::getId));
        return list.stream().map(MaterialVO::from).collect(Collectors.toList());
    }

    /** 实体 → 编辑回填 VO */
    private ArticleEditVO toEditVO(Article article) {
        ArticleEditVO vo = new ArticleEditVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setCategoryId(article.getCategoryId());
        vo.setCoverImage(article.getCoverImage());
        vo.setContent(article.getContent());
        vo.setStatus(article.getStatus());

        List<ArticleMaterial> relations = articleMaterialMapper.selectList(
                new LambdaQueryWrapper<ArticleMaterial>()
                        .eq(ArticleMaterial::getArticleId, article.getId()));
        vo.setMaterialIds(relations.stream()
                .map(ArticleMaterial::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return vo;
    }

    // =====================================================================
    //  内部方法
    // =====================================================================

    /** 重建资讯与素材的关联：先删后插，逻辑简单且幂等 */
    private void rebuildMaterials(Long articleId, List<Long> materialIds, Long userId) {
        articleMaterialMapper.delete(new LambdaQueryWrapper<ArticleMaterial>()
                .eq(ArticleMaterial::getArticleId, articleId));

        List<Long> ids = distinctIds(materialIds);
        if (ids.isEmpty()) {
            return;
        }
        // 只允许关联自己的素材，避免拿别人的素材 id 拼内容
        Long owned = materialMapper.selectCount(new LambdaQueryWrapper<Material>()
                .in(Material::getId, ids)
                .eq(Material::getUserId, userId));
        if (owned == null || owned.intValue() != ids.size()) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "存在无权使用的素材，请刷新后重试");
        }
        for (Long materialId : ids) {
            articleMaterialMapper.insert(new ArticleMaterial(articleId, materialId));
        }
    }

    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> set = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null) {
                set.add(id);
            }
        }
        return new ArrayList<>(set);
    }

    private Page<Article> buildPage(long page, long size) {
        long current = page < 1 ? 1 : page;
        long pageSize = size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return new Page<>(current, pageSize);
    }

    private PageVO<ArticleListVO> buildPageVO(Page<Article> result) {
        return new PageVO<>(enrich(result.getRecords()), result.getTotal(),
                result.getCurrent(), result.getSize());
    }

    /** 批量补齐栏目名与作者名，避免逐条查库（N+1） */
    private List<ArticleListVO> enrich(List<Article> records) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> categoryIds = records.stream()
                .map(Article::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = records.stream()
                .map(Article::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> categoryNames = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            for (Category c : categoryMapper.selectBatchIds(categoryIds)) {
                categoryNames.put(c.getId(), c.getName());
            }
        }
        Map<Long, String> authorNames = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User u : userMapper.selectBatchIds(userIds)) {
                authorNames.put(u.getId(), displayName(u));
            }
        }

        List<ArticleListVO> list = new ArrayList<>(records.size());
        for (Article a : records) {
            ArticleListVO vo = new ArticleListVO();
            vo.setId(a.getId());
            vo.setTitle(a.getTitle());
            vo.setCoverImage(a.getCoverImage());
            vo.setCategoryId(a.getCategoryId());
            vo.setCategoryName(categoryNames.get(a.getCategoryId()));
            vo.setAuthorId(a.getUserId());
            vo.setAuthorName(authorNames.get(a.getUserId()));
            vo.setPublishTime(a.getPublishTime());
            vo.setUpdateTime(a.getUpdateTime());
            vo.setViewCount(a.getViewCount());
            vo.setIsTop(a.getIsTop());
            vo.setStatus(a.getStatus());
            vo.setRejectReason(a.getRejectReason());
            vo.setSummary(summarize(a.getContent()));
            list.add(vo);
        }
        return list;
    }

    /** 昵称优先，没有就用账号 */
    private String displayName(User user) {
        if (user == null) {
            return null;
        }
        return (user.getNickname() != null && !user.getNickname().isEmpty())
                ? user.getNickname() : user.getUsername();
    }

    /** 去 HTML 标签后截断，作为列表摘要 */
    private String summarize(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        String text = html.replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return text.length() > SUMMARY_LEN ? text.substring(0, SUMMARY_LEN) + "…" : text;
    }

}
