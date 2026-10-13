const KEY = 'financeai.auth';

/** Shape: { user: { name, gender }, tokens: { accessToken, refreshToken } } */
export function loadAuth() {
    try {
        const raw = localStorage.getItem(KEY);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

export function saveAuth(auth) {
    try {
        localStorage.setItem(KEY, JSON.stringify(auth));
    } catch {
        /* storage unavailable, session stays in memory only */
    }
}

export function clearAuth() {
    try {
        localStorage.removeItem(KEY);
    } catch {
        /* ignore */
    }
}

export function toAuth(response) {
    return {
        user: { name: response.name, gender: response.gender },
        tokens: {
            accessToken: response.accessToken,
            refreshToken: response.refreshToken,
        },
    };
}