-- 多语言消息配置表
-- 用于数据库存储的可配置多语言消息

USE zhuji_user_org;

-- 多语言消息表
CREATE TABLE IF NOT EXISTS sys_i18n_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    message_key VARCHAR(200) NOT NULL COMMENT '消息键',
    locale VARCHAR(20) NOT NULL COMMENT '语言区域：zh_CN, en_US, ja_JP, ko_KR等',
    message_value TEXT NOT NULL COMMENT '消息内容',
    module VARCHAR(50) DEFAULT 'common' COMMENT '模块：common, user, workflow等',
    description VARCHAR(200) COMMENT '描述',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    UNIQUE KEY uk_key_locale (message_key, locale),
    INDEX idx_message_key (message_key),
    INDEX idx_locale (locale),
    INDEX idx_module (module),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多语言消息表';

-- 初始化一些默认的多语言消息
-- 中文消息
INSERT INTO sys_i18n_message (message_key, locale, message_value, module, description, is_active) VALUES
('common.success', 'zh_CN', '操作成功', 'common', '通用成功消息', 1),
('common.error', 'zh_CN', '操作失败', 'common', '通用失败消息', 1),
('common.not.found', 'zh_CN', '资源不存在', 'common', '资源不存在消息', 1),
('common.unauthorized', 'zh_CN', '未授权访问', 'common', '未授权访问消息', 1),
('common.forbidden', 'zh_CN', '禁止访问', 'common', '禁止访问消息', 1),
('common.bad.request', 'zh_CN', '请求参数错误', 'common', '请求参数错误消息', 1),
('common.server.error', 'zh_CN', '服务器内部错误', 'common', '服务器内部错误消息', 1),
('user.not.found', 'zh_CN', '用户不存在', 'user', '用户不存在消息', 1),
('user.login.success', 'zh_CN', '登录成功', 'user', '登录成功消息', 1),
('user.login.failed', 'zh_CN', '登录失败，用户名或密码错误', 'user', '登录失败消息', 1),
('role.not.found', 'zh_CN', '角色不存在', 'user', '角色不存在消息', 1),
('permission.denied', 'zh_CN', '无权限访问', 'user', '权限拒绝消息', 1),
('org.not.found', 'zh_CN', '组织不存在', 'user', '组织不存在消息', 1),
('workflow.process.not.found', 'zh_CN', '流程定义不存在', 'workflow', '流程不存在消息', 1),
('workflow.instance.start.success', 'zh_CN', '流程启动成功', 'workflow', '流程启动成功消息', 1),
('workflow.task.complete.success', 'zh_CN', '任务完成成功', 'workflow', '任务完成成功消息', 1),
('i18n.language.switch.success', 'zh_CN', '语言切换成功', 'i18n', '语言切换成功消息', 1);

-- 英文消息
INSERT INTO sys_i18n_message (message_key, locale, message_value, module, description, is_active) VALUES
('common.success', 'en_US', 'Operation successful', 'common', 'General success message', 1),
('common.error', 'en_US', 'Operation failed', 'common', 'General error message', 1),
('common.not.found', 'en_US', 'Resource not found', 'common', 'Resource not found message', 1),
('common.unauthorized', 'en_US', 'Unauthorized access', 'common', 'Unauthorized access message', 1),
('common.forbidden', 'en_US', 'Access forbidden', 'common', 'Access forbidden message', 1),
('common.bad.request', 'en_US', 'Invalid request parameter', 'common', 'Invalid request parameter message', 1),
('common.server.error', 'en_US', 'Internal server error', 'common', 'Internal server error message', 1),
('user.not.found', 'en_US', 'User not found', 'user', 'User not found message', 1),
('user.login.success', 'en_US', 'Login successful', 'user', 'Login successful message', 1),
('user.login.failed', 'en_US', 'Login failed, incorrect username or password', 'user', 'Login failed message', 1),
('role.not.found', 'en_US', 'Role not found', 'user', 'Role not found message', 1),
('permission.denied', 'en_US', 'Permission denied', 'user', 'Permission denied message', 1),
('org.not.found', 'en_US', 'Organization not found', 'user', 'Organization not found message', 1),
('workflow.process.not.found', 'en_US', 'Process definition not found', 'workflow', 'Process not found message', 1),
('workflow.instance.start.success', 'en_US', 'Process instance started successfully', 'workflow', 'Process start success message', 1),
('workflow.task.complete.success', 'en_US', 'Task completed successfully', 'workflow', 'Task complete success message', 1),
('i18n.language.switch.success', 'en_US', 'Language switched successfully', 'i18n', 'Language switch success message', 1);

