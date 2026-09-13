-- ========================================
-- Smart Medical 业务流程测试数据
-- 生成时间: 2026-07-21
-- 最后更新: 2026-09-13
-- 目的: 补全P0级核心业务流程数据
-- 说明: 本文件包含业务流程数据（排班/挂号/病历/处方等）
--       基础配置数据（角色/用户/科室/医生/药品）在CreateTable.sql中
--       关联方向: registration.order_id -> order.id（order 表无 registration_id 列）
-- ========================================

-- 统一时区：容器 TZ 与后端均强制 Asia/Shanghai，避免手工导入时 CURDATE() 跨日
SET time_zone = '+08:00';
START TRANSACTION;

-- ========================================
-- 0. 幂等清理（使本文件可重复执行）
-- 说明: 只按主键区间删除「本文件拥有」的数据（2043000000000001000 ~ 2043000000000019999），
--       不按业务键删除，避免误伤 CreateTable.sql 的基础数据与运行期雪花 id。
--       区间下界已排除 CreateTable.sql 中的 2040599544698343424。
-- ========================================
DELETE FROM registration_status_log        WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM prescription_item              WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM prescription                   WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM medical_record                 WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM order_status_log               WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM order_item                     WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM payment_record                 WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM `order`                        WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM registration                   WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM consultation_room_status       WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM chat                           WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM registration_schedule          WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM registration_schedule_template WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;
DELETE FROM inventory_transaction          WHERE id BETWEEN 2043000000000001000 AND 2043000000000019999;

-- ========================================
-- 1. 李娜医生排班数据（doctor_id=2）
-- ========================================

-- 清空张伟医生的历史排班数据
DELETE FROM registration_schedule WHERE registration_schedule_template_id IN (
    SELECT id FROM registration_schedule_template WHERE doctor_id = 1
);
DELETE FROM registration_schedule_template WHERE doctor_id = 1;

-- 插入李娜医生的排班模板（未来7天）
INSERT INTO registration_schedule_template VALUES
-- 2026-07-22 上午
(2043000000000001001, '李娜医生普通门诊', 2, '2026-07-22', 0, '09:00:00', '12:00:00', 40, 1000, 1, 1, 1, '心内科门诊', NOW(), NOW(), 0, NULL),
-- 2026-07-22 下午
(2043000000000001002, '李娜医生普通门诊', 2, '2026-07-22', 0, '14:00:00', '17:00:00', 40, 1000, 1, 1, 2, '心内科门诊', NOW(), NOW(), 0, NULL),
-- 2026-07-23 上午
(2043000000000001003, '李娜医生普通门诊', 2, '2026-07-23', 0, '09:00:00', '12:00:00', 40, 1000, 1, 1, 1, '心内科门诊', NOW(), NOW(), 0, NULL),
-- 2026-07-23 下午
(2043000000000001004, '李娜医生普通门诊', 2, '2026-07-23', 0, '14:00:00', '17:00:00', 40, 1000, 1, 1, 2, '心内科门诊', NOW(), NOW(), 0, NULL),
-- 2026-07-24 上午
(2043000000000001005, '李娜医生普通门诊', 2, '2026-07-24', 0, '09:00:00', '12:00:00', 40, 1000, 1, 1, 1, '心内科门诊', NOW(), NOW(), 0, NULL);

