import { useEffect, useMemo, useRef, useState } from 'react';
import { api } from '../api/client.js';
import ExpenseForm from '../components/ExpenseForm.jsx';
import Modal from '../components/Modal.jsx';
import MonthPicker from '../components/MonthPicker.jsx';
import Pagination from '../components/Pagination.jsx';
import ToastStack, { useToasts } from '../components/Toast.jsx';
import { currentMonth, formatINR, isValidMonth, longDate, monthLabel } from '../utils/format.js';

const PAGE_SIZE = 10;

const byNewest = (a, b) => {
    if (a.date !== b.date) return a.date < b.date ? 1 : -1;
    if (Boolean(a.pending) !== Boolean(b.pending)) return a.pending ? -1 : 1;
    return (Number(b.id) || 0) - (Number(a.id) || 0);
};

const upsert = (list, item) => {
    const exists = list.some((e) => e.id === item.id);
    const next = exists ? list.map((e) => (e.id === item.id ? item : e)) : [...list, item];
    return next.sort(byNewest);
};

const errorText = (err) =>
    Object.values(err.fieldErrors || {})[0] || err.message || 'Something went wrong';

function CategoryBadge({ category, pending }) {
    if (!category) {
        return (
            <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-500">
        {pending ? 'Auto…' : 'Other'}
      </span>
        );
    }
    return (
        <span className="rounded-full bg-brand-50 px-2 py-0.5 text-xs font-medium text-brand-700">
      {category}
    </span>
    );
}

function TableSkeleton() {
    return (
        <div className="animate-pulse divide-y divide-slate-100 rounded-2xl bg-white shadow-card" aria-busy="true">
            {[0, 1, 2, 3, 4].map((i) => (
                <div key={i} className="flex items-center justify-between px-4 py-4">
                    <div className="h-4 w-1/3 rounded bg-slate-200" />
                    <div className="h-4 w-16 rounded bg-slate-200" />
                </div>
            ))}
        </div>
    );
}

