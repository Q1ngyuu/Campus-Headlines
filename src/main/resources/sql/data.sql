-- =====================================================================
--  校园头条 演示数据（可选）
--
--  用途：让首页一打开就有内容，方便演示与答辩。
--  前置：先执行 schema.sql（6 张表 + 4 个栏目）
--  特点：所有插入都是「不存在才插」，重复执行不会产生重复数据
--
--  演示账号（密码均为 123456，BCrypt 存储）：
--    zhangwei / 123456   张伟（学生）
--    lisi     / 123456   李思（学生）
--    wangfang / 123456   王芳（学生）
--    admin    / admin123456  系统管理员（role=1）
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. 演示用户
-- ---------------------------------------------------------------------
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
SELECT 'zhangwei', '$2a$10$eZejVn2lOHeRvANLoh7OtOppJTb8fJgwLQKwwi4696t0U.l/2rzpO', '张伟', 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'zhangwei');

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
SELECT 'lisi', '$2a$10$eZejVn2lOHeRvANLoh7OtOppJTb8fJgwLQKwwi4696t0U.l/2rzpO', '李思', 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'lisi');

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
SELECT 'wangfang', '$2a$10$eZejVn2lOHeRvANLoh7OtOppJTb8fJgwLQKwwi4696t0U.l/2rzpO', '王芳', 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'wangfang');

-- 管理员：密码 admin123456
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
SELECT 'admin', '$2a$10$dtQFUaP8cGdmd2MSPZUvs.1D.T66lTI54.uU5ZUtJi0DcfaQEieYO', '系统管理员', 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin');

-- ---------------------------------------------------------------------
-- 2. 栏目（schema.sql 已插入则跳过）
-- ---------------------------------------------------------------------
INSERT INTO `category` (`name`, `sort_order`, `status`)
SELECT '校园新闻', 10, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = '校园新闻');
INSERT INTO `category` (`name`, `sort_order`, `status`)
SELECT '活动通知', 20, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = '活动通知');
INSERT INTO `category` (`name`, `sort_order`, `status`)
SELECT '社团风采', 30, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = '社团风采');
INSERT INTO `category` (`name`, `sort_order`, `status`)
SELECT '学习分享', 40, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = '学习分享');