-- 插入李娜医生的具体时段（每小时一个时段）
-- remaining_quota = 剩余号源（RegistrationScheduleServiceImpl#deductRemainingQuota 下单时 -1，releaseQuota 取消时 +1）
-- 已被挂号占用的时段（2001/2002/2003/2004/2007）余量已扣减为 12；2010 对应挂号 3005 已取消、号源已释放，故仍为 13
INSERT INTO registration_schedule VALUES
-- 2026-07-22 上午 3小时（09-10, 10-11, 11-12）
(2043000000000002001, 2, 2043000000000001001, '2026-07-22 09:00:00', '2026-07-22 10:00:00', 1, 12, NOW(), NOW(), 0),
(2043000000000002002, 2, 2043000000000001001, '2026-07-22 10:00:00', '2026-07-22 11:00:00', 1, 12, NOW(), NOW(), 0),
(2043000000000002003, 2, 2043000000000001001, '2026-07-22 11:00:00', '2026-07-22 12:00:00', 1, 12, NOW(), NOW(), 0),
-- 2026-07-22 下午 3小时（14-15, 15-16, 16-17）
(2043000000000002004, 2, 2043000000000001002, '2026-07-22 14:00:00', '2026-07-22 15:00:00', 1, 12, NOW(), NOW(), 0),
(2043000000000002005, 2, 2043000000000001002, '2026-07-22 15:00:00', '2026-07-22 16:00:00', 1, 13, NOW(), NOW(), 0),
(2043000000000002006, 2, 2043000000000001002, '2026-07-22 16:00:00', '2026-07-22 17:00:00', 1, 14, NOW(), NOW(), 0),
-- 2026-07-23 上午 3小时
(2043000000000002007, 2, 2043000000000001003, '2026-07-23 09:00:00', '2026-07-23 10:00:00', 1, 12, NOW(), NOW(), 0),
(2043000000000002008, 2, 2043000000000001003, '2026-07-23 10:00:00', '2026-07-23 11:00:00', 1, 13, NOW(), NOW(), 0),
(2043000000000002009, 2, 2043000000000001003, '2026-07-23 11:00:00', '2026-07-23 12:00:00', 1, 14, NOW(), NOW(), 0),
-- 2026-07-23 下午 3小时
(2043000000000002010, 2, 2043000000000001004, '2026-07-23 14:00:00', '2026-07-23 15:00:00', 1, 13, NOW(), NOW(), 0),
(2043000000000002011, 2, 2043000000000001004, '2026-07-23 15:00:00', '2026-07-23 16:00:00', 1, 13, NOW(), NOW(), 0),
(2043000000000002012, 2, 2043000000000001004, '2026-07-23 16:00:00', '2026-07-23 17:00:00', 1, 14, NOW(), NOW(), 0),
-- 2026-07-24 上午 3小时
(2043000000000002013, 2, 2043000000000001005, '2026-07-24 09:00:00', '2026-07-24 10:00:00', 1, 13, NOW(), NOW(), 0),
(2043000000000002014, 2, 2043000000000001005, '2026-07-24 10:00:00', '2026-07-24 11:00:00', 1, 13, NOW(), NOW(), 0),
(2043000000000002015, 2, 2043000000000001005, '2026-07-24 11:00:00', '2026-07-24 12:00:00', 1, 14, NOW(), NOW(), 0);

-- ========================================
-- 2. 挂号记录数据（registration）
-- 说明: registration_time = 挂号下单时刻（RegistrationManager#create 写 now()），与 order_create_time 同源；
--       须满足 registration_time <= check_in_time <= visit_start_time <= visit_end_time
-- ========================================

-- 等待支付状态（user_id=1 张三，挂 2026-07-22 上午李娜医生的号）
INSERT INTO registration VALUES
(2043000000000003001, 1, 2043000000000002001, NULL, 0, '2026-07-22 08:50:00', NULL, NULL, NULL, NOW(), NOW(), 0);

-- 已报到状态（user_id=2 李四，挂 2026-07-22 上午李娜医生的号，已报到）
INSERT INTO registration VALUES
(2043000000000003002, 2, 2043000000000002002, NULL, 5, '2026-07-22 09:00:00', '2026-07-22 09:55:00', NULL, NULL, NOW(), NOW(), 0);

-- 就诊中状态（user_id=3 王五，挂 2026-07-22 下午李娜医生的号，正在就诊）
-- 演示合成态：3003 保留「就诊中」样例，故不回退为 4（完成）；与已提交病历 4002 / 处方 5002 并存属有意构造
INSERT INTO registration VALUES
(2043000000000003003, 3, 2043000000000002004, NULL, 6, '2026-07-22 13:50:00', '2026-07-22 13:58:00', '2026-07-22 14:00:00', NULL, NOW(), NOW(), 0);

