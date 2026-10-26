package com.campus.headlines.util;

import com.campus.headlines.common.BusinessException;
import com.campus.headlines.common.Result;
import com.campus.headlines.entity.Material;
import com.campus.headlines.vo.UploadFileVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件上传工具：校验、生成路径、落盘。
 *
 * <p>安全要点（前端 accept 只是体验，真正的防线在这里）：</p>
 * <ol>
 *   <li><b>扩展名白名单</b>：只认配置里列出的后缀，其余一律拒绝；</li>
 *   <li><b>不用原始文件名落盘</b>：文件名用 UUID，彻底避开中文、空格、
 *       以及 {@code ../} 这类路径穿越；</li>
 *   <li><b>大小双重校验</b>：MultipartResolver 是第一道（请求级），
 *       这里按类型做第二道（图片 10MB / 视频 50MB）；</li>
 *   <li><b>路径归一化校验</b>：写盘前确认最终路径仍在 basePath 之下。</li>
 * </ol>
 *
 * <p>存储结构：{@code {upload.base-path}/{yyyyMM}/{uuid}.{ext}}，
 * 对外地址：{@code /upload/{yyyyMM}/{uuid}.{ext}}（由 spring-mvc.xml 的
 * {@code <mvc:resources mapping="/upload/**" location="file:..."/>} 提供访问）。</p>
 */
