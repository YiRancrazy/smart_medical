export function formatDateTime(value?: string | number | Date): string {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return '-'
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function formatMoney(value?: number): string {
  if (value === undefined || value === null) return '-'
  return `¥${Number(value).toFixed(2)}`
}
