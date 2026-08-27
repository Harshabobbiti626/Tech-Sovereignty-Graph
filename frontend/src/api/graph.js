import { api } from './client'

export const getGraph = () => api('/api/graph')

export const getStats = () => api('/api/stats')

export const getAudit = (email, resource) =>
  api(`/api/audit/${encodeURIComponent(email)}${resource ? `?resource=${encodeURIComponent(resource)}` : ''}`)

export const getImpact = (group) => api(`/api/impact/${encodeURIComponent(group)}`)

export const getNodeContext = (id) => api(`/api/nodes/${encodeURIComponent(id)}/context`)

export const getHealth = () => api('/api/health', { timeoutMs: 15000 })
