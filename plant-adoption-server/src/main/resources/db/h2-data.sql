MERGE INTO sys_department (id, name, parent_id, level, sort, leader, phone, status, deleted)
KEY(id)
VALUES
  (1, '总公司', 0, 1, 1, '张总', '010-12345678', 1, 0),
  (2, '技术部', 1, 2, 1, '李经理', '010-12345679', 1, 0),
  (3, '产品部', 1, 2, 2, '王经理', '010-12345680', 1, 0);

MERGE INTO sys_config (config_key, config_value, config_type, description, deleted)
KEY(config_key)
VALUES
  ('max_plants_per_user', '3', 'number', '每个用户最大领养绿植数', 0),
  ('care_reminder_days', '7', 'number', '养护提醒间隔天数', 0),
  ('drift_bottle_daily_limit', '3', 'number', '漂流瓶每日发送上限', 0),
  ('system_notice', '欢迎使用绿植领养系统', 'string', '系统公告', 0),
  ('enable_sms_login', 'false', 'boolean', '是否启用短信登录', 0);

MERGE INTO sys_user (id, username, phone, password, nickname, avatar, department_id, role, status, deleted)
KEY(id)
VALUES
  (1, 'admin', '13800138000', NULL, '系统管理员', NULL, 2, 'ADMIN', 1, 0),
  (2, 'user', '13900139000', NULL, '普通用户', NULL, 2, 'USER', 1, 0);

MERGE INTO plant (id, name, variety, location, image_url, qr_code_url, status, adopter_id, care_count, description, care_tips, deleted)
KEY(id)
VALUES
  (1, '发财树', '马拉巴栗', 'A区大厅', NULL, NULL, 'AVAILABLE', NULL, 0, '寓意吉祥，适合办公室养护', '见干见湿，避免积水', 0),
  (2, '绿萝', '黄金葛', 'B区会议室', NULL, NULL, 'AVAILABLE', NULL, 0, '净化空气能力强', '半阴环境，保持湿润', 0),
  (3, '吊兰', '金边吊兰', 'C区休息室', NULL, NULL, 'AVAILABLE', NULL, 0, '观赏性强', '定期浇水，避免暴晒', 0);
