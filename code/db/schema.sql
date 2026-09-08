-- 第一期开发表结构占位脚本
-- 为什么：先把字符集/时区/命名约定落地，后续每个里程碑按 tasks.md 逐步补齐真实表结构

CREATE DATABASE IF NOT EXISTS cszd_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE cszd_db;

-- 为什么：避免直接使用 MySQL 保留字 user，账号体系统一落到 sys_user。
-- 第一阶段只定义支持登录、角色、状态和后续改密/重置密码所需的最小字段，
-- 不提前引入最后登录时间、删除标记等当前任务未要求的字段，避免过度设计。
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  account VARCHAR(32) NOT NULL COMMENT '登录账号；系统管理员为 admin，业务管理员阶段要求与手机号一致',
  phone VARCHAR(20) DEFAULT NULL COMMENT '手机号；业务管理员必填且唯一，系统管理员可为空',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  name VARCHAR(64) NOT NULL COMMENT '用户名称',
  role_code VARCHAR(32) NOT NULL COMMENT '角色编码：SYSTEM_ADMIN / BUSINESS_ADMIN',
  user_status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '用户状态：ENABLED / DISABLED',
  is_default_admin TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为默认系统管理员',
  last_login_at TIMESTAMP NULL DEFAULT NULL COMMENT '最后登录时间',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_account (account),
  UNIQUE KEY uk_sys_user_phone (phone),
  KEY idx_sys_user_role_code (role_code),
  KEY idx_sys_user_status (user_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

-- 为什么：平台、充电站状态、充电桩状态统一收口到系统字典，避免后续各页面维护独立枚举。
-- 第一版仅内置 PLATFORM / STATION_STATUS / PILE_STATUS 三类，页面暂不开放新增字典类型。
CREATE TABLE IF NOT EXISTS sys_dict_type (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  type_code VARCHAR(64) NOT NULL COMMENT '类型编码：PLATFORM / STATION_STATUS / PILE_STATUS',
  type_name VARCHAR(64) NOT NULL COMMENT '类型名称',
  type_status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '类型状态：ENABLED / DISABLED',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_dict_type_code (type_code),
  UNIQUE KEY uk_sys_dict_type_name (type_name),
  KEY idx_sys_dict_type_status (type_status),
  KEY idx_sys_dict_type_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统字典类型表';

-- 为什么：字典项统一使用编码、名称、状态、排序四个核心字段，满足后续列表、下拉和筛选复用。
CREATE TABLE IF NOT EXISTS sys_dict_item (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  type_id BIGINT NOT NULL COMMENT '字典类型ID',
  item_code VARCHAR(64) NOT NULL COMMENT '项目编码',
  item_name VARCHAR(64) NOT NULL COMMENT '项目名称',
  item_status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '项目状态：ENABLED / DISABLED',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_dict_item_type_code (type_id, item_code),
  UNIQUE KEY uk_sys_dict_item_type_name (type_id, item_name),
  KEY idx_sys_dict_item_status (item_status),
  KEY idx_sys_dict_item_sort_order (sort_order),
  CONSTRAINT fk_sys_dict_item_type_id FOREIGN KEY (type_id) REFERENCES sys_dict_type (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统字典项表';

-- TODO: 里程碑 B 后续任务继续补齐：默认管理员初始化、系统字典页面与接口；
-- 为什么：业务域表统一使用 biz_ 前缀，系统域保留 sys_ 前缀，
-- 这样能与 sys_user、sys_dict_type、sys_dict_item 保持清晰分层。
-- 当前阶段先定义支撑基础信息录入、平台筛选、状态筛选和后续详情扩展的最小真实表结构。
-- 其中 station_code 为充电站编号；充电站编号由系统自动生成，且全局唯一。
-- platform_station_code 为人工录入的平台站点编号，可为空，但平台站点编号在同一平台内唯一。
-- station_status_item_id 表示当前单一生命周期状态，第一期范围为：签合同 / 建设中 / 运营中 / 运营异常 / 停用，
-- 后续新增充电站时默认应落到“签合同”对应的启用字典项。
CREATE TABLE IF NOT EXISTS biz_station (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  station_code VARCHAR(32) NOT NULL COMMENT '充电站编号；充电站编号由系统自动生成，且全局唯一',
  station_name VARCHAR(128) NOT NULL COMMENT '充电站名称',
  platform_item_id BIGINT NOT NULL COMMENT '所在平台字典项ID；关联 PLATFORM 类型字典项',
  platform_station_code VARCHAR(64) DEFAULT NULL COMMENT '平台站点编号；人工录入，可为空，平台站点编号在同一平台内唯一',
  address VARCHAR(255) DEFAULT NULL COMMENT '地址',
  contact_name VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  station_status_item_id BIGINT NOT NULL COMMENT '生命周期状态字典项ID；单一状态，默认指向签合同',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_station_station_code (station_code),
  UNIQUE KEY uk_biz_station_platform_station_code (platform_item_id, platform_station_code),
  KEY idx_biz_station_station_name (station_name),
  KEY idx_biz_station_platform_item_id (platform_item_id),
  KEY idx_biz_station_station_status_item_id (station_status_item_id),
  CONSTRAINT fk_biz_station_platform_item_id FOREIGN KEY (platform_item_id) REFERENCES sys_dict_item (id),
  CONSTRAINT fk_biz_station_station_status_item_id FOREIGN KEY (station_status_item_id) REFERENCES sys_dict_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='充电站表';

-- 为什么：状态变更备注需要落库，且后续历史变化时间线与日志审计都依赖每次变更的完整记录，
-- 因此独立建状态变更日志表，而不是在 biz_station 上只保留最近一次备注。
-- 当前任务只负责写入（状态切换时记录原状态/新状态/备注/操作人账号），查询展示在后续时间线任务落地。
CREATE TABLE IF NOT EXISTS biz_station_status_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  station_id BIGINT NOT NULL COMMENT '充电站ID；关联 biz_station.id',
  from_status_item_id BIGINT NOT NULL COMMENT '变更前状态字典项ID',
  to_status_item_id BIGINT NOT NULL COMMENT '变更后状态字典项ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '状态变更备注；可选，最多500字符',
  operator_account VARCHAR(32) DEFAULT NULL COMMENT '操作人登录账号',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_station_status_log_station_id (station_id),
  KEY idx_biz_station_status_log_created_at (created_at),
  CONSTRAINT fk_biz_station_status_log_station_id FOREIGN KEY (station_id) REFERENCES biz_station (id),
  CONSTRAINT fk_biz_station_status_log_from_status_item_id FOREIGN KEY (from_status_item_id) REFERENCES sys_dict_item (id),
  CONSTRAINT fk_biz_station_status_log_to_status_item_id FOREIGN KEY (to_status_item_id) REFERENCES sys_dict_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='充电站状态变更日志表';

-- 为什么：充电桩为业务域实体，按 biz_ 前缀规范落到 biz_pile。
-- 当前管理粒度为桩级（不管理单枪明细），充电枪数量以 gun_count 记录在桩上；
-- 未来扩展到枪级时，通过新建 biz_gun 表以 pile_id 关联扩展，不在本期提前建表。
-- pile_code 为桩编号；桩编号由系统自动生成，且全局唯一（格式与充电站编号一致：前缀-年月-序号）。
-- charge_type 表示充电类型，第一期范围为快充 / 慢充，由应用层枚举校验，不引入字典。
-- pile_status_item_id 表示当前单一生命周期状态，第一期范围为：运营中 / 运营异常 / 停用，
-- 新增充电桩时默认应落到“运营中”对应的启用字典项。
-- gun_count 表示充电枪数量，业务上至少为 1，由应用层在新增/编辑时校验。
CREATE TABLE IF NOT EXISTS biz_pile (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  pile_code VARCHAR(32) NOT NULL COMMENT '桩编号；桩编号由系统自动生成，且全局唯一',
  station_id BIGINT NOT NULL COMMENT '所属充电站ID；关联 biz_station.id',
  charge_type VARCHAR(16) NOT NULL COMMENT '充电类型：FAST 快充 / SLOW 慢充',
  gun_count INT NOT NULL COMMENT '充电枪数量；至少为1，由应用层校验',
  pile_status_item_id BIGINT NOT NULL COMMENT '生命周期状态字典项ID；单一状态，默认指向运营中',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_pile_pile_code (pile_code),
  KEY idx_biz_pile_station_id (station_id),
  KEY idx_biz_pile_pile_status_item_id (pile_status_item_id),
  CONSTRAINT fk_biz_pile_station_id FOREIGN KEY (station_id) REFERENCES biz_station (id),
  CONSTRAINT fk_biz_pile_pile_status_item_id FOREIGN KEY (pile_status_item_id) REFERENCES sys_dict_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='充电桩表';

-- 为什么：充电桩状态切换与站点一样需要落完整的变更记录，
-- 供后续历史变化时间线与日志审计使用，而不是在 biz_pile 上只保留最近一次备注。
CREATE TABLE IF NOT EXISTS biz_pile_status_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  pile_id BIGINT NOT NULL COMMENT '充电桩ID；关联 biz_pile.id',
  from_status_item_id BIGINT NOT NULL COMMENT '变更前状态字典项ID',
  to_status_item_id BIGINT NOT NULL COMMENT '变更后状态字典项ID',
  remark VARCHAR(500) DEFAULT NULL COMMENT '状态变更备注；可选，最多500字符',
  operator_account VARCHAR(32) DEFAULT NULL COMMENT '操作人登录账号',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_pile_status_log_pile_id (pile_id),
  KEY idx_biz_pile_status_log_created_at (created_at),
  CONSTRAINT fk_biz_pile_status_log_pile_id FOREIGN KEY (pile_id) REFERENCES biz_pile (id),
  CONSTRAINT fk_biz_pile_status_log_from_status_item_id FOREIGN KEY (from_status_item_id) REFERENCES sys_dict_item (id),
  CONSTRAINT fk_biz_pile_status_log_to_status_item_id FOREIGN KEY (to_status_item_id) REFERENCES sys_dict_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='充电桩状态变更日志表';

-- 为什么：股东为业务域实体，按 biz_ 前缀规范落到 biz_shareholder。
-- 股东是独立主档实体，与充电站解耦：充电站是否归属某股东由后续里程碑的持股关系表表达，
-- 主档本身不持有 station_id，避免一对多关系写死在主档侧，从而支持同一股东跨站复用。
-- shareholder_name 股东名称为必填文本，需求未要求唯一性，先以普通索引支撑列表页按名称搜索；
-- 联系人与联系电话为可选录入项，第一期不参与唯一性校验。
-- shareholder_status 仅有启用/停用两态（退股不属于主档状态，由后续股权调整记录表达），
-- 参照 sys_user 的实现方式，用应用层枚举校验，不引入字典项。
-- 第一期不做物理删除，股东通过停用状态管理（与用户管理删除策略一致）。
CREATE TABLE IF NOT EXISTS biz_shareholder (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  shareholder_name VARCHAR(128) NOT NULL COMMENT '股东名称；必填，不做唯一约束，支持按名称检索',
  contact_name VARCHAR(64) DEFAULT NULL COMMENT '联系人；可选',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话；可选',
  shareholder_status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '股东状态：ENABLED 启用 / DISABLED 停用',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_shareholder_shareholder_name (shareholder_name),
  KEY idx_biz_shareholder_shareholder_status (shareholder_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='股东主档表';

-- 为什么：持股关系是股东与充电站的中间实体，spec 7.5 已定义字段与状态；
-- 关联两侧只存主键ID（与 biz_pile.station_id 同口径），不做物理外键，由应用层维护引用一致性。
-- holding_status 两态：ACTIVE 持股中 / EXITED 已退出；退股不物理删除（spec 8.2 删除策略），
-- 同一股东对同一站可能存在“退出后再持股”的历史记录，因此不加 (station_id, shareholder_id)
-- 唯一约束，唯一性由里程碑 F 的维护逻辑在应用层校验。
CREATE TABLE IF NOT EXISTS biz_shareholding (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  station_id BIGINT NOT NULL COMMENT '充电站ID；引用 biz_station.id',
  shareholder_id BIGINT NOT NULL COMMENT '股东ID；引用 biz_shareholder.id',
  ownership_percentage DECIMAL(5,2) NOT NULL COMMENT '股份占比；0-100，同一站点所有持股中股东合计须为100',
  holding_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '持股状态：ACTIVE 持股中 / EXITED 已退出',
  effective_at TIMESTAMP NULL DEFAULT NULL COMMENT '生效时间；股权调整立即生效，保存时记录当前时间',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_shareholding_shareholder_status (shareholder_id, holding_status),
  KEY idx_biz_shareholding_station (station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='持股关系表';

-- 为什么：股权调整独立页面保存成功后要求写入一条调整事件（spec 8.3），
-- 为里程碑 G 的历史变化时间线与审计留存完整的前后持股快照；
-- 快照为 JSON 数组并内嵌股东名称，避免后续股东改名导致历史事件展示失真。
-- total_percentage 记录保存后所有持股中关系的占比合计，合计非 100 的调整
-- 已在保存前经用户二次确认放行（spec 8.3 交互要求），事件中如实留痕。
-- station_id 与 biz_shareholding 口径一致：不做物理外键，由应用层维护引用一致性。
CREATE TABLE IF NOT EXISTS biz_equity_adjustment_event (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  station_id BIGINT NOT NULL COMMENT '充电站ID；引用 biz_station.id',
  before_snapshot TEXT NOT NULL COMMENT '调整前持股快照；JSON数组，元素含持股关系ID/股东ID/股东名称/股份占比',
  after_snapshot TEXT NOT NULL COMMENT '调整后持股快照；JSON数组，结构同调整前快照',
  total_percentage DECIMAL(5,2) NOT NULL COMMENT '调整后持股中占比合计；正常为100.00，非100需保存前二次确认',
  operator_account VARCHAR(32) DEFAULT NULL COMMENT '操作人登录账号',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_equity_adjustment_event_station_id (station_id),
  KEY idx_biz_equity_adjustment_event_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='股权调整事件表';

-- 为什么：spec 9 要求对所有关键操作进行统一审计，时间线、全局日志页都依赖这份数据；
-- 将分散在站/桩/股东/持股/字典/用户等模块的操作集中记录，避免为每个对象单独维护日志表。
-- target_type / operation_type 使用字符串枚举，由应用层统一校验，便于全局日志页按类型筛选。
-- before_content / after_content 使用 TEXT 存储 JSON 或文本摘要，兼容不同对象的结构差异。
-- station_id 允许为空：字典维护、用户管理等操作不归属具体充电站；站点/桩/持股/股权调整等操作填写该字段，
-- 用于全局日志页按充电站筛选。
CREATE TABLE IF NOT EXISTS biz_operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  operator_account VARCHAR(32) DEFAULT NULL COMMENT '操作人登录账号',
  operator_role VARCHAR(32) DEFAULT NULL COMMENT '操作人角色：SYSTEM_ADMIN / BUSINESS_ADMIN',
  target_type VARCHAR(32) NOT NULL COMMENT '对象类型：STATION 充电站 / PILE 充电桩 / SHAREHOLDER 股东 / SHAREHOLDING 持股关系 / SYSTEM_DICT 系统字典 / USER 用户',
  target_id VARCHAR(64) DEFAULT NULL COMMENT '对象标识；业务对象编号或主键ID字符串',
  target_name VARCHAR(128) DEFAULT NULL COMMENT '对象名称；如充电站名称、股东名称，用于快速展示',
  operation_type VARCHAR(32) NOT NULL COMMENT '操作类型：CREATE 新增 / UPDATE 修改 / STATUS_CHANGE 状态变更 / ENABLE 启用 / DISABLE 停用 / RESET_PASSWORD 重置密码 / EQUITY_ADJUST 股权调整 / EXIT 退出',
  before_content TEXT DEFAULT NULL COMMENT '修改前内容；JSON或文本摘要',
  after_content TEXT DEFAULT NULL COMMENT '修改后内容；JSON或文本摘要',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注；如状态变更原因',
  station_id BIGINT DEFAULT NULL COMMENT '关联充电站ID；用于按充电站筛选，非站点操作可为空',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_biz_operation_log_created_at (created_at),
  KEY idx_biz_operation_log_operator_account (operator_account),
  KEY idx_biz_operation_log_target_type (target_type),
  KEY idx_biz_operation_log_station_id (station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一操作日志表';
