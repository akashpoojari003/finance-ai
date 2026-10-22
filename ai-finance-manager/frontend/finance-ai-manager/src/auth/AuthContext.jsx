import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, AUTH_LOGOUT_EVENT, AUTH_UPDATED_EVENT } from '../api/client.js';
import { clearAuth, loadAuth, saveAuth, toAuth } from './authStorage.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [auth, setAuth] = useState(() => loadAuth());

    useEffect(() => {
        const syncFromStorage = () => setAuth(loadAuth());
        const handleLogout = () => setAuth(null);

        window.addEventListener(AUTH_UPDATED_EVENT, syncFromStorage);
        window.addEventListener(AUTH_LOGOUT_EVENT, handleLogout);
        window.addEventListener('storage', syncFromStorage);

        return () => {
            window.removeEventListener(AUTH_UPDATED_EVENT, syncFromStorage);
            window.removeEventListener(AUTH_LOGOUT_EVENT, handleLogout);
            window.removeEventListener('storage', syncFromStorage);
        };
    }, []);

    const persist = useCallback((response) => {
        const next = toAuth(response);
        saveAuth(next);
        setAuth(next);
    }, []);

    const login = useCallback(
        async (email, password) => {
            const data = await api.post(
                '/api/auth/login',
                { email, password },
                { auth: false }
            );
            persist(data);
        },
        [persist]
    );

    const signup = useCallback(
        async (form) => {
            const data = await api.post(
                '/api/auth/signup',
                form,
                { auth: false }
            );
            persist(data);
        },
        [persist]
    );

    const logout = useCallback(() => {
        clearAuth();
        setAuth(null);
    }, []);

    // ADD THIS HERE
    const updateUser = useCallback((patch) => {
        const current = loadAuth();
        if (!current) return;

        const next = {
            ...current,
            user: {
                ...current.user,
                ...patch,
            },
        };

        saveAuth(next);
        setAuth(next);
    }, []);

    const value = useMemo(
        () => ({
            user: auth?.user ?? null,
            tokens: auth?.tokens ?? null,
            isAuthenticated: Boolean(auth?.tokens?.accessToken),
            login,
            signup,
            logout,
            updateUser,
        }),
        [auth, login, signup, logout, updateUser]
    );

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const ctx = useContext(AuthContext);

    if (!ctx) {
        throw new Error('useAuth must be used inside AuthProvider');
    }

    return ctx;
}