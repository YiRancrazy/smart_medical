import { adminRoutes } from './admin'
import { doctorRoutes } from './doctor'
import { pharmacistRoutes } from './pharmacist'

export function getRoleRoutes(roleId: number) {
  if (roleId === 1) return [adminRoutes]
  if (roleId === 2) return [doctorRoutes]
  if (roleId === 6) return [pharmacistRoutes]
  return []
}
