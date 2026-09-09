-- ============================================================================
-- 长沙众电 充电站/充电桩 初始化数据
-- 数据来源：doc/长沙众电.csv（83 行原始数据）
-- 目标库：cszd_db（与 20260909_cleanup_test_data.sql 口径一致；如需在其他库执行，请先调整 USE 语句）
--
-- 初始化规则（已与业务方确认）：
--   1. 83 个充电站全部写入，站点状态、桩状态统一为「运营中」；
--   2. 平台按 PLATFORM 字典项名称匹配（字典由系统管理员维护）：
--      华自 / 云快充 / 小桔；
--   3. 枪数换算桩数：快充桩数 = 快充枪数除以 2 向上取整（奇数枪时最后一台桩 gun_count 记 1），
--      慢充桩数 = 慢充枪数（每桩 1 枪）；
--   4. 电表户号仅保留纯数字，其余（--、我方支付企业，专变、同上xxxx、暂无、空值）一律置 NULL；
--   5. platform_station_code（平台站点编号）、地址、联系人、联系电话统一置 NULL；
--   6. Excel 生成的「场站ID」不写入系统；
--   7. 站编号 ST-202609-0001 起顺序编号（与后端 StationCodeGenerator 规则一致，
--      初始化后系统新建站点自动从最大编号续号）；桩编号 PL-202609-0001 起全局连续（与 PileCodeGenerator 规则一致）。
--
-- 数据规模：充电站 83 个；充电桩 232 台
--   （快充 136 台 / 270 枪，慢充 96 台 / 96 枪）
--
-- 执行前提与注意事项：
--   a. 先执行 20260909_cleanup_test_data.sql 清空业务表，或确认 biz_station / biz_pile 为空；
--   b. PLATFORM 字典已维护「华自」「云快充」「小桔」三个启用项；
--      STATION_STATUS / PILE_STATUS 字典项由后端启动时自动内置（运营中 = OPERATING）；
--   c. 本脚本不可重复执行：station_code / pile_code 均有唯一约束，重复执行会报 Duplicate entry；
--   d. 保护机制：第 1 节字典变量若解析为 NULL，第 2/3 节 INSERT 会因 NOT NULL 约束立即失败，
--      事务不会提交，mysql 客户端默认中止执行，不会写入任何脏数据。
-- ============================================================================

USE cszd_db;

-- 脚本包含中文数据，显式声明连接字符集，避免客户端默认编码不一致导致乱码
SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- 1. 解析字典项 ID
--    运行期按「字典类型编码 + 项目名称/项目编码」动态匹配，不依赖字典数据的物理 ID；
--    平台字典由管理员维护，按名称匹配；状态字典由系统内置，按编码匹配。
-- ----------------------------------------------------------------------------

SET @platform_1 := (
    SELECT di.id
    FROM sys_dict_item di
    JOIN sys_dict_type dt ON dt.id = di.type_id
    WHERE dt.type_code = 'PLATFORM'
      AND dt.type_status = 'ENABLED'
      AND di.item_name = '华自'
      AND di.item_status = 'ENABLED'
);

SET @platform_2 := (
    SELECT di.id
    FROM sys_dict_item di
    JOIN sys_dict_type dt ON dt.id = di.type_id
    WHERE dt.type_code = 'PLATFORM'
      AND dt.type_status = 'ENABLED'
      AND di.item_name = '云快充'
      AND di.item_status = 'ENABLED'
);

SET @platform_3 := (
    SELECT di.id
    FROM sys_dict_item di
    JOIN sys_dict_type dt ON dt.id = di.type_id
    WHERE dt.type_code = 'PLATFORM'
      AND dt.type_status = 'ENABLED'
      AND di.item_name = '小桔'
      AND di.item_status = 'ENABLED'
);

SET @station_operating := (
    SELECT di.id
    FROM sys_dict_item di
    JOIN sys_dict_type dt ON dt.id = di.type_id
    WHERE dt.type_code = 'STATION_STATUS'
      AND dt.type_status = 'ENABLED'
      AND di.item_code = 'OPERATING'
      AND di.item_status = 'ENABLED'
);

SET @pile_operating := (
    SELECT di.id
    FROM sys_dict_item di
    JOIN sys_dict_type dt ON dt.id = di.type_id
    WHERE dt.type_code = 'PILE_STATUS'
      AND dt.type_status = 'ENABLED'
      AND di.item_code = 'OPERATING'
      AND di.item_status = 'ENABLED'
);

