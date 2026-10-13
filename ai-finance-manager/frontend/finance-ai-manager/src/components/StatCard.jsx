const TONES = {
    default: 'text-slate-900',
    success: 'text-emerald-600',
    danger: 'text-red-600',
};

export default function StatCard({ label, value, hint, tone = 'default' }) {
    return (
        <div className="card">
            <p className="text-sm text-slate-500">{label}</p>
            <p className={`mt-1 text-2xl font-bold ${TONES[tone] || TONES.default}`}>{value}</p>
            {hint && <p className="mt-1 text-xs text-slate-400">{hint}</p>}
        </div>
    );
}