import { useEffect, useState } from 'react';
import { api } from '../api/client.js';

export default function QuoteCard() {
    const [text, setText] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let active = true;
        api
            .get('/api/quote')
            .then((data) => {
                if (active) setText(data?.text || null);
            })
            .catch(() => {})
            .finally(() => {
                if (active) setLoading(false);
            });
        return () => {
            active = false;
        };
    }, []);

    if (loading) {
        return (
            <div className="card animate-pulse">
                <div className="h-4 w-3/4 rounded bg-slate-200" />
            </div>
        );
    }

    if (!text) return null;

    return (
        <div className="rounded-2xl bg-gradient-to-r from-brand-600 to-brand-500 p-4 text-white shadow-card sm:p-6">
            <p className="text-sm font-medium leading-relaxed sm:text-base">“{text}”</p>
        </div>
    );
}