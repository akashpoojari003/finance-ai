import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import AuthShell from '../components/AuthShell.jsx';
import FormField from '../components/FormField.jsx';
import { GENDERS, normalizePhone, validateSignup } from '../utils/validation.js';

export default function Signup() {
    const { signup, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const [form, setForm] = useState({
        name: '',
        email: '',
        password: '',
        gender: '',
        phone: '',
    });
    const [fieldErrors, setFieldErrors] = useState({});
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    if (isAuthenticated) return <Navigate to="/" replace />;

    const onChange = (e) => {
        const { name, value } = e.target;
        setForm((f) => ({ ...f, [name]: value }));
        setFieldErrors((errs) => ({ ...errs, [name]: undefined }));
    };

    const onSubmit = async (e) => {
        e.preventDefault();
        setError('');

        const errors = validateSignup(form);
        setFieldErrors(errors);
        if (Object.keys(errors).length > 0) return;

        setLoading(true);
        try {
            const phone = normalizePhone(form.phone);
            await signup({
                name: form.name.trim(),
                email: form.email.trim(),
                password: form.password,
                gender: form.gender,
                phone: phone || undefined,
            });
            navigate('/', { replace: true });
        } catch (err) {
            const serverFieldErrors = { ...(err.fieldErrors || {}) };
            if (err.status === 409) {
                serverFieldErrors.email = 'This email is already registered';
            }
            setFieldErrors(serverFieldErrors);
            if (Object.keys(serverFieldErrors).length === 0) {
                setError(err.message || 'Signup failed');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <AuthShell
            title="Create your account"
            subtitle="Set a budget and get alerts before you overspend."
            footer={
                <>
                    Already have an account?{' '}
                    <Link to="/login" className="font-semibold text-brand-600 hover:underline">
                        Log in
                    </Link>
                </>
            }
        >
            <form onSubmit={onSubmit} className="space-y-4" noValidate>
                {error && (
                    <div className="alert-error" role="alert">
                        {error}
                    </div>
                )}

                <FormField
                    id="name"
                    label="Name"
                    autoComplete="name"
                    placeholder="Your name"
                    value={form.name}
                    onChange={onChange}
                    error={fieldErrors.name}
                />

                <FormField
                    id="email"
                    label="Email"
                    type="email"
                    autoComplete="email"
                    placeholder="you@example.com"
                    value={form.email}
                    onChange={onChange}
                    error={fieldErrors.email}
                />

                <FormField
                    id="password"
                    label="Password"
                    type="password"
                    autoComplete="new-password"
                    placeholder="At least 8 characters"
                    value={form.password}
                    onChange={onChange}
                    error={fieldErrors.password}
                />

                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
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

                    <FormField
                        id="phone"
                        label="Phone (optional)"
                        type="tel"
                        autoComplete="tel"
                        placeholder="9876543210"
                        value={form.phone}
                        onChange={onChange}
                        error={fieldErrors.phone}
                    />
                </div>

                <button type="submit" disabled={loading} className="btn-primary w-full">
                    {loading ? 'Creating account...' : 'Sign up'}
                </button>
            </form>
        </AuthShell>
    );
}