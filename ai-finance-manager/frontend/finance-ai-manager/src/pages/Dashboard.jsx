import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../auth/AuthContext.jsx';
import CategoryPie from '../components/CategoryPie.jsx';
import DailyBar from '../components/DailyBar.jsx';
import InsightsCard from '../components/InsightsCard.jsx';
import LimitProgress from '../components/LimitProgress.jsx';
import QuoteCard from '../components/QuoteCard.jsx';
import SetLimitForm from '../components/SetLimitForm.jsx';
import StatCard from '../components/StatCard.jsx';
import { currentMonth, formatINR, isValidMonth, monthLabel } from '../utils/format.js';
import MonthPicker from '../components/MonthPicker.jsx';

function DashboardSkeleton() {
    return (
        <div className="animate-pulse space-y-4" aria-busy="true" aria-label="Loading dashboard">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                {[0, 1, 2].map((i) => (
                    <div key={i} className="card space-y-3">
                        <div className="h-3 w-16 rounded bg-slate-200" />
                        <div className="h-7 w-28 rounded bg-slate-200" />
                    </div>
                ))}
            </div>
            <div className="card h-28" />
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div className="card h-72" />
                <div className="card h-72" />
            </div>
        </div>
    );
}

// function MonthPicker({ month, onChange }) {
//     return (
//         <div className="flex items-center gap-2">
//             <button
//                 type="button"
//                 onClick={() => onChange(shiftMonth(month, -1))}
//                 className="btn-secondary px-3 py-2"
//                 aria-label="Previous month"
//             >
//                 ‹
//             </button>
//             <input
//                 type="month"
//                 value={month}
//                 onChange={(e) => onChange(e.target.value)}
//                 className="input w-auto py-2"
//                 aria-label="Select month"
//             />
//             <button
//                 type="button"
//                 onClick={() => onChange(shiftMonth(month, 1))}
//                 className="btn-secondary px-3 py-2"
//                 aria-label="Next month"
//             >
//                 ›
//             </button>
//         </div>
//     );
// }

export default function Dashboard() {
    const { user } = useAuth();
    const [month, setMonth] = useState(currentMonth);
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [reloadKey, setReloadKey] = useState(0);

    useEffect(() => {
        let active = true;
        setLoading(true);
        setError('');
        api
            .get('/api/dashboard', { params: { month } })
            .then((res) => {
                if (active) setData(res);
            })
            .catch((err) => {
                if (active) setError(err.message || 'Could not load dashboard');
            })
            .finally(() => {
                if (active) setLoading(false);
            });
        return () => {
            active = false;
        };
    }, [month, reloadKey]);

    const changeMonth = (next) => {
        if (!isValidMonth(next) || next === month) return;
        setData(null);
        setMonth(next);
    };

    const reload = () => setReloadKey((k) => k + 1);

    const hasSpend = data ? Number(data.totalSpent) > 0 : false;
    const remainingNegative = data?.remaining != null && Number(data.remaining) < 0;
    const firstName = user?.name?.split(' ')[0];

    return (
        <div className="space-y-4">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-xl font-semibold text-slate-900">
                        {firstName ? `Hi, ${firstName}` : 'Dashboard'}
                    </h1>
                    <p className="text-sm text-slate-500">{monthLabel(month)}</p>
                </div>
                <MonthPicker month={month} onChange={changeMonth} />
            </div>

            <QuoteCard />

            {loading && !data && <DashboardSkeleton />}

            {!loading && !data && error && (
                <div className="card text-center">
                    <p className="alert-error inline-block">{error}</p>
                    <div className="mt-4">
                        <button type="button" onClick={reload} className="btn-primary">
                            Try again
                        </button>
                    </div>
                </div>
            )}

            {data && (
                <div className={`space-y-4 transition-opacity ${loading ? 'opacity-60' : ''}`}>
                    {error && <div className="alert-error">{error}</div>}

                    {!hasSpend && (
                        <div className="card flex flex-col items-start gap-3 sm:flex-row sm:items-center sm:justify-between">
                            <p className="text-sm text-slate-600">
                                No expenses recorded for {monthLabel(month)} yet.
                            </p>
                            <Link to="/expenses" className="btn-primary">
                                Add an expense
                            </Link>
                        </div>
                    )}

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                        <StatCard label="Spent" value={formatINR(data.totalSpent)} />
                        <StatCard
                            label="Limit"
                            value={formatINR(data.limitAmount)}
                            hint={data.limitAmount == null ? 'No limit set' : undefined}
                        />
                        <StatCard
                            label="Remaining"
                            value={formatINR(data.remaining)}
                            tone={remainingNegative ? 'danger' : data.remaining != null ? 'success' : 'default'}
                            hint={data.remaining == null ? 'Set a limit to see this' : undefined}
                        />
                    </div>

                    <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                        <div className="md:col-span-2">
                            <LimitProgress
                                spent={data.totalSpent}
                                limit={data.limitAmount}
                                percent={data.percentUsed}
                                remaining={data.remaining}
                            />
                        </div>
                        <SetLimitForm month={month} currentLimit={data.limitAmount} onSaved={reload} />
                    </div>

                    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                        <CategoryPie data={data.byCategory} />
                        <DailyBar data={data.daily} />
                    </div>

                    <InsightsCard month={month} enabled={hasSpend} />
                </div>
            )}
        </div>
    );
}