-- ---------------------------------------------------------------------
-- 3. 已通过的资讯（status=2，会出现在首页）
--    封面用仓库内的 SVG（/static/img/covers/*.svg），不依赖上传目录
-- ---------------------------------------------------------------------
INSERT INTO `article` (`user_id`, `category_id`, `title`, `cover_image`, `content`, `status`, `view_count`, `is_top`, `publish_time`)
SELECT (SELECT id FROM `user` WHERE `username` = 'lisi'),
       (SELECT id FROM `category` WHERE `name` = '校园新闻'),
       '图书馆自 11 月起延长开放时间至 23:00',
       '/static/img/covers/c5.svg',
       '<p>为满足同学们的备考与自习需求，经学校研究决定，图书馆自 11 月 1 日起调整开放时间。</p><p>具体安排：周一至周日 07:30—23:00，其中 22:30 停止入场；自习区与借阅区同步延长。法定节假日另行通知。</p><p>请同学们合理安排时间，离开时带走个人物品，共同维护安静的学习环境。</p>',
       2, 512, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '图书馆自 11 月起延长开放时间至 23:00');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `cover_image`, `content`, `status`, `view_count`, `is_top`, `publish_time`)
SELECT (SELECT id FROM `user` WHERE `username` = 'zhangwei'),
       (SELECT id FROM `category` WHERE `name` = '活动通知'),
       '第十二届校园运动会报名正式启动',
       '/static/img/covers/c6.svg',
       '<p>第十二届校园运动会将于下月中旬在学校田径场举行，即日起接受各学院报名。</p><p>比赛项目分为田径类（100 米、400 米、跳远、铅球等）与趣味类（拔河、两人三足、接力投篮）。每人限报两个单项，可兼报一个集体项目。</p><p>报名方式：各学院学生会统一汇总后于本周五 17:00 前交至校体育部；报名表见班级群文件。</p>',
       2, 431, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '第十二届校园运动会报名正式启动');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `cover_image`, `content`, `status`, `view_count`, `is_top`, `publish_time`)
SELECT (SELECT id FROM `user` WHERE `username` = 'wangfang'),
       (SELECT id FROM `category` WHERE `name` = '校园新闻'),
       '新食堂三楼正式营业，推出 30 道新菜品',
       '/static/img/covers/c1.svg',
       '<p>经过一个暑假的改造，第三食堂三楼于本周一正式营业，营业首日接待师生超过 2000 人次。</p><p>三层以「轻食 + 地方风味」为定位，新增窗口 8 个，推出 30 道菜品，其中 12 道为低油低盐的健身餐，支持扫码点单、错峰取餐。</p><p>后勤集团表示，将根据师生反馈每月更新菜单，欢迎通过食堂意见箱或「校园服务」小程序提出建议。</p>',
       2, 624, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '新食堂三楼正式营业，推出 30 道新菜品');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `cover_image`, `content`, `status`, `view_count`, `is_top`, `publish_time`)
SELECT (SELECT id FROM `user` WHERE `username` = 'zhangwei'),
       (SELECT id FROM `category` WHERE `name` = '校园新闻'),
       '我校在第十五届大学生数学建模竞赛中获佳绩',
       '/static/img/covers/c1.svg',
       '<p>第十五届全国大学生数学建模竞赛成绩日前揭晓，我校共派出 18 支队伍参赛，获全国一等奖 2 项、二等奖 3 项，省级奖项 9 项，创历年最好成绩。</p><p>本次赛题涉及城市交通流量预测与新能源调度优化，参赛队伍在三天时间内完成建模、求解与论文撰写。</p><p>指导教师表示，学校近两年开设的「数学建模与算法实践」选修课为队伍储备打下了基础，明年将继续扩大培训规模。</p>',
       2, 328, 0, DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '我校在第十五届大学生数学建模竞赛中获佳绩');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `cover_image`, `content`, `status`, `view_count`, `is_top`, `publish_time`)
SELECT (SELECT id FROM `user` WHERE `username` = 'wangfang'),
       (SELECT id FROM `category` WHERE `name` = '活动通知'),
       '关于举办 2026 年校园文化艺术节的通知',
       '/static/img/covers/c2.svg',
       '<p>为进一步丰富校园文化生活，学校定于下月举办 2026 年校园文化艺术节，现面向全校师生征集节目与作品。</p><p>活动分为舞台展演、书画摄影展览、创意市集三个板块。舞台类节目时长不超过 8 分钟，书画摄影作品每人限投 3 件。</p><p>报名截止时间为本月 25 日，报名邮箱及作品提交方式见校团委公众号推文。</p>',
       2, 276, 0, DATE_SUB(NOW(), INTERVAL 5 DAY)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '关于举办 2026 年校园文化艺术节的通知');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `cover_image`, `content`, `status`, `view_count`, `is_top`, `publish_time`)
SELECT (SELECT id FROM `user` WHERE `username` = 'lisi'),
       (SELECT id FROM `category` WHERE `name` = '社团风采'),
       '天文社「秋季星空观测」活动圆满结束',
       '/static/img/covers/c3.svg',
       '<p>上周六晚，天文社在图书馆天台组织了本学期第一次秋季星空观测活动，共有 60 余名同学参与。</p><p>活动现场架设了 3 台折射式望远镜，社团骨干讲解了秋季四边形的辨认方法，并带领大家观测了土星环与昴星团。</p><p>据社团负责人介绍，下一次观测活动将在月食当晚举行，届时会提前在社团公众号发布通知。</p>',
       2, 198, 0, DATE_SUB(NOW(), INTERVAL 6 DAY)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '天文社「秋季星空观测」活动圆满结束');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `cover_image`, `content`, `status`, `view_count`, `is_top`, `publish_time`)
SELECT (SELECT id FROM `user` WHERE `username` = 'wangfang'),
       (SELECT id FROM `category` WHERE `name` = '社团风采'),
       '青年志愿者协会走进社区开展支教活动',
       '/static/img/covers/c3.svg',
       '<p>上周日，青年志愿者协会的 20 名成员前往学校周边的阳光社区，为社区儿童开展了一天的科普支教活动。</p><p>活动内容包括趣味物理小实验、手绘地图和阅读分享三个环节，志愿者还向社区图书角捐赠了 120 册少儿读物。</p><p>协会表示，本学期将保持每月一次的社区服务频率，欢迎有兴趣的同学报名参与。</p>',
       2, 165, 0, DATE_SUB(NOW(), INTERVAL 4 DAY)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '青年志愿者协会走进社区开展支教活动');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `cover_image`, `content`, `status`, `view_count`, `is_top`, `publish_time`)
SELECT (SELECT id FROM `user` WHERE `username` = 'zhangwei'),
       (SELECT id FROM `category` WHERE `name` = '学习分享'),
       '四级备考经验分享：三个月从 425 到 560',
       '/static/img/covers/c4.svg',
       '<p>去年 12 月我第一次考四级只有 425 分，今年 6 月提到了 560 分。这篇把三个月的做法整理出来，供学弟学妹参考。</p><p>词汇：每天固定 40 分钟背 60 个新词 + 复习前一天的，用 App 的「例句填空」模式比单纯认词有效。</p><p>听力与阅读占分最高，建议每天各做一套真题的对应部分，做完必须精听/精读一遍，把生词和长难句记到本子上。</p><p>写作和翻译不要最后一周才背模板，提前一个月每周写两篇，找同学互改，比背十个模板有用。</p>',
       2, 803, 0, DATE_SUB(NOW(), INTERVAL 7 DAY)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '四级备考经验分享：三个月从 425 到 560');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `cover_image`, `content`, `status`, `view_count`, `is_top`, `publish_time`)
SELECT (SELECT id FROM `user` WHERE `username` = 'lisi'),
       (SELECT id FROM `category` WHERE `name` = '学习分享'),
       '实验室开放日：走进人工智能实验室',
       '/static/img/covers/c4.svg',
       '<p>本周三下午，计算机学院人工智能实验室举办了本学期第一次开放日活动，40 名本科生走进实验室参观体验。</p><p>实验室老师介绍了实验室的研究方向，包括计算机视觉、自然语言处理与智能机器人，并演示了目标检测与语音识别两个小项目。</p><p>对科研感兴趣的同学可以关注实验室主页，下学期将开放本科生科研助理的招募。</p>',
       2, 342, 0, DATE_SUB(NOW(), INTERVAL 8 DAY)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '实验室开放日：走进人工智能实验室');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `cover_image`, `content`, `status`, `view_count`, `is_top`, `publish_time`)
SELECT (SELECT id FROM `user` WHERE `username` = 'wangfang'),
       (SELECT id FROM `category` WHERE `name` = '活动通知'),
       '关于国庆假期图书馆开放安排的通知',
       '/static/img/covers/c2.svg',
       '<p>国庆假期期间，图书馆开放安排如下：10 月 1 日至 3 日闭馆，10 月 4 日起恢复正常开放。</p><p>闭馆期间到期应还的图书，借阅期限自动顺延至开馆后三个工作日，不产生逾期费用。</p><p>请同学们提前安排好借还书时间，祝假期愉快。</p>',
       2, 289, 0, DATE_SUB(NOW(), INTERVAL 10 DAY)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '关于国庆假期图书馆开放安排的通知');