export default function Expenses() {
    const [month, setMonth] = useState(currentMonth);
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [reloadKey, setReloadKey] = useState(0);
    const [page, setPage] = useState(1);
    const [formState, setFormState] = useState(null);
    const [deleteTarget, setDeleteTarget] = useState(null);
    const { toasts, push, dismiss } = useToasts();

    const monthRef = useRef(month);
    useEffect(() => {
        monthRef.current = month;
    }, [month]);
    const inMonth = (date) => date.startsWith(monthRef.current);

    useEffect(() => {
        let active = true;
        setLoading(true);
        setError('');
        api
            .get('/api/expenses', { params: { month } })
            .then((res) => {
                if (active) setItems((res || []).sort(byNewest));
            })
            .catch((err) => {
                if (active) setError(err.message || 'Could not load expenses');
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
        setItems([]);
        setPage(1);
        setMonth(next);
    };

    const total = useMemo(() => items.reduce((sum, e) => sum + Number(e.amount), 0), [items]);
    const pageCount = Math.max(1, Math.ceil(items.length / PAGE_SIZE));
    const currentPage = Math.min(page, pageCount);
    const pageItems = items.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

    /* ---------- optimistic mutations ---------- */

    const createExpense = async (payload) => {
        const tempId = `tmp-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
        if (inMonth(payload.date)) {
            setItems((prev) => upsert(prev, { id: tempId, ...payload, pending: true }));
        }
        try {
            const saved = await api.post('/api/expenses', payload);
            setItems((prev) => {
                const without = prev.filter((e) => e.id !== tempId);
                return inMonth(saved.date) ? upsert(without, saved) : without;
            });
            push(
                'success',
                inMonth(saved.date)
                    ? 'Expense added'
                    : `Expense added to ${monthLabel(saved.date.slice(0, 7))}`
            );
        } catch (err) {
            setItems((prev) => prev.filter((e) => e.id !== tempId));
            push('error', errorText(err));
        }
    };

    const updateExpense = async (original, payload) => {
        const optimistic = {
            ...original,
            amount: payload.amount,
            note: payload.note,
            date: payload.date,
            category: payload.category || original.category,
            pending: true,
        };
        setItems((prev) =>
            inMonth(optimistic.date)
                ? upsert(prev, optimistic)
                : prev.filter((e) => e.id !== original.id)
        );
        try {
            const saved = await api.put(`/api/expenses/${original.id}`, payload);
            setItems((prev) =>
                inMonth(saved.date) ? upsert(prev, saved) : prev.filter((e) => e.id !== saved.id)
            );
            push(
                'success',
                inMonth(saved.date)
                    ? 'Expense updated'
                    : `Expense moved to ${monthLabel(saved.date.slice(0, 7))}`
            );
        } catch (err) {
            setItems((prev) => upsert(prev, original));
            push('error', errorText(err));
        }
    };

    const deleteExpense = async (target) => {
        setItems((prev) => prev.filter((e) => e.id !== target.id));
        try {
            await api.delete(`/api/expenses/${target.id}`);
            push('success', 'Expense deleted');
        } catch (err) {
            if (err.status !== 404) {
                setItems((prev) => upsert(prev, target));
            }
            push('error', errorText(err));
        }
    };

    /* ---------- handlers ---------- */

    const handleSave = (payload) => {
        const target = formState?.expense;
        setFormState(null);
        if (target) updateExpense(target, payload);
        else createExpense(payload);
    };

    const handleConfirmDelete = () => {
        const target = deleteTarget;
        setDeleteTarget(null);
        if (target) deleteExpense(target);
    };

    const closeForm = () => setFormState(null);
    const closeDelete = () => setDeleteTarget(null);

    /* ---------- render ---------- */

    return (
        <div className="space-y-4">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-xl font-semibold text-slate-900">Expenses</h1>
                    <p className="text-sm text-slate-500">
                        {monthLabel(month)}
                        {items.length > 0 &&
                            ` · ${items.length} ${items.length === 1 ? 'expense' : 'expenses'} · ${formatINR(total)}`}
                    </p>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                    <MonthPicker month={month} onChange={changeMonth} />
                    <button type="button" className="btn-primary" onClick={() => setFormState({ expense: null })}>
                        + Add
                    </button>
                </div>
            </div>

            {loading && items.length === 0 && !error && <TableSkeleton />}

            {!loading && error && items.length === 0 && (
                <div className="card text-center">
                    <p className="alert-error inline-block">{error}</p>
                    <div className="mt-4">
                        <button type="button" className="btn-primary" onClick={() => setReloadKey((k) => k + 1)}>
                            Try again
                        </button>
                    </div>
                </div>
            )}

            {!loading && !error && items.length === 0 && (
                <div className="card flex flex-col items-start gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <p className="text-sm text-slate-600">No expenses in {monthLabel(month)} yet.</p>
                    <button type="button" className="btn-primary" onClick={() => setFormState({ expense: null })}>
                        Add your first expense
                    </button>
                </div>
            )}

            {items.length > 0 && (
                <div
                    className={`overflow-hidden rounded-2xl bg-white shadow-card transition-opacity ${
                        loading ? 'opacity-60' : ''
                    }`}
                >
                    <table className="w-full text-left text-sm">
                        <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                        <tr>
                            <th className="px-4 py-3 font-medium">Date</th>
                            <th className="px-2 py-3 font-medium">Category</th>
                            <th className="hidden px-2 py-3 font-medium sm:table-cell">Note</th>
                            <th className="px-2 py-3 text-right font-medium">Amount</th>
                            <th className="px-4 py-3" />
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                        {pageItems.map((e) => (
                            <tr key={e.id} className={e.pending ? 'opacity-60' : ''}>
                                <td className="whitespace-nowrap px-4 py-3 text-slate-600">{longDate(e.date)}</td>
                                <td className="px-2 py-3">
                                    <CategoryBadge category={e.category} pending={e.pending} />
                                    {e.note && (
                                        <p className="mt-1 max-w-[9rem] truncate text-xs text-slate-500 sm:hidden">
                                            {e.note}
                                        </p>
                                    )}
                                </td>
                                <td className="hidden max-w-xs truncate px-2 py-3 text-slate-600 sm:table-cell">
                                    {e.note || <span className="text-slate-300">—</span>}
                                </td>
                                <td className="whitespace-nowrap px-2 py-3 text-right font-semibold text-slate-900">
                                    {formatINR(e.amount)}
                                </td>
                                <td className="whitespace-nowrap px-4 py-3 text-right">
                                    <button
                                        type="button"
                                        disabled={e.pending}
                                        onClick={() => setFormState({ expense: e })}
                                        className="px-1 text-xs font-semibold text-brand-600 hover:underline disabled:opacity-40"
                                    >
                                        Edit
                                    </button>
                                    <button
                                        type="button"
                                        disabled={e.pending}
                                        onClick={() => setDeleteTarget(e)}
                                        className="px-1 text-xs font-semibold text-red-600 hover:underline disabled:opacity-40"
                                    >
                                        Delete
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    <Pagination page={currentPage} pageSize={PAGE_SIZE} total={items.length} onChange={setPage} />
                </div>
            )}

            {formState && (
                <ExpenseForm expense={formState.expense} onSubmit={handleSave} onClose={closeForm} />
            )}

            {deleteTarget && (
                <Modal title="Delete expense?" onClose={closeDelete}>
                    <p className="text-sm text-slate-600">
                        {formatINR(deleteTarget.amount)} on {longDate(deleteTarget.date)}
                        {deleteTarget.note ? ` (${deleteTarget.note})` : ''} will be removed.
                    </p>
                    <div className="mt-5 flex gap-3">
                        <button type="button" className="btn-secondary flex-1" onClick={closeDelete}>
                            Cancel
                        </button>
                        <button type="button" className="btn-danger flex-1" onClick={handleConfirmDelete}>
                            Delete
                        </button>
                    </div>
                </Modal>
            )}

            <ToastStack toasts={toasts} onDismiss={dismiss} />
        </div>
    );
}