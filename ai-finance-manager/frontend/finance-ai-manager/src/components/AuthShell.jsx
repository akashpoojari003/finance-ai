export default function AuthShell({ title, subtitle, children, footer }) {
    return (
        <div className="flex min-h-screen items-center justify-center bg-gradient-to-b from-brand-50 to-slate-50 px-4 py-8">
            <div className="w-full max-w-md">
                <div className="mb-6 text-center">
                    <h1 className="text-2xl font-bold text-brand-600">FinanceAI</h1>
                    <p className="mt-1 text-sm text-slate-500">Track spending. Stay on budget.</p>
                </div>
                <div className="card">
                    <h2 className="text-xl font-semibold text-slate-900">{title}</h2>
                    {subtitle && <p className="mt-1 text-sm text-slate-500">{subtitle}</p>}
                    <div className="mt-5">{children}</div>
                </div>
                <p className="mt-4 text-center text-sm text-slate-600">{footer}</p>
            </div>
        </div>
    );
}