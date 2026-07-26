export function formatDate(value?: string | number | Date): string {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return '-'
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

// U01: 后端金额统一以"分"返回（挂号价格/处方总额/单价），展示前 /100 转元
export function formatMoney(value?: number): string {
  if (value === undefined || value === null) return '-'
  return `¥${(Number(value) / 100).toFixed(2)}`
}

export function formatTime(value?: string | Date): string {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) {
    // 兼容纯时间字符串如 14:00:00
    const m = String(value).match(/(\d{2}):\d{2}/)
    return m ? m[0] : '-'
  }
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function maskPhone(phone?: string): string {
  if (!phone || phone.length !== 11) return phone || ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}
