-- ========================================
-- Smart Medical 业务流程测试数据
-- 生成时间: 2026-07-21
-- 最后更新: 2026-07-23
-- 目的: 补全P0级核心业务流程数据
-- 说明: 本文件包含业务流程数据（排班/挂号/病历/处方等）
--       基础配置数据（角色/用户/科室/医生/药品）在CreateTable.sql中
-- ========================================

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
(2043000000000001001, '李娜医生普通门诊', 2, '2026-07-22', 0, '09:00:00', '12:00:00', 40, 2, 1, 1, 1, '心内科门诊', NOW(), NOW(), 0, NULL),
-- 2026-07-22 下午
(2043000000000001002, '李娜医生普通门诊', 2, '2026-07-22', 0, '14:00:00', '17:00:00', 40, 2, 1, 1, 2, '心内科门诊', NOW(), NOW(), 0, NULL),
-- 2026-07-23 上午
(2043000000000001003, '李娜医生普通门诊', 2, '2026-07-23', 0, '09:00:00', '12:00:00', 40, 2, 1, 1, 1, '心内科门诊', NOW(), NOW(), 0, NULL),
-- 2026-07-23 下午
(2043000000000001004, '李娜医生普通门诊', 2, '2026-07-23', 0, '14:00:00', '17:00:00', 40, 2, 1, 1, 2, '心内科门诊', NOW(), NOW(), 0, NULL),
-- 2026-07-24 上午
(2043000000000001005, '李娜医生普通门诊', 2, '2026-07-24', 0, '09:00:00', '12:00:00', 40, 2, 1, 1, 1, '心内科门诊', NOW(), NOW(), 0, NULL);

-- 插入李娜医生的具体时段（每小时一个时段）
INSERT INTO registration_schedule VALUES
-- 2026-07-22 上午 3小时（09-10, 10-11, 11-12）
(2043000000000002001, 2, 2043000000000001001, '2026-07-22 09:00:00', '2026-07-22 10:00:00', 1, 13, NOW(), NOW(), 0),
(2043000000000002002, 2, 2043000000000001001, '2026-07-22 10:00:00', '2026-07-22 11:00:00', 1, 13, NOW(), NOW(), 0),
(2043000000000002003, 2, 2043000000000001001, '2026-07-22 11:00:00', '2026-07-22 12:00:00', 1, 14, NOW(), NOW(), 0),
-- 2026-07-22 下午 3小时（14-15, 15-16, 16-17）
(2043000000000002004, 2, 2043000000000001002, '2026-07-22 14:00:00', '2026-07-22 15:00:00', 1, 13, NOW(), NOW(), 0),
(2043000000000002005, 2, 2043000000000001002, '2026-07-22 15:00:00', '2026-07-22 16:00:00', 1, 13, NOW(), NOW(), 0),
(2043000000000002006, 2, 2043000000000001002, '2026-07-22 16:00:00', '2026-07-22 17:00:00', 1, 14, NOW(), NOW(), 0),
-- 2026-07-23 上午 3小时
(2043000000000002007, 2, 2043000000000001003, '2026-07-23 09:00:00', '2026-07-23 10:00:00', 1, 13, NOW(), NOW(), 0),
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
-- ========================================

-- 已预约状态（user_id=1 张三，挂 2026-07-22 上午李娜医生的号）
INSERT INTO registration VALUES
(2043000000000003001, 1, 2043000000000002001, NULL, 0, '2026-07-22 09:00:00', NULL, NULL, NULL, NOW(), NOW(), 0);

-- 已报到状态（user_id=2 李四，挂 2026-07-22 上午李娜医生的号，已报到）
INSERT INTO registration VALUES
(2043000000000003002, 2, 2043000000000002002, NULL, 5, '2026-07-22 10:00:00', '2026-07-22 09:55:00', NULL, NULL, NOW(), NOW(), 0);

-- 就诊中状态（user_id=3 王五，挂 2026-07-22 下午李娜医生的号，正在就诊）
INSERT INTO registration VALUES
(2043000000000003003, 3, 2043000000000002004, NULL, 6, '2026-07-22 14:00:00', '2026-07-22 13:58:00', '2026-07-22 14:00:00', NULL, NOW(), NOW(), 0);

-- 已就诊状态（user_id=4 赵六，挂 2026-07-21 的号，已完成）
INSERT INTO registration VALUES
(2043000000000003004, 4, 2043000000000002007, NULL, 2, '2026-07-23 09:00:00', '2026-07-23 08:50:00', '2026-07-23 09:00:00', '2026-07-23 09:30:00', NOW(), NOW(), 0);

-- 已取消状态（user_id=5 吴七，取消挂号）
INSERT INTO registration VALUES
(2043000000000003005, 5, 2043000000000002010, NULL, 3, '2026-07-23 14:00:00', NULL, NULL, NULL, NOW(), NOW(), 0);

-- ========================================
-- 3. 电子病历数据（medical_record）
-- ========================================