-- 已就诊状态（user_id=4 赵六，挂 2026-07-23 的号，status=4 完成）
INSERT INTO registration VALUES
(2043000000000003004, 4, 2043000000000002007, NULL, 4, '2026-07-23 08:40:00', '2026-07-23 08:50:00', '2026-07-23 09:00:00', '2026-07-23 09:30:00', NOW(), NOW(), 0);

-- 已取消状态（user_id=5 钱七，取消挂号）
INSERT INTO registration VALUES
(2043000000000003005, 5, 2043000000000002010, NULL, 3, '2026-07-23 13:30:00', NULL, NULL, NULL, NOW(), NOW(), 0);

-- 待就诊状态（user_id=6 孙八，挂 2026-07-22 上午李娜医生 11:00 的号，已支付待报到）
-- 用途: 覆盖 status=1 场景 —— 用户端「待就诊」列表、扫码报到（RegistrationCheckInManager 要求 status=1 且模板日期=今天）、
--       以及退号退款链路（validateCancelStatus 仅允许 0/1 取消）。对应订单 10008 已支付。
INSERT INTO registration VALUES
(2043000000000003006, 6, 2043000000000002003, NULL, 1, '2026-07-22 10:30:00', NULL, NULL, NULL, NOW(), NOW(), 0);

-- ========================================
-- 3. 电子病历数据（medical_record）
-- 说明: patient_id 口径 = registration.user_id（即 user.id），非 patient 表主键，
--       见 MedicalRecordManager#draft: record.setPatientId(reg.getUserId())
-- ========================================

-- 为已就诊的挂号记录（3004）创建病历（patient_id=4 赵六）
INSERT INTO medical_record VALUES
(2043000000000004001, 2043000000000003004, 2, 4,
 '胸闷气短3天',
 '患者3天前无明显诱因出现胸闷、气短，活动后加重，休息后可缓解。无胸痛、心悸、晕厥。',
 '高血压病史5年', -- past_history
 '双肺呼吸音清，心率78次/分，律齐，各瓣膜听诊区未闻及病理性杂音', -- physical_exam
 '冠状动脉粥样硬化性心脏病',
 '1. 完善心电图、心脏彩超检查\n2. 给予抗血小板、扩冠治疗\n3. 低盐低脂饮食，适量运动',
 1, -- status: 已提交
 NOW(), NOW(), 0);

-- 为就诊中的挂号记录（3003）创建病历（部分填写，patient_id=3 王五）
-- 演示合成态：配合上方 3003「就诊中」场景保留
INSERT INTO medical_record VALUES
(2043000000000004002, 2043000000000003003, 2, 3,
 '心悸1周',
 '患者1周前出现心悸，夜间明显，无明显诱因。',
 NULL, -- past_history
 NULL, -- physical_exam
 '心律失常',
 '1. 完善24小时动态心电图\n2. 给予美托洛尔控制心率',
 1, -- status: 已提交
 NOW(), NOW(), 0);

-- ========================================
-- 4. 处方数据（prescription）
-- ========================================

-- 为已就诊的病历（4001）创建处方
INSERT INTO prescription VALUES
(2043000000000005001, 2043000000000004001, NULL, 1, -- id, medical_record_id, order_id, status(已支付)
 8000, -- total_amount 总金额80元（分）
 NULL, NULL, 0, -- pharmacist_id, dispensed_at, version
 '阿司匹林肠溶片100mg*30片 + 阿托伐他汀钙片20mg*7片', -- remark（规格对齐 drug 表）
 NOW(), NOW(), 0);

-- 为就诊中的病历（4002）创建处方（待支付）
INSERT INTO prescription VALUES
(2043000000000005002, 2043000000000004002, NULL, 0, -- id, medical_record_id, order_id, status(待支付)
 3500, -- total_amount 总金额35元（分）
 NULL, NULL, 0, -- pharmacist_id, dispensed_at, version
 '布洛芬缓释胶囊0.3g*20粒', -- remark（须与下方 prescription_item.drug_id=7 一致）
 NOW(), NOW(), 0);

-- ========================================
-- 5. 处方明细数据（prescription_item）
-- ========================================

