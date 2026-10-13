import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import { monthLabel } from '../utils/format.js';

const AMOUNT_RE = /^\d{1,13}(\.\d{1,2})?$/;

export default function SetLimitForm({ month, currentLimit, onSaved }) {
    const [value, setValue] = useState('');
    const [error, setError] = useState('');
    const [saved, setSaved] = useState(false);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        setValue(currentLimit !== null && currentLimit !== undefined ? String(currentLimit) : '');
        setError('');
    }, [month, currentLimit]);

    useEffect(() => {
        setSaved(false);
    }, [month]);

    const onChange = (e) => {
        setValue(e.target.value);
        setError('');
        setSaved(false);
    };

    const onSubmit = async (e) => {
        e.preventDefault();
        const trimmed = value.trim();

        if (!AMOUNT_RE.test(trimmed) || Number(trimmed) <= 0) {
            setError('Enter an amount greater than 0 (max 2 decimals)');
            return;
        }

        setSaving(true);
        setError('');
        try {
            await api.put('/api/budget', { month, limitAmount: Number(trimmed) });
            setSaved(true);
            onSaved?.();
        } catch (err) {
            setError(err.fieldErrors?.limitAmount || err.fieldErrors?.month || err.message || 'Could not save');
        } finally {
            setSaving(false);
        }
    };

    return (
        <form onSubmit={onSubmit} className="card" noValidate>
            <h2 className="text-base font-semibold text-slate-900">Monthly limit</h2>
            <p className="mt-1 text-xs text-slate-500">For {monthLabel(month)}</p>

            <label htmlFor="limitAmount" className="label mt-4">
                Amount (₹)
            </label>
            <input
                id="limitAmount"
                name="limitAmount"
                type="text"
                inputMode="decimal"
                autoComplete="off"
                placeholder="e.g. 20000"
                value={value}
                onChange={onChange}
                className={`input ${error ? 'input-error' : ''}`}
                aria-invalid={Boolean(error)}
                aria-describedby={error ? 'limitAmount-error' : undefined}
            />
            {error && (
                <p id="limitAmount-error" className="field-error">
                    {error}
                </p>
            )}
            {saved && !error && <p className="mt-1 text-xs text-emerald-600">Limit saved.</p>}

            <button type="submit" disabled={saving} className="btn-primary mt-4 w-full">
                {saving ? 'Saving...' : currentLimit != null ? 'Update limit' : 'Set limit'}
            </button>
        </form>
    );
}