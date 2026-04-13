-- =============================================
-- 鍔炲叕瀹ょ豢妞嶉鍏荤郴缁熸暟鎹簱鍒濆鍖栬剼鏈?-- 鏁版嵁搴? plant_db
-- 瀛楃闆? utf8mb4
-- =============================================

CREATE DATABASE IF NOT EXISTS plant_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE plant_db;

-- =============================================
-- 1. 閮ㄩ棬琛?sys_department
-- =============================================
DROP TABLE IF EXISTS sys_department;
CREATE TABLE sys_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '閮ㄩ棬ID',
    name VARCHAR(100) NOT NULL COMMENT '閮ㄩ棬鍚嶇О',
    parent_id BIGINT DEFAULT 0 COMMENT '鐖堕儴闂↖D锛?琛ㄧず椤剁骇閮ㄩ棬',
    level INT DEFAULT 1 COMMENT '閮ㄩ棬灞傜骇',
    sort INT DEFAULT 0 COMMENT '鎺掑簭鍙?,
    leader VARCHAR(50) COMMENT '閮ㄩ棬璐熻矗浜?,
    phone VARCHAR(20) COMMENT '鑱旂郴鐢佃瘽',
    status TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0-绂佺敤锛?-鍚敤',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-鏈垹闄わ紝1-宸插垹闄?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='閮ㄩ棬琛?;

-- =============================================
-- 2. 绯荤粺閰嶇疆琛?sys_config
-- =============================================
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '閰嶇疆ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '閰嶇疆閿?,
    config_value TEXT COMMENT '閰嶇疆鍊?,
    config_type VARCHAR(50) DEFAULT 'string' COMMENT '閰嶇疆绫诲瀷锛歴tring/number/boolean/json',
    description VARCHAR(255) COMMENT '閰嶇疆鎻忚堪',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-鏈垹闄わ紝1-宸插垹闄?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绯荤粺閰嶇疆琛?;

