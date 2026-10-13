import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { formatCompact, formatINR, shortDate } from '../utils/format.js';

export default function DailyBar({ data }) {
    const rows = (data || []).map((d) => ({
        day: Number(d.date.slice(8, 10)),
        label: shortDate(d.date),
        total: Number(d.total),
    }));
    const hasSpend = rows.some((r) => r.total > 0);

    return (
        <div className="card">
            <h2 className="text-base font-semibold text-slate-900">Daily spending</h2>

            {!hasSpend ? (
                <p className="py-10 text-center text-sm text-slate-500">No spending recorded this month.</p>
            ) : (
                <div className="mt-4 h-56 w-full">
                    <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={rows} margin={{ top: 8, right: 4, left: -8, bottom: 0 }}>
                            <CartesianGrid vertical={false} stroke="#e2e8f0" />
                            <XAxis
                                dataKey="day"
                                tickLine={false}
                                axisLine={false}
                                interval="preserveStartEnd"
                                minTickGap={12}
                                tick={{ fontSize: 11, fill: '#64748b' }}
                            />
                            <YAxis
                                tickLine={false}
                                axisLine={false}
                                width={44}
                                tickFormatter={formatCompact}
                                tick={{ fontSize: 11, fill: '#64748b' }}
                            />
                            <Tooltip
                                cursor={{ fill: '#eef2ff' }}
                                labelFormatter={(_, payload) => payload?.[0]?.payload?.label}
                                formatter={(value) => [formatINR(value), 'Spent']}
                            />
                            <Bar dataKey="total" fill="#6366f1" radius={[4, 4, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            )}
        </div>
    );
}