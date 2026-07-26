import request from '../index'
import type { Result } from '../types'

/**
 * 医生处方作废 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 作废待支付状态的处方
 * @Datetime: 2026-07-17 10:30
 * @Version: 1.0
 */

/**
 * 作废处方
 * 业务约束：仅 status=0（待支付）的处方可作废
 */
export function cancelPrescription(prescriptionId: string | number) {
  return request.post<any, Result<void>>(`/api/doctor/v1/prescription/${prescriptionId}/cancel`)
}