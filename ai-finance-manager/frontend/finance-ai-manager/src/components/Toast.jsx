import { useCallback, useEffect, useRef, useState } from 'react';

const TONES = {
    success: 'bg-emerald-600',
    error: 'bg-red-600',
    info: 'bg-slate-800',
};

export function useToasts() {
    const [toasts, setToasts] = useState([]);
    const timers = useRef(new Map());

    const dismiss = useCallback((id) => {
        clearTimeout(timers.current.get(id));
        timers.current.delete(id);
        setToasts((list) => list.filter((t) => t.id !== id));
    }, []);

    const push = useCallback(
        (type, message) => {
            const id = `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
            setToasts((list) => [...list.slice(-2), { id, type, message }]);
            timers.current.set(
                id,
                setTimeout(() => dismiss(id), 3500)
            );
        },
        [dismiss]
    );

    useEffect(() => {
        const map = timers.current;
        return () => map.forEach((timer) => clearTimeout(timer));
    }, []);

    return { toasts, push, dismiss };
}

export default function ToastStack({ toasts, onDismiss }) {
    return (
        <div
            className="pointer-events-none fixed inset-x-0 bottom-20 z-50 flex flex-col items-center gap-2 px-4 md:bottom-6"
            aria-live="polite"
        >
            {toasts.map((t) => (
                <div
                    key={t.id}
                    role={t.type === 'error' ? 'alert' : 'status'}
                    className={`pointer-events-auto flex w-full max-w-sm items-start justify-between gap-3 rounded-xl px-4 py-3 text-sm text-white shadow-card ${
                        TONES[t.type] || TONES.info
                    }`}
                >
                    <span>{t.message}</span>
                    <button
                        type="button"
                        onClick={() => onDismiss(t.id)}
                        className="text-white/80 hover:text-white"
                        aria-label="Dismiss"
                    >
                        ✕
                    </button>
                </div>
            ))}
        </div>
    );
}