import axios from 'axios';
import { clearAuth, loadAuth, saveAuth, toAuth } from '../auth/authStorage.js';

const BASE_URL = import.meta.env.VITE_API_URL || '';

export const AUTH_UPDATED_EVENT = 'auth:updated';
export const AUTH_LOGOUT_EVENT = 'auth:logout';

export class ApiError extends Error {
    constructor(status, message, fieldErrors = {}) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.fieldErrors = fieldErrors;
    }
}

const http = axios.create({
    baseURL: BASE_URL,
    timeout: 15000,
    headers: { 'Content-Type': 'application/json' },
});

/* ---------- request: attach Bearer token ---------- */
http.interceptors.request.use((config) => {
    if (!config.skipAuth) {
        const token = loadAuth()?.tokens?.accessToken;
        if (token) config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

/* ---------- refresh (single in-flight, bare axios to avoid interceptor loops) ---------- */
let refreshPromise = null;

function refreshTokens() {
    if (!refreshPromise) {
        refreshPromise = (async () => {
            const refreshToken = loadAuth()?.tokens?.refreshToken;
            if (!refreshToken) throw new Error('No refresh token');

            const { data } = await axios.post(
                `${BASE_URL}/api/auth/refresh`,
                { refreshToken },
                { timeout: 15000, headers: { 'Content-Type': 'application/json' } }
            );
            saveAuth(toAuth(data));
            window.dispatchEvent(new Event(AUTH_UPDATED_EVENT));
            return data.accessToken;
        })().finally(() => {
            refreshPromise = null;
        });
    }
    return refreshPromise;
}

function forceLogout() {
    clearAuth();
    window.dispatchEvent(new Event(AUTH_LOGOUT_EVENT));
}

function toApiError(error, fallbackMessage) {
    if (error instanceof ApiError) return error;
    const res = error.response;
    if (!res) {
        const message =
            error.code === 'ECONNABORTED' ? 'Request timed out. Try again.' : 'Cannot reach the server.';
        return new ApiError(0, message);
    }
    const body = res.data;
    return new ApiError(
        res.status,
        fallbackMessage || body?.message || `Request failed (${res.status})`,
        body?.fieldErrors || {}
    );
}

/* ---------- response: on 401 refresh once and retry, else log out ---------- */
http.interceptors.response.use(
    (response) => response,
    async (error) => {
        const original = error.config;
        const status = error.response?.status;

        if (status === 401 && original && !original.skipAuth) {
            if (original._retry) {
                forceLogout();
                return Promise.reject(toApiError(error, 'Session expired. Please log in again.'));
            }
            original._retry = true;
            try {
                const token = await refreshTokens();
                original.headers.Authorization = `Bearer ${token}`;
                return http(original);
            } catch {
                forceLogout();
                return Promise.reject(toApiError(error, 'Session expired. Please log in again.'));
            }
        }

        return Promise.reject(toApiError(error));
    }
);

/* ---------- thin wrapper: returns response data, throws ApiError ---------- */
const unwrap = (promise) => promise.then((res) => (res.status === 204 ? null : res.data));

export const api = {
    get: (path, config) => unwrap(http.get(path, config)),
    post: (path, body, { auth = true } = {}) => unwrap(http.post(path, body, { skipAuth: !auth })),
    put: (path, body) => unwrap(http.put(path, body)),
    delete: (path) => unwrap(http.delete(path)),
};

export default http;