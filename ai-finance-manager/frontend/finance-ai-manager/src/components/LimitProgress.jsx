import { formatINR } from '../utils/format.js';

function tone(percent) {
    if (percent >= 100) {
        return { bar: 'bg-red-500', text: 'text-red-600', label: 'Over budget' };
    }
    if (percent >= 80) {
        return { bar: 'bg-amber-500', text: 'text-amber-600', label: 'Near your limit' };
    }
    return { bar: 'bg-emerald-500', text: 'text-emerald-600', label: 'On track' };
}

export default function LimitProgress({ spent, limit, percent, remaining }) {
    if (limit === null || limit === undefined || percent === null || percent === undefined) {
        return (
            <div className="card">
                <h2 className="text-base font-semibold text-slate-900">Budget progress</h2>
                <p className="mt-2 text-sm text-slate-500">
                    No limit set for this month. Set one to track your spending and get alerts.
                </p>
            </div>
        );
    }

    const pct = Number(percent);
    const t = tone(pct);
    const width = Math.min(Math.max(pct, 0), 100);

    return (
        <div className="card">
            <div className="flex items-baseline justify-between gap-2">
                <h2 className="text-base font-semibold text-slate-900">Budget progress</h2>
                <span className={`text-sm font-semibold ${t.text}`}>{t.label}</span>
            </div>

            <div
                className="mt-4 h-3 w-full overflow-hidden rounded-full bg-slate-100"
                role="progressbar"
                aria-valuemin={0}
                aria-valuemax={100}
                aria-valuenow={Math.round(width)}
                aria-label="Budget used"
            >
                <div className={`h-full rounded-full transition-all ${t.bar}`} style={{ width: `${width}%` }} />
            </div>

            <div className="mt-3 flex items-center justify-between text-sm">
        <span className="text-slate-600">
          {formatINR(spent)} of {formatINR(limit)}
        </span>
                <span className={`font-semibold ${t.text}`}>{pct.toFixed(0)}%</span>
            </div>

            <p className="mt-1 text-xs text-slate-500">
                {Number(remaining) < 0
                    ? `Over by ${formatINR(Math.abs(Number(remaining)))}`
                    : `${formatINR(remaining)} left this month`}
            </p>
        </div>
    );
}