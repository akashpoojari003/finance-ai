import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import AuthShell from '../components/AuthShell.jsx';
import FormField from '../components/FormField.jsx';
import { validateLogin } from '../utils/validation.js';

export default function Login() {
    const { login, isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const redirectTo = location.state?.from?.pathname || '/';

    const [form, setForm] = useState({ email: '', password: '' });
    const [fieldErrors, setFieldErrors] = useState({});
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    if (isAuthenticated) return <Navigate to={redirectTo} replace />;

    const onChange = (e) => {
        const { name, value } = e.target;
        setForm((f) => ({ ...f, [name]: value }));
        setFieldErrors((errs) => ({ ...errs, [name]: undefined }));
    };

    const onSubmit = async (e) => {
        e.preventDefault();
        setError('');

        const errors = validateLogin(form);
        setFieldErrors(errors);
        if (Object.keys(errors).length > 0) return;

        setLoading(true);
        try {
            await login(form.email.trim(), form.password);
            navigate(redirectTo, { replace: true });
        } catch (err) {
            const serverFieldErrors = err.fieldErrors || {};
            setFieldErrors(serverFieldErrors);
            if (Object.keys(serverFieldErrors).length === 0) {
                setError(err.message || 'Login failed');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <AuthShell
            title="Welcome back"
            subtitle="Log in to see your spending."
            footer={
                <>
                    New here?{' '}
                    <Link to="/signup" className="font-semibold text-brand-600 hover:underline">
                        Create an account
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
                    autoComplete="current-password"
                    placeholder="Your password"
                    value={form.password}
                    onChange={onChange}
                    error={fieldErrors.password}
                />

                <button type="submit" disabled={loading} className="btn-primary w-full">
                    {loading ? 'Logging in...' : 'Log in'}
                </button>
            </form>
        </AuthShell>
    );
}