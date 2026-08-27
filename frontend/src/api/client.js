export class ApiError extends Error {
  constructor(status, message, payload) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.payload = payload
    // 503 bodies carry the breaker's retry window for the countdown UI
    this.retryInMs = payload?.retryInMs ?? null
  }
}

const base = import.meta.env.VITE_API_BASE_URL ?? ''

export async function api(path, { timeoutMs = 30000 } = {}) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), timeoutMs)
  try {
    const res = await fetch(base + path, { signal: controller.signal })
    const body = await res.json().catch(() => null)
    if (!res.ok) {
      throw new ApiError(res.status, body?.error ?? res.statusText, body)
    }
    return body
  } catch (err) {
    if (err instanceof ApiError) throw err
    // fetch itself failed: backend down or network gone
    throw new ApiError(0, 'API unreachable', null)
  } finally {
    clearTimeout(timer)
  }
}