-- 处方5001的药品明细
INSERT INTO prescription_item VALUES
(2043000000000006001, 2043000000000005001, 1, -- id, prescription_id, drug_id=1 阿司匹林
 1, 3000, -- quantity=1, unit_price=30元（分）
 '口服，每次100mg，每日1次',
 NOW(), NOW(), 0),
(2043000000000006002, 2043000000000005001, 2, -- id, prescription_id, drug_id=2 阿托伐他汀
 1, 5000, -- quantity=1, unit_price=50元（分）
 '口服，每次20mg，每晚1次',
 NOW(), NOW(), 0);

-- 处方5002的药品明细
INSERT INTO prescription_item VALUES
(2043000000000006003, 2043000000000005002, 7, -- id, prescription_id, drug_id=7 布洛芬缓释胶囊
1, 3500, -- quantity=1, unit_price=35元（分）
'口服，每次0.3g，每日2次',
NOW(), NOW(), 0);

-- ========================================
-- 6. 挂号状态变更日志（registration_status_log）
-- 说明: operator_id 为操作人 userId（RegistrationCheckInManager / RegistrationManager 均传当前 userId）；
--       id 按「挂号 id 升序、同挂号内按时间顺序」连续编号，保证 ORDER BY id 即为时间线
--       （运行期 id 为雪花递增，天然满足该性质）
--       完整链路含 0->1 支付成功（PaymentRecordManager 支付回调写日志）
-- ========================================

-- 挂号3001：创建（from_status NULL = 初始创建）
INSERT INTO registration_status_log VALUES
(2043000000000007101, 2043000000000003001, NULL, 0, 1, 'user', '挂号创建', NOW(), NOW(), 0);

-- 挂号3002：等待支付(0)→已支付(1)→已报到(5)
INSERT INTO registration_status_log VALUES
(2043000000000007102, 2043000000000003002, 0, 1, 2, 'user', '挂号支付成功', NOW(), NOW(), 0),
(2043000000000007103, 2043000000000003002, 1, 5, 2, 'user', '患者扫码报到', NOW(), NOW(), 0);

-- 挂号3003：等待支付(0)→已支付(1)→已报到(5)→就诊中(6)
INSERT INTO registration_status_log VALUES
(2043000000000007104, 2043000000000003003, 0, 1, 3, 'user', '挂号支付成功', NOW(), NOW(), 0),
(2043000000000007105, 2043000000000003003, 1, 5, 3, 'user', '患者扫码报到', NOW(), NOW(), 0),
(2043000000000007106, 2043000000000003003, 5, 6, 2, 'doctor', '医生开始接诊', NOW(), NOW(), 0);

-- 挂号3004：等待支付(0)→已支付(1)→已报到(5)→就诊中(6)→完成(4)
INSERT INTO registration_status_log VALUES
(2043000000000007107, 2043000000000003004, 0, 1, 4, 'user', '挂号支付成功', NOW(), NOW(), 0),
(2043000000000007108, 2043000000000003004, 1, 5, 4, 'user', '患者扫码报到', NOW(), NOW(), 0),
(2043000000000007109, 2043000000000003004, 5, 6, 2, 'doctor', '医生开始接诊', NOW(), NOW(), 0),
(2043000000000007110, 2043000000000003004, 6, 4, 2, 'doctor', '医生结束接诊', NOW(), NOW(), 0);

-- 挂号3005：等待支付(0)→已取消(3)
INSERT INTO registration_status_log VALUES
(2043000000000007111, 2043000000000003005, 0, 3, 5, 'user', '用户取消挂号', NOW(), NOW(), 0);

-- 挂号3006：等待支付(0)→已支付(1)（待就诊，等待用户扫码报到）
INSERT INTO registration_status_log VALUES
(2043000000000007112, 2043000000000003006, 0, 1, 6, 'user', '挂号支付成功', NOW(), NOW(), 0);

-- ========================================
-- 7. 诊室占用状态（consultation_room_status）
-- ========================================

-- 诊室1（专家诊室一）2026-07-22 上午被李娜医生占用
INSERT INTO consultation_room_status VALUES
(2043000000000008001, 1, '2026-07-22 09:00:00', '2026-07-22 12:00:00', '门诊接诊', NOW(), NOW(), 0);

