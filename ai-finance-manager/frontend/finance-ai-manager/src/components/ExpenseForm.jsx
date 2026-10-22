import { useState } from 'react';
import { CATEGORIES } from '../utils/categories.js';
import { todayIST } from '../utils/format.js';
import FormField from './FormField.jsx';
import Modal from './Modal.jsx';

const AMOUNT_RE = /^\d{1,13}(\.\d{1,2})?$/;
const DATE_RE = /^\d{4}-\d{2}-\d{2}$/;

function validate({ amount, note, date }) {
    const errors = {};
    const trimmed = amount.trim();
    if (!trimmed) errors.amount = 'Amount is required';
    else if (!AMOUNT_RE.test(trimmed)) errors.amount = 'Enter a valid amount (max 2 decimals)';
    else if (Number(trimmed) <= 0) errors.amount = 'Amount must be greater than 0';

    if (!DATE_RE.test(date)) errors.date = 'Select a date';
    if (note.trim().length > 255) errors.note = 'Note must be 255 characters or fewer';
    return errors;
}

/** onSubmit(payload) is called synchronously; the parent closes the modal and saves optimistically. */
export default function ExpenseForm({ expense, onSubmit, onClose }) {
    const editing = Boolean(expense);
    const [form, setForm] = useState(() => ({
        amount: expense ? String(expense.amount) : '',
        note: expense?.note || '',
        date: expense?.date || todayIST(),
        category: expense?.category || '',
    }));
    const [errors, setErrors] = useState({});

    const categoryOptions =
        form.category && !CATEGORIES.includes(form.category)
            ? [form.category, ...CATEGORIES]
            : CATEGORIES;

    const onChange = (e) => {
        const { name, value } = e.target;
        setForm((f) => ({ ...f, [name]: value }));
        setErrors((errs) => ({ ...errs, [name]: undefined }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        const found = validate(form);
        setErrors(found);
        if (Object.keys(found).length > 0) return;

        onSubmit({
            amount: Number(form.amount.trim()),
            category: form.category || null,
            note: form.note.trim() || null,
            date: form.date,
        });
    };

    return (
        <Modal title={editing ? 'Edit expense' : 'Add expense'} onClose={onClose}>
            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
                <FormField
                    id="amount"
                    label="Amount (₹)"
                    type="text"
                    inputMode="decimal"
                    autoComplete="off"
                    autoFocus
                    placeholder="e.g. 250"
                    value={form.amount}
                    onChange={onChange}
                    error={errors.amount}
                />

                <FormField
                    id="note"
                    label="Note"
                    autoComplete="off"
                    placeholder="e.g. lunch with team"
                    value={form.note}
                    onChange={onChange}
                    error={errors.note}
                />

                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <FormField
                        id="date"
                        label="Date"
                        type="date"
                        value={form.date}
                        onChange={onChange}
                        error={errors.date}
                    />
                    <FormField
                        id="category"
                        label="Category"
                        as="select"
                        value={form.category}
                        onChange={onChange}
                        error={errors.category}
                    >
                        <option value="">Auto (from note)</option>
                        {categoryOptions.map((c) => (
                            <option key={c} value={c}>
                                {c}
                            </option>
                        ))}
                    </FormField>
                </div>

                <div className="flex gap-3 pt-2">
                    <button type="button" onClick={onClose} className="btn-secondary flex-1">
                        Cancel
                    </button>
                    <button type="submit" className="btn-primary flex-1">
                        {editing ? 'Save changes' : 'Add expense'}
                    </button>
                </div>
            </form>
        </Modal>
    );
}