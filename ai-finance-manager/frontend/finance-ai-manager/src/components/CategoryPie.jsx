import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import { formatINR } from '../utils/format.js';

const COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#06b6d4', '#8b5cf6', '#ec4899'];

export default function CategoryPie({ data }) {
    const items = (data || [])
        .map((d) => ({ name: d.category, value: Number(d.total) }))
        .filter((d) => d.value > 0);
    const total = items.reduce((sum, d) => sum + d.value, 0);

    return (
        <div className="card">
            <h2 className="text-base font-semibold text-slate-900">Spending by category</h2>

            {items.length === 0 ? (
                <p className="py-10 text-center text-sm text-slate-500">No expenses this month yet.</p>
            ) : (
                <>
                    <div className="mt-2 h-56 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={items}
                                    dataKey="value"
                                    nameKey="name"
                                    innerRadius={55}
                                    outerRadius={88}
                                    paddingAngle={2}
                                    stroke="none"
                                >
                                    {items.map((item, i) => (
                                        <Cell key={item.name} fill={COLORS[i % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip formatter={(value, name) => [formatINR(value), name]} />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>

                    <ul className="mt-2 space-y-2">
                        {items.map((item, i) => (
                            <li key={item.name} className="flex items-center justify-between text-sm">
                <span className="flex items-center gap-2 text-slate-700">
                  <span
                      className="h-2.5 w-2.5 rounded-full"
                      style={{ backgroundColor: COLORS[i % COLORS.length] }}
                  />
                    {item.name}
                </span>
                                <span className="text-slate-500">
                  {formatINR(item.value)} · {Math.round((item.value / total) * 100)}%
                </span>
                            </li>
                        ))}
                    </ul>
                </>
            )}
        </div>
    );
}