-- 创建用户密码表
CREATE TABLE user_auth (
    user_id CHAR(11) NOT NULL COMMENT '用户编号',
    password VARCHAR(64) NOT NULL COMMENT '用户密码(加密)',
    salt VARCHAR(32) COMMENT '密码盐值',
    last_login DATETIME COMMENT '最后登录时间',
    account_type TINYINT DEFAULT 1 COMMENT '账户类型: 1=普通用户, 2=管理员',
    status TINYINT DEFAULT 1 COMMENT '账户状态: 0=禁用, 1=正常',
    PRIMARY KEY (user_id)
) COMMENT '用户认证表';

-- 创建权限组表
CREATE TABLE permission_group (
    group_id INT AUTO_INCREMENT COMMENT '权限组ID',
    group_name VARCHAR(50) NOT NULL COMMENT '权限组名称',
    description VARCHAR(255) COMMENT '权限组描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (group_id),
    UNIQUE KEY (group_name)
) COMMENT '权限组表';

-- 创建用户-权限组关联表
CREATE TABLE user_group (
    user_id CHAR(11) NOT NULL COMMENT '用户编号',
    group_id INT NOT NULL COMMENT '权限组ID',
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    assigned_by CHAR(11) COMMENT '分配人',
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES client(cNumber) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES permission_group(group_id) ON DELETE CASCADE
) COMMENT '用户-权限组关联表';

-- 添加外键约束
ALTER TABLE user_auth ADD CONSTRAINT fk_user_auth_client 
FOREIGN KEY (user_id) REFERENCES client(cNumber) ON DELETE CASCADE;

-- 预置权限组数据
INSERT INTO permission_group (group_name, description) VALUES 
('普通用户', '基本预订查询权限'),
('管理员', '系统管理权限'),
('客房管理员', '客房管理权限'),
('预订管理员', '预订管理权限');

-- 修改现有的负责人表，使其与用户表关联(如需保留原有负责人表)
ALTER TABLE leader ADD COLUMN user_id CHAR(11) COMMENT '关联用户ID';
ALTER TABLE leader ADD CONSTRAINT fk_leader_client 
FOREIGN KEY (user_id) REFERENCES client(cNumber) ON DELETE SET NULL; 