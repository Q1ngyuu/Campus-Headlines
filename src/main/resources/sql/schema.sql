-- =====================================================================
--  校园头条（Campus Headlines）数据库建表脚本
--  DBMS    : MySQL 8.0.44
--  引擎     : InnoDB（支持事务与物理外键）
--  字符集   : utf8mb4 / utf8mb4_0900_ai_ci
--  说明     : 本脚本含 DROP TABLE，重复执行会清空数据，生产环境勿直接运行
--
--  资讯状态流转（article.status）：
--      0 草稿 ──提交──> 1 待审核 ──通过──> 2 已通过 ──> 写入 publish_time，前台可见
--                              └──驳回──> 3 已驳回 ──> 记录 reject_reason
--      3 已驳回 ──修改后重新提交──> 1 待审核（可再次流转）
--      0 草稿 / 3 已驳回 只有作者本人可见，学生角色无权直接把状态改成 2
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `favorite`;
DROP TABLE IF EXISTS `article_material`;
DROP TABLE IF EXISTS `article`;
DROP TABLE IF EXISTS `material`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `user`;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- 1. 用户表
-- ---------------------------------------------------------------------
CREATE TABLE `user` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    varchar(50)  NOT NULL                COMMENT '登录账号，全表唯一',
    `password`    varchar(100) NOT NULL                COMMENT '密码，BCrypt 加密后的密文（60 位），禁止明文存储',
    `nickname`    varchar(50)  DEFAULT NULL            COMMENT '昵称，展示用',
    `avatar`      varchar(500) DEFAULT NULL            COMMENT '头像访问路径，如 /upload/avatar/2026/10/x.png',
    `role`        tinyint      NOT NULL DEFAULT 0      COMMENT '角色：0-学生（投稿人），1-管理员（审核人）',
    `status`      tinyint      NOT NULL DEFAULT 1      COMMENT '账号状态：0-禁用（禁止登录），1-正常',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户表（学生与管理员共用，靠 role 区分）';

-- ---------------------------------------------------------------------
-- 2. 栏目分类表
-- ---------------------------------------------------------------------
CREATE TABLE `category` (
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        varchar(50) NOT NULL                COMMENT '栏目名称，如：校园新闻、活动通知、社团风采',
    `sort_order`  int         NOT NULL DEFAULT 0      COMMENT '排序值，数值越小越靠前',
    `status`      tinyint     NOT NULL DEFAULT 1      COMMENT '状态：0-停用（前台不展示），1-启用',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='栏目分类表';

-- ---------------------------------------------------------------------
-- 3. 素材表（上传的图片 / 视频）
-- ---------------------------------------------------------------------
CREATE TABLE `material` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     bigint       NOT NULL                COMMENT '上传者ID，关联 user.id',
    `file_name`   varchar(255) NOT NULL                COMMENT '原始文件名，如 运动会.jpg',
    `file_path`   varchar(500) NOT NULL                COMMENT '访问路径，如 /upload/image/2026/10/xxxx.jpg',
    `file_type`   varchar(20)  NOT NULL                COMMENT '素材类型：image-图片，video-视频',
    `file_size`   bigint       NOT NULL DEFAULT 0      COMMENT '文件大小，单位字节',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_material_user` (`user_id`),
    KEY `idx_material_type` (`file_type`),
    CONSTRAINT `fk_material_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='素材表（用户上传的图片与视频）';

-- ---------------------------------------------------------------------
-- 4. 资讯表（核心表）
-- ---------------------------------------------------------------------
CREATE TABLE `article` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       bigint       NOT NULL                COMMENT '作者ID，关联 user.id',
    `category_id`   bigint       DEFAULT NULL            COMMENT '栏目ID，关联 category.id，栏目删除后置空',
    `title`         varchar(200) NOT NULL                COMMENT '标题',
    `cover_image`   varchar(500) DEFAULT NULL            COMMENT '封面图访问路径',
    `content`       mediumtext                            COMMENT '正文，HTML 富文本，内嵌素材通过 article_material 关联',
    `status`        tinyint      NOT NULL DEFAULT 0      COMMENT '审核状态：0-草稿，1-待审核，2-已通过，3-已驳回',
    `reject_reason` varchar(500) DEFAULT NULL            COMMENT '驳回原因，status=3 时填写',
    `view_count`    int          NOT NULL DEFAULT 0      COMMENT '浏览量',
    `is_top`        tinyint      NOT NULL DEFAULT 0      COMMENT '是否置顶：0-否，1-是',
    `publish_time`  datetime     DEFAULT NULL            COMMENT '发布时间，审核通过时写入；草稿/待审核为 NULL',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_user` (`user_id`),
    KEY `idx_article_category` (`category_id`),
    KEY `idx_article_status_publish` (`status`, `publish_time`),
    KEY `idx_article_top` (`is_top`),
    CONSTRAINT `fk_article_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='资讯表（含草稿、审核流转）';

-- ---------------------------------------------------------------------
-- 5. 资讯-素材关联表（一篇资讯可内嵌多个素材，一个素材可被多篇引用）
-- ---------------------------------------------------------------------
CREATE TABLE `article_material` (
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`  bigint   NOT NULL                COMMENT '资讯ID，关联 article.id',
    `material_id` bigint   NOT NULL                COMMENT '素材ID，关联 material.id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关联建立时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_material` (`article_id`, `material_id`),
    KEY `idx_am_material` (`material_id`),
    CONSTRAINT `fk_am_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_am_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='资讯与素材的多对多关联表';

-- ---------------------------------------------------------------------
-- 6. 收藏表
-- ---------------------------------------------------------------------
CREATE TABLE `favorite` (
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     bigint   NOT NULL                COMMENT '收藏人ID，关联 user.id',
    `article_id`  bigint   NOT NULL                COMMENT '被收藏资讯ID，关联 article.id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite_user_article` (`user_id`, `article_id`),
    KEY `idx_favorite_article` (`article_id`),
    CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_favorite_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户收藏表';

-- ---------------------------------------------------------------------
-- 7. 初始化数据（可选，正式导入数据后删除本段即可）
-- ---------------------------------------------------------------------
INSERT INTO `category` (`name`, `sort_order`, `status`) VALUES
    ('校园新闻', 10, 1),
    ('活动通知', 20, 1),
    ('社团风采', 30, 1),
    ('学习分享', 40, 1);

-- 管理员账号不要在这里写死密文：密码必须用 BCrypt 现算，不同盐值结果不同。
-- 做法（登录功能写完后）：用 BCryptPasswordEncoder.encode("你的密码") 生成后再执行：
--   INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
--   VALUES ('admin', '<上一步生成的 60 位密文>', '管理员', 1, 1);