-- 诊室2（专家诊室二）2026-07-22 下午被李娜医生占用
INSERT INTO consultation_room_status VALUES
(2043000000000008002, 2, '2026-07-22 14:00:00', '2026-07-22 17:00:00', '门诊接诊', NOW(), NOW(), 0);

-- ========================================
-- 8. 聊天消息（chat）
-- ========================================

-- 张三（user_id=1）与李娜医生（doctor_id=2）的咨询记录
INSERT INTO chat VALUES
(2043000000000009001, 1, 2, 0, 0, '医生您好，我想咨询心悸的问题', NOW(), NOW(), 0),
(2043000000000009002, 2, 1, 0, 0, '您好，心悸多久了？有没有伴随其他症状？', NOW(), NOW(), 0),
(2043000000000009003, 1, 2, 0, 0, '大概一周了，晚上比较明显', NOW(), NOW(), 0);

-- ========================================
-- 9. 数据验证查询
-- 注: 本段位于业务数据插入途中，仅作人工参考；权威校验请在本文件整体导入后执行
-- ========================================

-- 查询李娜医生的排班数据
SELECT
    rst.id AS template_id,
    rst.registration_date,
    CASE rst.registration_type WHEN 0 THEN '上午' WHEN 1 THEN '下午' END AS session,
    rs.id AS schedule_id,
    rs.start_time,
    rs.remaining_quota
FROM registration_schedule_template rst
JOIN registration_schedule rs ON rs.registration_schedule_template_id = rst.id
WHERE rst.doctor_id = 2
ORDER BY rst.registration_date, rs.start_time;

-- 查询挂号记录及状态（状态码对齐 RegistrationStatusEnum）
SELECT
    r.id AS registration_id,
    u.nickname AS patient_name,
    rs.start_time AS appointment_time,
    CASE r.status
        WHEN 0 THEN '等待支付'
        WHEN 1 THEN '待就诊'
        WHEN 2 THEN '支付失败'
        WHEN 3 THEN '已取消'
        WHEN 4 THEN '已完成'
        WHEN 5 THEN '已报到'
        WHEN 6 THEN '就诊中'
        WHEN 7 THEN '待支付'
    END AS status,
    mr.id AS medical_record_id,
    p.id AS prescription_id
FROM registration r
JOIN user u ON r.user_id = u.id
JOIN registration_schedule rs ON r.registration_schedule_id = rs.id
LEFT JOIN medical_record mr ON mr.registration_id = r.id
LEFT JOIN prescription p ON p.medical_record_id = mr.id
ORDER BY r.id;

-- 查询处方明细
SELECT
    p.id AS prescription_id,
    p.total_amount / 100 AS total_amount_yuan,
    p.status,
    pi.quantity,
    d.common_name AS drug_name,
    pi.unit_price / 100 AS unit_price_yuan,
    pi.usage_method
FROM prescription p
JOIN prescription_item pi ON pi.prescription_id = p.id
JOIN drug d ON pi.drug_id = d.id
ORDER BY p.id, pi.id;

-- ========================================
-- 10. 订单数据（order）+ 挂号/处方关联回填
-- 说明: 用户端门诊费用页面查询 order 表（order_type_id 1=挂号 2=药品），
--       原种子缺失导致页面始终无数据；金额与排班模板 price、处方 total_amount 对齐
-- ========================================

