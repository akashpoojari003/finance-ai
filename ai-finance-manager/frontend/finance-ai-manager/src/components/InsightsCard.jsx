import { useEffect, useRef, useState } from 'react';
import { api } from '../api/client.js';

function Skeleton() {
    return (
        <div className="mt-4 animate-pulse space-y-3" aria-hidden="true">
            <div className="h-4 w-11/12 rounded bg-slate-200" />
            <div className="h-4 w-4/5 rounded bg-slate-200" />
            <div className="h-4 w-3/4 rounded bg-slate-200" />
        </div>
    );
}

/** Loads only once it scrolls into view, and only when there is spending to analyse. */
export default function InsightsCard({ month, enabled }) {
    const ref = useRef(null);
    const [visible, setVisible] = useState(false);
    const [retryKey, setRetryKey] = useState(0);
    const [state, setState] = useState({ status: 'idle', tips: [] });

    useEffect(() => {
        const el = ref.current;
        if (!el || typeof IntersectionObserver === 'undefined') {
            setVisible(true);
            return undefined;
        }
        const observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.isIntersecting) {
                    setVisible(true);
                    observer.disconnect();
                }
            },
            { rootMargin: '120px' }
        );
        observer.observe(el);
        return () => observer.disconnect();
    }, []);

    useEffect(() => {
        if (!visible || !enabled) {
            setState({ status: 'idle', tips: [] });
            return undefined;
        }
        let active = true;
        setState({ status: 'loading', tips: [] });
        api
            .get('/api/insights', { params: { month } })
            .then((data) => {
                if (active) setState({ status: 'done', tips: data?.tips || [] });
            })
            .catch(() => {
                if (active) setState({ status: 'error', tips: [] });
            });
        return () => {
            active = false;
        };
    }, [visible, enabled, month, retryKey]);

    return (
        <div ref={ref} className="card">
            <h2 className="text-base font-semibold text-slate-900">Saving tips</h2>

            {!enabled && (
                <p className="mt-2 text-sm text-slate-500">Add some expenses to get personalised tips.</p>
            )}

            {enabled && (state.status === 'idle' || state.status === 'loading') && <Skeleton />}

            {enabled && state.status === 'error' && (
                <div className="mt-2 flex items-center justify-between gap-3 text-sm">
                    <span className="text-slate-500">Couldn't load tips.</span>
                    <button
                        type="button"
                        onClick={() => setRetryKey((k) => k + 1)}
                        className="font-semibold text-brand-600 hover:underline"
                    >
                        Retry
                    </button>
                </div>
            )}

            {enabled && state.status === 'done' && state.tips.length === 0 && (
                <p className="mt-2 text-sm text-slate-500">No tips available right now.</p>
            )}

            {enabled && state.status === 'done' && state.tips.length > 0 && (
                <ul className="mt-3 space-y-3">
                    {state.tips.map((tip, i) => (
                        <li key={tip} className="flex gap-3 text-sm text-slate-700">
              <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-brand-50 text-xs font-semibold text-brand-700">
                {i + 1}
              </span>
                            <span className="pt-0.5">{tip}</span>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}