-- 鍒濆鍖栫郴缁熼厤缃暟鎹?INSERT INTO sys_config (config_key, config_value, config_type, description) VALUES
('max_plants_per_user', '3', 'number', '姣忕敤鎴锋渶澶ч鍏荤豢妞嶆暟閲?),
('care_reminder_days', '7', 'number', '鍏绘姢鎻愰啋闂撮殧澶╂暟'),
('adoption_timeout_hours', '24', 'number', '棰嗗吇鐢宠瓒呮椂灏忔椂鏁?),
('drift_bottle_daily_limit', '3', 'number', '婕傛祦鐡舵瘡鏃ュ彂閫佷笂闄?),
('system_notice', '娆㈣繋浣跨敤缁挎棰嗗吇绯荤粺锛?, 'string', '绯荤粺鍏憡'),
('enable_sms_login', 'false', 'boolean', '鏄惁鍚敤鐭俊鐧诲綍');

-- =============================================
-- 3. 鐢ㄦ埛琛?sys_user
-- =============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
    username VARCHAR(50) COMMENT '鐢ㄦ埛鍚?,
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '鎵嬫満鍙?,
    password VARCHAR(255) COMMENT '瀵嗙爜锛堝姞瀵嗗瓨鍌級',
    nickname VARCHAR(50) COMMENT '鏄电О',
    avatar VARCHAR(500) COMMENT '澶村儚URL',
    department_id BIGINT COMMENT '閮ㄩ棬ID',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '瑙掕壊锛歎SER-鏅€氱敤鎴凤紝ADMIN-绠＄悊鍛?,
    status TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0-绂佺敤锛?-鍚敤',
    last_login_time DATETIME COMMENT '鏈€鍚庣櫥褰曟椂闂?,
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-鏈垹闄わ紝1-宸插垹闄?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_phone (phone),
    INDEX idx_department_id (department_id),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鐢ㄦ埛琛?;

-- 鍒濆鍖栫鐞嗗憳璐﹀彿锛堝瘑鐮? admin123锛屼娇鐢˙Crypt鍔犲瘑锛?INSERT INTO sys_user (username, phone, password, nickname, role, status) VALUES
('admin', '13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '绯荤粺绠＄悊鍛?, 'ADMIN', 1);

-- =============================================
-- 4. 缁挎琛?plant
-- =============================================
DROP TABLE IF EXISTS plant;
CREATE TABLE plant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '缁挎ID',
    name VARCHAR(100) NOT NULL COMMENT '缁挎鍚嶇О',
    variety VARCHAR(100) COMMENT '鍝佺',
    location VARCHAR(200) COMMENT '鏀剧疆浣嶇疆',
    image_url VARCHAR(500) COMMENT '鍥剧墖URL',
    qr_code_url VARCHAR(500) COMMENT '浜岀淮鐮乁RL',
    status VARCHAR(20) DEFAULT 'AVAILABLE' COMMENT '鐘舵€侊細AVAILABLE-鍙鍏伙紝ADOPTED-宸查鍏伙紝MAINTENANCE-缁存姢涓?,
    adopter_id BIGINT COMMENT '棰嗗吇浜篒D',
    adoption_time DATETIME COMMENT '棰嗗吇鏃堕棿',
    last_care_time DATETIME COMMENT '鏈€鍚庡吇鎶ゆ椂闂?,
    care_count INT DEFAULT 0 COMMENT '鍏绘姢娆℃暟',
    description TEXT COMMENT '鎻忚堪',
    care_tips TEXT COMMENT '鍏绘姢鎻愮ず',
    care_plan_template_id BIGINT COMMENT '鍏绘姢璁″垝妯℃澘ID',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-鏈垹闄わ紝1-宸插垹闄?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_status (status),
    INDEX idx_adopter_id (adopter_id),
    INDEX idx_location (location),
    INDEX idx_care_plan_template_id (care_plan_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缁挎琛?;

-- =============================================
-- 5. 棰嗗吇璁板綍琛?adoption_record
-- =============================================
DROP TABLE IF EXISTS adoption_record;
CREATE TABLE adoption_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '璁板綍ID',
    plant_id BIGINT NOT NULL COMMENT '缁挎ID',
    plant_name VARCHAR(100) COMMENT '缁挎鍚嶇О锛堝啑浣欙級',
    user_id BIGINT NOT NULL COMMENT '鐢ㄦ埛ID',
    user_name VARCHAR(50) COMMENT '鐢ㄦ埛濮撳悕锛堝啑浣欙級',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '鐘舵€侊細PENDING-寰呭鎵癸紝APPROVED-宸查€氳繃锛孯EJECTED-宸叉嫆缁濓紝CANCELLED-宸插彇娑堬紝RETURNED-宸插綊杩?,
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鐢宠鏃堕棿',
    approve_time DATETIME COMMENT '瀹℃壒鏃堕棿',
    approver_id BIGINT COMMENT '瀹℃壒浜篒D',
    approve_remark VARCHAR(500) COMMENT '瀹℃壒澶囨敞',
    return_time DATETIME COMMENT '褰掕繕鏃堕棿',
    return_reason VARCHAR(500) COMMENT '褰掕繕鍘熷洜',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-鏈垹闄わ紝1-宸插垹闄?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_plant_id (plant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_apply_time (apply_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='棰嗗吇璁板綍琛?;

-- =============================================
-- 6. 鍏绘姢鏃ュ織琛?care_log
-- =============================================
DROP TABLE IF EXISTS care_log;
CREATE TABLE care_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '鏃ュ織ID',
    plant_id BIGINT NOT NULL COMMENT '缁挎ID',
    plant_name VARCHAR(100) COMMENT '缁挎鍚嶇О锛堝啑浣欙級',
    user_id BIGINT NOT NULL COMMENT '鍏绘姢浜篒D',
    user_name VARCHAR(50) COMMENT '鍏绘姢浜哄鍚嶏紙鍐椾綑锛?,
    care_type VARCHAR(50) NOT NULL COMMENT '鍏绘姢绫诲瀷锛歐ATER-娴囨按锛孎ERTILIZE-鏂借偉锛孭RUNE-淇壀锛孭EST_CONTROL-鐥呰櫕瀹抽槻娌伙紝OTHER-鍏朵粬',
    care_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍏绘姢鏃堕棿',
    description TEXT COMMENT '鍏绘姢鎻忚堪',
    images VARCHAR(2000) COMMENT '鍥剧墖URLs锛圝SON鏁扮粍锛?,
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-鏈垹闄わ紝1-宸插垹闄?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_plant_id (plant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_care_time (care_time),
    INDEX idx_care_type (care_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍏绘姢鏃ュ織琛?;

-- =============================================
-- 7. 绀惧尯甯栧瓙琛?community_post
-- =============================================
DROP TABLE IF EXISTS community_post;
CREATE TABLE community_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '甯栧瓙ID',
    user_id BIGINT NOT NULL COMMENT '鍙戝竷鑰匢D',
    user_name VARCHAR(50) COMMENT '鍙戝竷鑰呭鍚嶏紙鍐椾綑锛?,
    user_avatar VARCHAR(500) COMMENT '鍙戝竷鑰呭ご鍍忥紙鍐椾綑锛?,
    title VARCHAR(200) NOT NULL COMMENT '鏍囬',
    content TEXT NOT NULL COMMENT '鍐呭',
    images VARCHAR(5000) COMMENT '鍥剧墖URLs锛圝SON鏁扮粍锛?,
    post_type VARCHAR(20) DEFAULT 'SHARE' COMMENT '甯栧瓙绫诲瀷锛歋HARE-鍒嗕韩锛孮UESTION-闂瓟锛孍XPERIENCE-缁忛獙',
    view_count INT DEFAULT 0 COMMENT '娴忚鏁?,
    like_count INT DEFAULT 0 COMMENT '鐐硅禐鏁?,
    comment_count INT DEFAULT 0 COMMENT '璇勮鏁?,
    is_top TINYINT DEFAULT 0 COMMENT '鏄惁缃《锛?-鍚︼紝1-鏄?,
    is_essence TINYINT DEFAULT 0 COMMENT '鏄惁绮惧崕锛?-鍚︼紝1-鏄?,
    status TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0-闅愯棌锛?-鏄剧ず',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-鏈垹闄わ紝1-宸插垹闄?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_user_id (user_id),
    INDEX idx_post_type (post_type),
    INDEX idx_create_time (create_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绀惧尯甯栧瓙琛?;

-- =============================================
-- 8. 甯栧瓙璇勮琛?community_comment
-- =============================================
DROP TABLE IF EXISTS community_comment;
CREATE TABLE community_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '璇勮ID',
    post_id BIGINT NOT NULL COMMENT '甯栧瓙ID',
    user_id BIGINT NOT NULL COMMENT '璇勮鑰匢D',
    user_name VARCHAR(50) COMMENT '璇勮鑰呭鍚嶏紙鍐椾綑锛?,
    user_avatar VARCHAR(500) COMMENT '璇勮鑰呭ご鍍忥紙鍐椾綑锛?,
    parent_id BIGINT DEFAULT 0 COMMENT '鐖惰瘎璁篒D锛?琛ㄧず涓€绾ц瘎璁?,
    reply_user_id BIGINT COMMENT '鍥炲鐢ㄦ埛ID',
    reply_user_name VARCHAR(50) COMMENT '鍥炲鐢ㄦ埛濮撳悕',
    content TEXT NOT NULL COMMENT '璇勮鍐呭',
    like_count INT DEFAULT 0 COMMENT '鐐硅禐鏁?,
    status TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0-闅愯棌锛?-鏄剧ず',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-鏈垹闄わ紝1-宸插垹闄?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='甯栧瓙璇勮琛?;

-- =============================================
-- 9. 甯栧瓙鐐硅禐琛?post_like
-- =============================================
DROP TABLE IF EXISTS post_like;
CREATE TABLE post_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '鐐硅禐ID',
    post_id BIGINT NOT NULL COMMENT '甯栧瓙ID',
    comment_id BIGINT COMMENT '璇勮ID锛堢偣璧炶瘎璁烘椂浣跨敤锛?,
    user_id BIGINT NOT NULL COMMENT '鐢ㄦ埛ID',
    like_type VARCHAR(20) DEFAULT 'POST' COMMENT '鐐硅禐绫诲瀷锛歅OST-甯栧瓙锛孋OMMENT-璇勮',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-鏈垹闄わ紝1-宸插垹闄?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    UNIQUE KEY uk_post_user (post_id, user_id, like_type),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='甯栧瓙鐐硅禐琛?;

-- =============================================
-- 10. 閫氱煡琛?notification
-- =============================================
DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '閫氱煡ID',
    user_id BIGINT NOT NULL COMMENT '鎺ユ敹鐢ㄦ埛ID',
    title VARCHAR(200) NOT NULL COMMENT '鏍囬',
    content TEXT COMMENT '鍐呭',
    notification_type VARCHAR(50) NOT NULL COMMENT '閫氱煡绫诲瀷锛欰DOPTION-棰嗗吇锛孋ARE-鍏绘姢锛孲YSTEM-绯荤粺锛孡IKE-鐐硅禐锛孋OMMENT-璇勮',
    related_id BIGINT COMMENT '鍏宠仈涓氬姟ID',
    related_type VARCHAR(50) COMMENT '鍏宠仈涓氬姟绫诲瀷',
    is_read TINYINT DEFAULT 0 COMMENT '鏄惁宸茶锛?-鏈锛?-宸茶',
    read_time DATETIME COMMENT '闃呰鏃堕棿',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-鏈垹闄わ紝1-宸插垹闄?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_notification_type (notification_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='閫氱煡琛?;

-- =============================================
-- 11. 鍏绘姢璁″垝妯℃澘琛?care_plan_template
-- =============================================
DROP TABLE IF EXISTS care_plan_template;
CREATE TABLE care_plan_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '妯℃澘ID',
    template_name VARCHAR(100) NOT NULL COMMENT '妯℃澘鍚嶇О',
    plant_category VARCHAR(50) COMMENT '閫傞厤缁挎绫诲埆',
    plant_species VARCHAR(100) COMMENT '閫傞厤缁挎鍝佺',
    description TEXT COMMENT '妯℃澘鎻忚堪',
    create_by BIGINT NOT NULL COMMENT '鍒涘缓浜?,
    status TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤锛?-绂佺敤',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_plant_category (plant_category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍏绘姢璁″垝妯℃澘琛?;

-- =============================================
-- 12. 鍏绘姢璁″垝椤硅〃 care_plan_item
-- =============================================
DROP TABLE IF EXISTS care_plan_item;
CREATE TABLE care_plan_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '妯℃澘椤笽D',
    template_id BIGINT NOT NULL COMMENT '妯℃澘ID',
    care_type VARCHAR(50) NOT NULL COMMENT '鍏绘姢绫诲瀷',
    cycle_type VARCHAR(50) NOT NULL COMMENT '鍛ㄦ湡绫诲瀷',
    cycle_value INT NOT NULL COMMENT '鍛ㄦ湡鍊?,
    care_detail VARCHAR(500) NOT NULL COMMENT '鍏绘姢缁嗚妭',
    care_note TEXT COMMENT '娉ㄦ剰浜嬮」',
    remind_advance INT DEFAULT 1 COMMENT '鎻愬墠鎻愰啋灏忔椂鏁?,
    sort INT DEFAULT 0 COMMENT '鎺掑簭',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍏绘姢璁″垝妯℃澘椤硅〃';

-- =============================================
-- 13. 缁挎鍏绘姢璁″垝瀹炰緥琛?plant_care_plan
-- =============================================
DROP TABLE IF EXISTS plant_care_plan;
CREATE TABLE plant_care_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '瀹炰緥ID',
    plant_id BIGINT NOT NULL COMMENT '缁挎ID',
    template_id BIGINT NOT NULL COMMENT '妯℃澘ID',
    adopter_id BIGINT NOT NULL COMMENT '棰嗗吇浜篒D',
    adjust_note TEXT COMMENT '涓€у寲璋冩暣璇存槑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    UNIQUE KEY uk_plant_adopter (plant_id, adopter_id),
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缁挎鍏绘姢璁″垝瀹炰緥琛?;

-- =============================================
-- 14. 鍏绘姢浠诲姟琛?care_task
-- =============================================
DROP TABLE IF EXISTS care_task;
CREATE TABLE care_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '浠诲姟ID',
    plant_care_plan_id BIGINT NOT NULL COMMENT '璁″垝瀹炰緥ID',
    plan_item_id BIGINT NOT NULL COMMENT '璁″垝椤笽D',
    plant_id BIGINT NOT NULL COMMENT '缁挎ID',
    adopter_id BIGINT NOT NULL COMMENT '棰嗗吇浜篒D',
    due_date DATE NOT NULL COMMENT '搴旀墽琛屾棩鏈?,
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '鐘舵€侊細PENDING-寰呮墽琛岋紝DONE-宸叉墽琛岋紝OVERDUE-宸查€炬湡锛孋ANCELED-宸插彇娑?,
    care_log_id BIGINT COMMENT '鍏宠仈鍏绘姢璁板綍ID',
    completed_time DATETIME COMMENT '瀹屾垚鏃堕棿',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_adopter_due_date (adopter_id, due_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍏绘姢浠诲姟琛?;

-- =============================================
-- 15. 婕傛祦鐡惰〃 drift_bottle
-- =============================================
ALTER TABLE care_task
    ADD COLUMN last_remind_time DATETIME COMMENT '任务最近提醒时间' AFTER status;


CREATE TABLE drift_bottle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '婕傛祦鐡禝D',
    sender_id BIGINT NOT NULL COMMENT '鍙戦€佷汉ID',
    receiver_id BIGINT COMMENT '鎺ユ敹浜篒D',
    content TEXT NOT NULL COMMENT '鍐呭',
    image_urls VARCHAR(2000) COMMENT '鍥剧墖URL鍒楄〃',
    status VARCHAR(20) DEFAULT 'FLOATING' COMMENT '鐘舵€侊細FLOATING-寰呮嬀鍙栵紝PICKED-宸叉嬀鍙栵紝REPLIED-宸插洖澶嶏紝CLOSED-宸插叧闂?,
    pick_time DATETIME COMMENT '鎷惧彇鏃堕棿',
    reply_content TEXT COMMENT '鍥炲鍐呭',
    reply_time DATETIME COMMENT '鍥炲鏃堕棿',
    pick_expire_time DATETIME COMMENT '鎷惧彇瓒呮椂鏃堕棿',
    deleted TINYINT DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-鏈垹闄わ紝1-宸插垹闄?,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='婕傛祦鐡惰〃';

-- =============================================
-- 16. 婕傛祦鐡舵棩蹇楄〃 drift_bottle_log
-- =============================================
DROP TABLE IF EXISTS drift_bottle_log;
CREATE TABLE drift_bottle_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '鏃ュ織ID',
    bottle_id BIGINT NOT NULL COMMENT '婕傛祦鐡禝D',
    operator_id BIGINT NOT NULL COMMENT '鎿嶄綔浜篒D',
    operation VARCHAR(20) NOT NULL COMMENT '鎿嶄綔绫诲瀷',
    remark VARCHAR(200) COMMENT '澶囨敞',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    INDEX idx_bottle_id (bottle_id),
    INDEX idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='婕傛祦鐡舵搷浣滄棩蹇楄〃';

-- =============================================
-- 鍒濆鍖栨祴璇曟暟鎹?-- =============================================

-- 鍒濆鍖栭儴闂ㄦ暟鎹?INSERT INTO sys_department (name, parent_id, level, sort, leader, phone) VALUES
('鎬诲叕鍙?, 0, 1, 1, '寮犳€?, '010-12345678'),
('鎶€鏈儴', 1, 2, 1, '鏉庣粡鐞?, '010-12345679'),
('浜у搧閮?, 1, 2, 2, '鐜嬬粡鐞?, '010-12345680'),
('杩愯惀閮?, 1, 2, 3, '璧电粡鐞?, '010-12345681'),
('甯傚満閮?, 1, 2, 4, '閽辩粡鐞?, '010-12345682');

-- 鍒濆鍖栫豢妞嶆暟鎹?INSERT INTO plant (name, variety, location, status, description, care_tips) VALUES
('鍙戣储鏍?, '椹媺宸存牀', 'A鍖哄ぇ鍘?, 'AVAILABLE', '瀵撴剰鍚夌ゥ锛岄€傚悎鍔炲叕瀹ゅ吇娈?, '鍠滄俯鏆栨箍娑︼紝鑰愰槾鎬у己锛屾祰姘磋骞茶婀?),
('缁胯悵', '榛勯噾钁?, 'B鍖轰細璁', 'AVAILABLE', '鍑€鍖栫┖姘旇兘鍔涘己', '鍠滄暎灏勫厜锛屼繚鎸佸湡澹ゆ箍娑︼紝閬垮厤寮哄厜鐩村皠'),
('鍚婂叞', '閲戣竟鍚婂叞', 'C鍖轰紤鎭', 'AVAILABLE', '鍚告敹鐢查啗鏁堟灉濂?, '鍠滃崐闃寸幆澧冿紝淇濇寔鍦熷￥婀挎鼎锛屽畾鏈熸柦鑲?),
('瀵岃吹绔?, '涓囧勾绔?, 'D鍖哄墠鍙?, 'AVAILABLE', '姘村煿妞嶇墿锛屽共鍑€缇庤', '姣忓懆鎹㈡按涓€娆★紝閬垮厤闃冲厜鐩村皠'),
('鍚涘瓙鍏?, '澶ц姳鍚涘瓙鍏?, 'E鍖哄姙鍏', 'AVAILABLE', '鑺卞彾淇辩編锛岃璧忎环鍊奸珮', '鍠滃崐闃达紝蹇屽己鍏夛紝鍦熷￥淇濇寔寰鼎');

-- =============================================
-- 鍒涘缓瑙嗗浘
-- =============================================

-- 缁挎棰嗗吇缁熻瑙嗗浘
CREATE OR REPLACE VIEW v_plant_adoption_stats AS
SELECT 
    p.id AS plant_id,
    p.name AS plant_name,
    p.status,
    COUNT(DISTINCT ar.id) AS total_adoption_count,
    COUNT(DISTINCT cl.id) AS total_care_count,
    MAX(cl.care_time) AS last_care_time
FROM plant p
LEFT JOIN adoption_record ar ON p.id = ar.plant_id AND ar.deleted = 0
LEFT JOIN care_log cl ON p.id = cl.plant_id AND cl.deleted = 0
WHERE p.deleted = 0
GROUP BY p.id, p.name, p.status;

-- 鐢ㄦ埛棰嗗吇缁熻瑙嗗浘
CREATE OR REPLACE VIEW v_user_adoption_stats AS
SELECT 
    u.id AS user_id,
    u.nickname AS user_name,
    u.department_id,
    d.name AS department_name,
    COUNT(DISTINCT CASE WHEN p.status = 'ADOPTED' THEN p.id END) AS current_adopt_count,
    COUNT(DISTINCT ar.id) AS total_adoption_count,
    COUNT(DISTINCT cl.id) AS total_care_count
FROM sys_user u
LEFT JOIN plant p ON u.id = p.adopter_id AND p.deleted = 0
LEFT JOIN adoption_record ar ON u.id = ar.user_id AND ar.deleted = 0
LEFT JOIN care_log cl ON u.id = cl.user_id AND cl.deleted = 0
LEFT JOIN sys_department d ON u.department_id = d.id
WHERE u.deleted = 0
GROUP BY u.id, u.nickname, u.department_id, d.name;

-- =============================================
-- 鍒涘缓瀛樺偍杩囩▼
-- =============================================

-- 鏇存柊缁挎鍏绘姢缁熻
DELIMITER //
CREATE PROCEDURE sp_update_plant_care_stats(IN p_plant_id BIGINT)
BEGIN
    UPDATE plant 
    SET care_count = (
        SELECT COUNT(*) FROM care_log 
        WHERE plant_id = p_plant_id AND deleted = 0
    ),
    last_care_time = (
        SELECT MAX(care_time) FROM care_log 
        WHERE plant_id = p_plant_id AND deleted = 0
    )
    WHERE id = p_plant_id;
END //
DELIMITER ;

-- 妫€鏌ョ敤鎴烽鍏绘暟閲忛檺鍒?DELIMITER //
CREATE PROCEDURE sp_check_user_adoption_limit(
    IN p_user_id BIGINT,
    IN p_max_plants INT,
    OUT p_can_adopt BOOLEAN
)
BEGIN
    DECLARE current_count INT;
    
    SELECT COUNT(*) INTO current_count
    FROM plant 
    WHERE adopter_id = p_user_id AND status = 'ADOPTED' AND deleted = 0;
    
    SET p_can_adopt = (current_count < p_max_plants);
END //
DELIMITER ;

-- =============================================
-- 鍒涘缓瑙﹀彂鍣?-- =============================================

-- 棰嗗吇璁板綍鐘舵€佸彉鏇磋Е鍙戝櫒
DELIMITER //
CREATE TRIGGER tr_adoption_status_change
AFTER UPDATE ON adoption_record
FOR EACH ROW
BEGIN
    IF NEW.status = 'APPROVED' AND OLD.status != 'APPROVED' THEN
        UPDATE plant SET status = 'ADOPTED', adopter_id = NEW.user_id, adoption_time = NEW.approve_time
        WHERE id = NEW.plant_id;
    ELSEIF NEW.status = 'REJECTED' AND OLD.status = 'PENDING' THEN
        UPDATE plant SET status = 'AVAILABLE', adopter_id = NULL, adoption_time = NULL
        WHERE id = NEW.plant_id AND adopter_id = NEW.user_id;
    ELSEIF NEW.status = 'RETURNED' THEN
        UPDATE plant SET status = 'AVAILABLE', adopter_id = NULL, adoption_time = NULL
        WHERE id = NEW.plant_id;
    END IF;
END //
DELIMITER ;

-- 甯栧瓙璇勮鏁版洿鏂拌Е鍙戝櫒
DELIMITER //
CREATE TRIGGER tr_comment_count_insert
AFTER INSERT ON community_comment
FOR EACH ROW
BEGIN
    UPDATE community_post SET comment_count = comment_count + 1
    WHERE id = NEW.post_id;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER tr_comment_count_delete
AFTER UPDATE ON community_comment
FOR EACH ROW
BEGIN
    IF NEW.deleted = 1 AND OLD.deleted = 0 THEN
        UPDATE community_post SET comment_count = GREATEST(comment_count - 1, 0)
        WHERE id = NEW.post_id;
    END IF;
END //
DELIMITER ;

-- =============================================
-- 瀹屾垚鎻愮ず
-- =============================================
SELECT '鏁版嵁搴撳垵濮嬪寲瀹屾垚锛? AS message;

ALTER TABLE care_task
    ADD COLUMN cycle_type_override VARCHAR(50) COMMENT '任务个性化周期类型',
    ADD COLUMN cycle_value_override INT COMMENT '任务个性化周期数值',
    ADD COLUMN care_detail_override VARCHAR(500) COMMENT '任务个性化内容';