-- 挂号订单（order_type_id=1，对应 registration 3001-3005）
INSERT INTO `order` VALUES
(2043000000000010001, 1, 1, NULL, 2043000000000011001, 0, 1000, '2026-07-22 08:50:00', '2026-07-22 08:50:00', NOW(), NOW(), 0), -- 张三 待支付
(2043000000000010002, 2, 1, NULL, 2043000000000011002, 1, 1000, '2026-07-22 09:00:00', '2026-07-22 09:00:00', NOW(), NOW(), 0), -- 李四 已支付
(2043000000000010003, 3, 1, NULL, 2043000000000011003, 1, 1000, '2026-07-22 13:50:00', '2026-07-22 13:50:00', NOW(), NOW(), 0), -- 王五 已支付
(2043000000000010004, 4, 1, NULL, 2043000000000011004, 3, 1000, '2026-07-23 08:40:00', '2026-07-23 09:30:00', NOW(), NOW(), 0), -- 赵六 已完成（演示合成态：挂号订单无「已完成」代码路径，OrderStatus.FINISHED 仅药品订单发药后写入；此处保留以展示已完成挂号，对应 order_status_log 1->3 同属演示）
(2043000000000010005, 5, 1, NULL, 2043000000000011005, 2, 1000, '2026-07-23 13:30:00', '2026-07-23 14:00:00', NOW(), NOW(), 0), -- 钱七 已取消
(2043000000000010008, 6, 1, NULL, 2043000000000011008, 1, 1000, '2026-07-22 10:30:00', '2026-07-22 10:30:00', NOW(), NOW(), 0); -- 孙八 已支付（对应挂号 3006 待就诊）

-- 药品订单（order_type_id=2，对应处方 5001/5002）
INSERT INTO `order` VALUES
(2043000000000010006, 4, 2, NULL, 2043000000000011006, 1, 8000, '2026-07-23 10:00:00', '2026-07-23 10:00:00', NOW(), NOW(), 0), -- 赵六 处方5001 已支付
(2043000000000010007, 3, 2, NULL, 2043000000000011007, 0, 3500, '2026-07-22 14:30:00', '2026-07-22 14:30:00', NOW(), NOW(), 0); -- 王五 处方5002 待支付

-- ========================================
-- 10.1 订单明细（order_item）
-- 依据: RegistrationManager#createOrderAndRegistration —— 挂号订单必写明细
--       （production_id = registration.id、production_type_id = ProductionTypeConstant.DRUG = 1、quantity = 1）
--       PrescriptionManager#processDrugItems —— 药品订单写明细（production_id = drug.id）
-- 注: 代码未写 production_name，此处显式填充以使用户端缴费记录页可读展示
-- ========================================
INSERT INTO order_item (id, order_id, production_id, production_type_id, production_name, quantity, create_time, update_time, is_deleted) VALUES
-- 挂号订单明细
(2043000000000012001, 2043000000000010001, 2043000000000003001, 1, '挂号费', 1, NOW(), NOW(), 0),
(2043000000000012002, 2043000000000010002, 2043000000000003002, 1, '挂号费', 1, NOW(), NOW(), 0),
(2043000000000012003, 2043000000000010003, 2043000000000003003, 1, '挂号费', 1, NOW(), NOW(), 0),
(2043000000000012004, 2043000000000010004, 2043000000000003004, 1, '挂号费', 1, NOW(), NOW(), 0),
(2043000000000012005, 2043000000000010005, 2043000000000003005, 1, '挂号费', 1, NOW(), NOW(), 0),
(2043000000000012009, 2043000000000010008, 2043000000000003006, 1, '挂号费', 1, NOW(), NOW(), 0),
-- 药品订单明细（对应处方 5001 的 drug 1/2 与处方 5002 的 drug 7）
(2043000000000012006, 2043000000000010006, 1, 1, '阿司匹林肠溶片', 1, NOW(), NOW(), 0),
(2043000000000012007, 2043000000000010006, 2, 1, '阿托伐他汀钙片', 1, NOW(), NOW(), 0),
(2043000000000012008, 2043000000000010007, 7, 1, '布洛芬缓释胶囊', 1, NOW(), NOW(), 0);

