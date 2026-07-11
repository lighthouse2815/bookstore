/*
 * Safe-by-default staging smoke. It never prints credentials, cookies, access tokens or response bodies.
 * Required: SMOKE_API_BASE_URL, SMOKE_USERNAME, SMOKE_PASSWORD.
 * Set SMOKE_DESTRUCTIVE=true only for a disposable staging account to exercise logout-all.
 */
const baseUrl = required('SMOKE_API_BASE_URL').replace(/\/$/, '')
const username = required('SMOKE_USERNAME')
const password = required('SMOKE_PASSWORD')
const destructive = process.env.SMOKE_DESTRUCTIVE === 'true'
const origin = process.env.SMOKE_WEB_ORIGIN ?? new URL(baseUrl).origin
let cookie = ''
let accessToken = ''
let failures = 0

async function main() {
  await check('health endpoint', async () => {
    const response = await fetch(`${new URL(baseUrl).origin}/actuator/health`)
    assert(response.ok, `expected 2xx, got ${response.status}`)
    const body = await response.json()
    assert(body.status === 'UP', `expected UP, got ${body.status}`)
  })

  await check('CSRF bootstrap', async () => {
    const response = await fetch(`${baseUrl}/auth/web/csrf`, { headers: { Origin: origin } })
    assert(response.ok, `expected 2xx, got ${response.status}`)
    captureCookies(response)
    assert(cookie.includes('BOOKSTORE_CSRF='), 'BOOKSTORE_CSRF was not set')
  })

  await check('invalid CSRF is rejected', async () => {
    const response = await fetch(`${baseUrl}/auth/web/login`, {
      method: 'POST', headers: jsonHeaders({ 'X-CSRF-Token': 'invalid', Cookie: cookie, Origin: origin }),
      body: JSON.stringify({ username, password }),
    })
    assert(response.status === 403, `expected 403, got ${response.status}`)
  })

  await check('website login and secure refresh cookie', async () => {
    const csrf = readCookie('BOOKSTORE_CSRF')
    const response = await fetch(`${baseUrl}/auth/web/login`, {
      method: 'POST', headers: jsonHeaders({ 'X-CSRF-Token': csrf, Cookie: cookie, Origin: origin }),
      body: JSON.stringify({ username, password }),
    })
    assert(response.ok, `expected 2xx, got ${response.status}`)
    captureCookies(response)
    const rawCookies = response.headers.getSetCookie?.() ?? [response.headers.get('set-cookie') ?? '']
    const refresh = rawCookies.find((value) => value.startsWith('BOOKSTORE_REFRESH=')) ?? ''
    assert(refresh.includes('HttpOnly'), 'refresh cookie is not HttpOnly')
    assert(refresh.includes('SameSite='), 'refresh cookie has no SameSite flag')
    if (new URL(baseUrl).protocol === 'https:') assert(refresh.includes('Secure'), 'HTTPS refresh cookie is not Secure')
    const payload = await response.json()
    accessToken = payload?.data?.accessToken ?? ''
    assert(accessToken.length > 20, 'web login did not return an access token')
    assert(!JSON.stringify(payload).includes('refreshToken'), 'web login response exposed refreshToken')
  })

  await check('reload recovery via refresh cookie', async () => {
    const response = await fetch(`${baseUrl}/auth/web/refresh`, {
      method: 'POST', headers: jsonHeaders({ 'X-CSRF-Token': readCookie('BOOKSTORE_CSRF'), Cookie: cookie, Origin: origin }),
    })
    assert(response.ok, `expected 2xx, got ${response.status}`)
    captureCookies(response)
    const payload = await response.json()
    accessToken = payload?.data?.accessToken ?? ''
    assert(accessToken.length > 20, 'refresh did not return a replacement access token')
    assert(!JSON.stringify(payload).includes('refreshToken'), 'refresh response exposed refreshToken')
  })

  await check('session list contract', async () => {
    const response = await fetch(`${baseUrl}/auth/sessions`, { headers: { Authorization: `Bearer ${accessToken}` } })
    assert(response.ok, `expected 2xx, got ${response.status}`)
    const body = await response.json()
    assert(Array.isArray(body?.data), 'session list did not return an array')
    assert(!JSON.stringify(body).match(/refreshToken|tokenHash|token"\s*:/i), 'session list exposed token material')
  })

  if (destructive) {
    await check('logout-all contract', async () => {
      const response = await fetch(`${baseUrl}/auth/logout-all`, { method: 'POST', headers: { Authorization: `Bearer ${accessToken}` } })
      assert(response.ok, `expected 2xx, got ${response.status}`)
    })
  } else {
    skip('session revoke/logout-all', 'set SMOKE_DESTRUCTIVE=true for a disposable account')
  }

  skip('browser localStorage assertion', 'requires Playwright/browser session; website source keeps tokens memory-only')
  skip('password-reset/throttle/Google/checkout/QR/reconciliation/admin refund', 'require disposable test fixtures and provider/admin credentials')
  process.exitCode = failures === 0 ? 0 : 1
}

function required(name) { const value = process.env[name]; if (!value) throw new Error(`Missing required environment variable: ${name}`); return value }
function jsonHeaders(extra = {}) { return { 'Content-Type': 'application/json', ...extra } }
function captureCookies(response) {
  const rawCookies = response.headers.getSetCookie?.() ?? [response.headers.get('set-cookie') ?? '']
  for (const value of rawCookies) { const first = value.split(';', 1)[0]; if (first.includes('=')) upsertCookie(first) }
}
function upsertCookie(value) { const name = value.split('=', 1)[0]; const entries = cookie ? cookie.split(/;\s*/) : []; const next = entries.filter((entry) => !entry.startsWith(`${name}=`)); next.push(value); cookie = next.join('; ') }
function readCookie(name) { return cookie.split(/;\s*/).find((entry) => entry.startsWith(`${name}=`))?.slice(name.length + 1) ?? '' }
function assert(condition, message) { if (!condition) throw new Error(message) }
async function check(label, action) { try { await action(); console.log(`PASS  ${label}`) } catch (error) { failures++; console.error(`FAIL  ${label}: ${error instanceof Error ? error.message : 'unknown error'}`) } }
function skip(label, reason) { console.log(`SKIP  ${label}: ${reason}`) }

await main()
