const REFRESH_TOKEN_KEY = 'mediconnect.refreshToken';

let accessToken = null;

export function setTokens(nextAccessToken, nextRefreshToken) {
  accessToken = nextAccessToken || null;

  if (nextRefreshToken) {
    sessionStorage.setItem(REFRESH_TOKEN_KEY, nextRefreshToken);
  } else {
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}

export function getAccessToken() {
  return accessToken;
}

export function getRefreshToken() {
  return sessionStorage.getItem(REFRESH_TOKEN_KEY);
}

export function clearTokens() {
  accessToken = null;
  sessionStorage.removeItem(REFRESH_TOKEN_KEY);
}
