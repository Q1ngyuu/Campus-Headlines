package com.campus.headlines.service;

import com.campus.headlines.vo.MaterialVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 素材业务接口：上传、列表、删除。
 */
public interface MaterialService {

    /**
     * 上传素材：先落盘，再写 material 表。
     *
     * @param file   上传的文件
     * @param userId 当前登录用户 id
     * @return 入库后的素材信息
     */
    MaterialVO upload(MultipartFile file, Long userId);

    /**
     * 查询某用户的全部素材，按上传时间倒序。
     */
    List<MaterialVO> listByUser(Long userId);

    /**
     * 删除素材（物理删除：数据库记录 + 磁盘文件）。
     *
     * <p>会校验归属；若该素材已被某篇资讯引用（存在 article_material 记录），
     * 则拒绝删除，避免资讯里的图片变成死链。</p>
     *
     * @param id     素材 id
     * @param userId 当前登录用户 id
     */
    void delete(Long id, Long userId);
}
