import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import { useAuth } from '../auth/AuthContext.jsx';
import FormField from '../components/FormField.jsx';
import SetLimitForm from '../components/SetLimitForm.jsx';
import { currentMonth } from '../utils/format.js';
import { GENDERS } from '../utils/validation.js';

const E164_RE = /^\+[1-9]\d{6,14}$/;

const cleanPhone = (value) => (value || '').replace(/[\s-]/g, '');

/** Older accounts may hold a bare 10-digit Indian number; show it in E.164 form. */
function toE164Display(phone) {
    const p = cleanPhone(phone);
    if (!p) return '';
    if (p.startsWith('+')) return p;
    return /^\d{10}$/.test(p) ? `+91${p}` : `+${p}`;
}

function validate(form) {
    const errors = {};
    if (!form.name.trim()) errors.name = 'Name is required';
    else if (form.name.trim().length > 100) errors.name = 'Name must be 100 characters or fewer';

    if (!GENDERS.some((g) => g.value === form.gender)) errors.gender = 'Select a gender';

    const phone = cleanPhone(form.phone);
    if (phone && !E164_RE.test(phone)) {
        errors.phone = 'Use international format, e.g. +919876543210';
    } else if (form.whatsappOptIn && !phone) {
        errors.phone = 'Add a phone number to turn on WhatsApp reminders';
    }
    return errors;
}

function Toggle({ checked, onChange, label, description }) {
    return (
        <div className="flex items-start justify-between gap-4">
            <div>
                <p className="text-sm font-medium text-slate-900">{label}</p>
                {description && <p className="mt-0.5 text-xs text-slate-500">{description}</p>}
            </div>
            <button
                type="button"
                role="switch"
                aria-checked={checked}
                aria-label={label}
                onClick={() => onChange(!checked)}
                className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition focus:outline-none focus:ring-2 focus:ring-brand-300 ${
                    checked ? 'bg-brand-600' : 'bg-slate-300'
                }`}
            >
        <span
            className={`inline-block h-5 w-5 transform rounded-full bg-white shadow transition ${
                checked ? 'translate-x-5' : 'translate-x-0.5'
            }`}
        />
            </button>
        </div>
    );
}

export default function Settings() {
    const { logout, updateUser } = useAuth();
    const month = currentMonth();

    const [email, setEmail] = useState('');
    const [form, setForm] = useState(null);
    const [limit, setLimit] = useState(null);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState('');
    const [reloadKey, setReloadKey] = useState(0);

    const [fieldErrors, setFieldErrors] = useState({});
    const [error, setError] = useState('');
    const [saved, setSaved] = useState(false);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        let active = true;
        setLoading(true);
        setLoadError('');
        Promise.all([api.get('/api/profile'), api.get('/api/budget', { params: { month } })])
            .then(([profile, budget]) => {
                if (!active) return;
                setEmail(profile.email);
                setForm({
                    name: profile.name || '',
                    gender: profile.gender || '',
                    phone: toE164Display(profile.phone),
                    whatsappOptIn: Boolean(profile.whatsappOptIn),
                });
                setLimit(budget?.limitAmount ?? null);
            })
            .catch((err) => {
                if (active) setLoadError(err.message || 'Could not load settings');
            })
            .finally(() => {
                if (active) setLoading(false);
            });
        return () => {
            active = false;
        };
    }, [month, reloadKey]);

    const refreshLimit = () => {
        api
            .get('/api/budget', { params: { month } })
            .then((budget) => setLimit(budget?.limitAmount ?? null))
            .catch(() => {});
    };

    const onChange = (e) => {
        const { name, value } = e.target;
        setForm((f) => ({ ...f, [name]: value }));
        setFieldErrors((errs) => ({ ...errs, [name]: undefined }));
        setSaved(false);
    };

    const onToggle = (value) => {
        setForm((f) => ({ ...f, whatsappOptIn: value }));
        setFieldErrors((errs) => ({ ...errs, phone: undefined }));
        setSaved(false);
    };

    const onSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSaved(false);

        const found = validate(form);
        setFieldErrors(found);
        if (Object.keys(found).length > 0) return;

        setSaving(true);
        try {
            const phone = cleanPhone(form.phone);
            const res = await api.put('/api/profile', {
                name: form.name.trim(),
                gender: form.gender,
                phone: phone || null,
                whatsappOptIn: form.whatsappOptIn,
            });
            setForm({
                name: res.name,
                gender: res.gender,
                phone: toE164Display(res.phone),
                whatsappOptIn: res.whatsappOptIn,
            });
            updateUser({ name: res.name, gender: res.gender });
            setSaved(true);
        } catch (err) {
            const serverErrors = err.fieldErrors || {};
            setFieldErrors(serverErrors);
            if (Object.keys(serverErrors).length === 0) {
                setError(err.message || 'Could not save changes');
            }
        } finally {
            setSaving(false);
        }
    };

    if (loading && !form) {
        return (
            <div className="animate-pulse space-y-4" aria-busy="true">
                <div className="card h-64" />
                <div className="card h-40" />
            </div>
        );
    }

    if (loadError && !form) {
        return (
            <div className="card text-center">
                <p className="alert-error inline-block">{loadError}</p>
                <div className="mt-4">
                    <button type="button" className="btn-primary" onClick={() => setReloadKey((k) => k + 1)}>
                        Try again
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            <h1 className="text-xl font-semibold text-slate-900">Settings</h1>

            <form onSubmit={onSubmit} className="card space-y-4" noValidate>
                <div>
                    <h2 className="text-base font-semibold text-slate-900">Profile</h2>
                    <p className="mt-1 text-sm text-slate-500">{email}</p>
                </div>

                {error && (
                    <div className="alert-error" role="alert">
                        {error}
                    </div>
                )}

                <FormField
                    id="name"
                    label="Name"
                    autoComplete="name"
                    value={form.name}
                    onChange={onChange}
                    error={fieldErrors.name}
                />

                <FormField
                    id="gender"
                    label="Gender"
                    as="select"
                    value={form.gender}
                    onChange={onChange}
                    error={fieldErrors.gender}
                >
                    <option value="" disabled>
                        Select
                    </option>
                    {GENDERS.map((g) => (
                        <option key={g.value} value={g.value}>
                            {g.label}
                        </option>
                    ))}
                </FormField>

                <div className="space-y-4 rounded-xl border border-slate-100 bg-slate-50 p-4">
                    <Toggle
                        label="WhatsApp reminders"
                        description="Get budget alerts on WhatsApp. If delivery fails, we email you instead."
                        checked={form.whatsappOptIn}
                        onChange={onToggle}
                    />
                    <FormField
                        id="phone"
                        label="Phone (with country code)"
                        type="tel"
                        autoComplete="tel"
                        placeholder="+919876543210"
                        value={form.phone}
                        onChange={onChange}
                        error={fieldErrors.phone}
                    />
                </div>

                <div className="flex items-center gap-3">
                    <button type="submit" disabled={saving} className="btn-primary">
                        {saving ? 'Saving...' : 'Save changes'}
                    </button>
                    {saved && <span className="text-sm text-emerald-600">Saved</span>}
                </div>
            </form>

            <SetLimitForm month={month} currentLimit={limit} onSaved={refreshLimit} />

            <div className="card">
                <button type="button" onClick={logout} className="btn-danger w-full sm:w-auto">
                    Log out
                </button>
            </div>
        </div>
    );
}