-- ========================================
-- 10.2 订单状态流转日志（order_status_log）
-- 依据: OrderTimeOutManager / PaymentRecordManager / RegistrationCheckInManager / PharmacyManager 均写此表
-- ========================================
INSERT INTO order_status_log (id, order_id, from_status, to_status, operator_id, operator_role, remark, create_time, update_time, is_deleted) VALUES
(2043000000000013001, 2043000000000010001, 0, 0, 1, 'user',   '订单创建',     NOW(), NOW(), 0),
(2043000000000013002, 2043000000000010002, 0, 1, 2, 'user',   '支付成功',     NOW(), NOW(), 0),
(2043000000000013003, 2043000000000010003, 0, 1, 3, 'user',   '支付成功',     NOW(), NOW(), 0),
(2043000000000013004, 2043000000000010004, 0, 1, 4, 'user',   '支付成功',     NOW(), NOW(), 0),
(2043000000000013005, 2043000000000010004, 1, 3, 2, 'doctor', '就诊完成',     NOW(), NOW(), 0),
(2043000000000013006, 2043000000000010005, 0, 2, 5, 'user',   '用户取消订单', NOW(), NOW(), 0),
(2043000000000013007, 2043000000000010006, 0, 1, 4, 'user',   '支付成功',     NOW(), NOW(), 0),
(2043000000000013008, 2043000000000010007, 0, 0, 3, 'user',   '订单创建',     NOW(), NOW(), 0),
(2043000000000013009, 2043000000000010008, 0, 1, 6, 'user',   '支付成功',     NOW(), NOW(), 0);

-- ========================================
-- 10.3 支付记录（payment_record）
-- 依据: 退款链路取「成功支付记录」硬编码 status=2
--       （PaymentRecordServiceImpl#getSuccessPaymentRecordByOrderId）
-- 注: 只补已支付/已完成订单；transaction_sn 受唯一索引 uk_transaction_sn 约束
-- ========================================
INSERT INTO payment_record (id, sn, order_id, total_amount, real_amount, payment_method_id, status, transaction_sn, payment_time, create_time, update_time, is_deleted) VALUES
(2043000000000014001, 2043000000000015001, 2043000000000010002, 1000, 1000, 1, 2, 9000000000000001001, '2026-07-22 09:00:00', NOW(), NOW(), 0),
(2043000000000014002, 2043000000000015002, 2043000000000010003, 1000, 1000, 1, 2, 9000000000000001002, '2026-07-22 13:50:00', NOW(), NOW(), 0),
(2043000000000014003, 2043000000000015003, 2043000000000010004, 1000, 1000, 1, 2, 9000000000000001003, '2026-07-23 08:40:00', NOW(), NOW(), 0),
(2043000000000014004, 2043000000000015004, 2043000000000010006, 8000, 8000, 1, 2, 9000000000000001004, '2026-07-23 10:00:00', NOW(), NOW(), 0),
(2043000000000014005, 2043000000000015005, 2043000000000010008, 1000, 1000, 1, 2, 9000000000000001005, '2026-07-22 10:30:00', NOW(), NOW(), 0);

-- 回填挂号关联订单（registration.order_id -> order.id）
UPDATE registration SET order_id = 2043000000000010001 WHERE id = 2043000000000003001;
UPDATE registration SET order_id = 2043000000000010002 WHERE id = 2043000000000003002;
UPDATE registration SET order_id = 2043000000000010003 WHERE id = 2043000000000003003;
UPDATE registration SET order_id = 2043000000000010004 WHERE id = 2043000000000003004;
UPDATE registration SET order_id = 2043000000000010005 WHERE id = 2043000000000003005;
UPDATE registration SET order_id = 2043000000000010008 WHERE id = 2043000000000003006;

-- 回填处方关联订单
UPDATE prescription SET order_id = 2043000000000010006 WHERE id = 2043000000000005001;
UPDATE prescription SET order_id = 2043000000000010007 WHERE id = 2043000000000005002;

-- ========================================
-- 10.4 药品库存数据补齐（库存预警样例 + 库存异动流水覆盖）
-- 依据: 看板「库存预警」= DrugInventoryServiceImpl#countLowStock（available_quantity < min_stock），
--       原种子 10 行库存全部高于预警线 → 看板恒为 0，无样例可展示。
--       异动流水字段写法对齐 PrescriptionManager#processDrugItems（type=4 锁定、related_order=order.sn、
--       operator_id=doctorId、operator_name='doctor'）。
-- 说明: drug_inventory 用绝对值 SET，天然幂等；inventory_transaction 由第 0 节幂等清理。
-- 注: 基础数据里 locked_quantity 的既有数值（10~20）未逐笔还原为锁定流水，此处只补 1 条真实来源的锁定样例。
-- ========================================