-- 为已就诊的挂号记录（3004）创建病历
INSERT INTO medical_record VALUES
(2043000000000004001, 2043000000000003004, 2, 1,
 '胸闷气短3天',
 '患者3天前无明显诱因出现胸闷、气短，活动后加重，休息后可缓解。无胸痛、心悸、晕厥。',
 '高血压病史5年', -- past_history
 '双肺呼吸音清，心率78次/分，律齐，各瓣膜听诊区未闻及病理性杂音', -- physical_exam
 '冠状动脉粥样硬化性心脏病',
 '1. 完善心电图、心脏彩超检查\n2. 给予抗血小板、扩冠治疗\n3. 低盐低脂饮食，适量运动',
 1, -- status: 已提交
 NOW(), NOW(), 0);

-- 为就诊中的挂号记录（3003）创建病历（部分填写）
INSERT INTO medical_record VALUES
(2043000000000004002, 2043000000000003003, 2, 1,
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
 '阿司匹林肠溶片100mg*30片 + 阿托伐他汀钙片20mg*14片', -- remark
 NOW(), NOW(), 0);

-- 为就诊中的病历（4002）创建处方（待支付）
INSERT INTO prescription VALUES
(2043000000000005002, 2043000000000004002, NULL, 0, -- id, medical_record_id, order_id, status(待支付)
 3500, -- total_amount 总金额35元（分）
 NULL, NULL, 0, -- pharmacist_id, dispensed_at, version
 '酒石酸美托洛尔片25mg*20片', -- remark
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
-- ========================================

-- 挂号3002的状态变更：已预约→已报到
INSERT INTO registration_status_log VALUES
(2043000000000007001, 2043000000000003002, 0, 5, 1, 'user', '患者扫码报到', NOW(), NOW(), 0);

-- 挂号3003的状态变更：已预约→已报到→就诊中
INSERT INTO registration_status_log VALUES
(2043000000000007002, 2043000000000003003, 0, 5, 1, 'user', '患者扫码报到', NOW(), NOW(), 0),
(2043000000000007003, 2043000000000003003, 5, 6, 2, 'doctor', '医生开始接诊', NOW(), NOW(), 0);

-- 挂号3004的状态变更：已预约→已报到→就诊中→已就诊
INSERT INTO registration_status_log VALUES
(2043000000000007004, 2043000000000003004, 0, 5, 1, 'user', '患者扫码报到', NOW(), NOW(), 0),
(2043000000000007005, 2043000000000003004, 5, 6, 2, 'doctor', '医生开始接诊', NOW(), NOW(), 0),
(2043000000000007006, 2043000000000003004, 6, 2, 2, 'doctor', '医生结束接诊', NOW(), NOW(), 0);

-- ========================================
-- 7. 诊室占用状态（consultation_room_status）
-- ========================================

-- 诊室1（心内科门诊1）2026-07-22 上午被李娜医生占用
INSERT INTO consultation_room_status VALUES
(2043000000000008001, 1, '2026-07-22 09:00:00', '2026-07-22 12:00:00', '门诊接诊', NOW(), NOW(), 0);

-- 诊室2（心内科门诊2）2026-07-22 下午被李娜医生占用
INSERT INTO consultation_room_status VALUES
(2043000000000008002, 2, '2026-07-22 14:00:00', '2026-07-22 17:00:00', '门诊接诊', NOW(), NOW(), 0);

-- ========================================
-- 8. 聊天消息（chat）
-- ========================================

-- 张三（user_id=1）与李娜医生（doctor_id=2）的咨询记录
INSERT INTO chat VALUES
(2043000000000009001, 1, 2, 0, 0, '医生您好，我想咨询心悸的问题', NOW(), NOW(), 0),
(2043000000000009002, 2, 1, 1, 0, '您好，心悸多久了？有没有伴随其他症状？', NOW(), NOW(), 0),
(2043000000000009003, 1, 2, 0, 0, '大概一周了，晚上比较明显', NOW(), NOW(), 0);

-- ========================================
-- 9. 数据验证查询
-- ========================================

-- 查询李娜医生的排班数据
SELECT
    rst.id AS template_id,
    rst.registration_date,
    CASE rst.registration_type WHEN 0 THEN '上午' WHEN 1 THEN '下午' END AS session,
    rs.id AS schedule_id,
    rs.start_time,
    rs.remain_quota
FROM registration_schedule_template rst
JOIN registration_schedule rs ON rs.registration_schedule_template_id = rst.id
WHERE rst.doctor_id = 2
ORDER BY rst.registration_date, rs.start_time;

-- 查询挂号记录及状态
SELECT
    r.id AS registration_id,
    u.name AS patient_name,
    rs.start_time AS appointment_time,
    CASE r.status
        WHEN 0 THEN '已预约'
        WHEN 5 THEN '已报到'
        WHEN 6 THEN '就诊中'
        WHEN 2 THEN '已就诊'
        WHEN 3 THEN '已取消'
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
    d.name AS drug_name,
    pi.unit_price / 100 AS unit_price_yuan,
    pi.usage_instruction
FROM prescription p
JOIN prescription_item pi ON pi.prescription_id = p.id
JOIN drug d ON pi.drug_id = d.id
ORDER BY p.id, pi.id;

-- ========================================
-- 10. 清理历史数据（谨慎执行）
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