-- 字典项自检：5 个值应全部非 NULL（任一为 NULL 说明对应字典项缺失或未启用，请先补齐字典数据）
SELECT @platform_1        AS platform_1_id,
       @platform_2        AS platform_2_id,
       @platform_3        AS platform_3_id,
       @station_operating AS station_operating_id,
       @pile_operating    AS pile_operating_id;

-- 目标表自检：两条计数应均为 0，否则说明尚未执行 20260909_cleanup_test_data.sql
SELECT (SELECT COUNT(*) FROM biz_station) AS station_exists,
       (SELECT COUNT(*) FROM biz_pile)    AS pile_exists;

START TRANSACTION;

-- ----------------------------------------------------------------------------
-- 2. 写入充电站（83 个）
--    platform_item_id / station_status_item_id 引用第 1 节解析的字典变量；
--    platform_station_code / address / contact_name / contact_phone 统一置 NULL。
-- ----------------------------------------------------------------------------
INSERT INTO biz_station
    (station_code, station_name, platform_item_id, platform_station_code,
     address, contact_name, contact_phone, meter_account_number, station_status_item_id)
VALUES
('ST-202609-0001', '华自浏阳官渡镇冯绪斌祥和酒店充电桩', @platform_1, NULL, NULL, NULL, NULL, '4350010084047', @station_operating),
('ST-202609-0002', '华自浏阳古港镇银杏便民充电站', @platform_1, NULL, NULL, NULL, NULL, '4350010096374', @station_operating),
('ST-202609-0003', '华自浏阳金刚镇集镇便民充电桩', @platform_1, NULL, NULL, NULL, NULL, '4350010023318', @station_operating),
('ST-202609-0004', '华自浏阳金刚镇星星村便民充电桩', @platform_1, NULL, NULL, NULL, NULL, '4350010022586', @station_operating),
('ST-202609-0005', '华自浏阳洞阳镇长东聚运新材充电站', @platform_1, NULL, NULL, NULL, NULL, '1217559034', @station_operating),
('ST-202609-0006', '华自充电浏阳大瑶花炮大道交流', @platform_1, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0007', '华自浏阳官渡书香村便民充电桩', @platform_1, NULL, NULL, NULL, NULL, '4350010116557', @station_operating),
('ST-202609-0008', '华自充电浏阳万象城交流', @platform_1, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0009', '华自充电浏阳集里广虹新能源便民充电站', @platform_1, NULL, NULL, NULL, NULL, '4350009444245', @station_operating),
('ST-202609-0010', '华自浏阳金刚镇金市村便民充电站', @platform_1, NULL, NULL, NULL, NULL, '4350009290641', @station_operating),
('ST-202609-0011', '华自浏阳荷花街道重卡充电站项目', @platform_1, NULL, NULL, NULL, NULL, '4350004436484', @station_operating),
('ST-202609-0012', '华自浏阳大瑶镇花炮大道物流园充电站项目', @platform_1, NULL, NULL, NULL, NULL, '4350002973758', @station_operating),
('ST-202609-0013', '华自永和镇铁山村委便民充电站', @platform_1, NULL, NULL, NULL, NULL, '4350003565454', @station_operating),
('ST-202609-0014', '华自浏阳普迹镇便民充电站', @platform_1, NULL, NULL, NULL, NULL, '4350002432832', @station_operating),
('ST-202609-0015', '华自浏阳枨冲镇便民充电站', @platform_1, NULL, NULL, NULL, NULL, '4350002791933', @station_operating),
('ST-202609-0016', '华自浏阳永和镇永鑫社区便民充电站', @platform_1, NULL, NULL, NULL, NULL, '4350002793696', @station_operating),
('ST-202609-0017', '华自充电浏阳禧和', @platform_1, NULL, NULL, NULL, NULL, '4303130508936', @station_operating),
('ST-202609-0018', '华自充电浏阳西站', @platform_1, NULL, NULL, NULL, NULL, '4303128818537', @station_operating),
('ST-202609-0019', '华自充电浏阳达浒镇站', @platform_1, NULL, NULL, NULL, NULL, '4350001823741', @station_operating),
('ST-202609-0020', '华自充电浏阳健康大道万象城站', @platform_1, NULL, NULL, NULL, NULL, '4350001297476', @station_operating),
('ST-202609-0021', '华自充电浏阳官桥镇站', @platform_1, NULL, NULL, NULL, NULL, '4350001005051', @station_operating),
('ST-202609-0022', '华自充电浏阳北盛镇二站', @platform_1, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0023', '华自充电浏阳北盛镇站', @platform_1, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0024', '华自充电浏阳集里派出所站', @platform_1, NULL, NULL, NULL, NULL, '4350000564093', @station_operating),
('ST-202609-0025', '华自充电浏阳永和镇站', @platform_1, NULL, NULL, NULL, NULL, '4350000868808', @station_operating),
('ST-202609-0026', '华自充电浏阳荷花街道站', @platform_1, NULL, NULL, NULL, NULL, '4350000571581', @station_operating),
('ST-202609-0027', '华自充电浏阳葛家镇站', @platform_1, NULL, NULL, NULL, NULL, '4350000468292', @station_operating),
('ST-202609-0028', '华自充电浏阳泰天站', @platform_1, NULL, NULL, NULL, NULL, '4303131199357', @station_operating),
('ST-202609-0029', '众电浏阳金刚镇李畋社区便民充电站', @platform_2, NULL, NULL, NULL, NULL, '4350010067635', @station_operating),
('ST-202609-0030', '众电浏阳杨潭堂兴烟花便民充电桩', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0031', '众电浏阳金滩繁星烟花民宿充电桩', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0032', '众电浏阳十里金滩烟花民宿充电站', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0033', '众电能源浏阳市大瑶李畋包装便民充电站', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0034', '众电能源浏阳市金刚镇平湾村充电桩', @platform_2, NULL, NULL, NULL, NULL, '4350009581507', @station_operating),
('ST-202609-0035', '众电能源沿溪喜来乐便民充电桩', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0036', '众电浏阳长沙神弧便民充电桩', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0037', '众电长兴湖学校充电站', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0038', '众电浏阳泊雅酒店便民充', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0039', '众电浏阳车海装饰便民充', @platform_2, NULL, NULL, NULL, NULL, '4350009495754', @station_operating),
('ST-202609-0040', '众电浏阳湘龙烟花便民充', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0041', '众电小河清溪山房民宿便民充', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0042', '众电浏阳江铃福特便民充', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0043', '众电浏阳永和镇铁山村便民充', @platform_2, NULL, NULL, NULL, NULL, '4350003734654', @station_operating),
('ST-202609-0044', '众电浏阳笑哈哈食品厂便民充', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0045', '众电永和铁山村便民充电桩', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0046', '众电金刚镇唐人鞭炮便民充电桩', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0047', '众电大瑶汇丰社区便民充', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0048', '众电古港范市村充电站', @platform_2, NULL, NULL, NULL, NULL, '4350001640497', @station_operating),
('ST-202609-0049', '众电浏阳曼与蔓美学庄园便民充电桩', @platform_2, NULL, NULL, NULL, NULL, '4350002843414', @station_operating),
('ST-202609-0050', '众电浏阳绿山墙名宿充电站', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0051', '众电浏阳葛家镇政府便民充电站', @platform_2, NULL, NULL, NULL, NULL, '4350000404250', @station_operating),
('ST-202609-0052', '众电浏阳永安轨道交通学院充电桩', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0053', '众电浏阳禧园小区充电站', @platform_2, NULL, NULL, NULL, NULL, '4350002274345', @station_operating),
('ST-202609-0054', '众电浏阳永安镇人民政府充电站二', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0055', '众电澄潭江龙凤山庄充电站', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0056', '众电浏宿栖溪俚便民充电站', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0057', '众电陌野半山渡隐竹民宿充电站', @platform_2, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0058', '众电浏阳东沙大温民宿充电站', @platform_3, NULL, NULL, NULL, NULL, '4350009810214', @station_operating),
('ST-202609-0059', '众电浏阳亚朵酒店充电站', @platform_3, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0060', '众电浏阳古港中澳国防公园充电站', @platform_3, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0061', '众电浏阳官渡镇田螺小镇便民充电桩', @platform_3, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0062', '众电浏阳市古港新城充电站', @platform_3, NULL, NULL, NULL, NULL, '4350009989889', @station_operating),
('ST-202609-0063', '众电浏阳张坊听竹苑民宿充电桩', @platform_3, NULL, NULL, NULL, NULL, '4350009876183', @station_operating),
('ST-202609-0064', '众电浏阳张坊镇政府便民充电站', @platform_3, NULL, NULL, NULL, NULL, '4350010319304', @station_operating),
('ST-202609-0065', '众电浏阳淮川办事处充电站', @platform_3, NULL, NULL, NULL, NULL, '4350009199472', @station_operating),
('ST-202609-0066', '众电浏阳淮川城东社区便民充电站', @platform_3, NULL, NULL, NULL, NULL, '4350009324410', @station_operating),
('ST-202609-0067', '众电浏阳荷花G106国道纸立方便民充电桩', @platform_3, NULL, NULL, NULL, NULL, '4350010094413', @station_operating),
('ST-202609-0068', '众电浏阳荷花深森机械便民充电桩', @platform_3, NULL, NULL, NULL, NULL, '4350010192899', @station_operating),
('ST-202609-0069', '众电浏阳荷花碧景湾小区便民充电桩', @platform_3, NULL, NULL, NULL, NULL, '4350009847666', @station_operating),
('ST-202609-0070', '众电浏阳金刚镇政府便民充电桩', @platform_3, NULL, NULL, NULL, NULL, '4350010196592', @station_operating),
('ST-202609-0071', '众电浏阳集里双拥路全季酒店便民充电桩项目', @platform_3, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0072', '众电浏阳高坪镇太坪村充电站', @platform_3, NULL, NULL, NULL, NULL, '4350010203295', @station_operating),
('ST-202609-0073', '众电邵阳大祥区璟禹城兮美酒店充电桩项目', @platform_3, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0074', '众电浏阳洞阳陶然溪小区充电桩', @platform_3, NULL, NULL, NULL, NULL, '4350010553184', @station_operating),
('ST-202609-0075', '众电浏阳张坊镇杨林广场便民充电桩', @platform_3, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0076', '众电浏阳达浒镇石田山居便民充电桩', @platform_3, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0077', '众电长沙县高桥镇政府便民充电桩', @platform_3, NULL, NULL, NULL, NULL, '4303132841578', @station_operating),
('ST-202609-0078', '众电长沙县高桥白鹭湖便民充电桩', @platform_3, NULL, NULL, NULL, NULL, '4303132800630', @station_operating),
('ST-202609-0079', '众电长沙县金井镇政府便民充电桩', @platform_3, NULL, NULL, NULL, NULL, '4303133379728', @station_operating),
('ST-202609-0080', '众电长沙县金井镇石壁湖公园便民充电桩', @platform_3, NULL, NULL, NULL, NULL, '4303132792876', @station_operating),
('ST-202609-0081', '众电浏阳集里街道办事处便民充电桩', @platform_3, NULL, NULL, NULL, NULL, NULL, @station_operating),
('ST-202609-0082', '华自充电浏阳镇头集镇石小明充电桩', @platform_1, NULL, NULL, NULL, NULL, '4350010184800', @station_operating),
('ST-202609-0083', '华自充电浏阳集里奇瑞风云钰龙汽车站', @platform_1, NULL, NULL, NULL, NULL, '4350009589704', @station_operating);

-- ----------------------------------------------------------------------------
-- 3. 写入充电桩（232 台）
--    station_id 通过全局唯一的 station_code 反查，不依赖自增 ID 的物理值。
-- ----------------------------------------------------------------------------
INSERT INTO biz_pile
    (pile_code, station_id, charge_type, gun_count, pile_status_item_id)
VALUES
('PL-202609-0001', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0001'), 'FAST', 2, @pile_operating),
('PL-202609-0002', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0002'), 'FAST', 2, @pile_operating),
('PL-202609-0003', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0003'), 'FAST', 2, @pile_operating),
('PL-202609-0004', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0004'), 'FAST', 2, @pile_operating),
('PL-202609-0005', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0005'), 'FAST', 2, @pile_operating),
('PL-202609-0006', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0005'), 'FAST', 2, @pile_operating),
('PL-202609-0007', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0005'), 'FAST', 2, @pile_operating),
('PL-202609-0008', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0005'), 'FAST', 2, @pile_operating),
('PL-202609-0009', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0005'), 'FAST', 2, @pile_operating),
('PL-202609-0010', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0006'), 'SLOW', 1, @pile_operating),
('PL-202609-0011', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0007'), 'FAST', 2, @pile_operating),
('PL-202609-0012', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0008'), 'SLOW', 1, @pile_operating),
('PL-202609-0013', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0008'), 'SLOW', 1, @pile_operating),
('PL-202609-0014', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0009'), 'FAST', 2, @pile_operating),
('PL-202609-0015', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0010'), 'FAST', 2, @pile_operating),
('PL-202609-0016', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0017', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0018', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0019', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0020', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0021', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0022', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0023', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0024', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0025', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0026', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0027', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0011'), 'FAST', 2, @pile_operating),
('PL-202609-0028', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0029', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0030', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0031', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0032', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0033', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0034', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0035', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0036', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0037', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0038', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0039', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0012'), 'FAST', 2, @pile_operating),
('PL-202609-0040', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0013'), 'FAST', 2, @pile_operating),
('PL-202609-0041', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0013'), 'FAST', 2, @pile_operating),
('PL-202609-0042', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0014'), 'FAST', 2, @pile_operating),
('PL-202609-0043', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0015'), 'FAST', 2, @pile_operating),
('PL-202609-0044', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0015'), 'FAST', 2, @pile_operating),
('PL-202609-0045', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0016'), 'FAST', 2, @pile_operating),
('PL-202609-0046', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0016'), 'FAST', 2, @pile_operating),
('PL-202609-0047', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0048', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0049', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0050', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0051', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0052', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0053', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0054', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0055', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0056', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0057', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0058', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0059', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0060', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0017'), 'FAST', 2, @pile_operating),
('PL-202609-0061', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0018'), 'FAST', 2, @pile_operating),
('PL-202609-0062', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0018'), 'FAST', 2, @pile_operating),
('PL-202609-0063', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0018'), 'FAST', 2, @pile_operating),
('PL-202609-0064', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0018'), 'FAST', 2, @pile_operating),
('PL-202609-0065', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0018'), 'FAST', 2, @pile_operating),
('PL-202609-0066', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0018'), 'FAST', 2, @pile_operating),
('PL-202609-0067', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0018'), 'FAST', 2, @pile_operating),
('PL-202609-0068', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0018'), 'FAST', 2, @pile_operating),
('PL-202609-0069', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0018'), 'FAST', 2, @pile_operating),
('PL-202609-0070', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0018'), 'FAST', 2, @pile_operating),
('PL-202609-0071', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0019'), 'FAST', 2, @pile_operating),
('PL-202609-0072', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0019'), 'FAST', 2, @pile_operating),
('PL-202609-0073', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'FAST', 2, @pile_operating),
('PL-202609-0074', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'FAST', 2, @pile_operating),
('PL-202609-0075', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'FAST', 2, @pile_operating),
('PL-202609-0076', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'FAST', 2, @pile_operating),
('PL-202609-0077', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'FAST', 2, @pile_operating),
('PL-202609-0078', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'FAST', 2, @pile_operating),
('PL-202609-0079', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'FAST', 2, @pile_operating),
('PL-202609-0080', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'FAST', 2, @pile_operating),
('PL-202609-0081', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'FAST', 2, @pile_operating),
('PL-202609-0082', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'FAST', 2, @pile_operating),
('PL-202609-0083', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'FAST', 2, @pile_operating),
('PL-202609-0084', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'SLOW', 1, @pile_operating),
('PL-202609-0085', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0020'), 'SLOW', 1, @pile_operating),
('PL-202609-0086', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0021'), 'FAST', 2, @pile_operating),
('PL-202609-0087', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0021'), 'FAST', 2, @pile_operating),
('PL-202609-0088', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0024'), 'FAST', 2, @pile_operating),
('PL-202609-0089', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0025'), 'FAST', 2, @pile_operating),
('PL-202609-0090', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0025'), 'FAST', 2, @pile_operating),
('PL-202609-0091', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0026'), 'FAST', 2, @pile_operating),
('PL-202609-0092', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0027'), 'FAST', 2, @pile_operating),
('PL-202609-0093', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0028'), 'FAST', 2, @pile_operating),
('PL-202609-0094', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0028'), 'FAST', 2, @pile_operating),
('PL-202609-0095', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0028'), 'FAST', 2, @pile_operating),
('PL-202609-0096', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0028'), 'FAST', 2, @pile_operating),
('PL-202609-0097', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0028'), 'FAST', 2, @pile_operating),
('PL-202609-0098', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0028'), 'FAST', 2, @pile_operating),
('PL-202609-0099', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0028'), 'FAST', 2, @pile_operating),
('PL-202609-0100', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0028'), 'FAST', 2, @pile_operating),
('PL-202609-0101', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0028'), 'FAST', 2, @pile_operating),
('PL-202609-0102', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0029'), 'FAST', 2, @pile_operating),
('PL-202609-0103', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0030'), 'FAST', 2, @pile_operating),
('PL-202609-0104', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0031'), 'SLOW', 1, @pile_operating),
('PL-202609-0105', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0032'), 'SLOW', 1, @pile_operating),
('PL-202609-0106', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0033'), 'FAST', 2, @pile_operating),
('PL-202609-0107', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0033'), 'SLOW', 1, @pile_operating),
('PL-202609-0108', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0034'), 'FAST', 2, @pile_operating),
('PL-202609-0109', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0034'), 'FAST', 2, @pile_operating),
('PL-202609-0110', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0035'), 'SLOW', 1, @pile_operating),
('PL-202609-0111', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0036'), 'SLOW', 1, @pile_operating),
('PL-202609-0112', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0037'), 'SLOW', 1, @pile_operating),
('PL-202609-0113', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0037'), 'SLOW', 1, @pile_operating),
('PL-202609-0114', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0038'), 'SLOW', 1, @pile_operating),
('PL-202609-0115', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0038'), 'SLOW', 1, @pile_operating),
('PL-202609-0116', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0039'), 'SLOW', 1, @pile_operating),
('PL-202609-0117', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0040'), 'SLOW', 1, @pile_operating),
('PL-202609-0118', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0041'), 'SLOW', 1, @pile_operating),
('PL-202609-0119', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0041'), 'SLOW', 1, @pile_operating),
('PL-202609-0120', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0042'), 'SLOW', 1, @pile_operating),
('PL-202609-0121', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0043'), 'SLOW', 1, @pile_operating),
('PL-202609-0122', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0043'), 'SLOW', 1, @pile_operating),
('PL-202609-0123', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0044'), 'SLOW', 1, @pile_operating),
('PL-202609-0124', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0044'), 'SLOW', 1, @pile_operating),
('PL-202609-0125', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0045'), 'SLOW', 1, @pile_operating),
('PL-202609-0126', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0046'), 'SLOW', 1, @pile_operating),
('PL-202609-0127', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0046'), 'SLOW', 1, @pile_operating),
('PL-202609-0128', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0047'), 'SLOW', 1, @pile_operating),
('PL-202609-0129', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0047'), 'SLOW', 1, @pile_operating),
('PL-202609-0130', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0048'), 'SLOW', 1, @pile_operating),
('PL-202609-0131', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0048'), 'SLOW', 1, @pile_operating),
('PL-202609-0132', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0049'), 'SLOW', 1, @pile_operating),
('PL-202609-0133', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0049'), 'SLOW', 1, @pile_operating),
('PL-202609-0134', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0050'), 'SLOW', 1, @pile_operating),
('PL-202609-0135', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0050'), 'SLOW', 1, @pile_operating),
('PL-202609-0136', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0051'), 'SLOW', 1, @pile_operating),
('PL-202609-0137', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0051'), 'SLOW', 1, @pile_operating),
('PL-202609-0138', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0052'), 'FAST', 2, @pile_operating),
('PL-202609-0139', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0053'), 'FAST', 1, @pile_operating),
('PL-202609-0140', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0054'), 'SLOW', 1, @pile_operating),
('PL-202609-0141', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0054'), 'SLOW', 1, @pile_operating),
('PL-202609-0142', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0054'), 'SLOW', 1, @pile_operating),
('PL-202609-0143', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0054'), 'SLOW', 1, @pile_operating),
('PL-202609-0144', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0054'), 'SLOW', 1, @pile_operating),
('PL-202609-0145', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0054'), 'SLOW', 1, @pile_operating),
('PL-202609-0146', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0054'), 'SLOW', 1, @pile_operating),
('PL-202609-0147', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0054'), 'SLOW', 1, @pile_operating),
('PL-202609-0148', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0054'), 'SLOW', 1, @pile_operating),
('PL-202609-0149', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0054'), 'SLOW', 1, @pile_operating),
('PL-202609-0150', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0055'), 'SLOW', 1, @pile_operating),
('PL-202609-0151', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0055'), 'SLOW', 1, @pile_operating),
('PL-202609-0152', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0056'), 'SLOW', 1, @pile_operating),
('PL-202609-0153', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0057'), 'SLOW', 1, @pile_operating),
('PL-202609-0154', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0057'), 'SLOW', 1, @pile_operating),
('PL-202609-0155', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0058'), 'FAST', 2, @pile_operating),
('PL-202609-0156', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0058'), 'FAST', 2, @pile_operating),
('PL-202609-0157', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0058'), 'SLOW', 1, @pile_operating),
('PL-202609-0158', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0058'), 'SLOW', 1, @pile_operating),
('PL-202609-0159', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0058'), 'SLOW', 1, @pile_operating),
('PL-202609-0160', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0058'), 'SLOW', 1, @pile_operating),
('PL-202609-0161', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0058'), 'SLOW', 1, @pile_operating),
('PL-202609-0162', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0058'), 'SLOW', 1, @pile_operating),
('PL-202609-0163', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0059'), 'SLOW', 1, @pile_operating),
('PL-202609-0164', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0059'), 'SLOW', 1, @pile_operating),
('PL-202609-0165', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0059'), 'SLOW', 1, @pile_operating),
('PL-202609-0166', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0059'), 'SLOW', 1, @pile_operating),
('PL-202609-0167', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0059'), 'SLOW', 1, @pile_operating),
('PL-202609-0168', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0059'), 'SLOW', 1, @pile_operating),
('PL-202609-0169', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0060'), 'FAST', 2, @pile_operating),
('PL-202609-0170', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0060'), 'FAST', 2, @pile_operating),
('PL-202609-0171', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0061'), 'FAST', 1, @pile_operating),
('PL-202609-0172', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0062'), 'FAST', 2, @pile_operating),
('PL-202609-0173', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0063'), 'SLOW', 1, @pile_operating),
('PL-202609-0174', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0063'), 'SLOW', 1, @pile_operating),
('PL-202609-0175', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0064'), 'FAST', 2, @pile_operating),
('PL-202609-0176', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0064'), 'FAST', 2, @pile_operating),
('PL-202609-0177', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0064'), 'SLOW', 1, @pile_operating),
('PL-202609-0178', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0064'), 'SLOW', 1, @pile_operating),
('PL-202609-0179', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0065'), 'FAST', 2, @pile_operating),
('PL-202609-0180', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0065'), 'FAST', 2, @pile_operating),
('PL-202609-0181', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0065'), 'FAST', 2, @pile_operating),
('PL-202609-0182', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0065'), 'FAST', 2, @pile_operating),
('PL-202609-0183', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0066'), 'FAST', 2, @pile_operating),
('PL-202609-0184', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0066'), 'FAST', 2, @pile_operating),
('PL-202609-0185', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0066'), 'FAST', 2, @pile_operating),
('PL-202609-0186', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0066'), 'FAST', 2, @pile_operating),
('PL-202609-0187', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0067'), 'FAST', 2, @pile_operating),
('PL-202609-0188', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0068'), 'FAST', 2, @pile_operating),
('PL-202609-0189', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0069'), 'SLOW', 1, @pile_operating),
('PL-202609-0190', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0069'), 'SLOW', 1, @pile_operating),
('PL-202609-0191', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0069'), 'SLOW', 1, @pile_operating),
('PL-202609-0192', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0069'), 'SLOW', 1, @pile_operating),
('PL-202609-0193', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0069'), 'SLOW', 1, @pile_operating),
('PL-202609-0194', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0069'), 'SLOW', 1, @pile_operating),
('PL-202609-0195', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0070'), 'FAST', 2, @pile_operating),
('PL-202609-0196', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0070'), 'SLOW', 1, @pile_operating),
('PL-202609-0197', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0070'), 'SLOW', 1, @pile_operating),
('PL-202609-0198', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0070'), 'SLOW', 1, @pile_operating),
('PL-202609-0199', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0071'), 'FAST', 2, @pile_operating),
('PL-202609-0200', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0071'), 'FAST', 2, @pile_operating),
('PL-202609-0201', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0072'), 'FAST', 2, @pile_operating),
('PL-202609-0202', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0073'), 'FAST', 2, @pile_operating),
('PL-202609-0203', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0073'), 'SLOW', 1, @pile_operating),
('PL-202609-0204', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0073'), 'SLOW', 1, @pile_operating),
('PL-202609-0205', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0074'), 'SLOW', 1, @pile_operating),
('PL-202609-0206', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0074'), 'SLOW', 1, @pile_operating),
('PL-202609-0207', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0074'), 'SLOW', 1, @pile_operating),
('PL-202609-0208', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0074'), 'SLOW', 1, @pile_operating),
('PL-202609-0209', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0074'), 'SLOW', 1, @pile_operating),
('PL-202609-0210', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0074'), 'SLOW', 1, @pile_operating),
('PL-202609-0211', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0075'), 'FAST', 2, @pile_operating),
('PL-202609-0212', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0075'), 'FAST', 2, @pile_operating),
('PL-202609-0213', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0076'), 'SLOW', 1, @pile_operating),
('PL-202609-0214', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0077'), 'FAST', 2, @pile_operating),
('PL-202609-0215', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0077'), 'SLOW', 1, @pile_operating),
('PL-202609-0216', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0077'), 'SLOW', 1, @pile_operating),
('PL-202609-0217', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0077'), 'SLOW', 1, @pile_operating),
('PL-202609-0218', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0077'), 'SLOW', 1, @pile_operating),
('PL-202609-0219', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0077'), 'SLOW', 1, @pile_operating),
('PL-202609-0220', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0078'), 'FAST', 2, @pile_operating),
('PL-202609-0221', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0078'), 'SLOW', 1, @pile_operating),
('PL-202609-0222', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0078'), 'SLOW', 1, @pile_operating),
('PL-202609-0223', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0079'), 'FAST', 2, @pile_operating),
('PL-202609-0224', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0080'), 'FAST', 2, @pile_operating),
('PL-202609-0225', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0080'), 'SLOW', 1, @pile_operating),
('PL-202609-0226', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0080'), 'SLOW', 1, @pile_operating),
('PL-202609-0227', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0081'), 'FAST', 2, @pile_operating),
('PL-202609-0228', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0081'), 'FAST', 2, @pile_operating),
('PL-202609-0229', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0081'), 'SLOW', 1, @pile_operating),
('PL-202609-0230', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0081'), 'SLOW', 1, @pile_operating),
('PL-202609-0231', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0082'), 'FAST', 2, @pile_operating),
('PL-202609-0232', (SELECT id FROM biz_station WHERE station_code = 'ST-202609-0083'), 'FAST', 2, @pile_operating);

COMMIT;

-- ----------------------------------------------------------------------------
-- 4. 初始化结果核验（只读查询，供人工确认）
-- ----------------------------------------------------------------------------

-- 4.1 总量：充电站应为 83，充电桩应为 232
SELECT COUNT(*) AS station_count FROM biz_station;
SELECT COUNT(*) AS pile_count FROM biz_pile;

-- 4.2 平台分布：华自 / 云快充 / 小桔
SELECT di.item_name AS platform_name, COUNT(*) AS station_count
FROM biz_station s
JOIN sys_dict_item di ON di.id = s.platform_item_id
GROUP BY di.item_name
ORDER BY station_count DESC;

-- 4.3 快慢桩分布：快充 136 台 / 270 枪，慢充 96 台 / 96 枪
SELECT p.charge_type, COUNT(*) AS pile_count, SUM(p.gun_count) AS gun_count
FROM biz_pile p
GROUP BY p.charge_type;

-- 4.4 无桩充电站：预期 2 个（华自充电浏阳北盛镇二站、华自充电浏阳北盛镇站）
SELECT s.station_code, s.station_name
FROM biz_station s
LEFT JOIN biz_pile p ON p.station_id = s.id
WHERE p.id IS NULL;

-- 4.5 电表户号：非空应为 50 个，其余为 NULL
SELECT COUNT(*) AS meter_filled_count
FROM biz_station
WHERE meter_account_number IS NOT NULL;