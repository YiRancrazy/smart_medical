import axios from 'axios'
import CryptoJS from 'crypto-js'

const DEVICE_KEY = 'sm_captcha_device_id'

/**
 * 验证码请求工具（裸 axios，不经 request 拦截器：验证码接口返回 anji 的 repCode 而非统一 Result）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 滑块验证码 get/check，并维护设备级 deviceId（用于后端按「设备+IP」绑定通过标记）
 * @Datetime: 2026-09-04 15:00
 * @Version: 1.0
 */

// admin 端沿用 loginByRole 的 baseURL 规则；dev 走 vite proxy 同源
const baseURL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')

/**
 * 获取设备唯一 ID（首次生成后持久化，get/check/login 三处共用同一标识）
 */
export function getDeviceId(): string {
  let id = localStorage.getItem(DEVICE_KEY)
  if (!id) {
    id = typeof crypto.randomUUID === 'function'
      ? crypto.randomUUID()
      : `dc-${Date.now()}-${Math.random().toString(16).slice(2)}`
    localStorage.setItem(DEVICE_KEY, id)
  }
  return id
}

export interface CaptchaImgData {
  token: string
  secretKey: string
  originalImageBase64: string
  jigsawImageBase64: string
}

/**
 * 获取滑块验证码图片（原图 + 滑块图 + token）。原始数组回 base64 串，此处补齐 data URI 前缀。
 */
function toDataUri(b64?: string): string {
  if (!b64) return ''
  return b64.startsWith('data:') ? b64 : `data:image/png;base64,${b64}`
}

export async function fetchCaptcha(): Promise<CaptchaImgData> {
  const res = await axios.post(
    `${baseURL}/api/captcha/get`,
    {},
    { headers: { 'X-Device-Id': getDeviceId() } }
  )
  const body = res.data
  if (!body || body.repCode !== '0000' || !body.repData) {
    throw new Error(body?.repMsg || '验证码获取失败')
  }
  return {
    token: body.repData.token,
    secretKey: body.repData.secretKey,
    originalImageBase64: toDataUri(body.repData.originalImageBase64),
    jigsawImageBase64: toDataUri(body.repData.jigsawImageBase64)
  }
}

/**
 * 校验滑块（独立接口）。通过后后端已为该设备/IP 打通过标记，登录无需再携带验证码。
 * pointJson 需按 anji 后端约定 AES/ECB/PKCS5 加密（密钥为 get 下发的 secretKey 原文，128 位）。
 * @param token 验证码 token
 * @param x 滑块落点 x（背景图自然像素坐标）
 * @param y 滑块落点 y（背景图自然像素坐标）
 * @param secretKey get 接口下发的 AES 密钥（16 字节）
 */
export async function checkCaptcha(token: string, x: number, y: number, secretKey: string): Promise<void> {
  const key = CryptoJS.enc.Utf8.parse(secretKey)
  const cipher = CryptoJS.AES.encrypt(JSON.stringify({ x, y }), key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  })
  const res = await axios.post(
    `${baseURL}/api/captcha/check`,
    { token, pointJson: cipher.toString() },
    { headers: { 'X-Device-Id': getDeviceId() } }
  )
  const body = res.data
  if (!body || body.repCode !== '0000') {
    throw new Error(body?.repMsg || '校验失败，请重试')
  }
}