-- ① 制造 1 条库存预警：药品 5（头孢克肟分散片）盘点报损后可用量 20 < 预警线 60
--    保持不变量 stock_quantity = available_quantity + locked_quantity（40 = 20 + 20）
UPDATE drug_inventory
   SET stock_quantity = 40,
       available_quantity = 20
 WHERE id = 5;

INSERT INTO inventory_transaction
    (id, drug_id, warehouse_id, transaction_type, related_order, quantity_change,
     quantity_before, quantity_after, remark, operator_id, operator_name, create_time)
VALUES
-- ② 对应①的盘点调整流水（transaction_type=3 盘点调整）
(2043000000000016001, 5, 1, 3, NULL, -560, 600, 40,
 '库存盘点报损，可用量已低于预警线', 2, '李药师', NOW()),
-- ③ 待支付处方 5002 的库存锁定流水（transaction_type=4 锁定），关联订单 10007 的 sn
(2043000000000016002, 7, 1, 4, '2043000000000011007', 1, 431, 430,
 '开方锁定库存', 2, 'doctor', NOW());

-- ========================================
-- 11. 时间对齐（把业务时间平移到「今天」起，保持相对关系不变）
-- 说明: 本文件的排班/挂号/订单业务时间硬编码为 2026-07-22 ~ 07-24，已过期；
--       而 Java 侧号源按 [today, today+maxAdvanceDays] 过滤
--       （RegistrationScheduleManager、RegistrationScheduleMapper#listRegistrationSchedulesByDoctorIdAndMaxAdvanceDays），
--       过期后用户端挂号页查不到任何号源。此处统一平移，锚点 = 排班首日 2026-07-22。
-- 重要: 只平移「业务时间」列。各表的 create_time/update_time 在本文件里是 NOW()（脚本执行时刻），
--       严禁平移，否则会被推到未来。模板 start_time/end_time 为 TIME 类型，无需平移。
-- ========================================
SET @shift = DATEDIFF(CURDATE(), '2026-07-22');

UPDATE registration_schedule_template
   SET registration_date = DATE_ADD(registration_date, INTERVAL @shift DAY);

UPDATE registration_schedule
   SET start_time = DATE_ADD(start_time, INTERVAL @shift DAY),
       end_time   = DATE_ADD(end_time,   INTERVAL @shift DAY);

UPDATE registration
   SET registration_time = DATE_ADD(registration_time, INTERVAL @shift DAY),
       check_in_time     = DATE_ADD(check_in_time,     INTERVAL @shift DAY),
       visit_start_time  = DATE_ADD(visit_start_time,  INTERVAL @shift DAY),
       visit_end_time    = DATE_ADD(visit_end_time,    INTERVAL @shift DAY);

UPDATE `order`
   SET order_create_time = DATE_ADD(order_create_time, INTERVAL @shift DAY),
       order_update_time = DATE_ADD(order_update_time, INTERVAL @shift DAY);

UPDATE payment_record
   SET payment_time = DATE_ADD(payment_time, INTERVAL @shift DAY);

UPDATE consultation_room_status
   SET occupancy_start_time = DATE_ADD(occupancy_start_time, INTERVAL @shift DAY),
       occupancy_end_time   = DATE_ADD(occupancy_end_time,   INTERVAL @shift DAY);

COMMIT;

-- ========================================
-- 12. 清理历史数据（谨慎执行）
-- 说明: 文件开头的「第 0 节 幂等清理」已覆盖此用途，本节仅作参考保留
-- ========================================

-- 如需重新初始化，可执行以下语句（取消注释）
-- DELETE FROM registration_status_log;
-- DELETE FROM prescription_item;
-- DELETE FROM prescription;
-- DELETE FROM medical_record;
-- DELETE FROM registration;
-- DELETE FROM registration_schedule WHERE registration_schedule_template_id LIKE '2043000000000001%';
-- DELETE FROM registration_schedule_template WHERE id LIKE '2043000000000001%';
-- DELETE FROM consultation_room_status WHERE id LIKE '2043000000000008%';
-- DELETE FROM chat WHERE id LIKE '2043000000000009%';