-- 日语消息
INSERT INTO sys_i18n_message (message_key, locale, message_value, module, description, is_active) VALUES
('common.success', 'ja_JP', '操作成功', 'common', '汎用成功メッセージ', 1),
('common.error', 'ja_JP', '操作失敗', 'common', '汎用エラーメッセージ', 1),
('common.not.found', 'ja_JP', 'リソースが見つかりません', 'common', 'リソース未発見メッセージ', 1),
('common.unauthorized', 'ja_JP', '未認証アクセス', 'common', '未認証アクセスメッセージ', 1),
('common.forbidden', 'ja_JP', 'アクセス禁止', 'common', 'アクセス禁止メッセージ', 1),
('common.bad.request', 'ja_JP', 'リクエストパラメータが無効です', 'common', 'リクエストパラメータ無効メッセージ', 1),
('common.server.error', 'ja_JP', 'サーバー内部エラー', 'common', 'サーバー内部エラーメッセージ', 1),
('user.not.found', 'ja_JP', 'ユーザーが見つかりません', 'user', 'ユーザー未発見メッセージ', 1),
('user.login.success', 'ja_JP', 'ログイン成功', 'user', 'ログイン成功メッセージ', 1),
('user.login.failed', 'ja_JP', 'ログイン失敗、ユーザー名またはパスワードが間違っています', 'user', 'ログイン失敗メッセージ', 1),
('role.not.found', 'ja_JP', 'ロールが見つかりません', 'user', 'ロール未発見メッセージ', 1),
('permission.denied', 'ja_JP', 'アクセス権限がありません', 'user', '権限拒否メッセージ', 1),
('org.not.found', 'ja_JP', '組織が見つかりません', 'user', '組織未発見メッセージ', 1),
('workflow.process.not.found', 'ja_JP', 'プロセス定義が見つかりません', 'workflow', 'プロセス未発見メッセージ', 1),
('workflow.instance.start.success', 'ja_JP', 'プロセス開始成功', 'workflow', 'プロセス開始成功メッセージ', 1),
('workflow.task.complete.success', 'ja_JP', 'タスク完了成功', 'workflow', 'タスク完了成功メッセージ', 1),
('i18n.language.switch.success', 'ja_JP', '言語切り替え成功', 'i18n', '言語切り替え成功メッセージ', 1);

-- 韩语消息
INSERT INTO sys_i18n_message (message_key, locale, message_value, module, description, is_active) VALUES
('common.success', 'ko_KR', '작업 성공', 'common', '일반 성공 메시지', 1),
('common.error', 'ko_KR', '작업 실패', 'common', '일반 오류 메시지', 1),
('common.not.found', 'ko_KR', '리소스를 찾을 수 없음', 'common', '리소스 미발견 메시지', 1),
('common.unauthorized', 'ko_KR', '인증되지 않은 접근', 'common', '미인증 접근 메시지', 1),
('common.forbidden', 'ko_KR', '접근 금지', 'common', '접근 금지 메시지', 1),
('common.bad.request', 'ko_KR', '요청 매개변수 유효하지 않음', 'common', '요청 매개변수 무효 메시지', 1),
('common.server.error', 'ko_KR', '서버 내부 오류', 'common', '서버 내부 오류 메시지', 1),
('user.not.found', 'ko_KR', '사용자를 찾을 수 없음', 'user', '사용자 미발견 메시지', 1),
('user.login.success', 'ko_KR', '로그인 성공', 'user', '로그인 성공 메시지', 1),
('user.login.failed', 'ko_KR', '로그인 실패, 사용자명 또는 비밀번호가 잘못됨', 'user', '로그인 실패 메시지', 1),
('role.not.found', 'ko_KR', '역할을 찾을 수 없음', 'user', '역할 미발견 메시지', 1),
('permission.denied', 'ko_KR', '권한이 없음', 'user', '권한 거부 메시지', 1),
('org.not.found', 'ko_KR', '조직을 찾을 수 없음', 'user', '조직 미발견 메시지', 1),
('workflow.process.not.found', 'ko_KR', '프로세스 정의를 찾을 수 없음', 'workflow', '프로세스 미발견 메시지', 1),
('workflow.instance.start.success', 'ko_KR', '프로세스 시작 성공', 'workflow', '프로세스 시작 성공 메시지', 1),
('workflow.task.complete.success', 'ko_KR', '태스크 완료 성공', 'workflow', '태스크 완료 성공 메시지', 1),
('i18n.language.switch.success', 'ko_KR', '언어 전환 성공', 'i18n', '언어 전환 성공 메시지', 1);
