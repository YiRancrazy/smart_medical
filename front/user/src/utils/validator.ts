export function isPhone(value: string): boolean {
  return /^1\d{10}$/.test(value)
}

export function isIdCard(value: string): boolean {
  return /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(value)
}
