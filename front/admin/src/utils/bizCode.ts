export const BIZ_ERROR_MAP: Record<number, string> = {
  1001: '挂号记录不存在',
  1002: '当前挂号状态不可操作',
  1003: '无权操作此挂号',
  2001: '病历已提交，不可修改',
  2002: '病历不存在',
  3001: '药品不存在',
  3002: '药品库存不足',
  3003: '库存正在被其他操作锁定，请稍后重试',
  4001: '处方不存在',
  4002: '处方未支付',
  4003: '无权操作此处方',
  4004: '处方已发药',
  4005: '处方已取消',
  5001: '订单状态不允许此操作',
  6001: '医生不匹配',
  7001: '操作冲突，请刷新后重试'
}

export function getBizErrorMessage(code: number, fallback?: string): string {
  return BIZ_ERROR_MAP[code] || fallback || '操作失败'
}