@Component
public class FileUploadUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUploadUtil.class);

    /** 月份目录格式 */
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    /** 原始文件名最大保留长度（material.file_name 是 varchar(255)，留足余量） */
    private static final int MAX_NAME_LENGTH = 100;

    /** 磁盘根目录，绝对路径 */
    @Value("${upload.base-path}")
    private String basePath;

    /** 对外访问前缀，默认 /upload */
    @Value("${upload.url-prefix:/upload}")
    private String urlPrefix;

    /** 图片单文件上限（字节） */
    @Value("${upload.max-image-size:10485760}")
    private long maxImageSize;

    /** 视频单文件上限（字节） */
    @Value("${upload.max-video-size:52428800}")
    private long maxVideoSize;

    /** 图片扩展名白名单 */
    @Value("${upload.image-ext:jpg,jpeg,png,gif,webp}")
    private String imageExtConfig;

    /** 视频扩展名白名单 */
    @Value("${upload.video-ext:mp4,webm,ogg}")
    private String videoExtConfig;

    private Set<String> imageExts = new LinkedHashSet<>();

    private Set<String> videoExts = new LinkedHashSet<>();

    /** 启动时创建根目录，并把配置里的白名单切成集合 */
    @PostConstruct
    public void init() {
        this.imageExts = splitExts(imageExtConfig);
        this.videoExts = splitExts(videoExtConfig);
        try {
            Files.createDirectories(Paths.get(basePath));
            log.info("上传根目录: {}，图片类型: {}，视频类型: {}", basePath, imageExts, videoExts);
        } catch (IOException e) {
            // 建不出来就直接让应用启动失败，比等到用户上传时才报错好
            throw new IllegalStateException("无法创建上传目录: " + basePath, e);
        }
    }

    /**
     * 校验并保存文件。
     *
     * @param file 上传的文件
     * @return 落盘结果（含相对路径、绝对路径、类型、大小）
     */
    public UploadFileVO upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        // ---------- 1. 文件名与扩展名 ----------
        String originalName = sanitizeFileName(file.getOriginalFilename());
        String ext = extensionOf(originalName);
        if (ext.isEmpty()) {
            throw new BusinessException("文件缺少扩展名，无法识别类型");
        }

        // ---------- 2. 白名单判定类型 ----------
        String fileType = resolveFileType(ext);
        if (fileType == null) {
            throw new BusinessException("不支持的文件类型：." + ext
                    + "。图片支持 " + String.join("/", imageExts)
                    + "，视频支持 " + String.join("/", videoExts));
        }

        // ---------- 3. 大小二次校验 ----------
        long size = file.getSize();
        long limit = Material.TYPE_IMAGE.equals(fileType) ? maxImageSize : maxVideoSize;
        if (size <= 0) {
            throw new BusinessException("文件内容为空");
        }
        if (size > limit) {
            throw new BusinessException(String.format("文件过大，%s 最大允许 %d MB，当前 %.1f MB",
                    Material.TYPE_IMAGE.equals(fileType) ? "图片" : "视频",
                    limit / 1024 / 1024, size / 1024.0 / 1024.0));
        }

        // ---------- 4. 生成路径 ----------
        String month = LocalDate.now().format(MONTH_FORMAT);
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path rootPath = Paths.get(basePath).toAbsolutePath().normalize();
        Path dir = rootPath.resolve(month);
        Path target = dir.resolve(storedName).normalize();

        // 双保险：UUID 生成的名字不可能穿越，但归一化后再确认一次
        if (!target.startsWith(rootPath)) {
            throw new BusinessException("非法的存储路径");
        }

        // ---------- 5. 落盘 ----------
        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("文件保存失败: {}", target, e);
            throw new BusinessException(Result.CODE_ERROR, "文件保存失败，请稍后重试");
        }

        UploadFileVO vo = new UploadFileVO();
        vo.setOriginalName(originalName);
        vo.setStoredName(storedName);
        vo.setExt(ext);
        vo.setFileType(fileType);
        vo.setFileSize(size);
        vo.setRelativePath(urlPrefix + "/" + month + "/" + storedName);
        vo.setAbsolutePath(target.toString());
        log.info("文件已保存: {} -> {} ({} bytes)", originalName, vo.getRelativePath(), size);
        return vo;
    }

    /**
     * 按对外相对路径删除磁盘文件，失败只记日志不抛异常——
     * 数据库记录已删的前提下，残留文件不应阻塞业务。
     */
    public void deleteByRelativePath(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return;
        }
        try {
            Path rootPath = Paths.get(basePath).toAbsolutePath().normalize();
            String sub = relativePath.startsWith(urlPrefix)
                    ? relativePath.substring(urlPrefix.length())
                    : relativePath;
            Path target = rootPath.resolve(sub.replaceFirst("^/+", "")).normalize();
            if (!target.startsWith(rootPath)) {
                log.warn("拒绝删除越界路径: {}", relativePath);
                return;
            }
            boolean deleted = Files.deleteIfExists(target);
            log.info("删除磁盘文件: {} -> {}", target, deleted ? "成功" : "文件不存在");
        } catch (IOException e) {
            log.warn("删除磁盘文件失败: {}", relativePath, e);
        }
    }

    // ---------------- 内部方法 ----------------

    /** 只保留文件名，去掉浏览器可能带上的路径，并做长度裁剪 */
    private String sanitizeFileName(String rawName) {
        if (rawName == null) {
            return "";
        }
        String name = rawName.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[\\r\\n\\t]", "").trim();
        if (name.length() <= MAX_NAME_LENGTH) {
            return name;
        }
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String suffix = dot > 0 ? name.substring(dot) : "";
        int keep = Math.max(1, MAX_NAME_LENGTH - suffix.length());
        return base.substring(0, Math.min(base.length(), keep)) + suffix;
    }

    /** 取小写扩展名，无扩展名返回空串 */
    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

    /** 按白名单返回 image / video，不在白名单返回 null */
    private String resolveFileType(String ext) {
        if (imageExts.contains(ext)) {
            return Material.TYPE_IMAGE;
        }
        if (videoExts.contains(ext)) {
            return Material.TYPE_VIDEO;
        }
        return null;
    }

    private Set<String> splitExts(String config) {
        if (config == null || config.trim().isEmpty()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(config.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** 供其他组件查询白名单（如前端提示） */
    public Set<String> getImageExts() {
        return imageExts;
    }

    public Set<String> getVideoExts() {
        return videoExts;
    }
}
