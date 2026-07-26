const TOKEN_KEY = 'user_access_token'
const UID_KEY = 'user_uid'
const USER_INFO_KEY = 'user_info'
const SELECTED_PATIENT_CARD_ID_KEY = 'selected_patient_card_id'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getUid(): string | null {
  return localStorage.getItem(UID_KEY)
}

export function setUid(uid: string) {
  localStorage.setItem(UID_KEY, uid)
}

export function getUserInfo(): any {
  const raw = localStorage.getItem(USER_INFO_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function setUserInfo(info: any) {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
}

export function getSelectedPatientCardId(): string | null {
  return localStorage.getItem(SELECTED_PATIENT_CARD_ID_KEY)
}

export function setSelectedPatientCardId(id: string | null) {
  if (id == null) {
    localStorage.removeItem(SELECTED_PATIENT_CARD_ID_KEY)
  } else {
    localStorage.setItem(SELECTED_PATIENT_CARD_ID_KEY, id)
  }
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(UID_KEY)
  localStorage.removeItem(USER_INFO_KEY)
  localStorage.removeItem(SELECTED_PATIENT_CARD_ID_KEY)
}
