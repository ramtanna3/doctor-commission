/**
 * Central API fetch utility.
 * Automatically injects the JWT Bearer token from localStorage
 * into every request. On 401, clears the token and reloads the page
 * so the Login screen is shown.
 */

const TOKEN_KEY = 'dcas_token';
const USER_ID_KEY = 'dcas_userId';
const USERNAME_KEY = 'dcas_username';
const DISPLAY_NAME_KEY = 'dcas_display_name';
const COMPANY_KEY = 'dcas_company';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function getUsername(): string | null {
  return localStorage.getItem(USERNAME_KEY);
}

export function getDisplayName(): string | null {
  return localStorage.getItem(DISPLAY_NAME_KEY);
}

export function getCompanyName(): string | null {
  return localStorage.getItem(COMPANY_KEY);
}

export function setToken(
  token: string,
  userId: number | string,
  username?: string,
  displayName?: string,
  companyName?: string
): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_ID_KEY, String(userId));
  if (username)    localStorage.setItem(USERNAME_KEY, username);
  if (displayName) localStorage.setItem(DISPLAY_NAME_KEY, displayName);
  if (companyName) localStorage.setItem(COMPANY_KEY, companyName);
}

export function updateProfileCache(displayName: string, companyName: string): void {
  localStorage.setItem(DISPLAY_NAME_KEY, displayName);
  localStorage.setItem(COMPANY_KEY, companyName);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_ID_KEY);
  localStorage.removeItem(USERNAME_KEY);
  localStorage.removeItem(DISPLAY_NAME_KEY);
  localStorage.removeItem(COMPANY_KEY);
}

export function isLoggedIn(): boolean {
  return !!getToken();
}

export async function apiFetch(url: string, options: RequestInit = {}): Promise<Response> {
  const token = getToken();
  const headers = new Headers(options.headers as HeadersInit | undefined);

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  // Remove legacy header if somehow still present
  headers.delete('x-user-id');

  const response = await fetch(url, { ...options, headers });

  if (response.status === 401) {
    clearToken();
    // Small delay so any in-flight state settles, then reload to show Login
    setTimeout(() => window.location.reload(), 100);
  }

  return response;
}
