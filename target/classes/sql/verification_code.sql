-- 创建验证码表
CREATE TABLE IF NOT EXISTS `verification_code` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `target` varchar(100) NOT NULL COMMENT '目标(手机号或邮箱)',
  `code` varchar(10) NOT NULL COMMENT '验证码',
  `type` varchar(10) NOT NULL COMMENT '类型(PHONE/EMAIL)',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `used` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已使用',
  PRIMARY KEY (`id`),
  KEY `idx_user_target_type` (`user_id`, `target`, `type`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码表';
