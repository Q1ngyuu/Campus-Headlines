package com.campus.headlines.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.entity.ArticleMaterial;
import com.campus.headlines.entity.Material;
import com.campus.headlines.mapper.ArticleMaterialMapper;
import com.campus.headlines.mapper.MaterialMapper;
import com.campus.headlines.service.MaterialService;
import com.campus.headlines.util.FileUploadUtil;
import com.campus.headlines.vo.MaterialVO;
import com.campus.headlines.vo.UploadFileVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 素材业务实现。
 *
 * <p>删除策略选择<b>物理删除</b>：素材是用户自己的中间产物，可重新上传；
 * 磁盘只增不减会很快把空间吃满，而逻辑删除需要额外状态字段且文件依然占空间。
 * 为防止误删导致资讯缺图，删除前会检查 article_material 是否存在引用。</p>
 */
@Service
public class MaterialServiceImpl implements MaterialService {

    private static final Logger log = LoggerFactory.getLogger(MaterialServiceImpl.class);

    @Resource
    private MaterialMapper materialMapper;

    @Resource
    private ArticleMaterialMapper articleMaterialMapper;

    @Resource
    private FileUploadUtil fileUploadUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialVO upload(MultipartFile file, Long userId) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }

        // 1. 校验并落盘（扩展名白名单、大小、路径都在工具类里统一处理）
        UploadFileVO uploaded = fileUploadUtil.upload(file);

        // 2. 写数据库；失败则把刚写的文件删掉，避免产生孤儿文件
        Material material = new Material();
        material.setUserId(userId);
        material.setFileName(uploaded.getOriginalName());
        material.setFilePath(uploaded.getRelativePath());
        material.setFileType(uploaded.getFileType());
        material.setFileSize(uploaded.getFileSize());
        try {
            materialMapper.insert(material);
        } catch (RuntimeException e) {
            log.error("素材入库失败，回滚磁盘文件: {}", uploaded.getRelativePath(), e);
            fileUploadUtil.deleteByRelativePath(uploaded.getRelativePath());
            throw e;
        }

        log.info("素材上传成功: userId={}, id={}, path={}", userId, material.getId(), uploaded.getRelativePath());
        return MaterialVO.from(materialMapper.selectById(material.getId()));
    }

    @Override
    public List<MaterialVO> listByUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "请先登录");
        }
        List<Material> list = materialMapper.selectList(
                new LambdaQueryWrapper<Material>()
                        .eq(Material::getUserId, userId)
                        .orderByDesc(Material::getCreateTime)
                        .orderByDesc(Material::getId));
        return list.stream().map(MaterialVO::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        if (id == null) {
            throw new BusinessException("素材ID不能为空");
        }
        Material material = materialMapper.selectById(id);
        if (material == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "素材不存在或已被删除");
        }
        if (userId == null || !userId.equals(material.getUserId())) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "无权删除他人的素材");
        }

        // 被资讯引用时不允许删，否则正文/封面会出现死链
        Long referenced = articleMaterialMapper.selectCount(
                new LambdaQueryWrapper<ArticleMaterial>().eq(ArticleMaterial::getMaterialId, id));
        if (referenced != null && referenced > 0) {
            throw new BusinessException("该素材已被 " + referenced + " 篇资讯引用，请先在资讯中移除后再删除");
        }

        // 先删库再删盘：库删失败会回滚，盘删失败只留垃圾文件，不影响一致性
        materialMapper.deleteById(id);
        fileUploadUtil.deleteByRelativePath(material.getFilePath());
        log.info("素材删除成功: id={}, userId={}, path={}", id, userId, material.getFilePath());
    }
}
