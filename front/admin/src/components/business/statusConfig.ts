/**
 * 状态标签配置（F31: 集中定义，方便与后端枚举对照）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号/处方状态码 → 颜色 + 文案映射
 * @Datetime: 2026-07-24 17:00
 * @Version: 1.0
 */

// 对应后端 RegistrationStatusEnum.java
export const registrationColorMap: Record<number, string> = {
  0: 'orange',     // 等待支付
  1: 'blue',       // 支付成功/待就诊
  2: 'red',        // 支付失败
  3: 'default',    // 取消
  4: 'green',      // 完成
  5: 'cyan',       // 已报到
  6: 'processing', // 就诊中
  7: 'gold'        // 待支付（处方补缴）
}

export const registrationLabelMap: Record<number, string> = {
  0: '待支付',
  1: '待就诊',
  2: '支付失败',
  3: '已取消',
  4: '完成',
  5: '已报到',
  6: '就诊中',
  7: '待支付'
}

// 对应后端 PrescriptionStatus.java
export const prescriptionColorMap: Record<number, string> = {
  0: 'orange',   // 待支付
  1: 'blue',     // 已支付
  2: 'green',    // 已发药
  3: 'default'   // 已取消
}

export const prescriptionLabelMap: Record<number, string> = {
  0: '待支付',
  1: '已支付',
  2: '已发药',
  3: '已取消'
}
