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

/**
 * 根据 18 位或 15 位身份证号计算当前完整周岁
 */
export function calculateAgeByIdCard(idCard?: string): number | null {
  const value = (idCard || '').trim()
  let birthText = ''
  if (/^\d{17}[\dXx]$/.test(value)) {
    birthText = value.slice(6, 14)
  } else if (/^\d{15}$/.test(value)) {
    birthText = `19${value.slice(6, 12)}`
  } else {
    return null
  }

  const year = Number(birthText.slice(0, 4))
  const month = Number(birthText.slice(4, 6))
  const day = Number(birthText.slice(6, 8))
  const birthDate = new Date(year, month - 1, day)
  if (
    birthDate.getFullYear() !== year ||
    birthDate.getMonth() !== month - 1 ||
    birthDate.getDate() !== day
  ) {
    return null
  }

  const today = new Date()
  if (birthDate.getTime() > today.getTime()) {
    return null
  }

  let age = today.getFullYear() - year
  const birthdayPassed =
    today.getMonth() > month - 1 ||
    (today.getMonth() === month - 1 && today.getDate() >= day)
  if (!birthdayPassed) {
    age -= 1
  }
  return age
}
