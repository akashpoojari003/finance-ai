const IST = 'Asia/Kolkata';

const inr0 = new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
});
const inr2 = new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
});
const compact = new Intl.NumberFormat('en-IN', { notation: 'compact', maximumFractionDigits: 1 });

export function formatINR(value) {
    if (value === null || value === undefined || value === '') return '—';
    const n = Number(value);
    if (Number.isNaN(n)) return '—';
    return Number.isInteger(n) ? inr0.format(n) : inr2.format(n);
}

export const formatCompact = (value) => compact.format(Number(value) || 0);

const MONTH_RE = /^\d{4}-(0[1-9]|1[0-2])$/;

export const isValidMonth = (value) => MONTH_RE.test(value || '');

/** Current YYYY-MM in Asia/Kolkata. */
export function currentMonth() {
    const parts = new Intl.DateTimeFormat('en-CA', {
        timeZone: IST,
        year: 'numeric',
        month: '2-digit',
    }).formatToParts(new Date());
    const year = parts.find((p) => p.type === 'year').value;
    const month = parts.find((p) => p.type === 'month').value;
    return `${year}-${month}`;
}

export function shiftMonth(month, delta) {
    const [y, m] = month.split('-').map(Number);
    return new Date(Date.UTC(y, m - 1 + delta, 1)).toISOString().slice(0, 7);
}

export function monthLabel(month) {
    const [y, m] = month.split('-').map(Number);
    return new Date(Date.UTC(y, m - 1, 1)).toLocaleDateString('en-IN', {
        month: 'long',
        year: 'numeric',
        timeZone: 'UTC',
    });
}

export function shortDate(isoDate) {
    return new Date(`${isoDate}T00:00:00Z`).toLocaleDateString('en-IN', {
        day: 'numeric',
        month: 'short',
        timeZone: 'UTC',
    });
}

export function todayIST() {
    return new Intl.DateTimeFormat('en-CA', {
        timeZone: IST,
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
    }).format(new Date());
}

export function longDate(isoDate) {
    return new Date(`${isoDate}T00:00:00Z`).toLocaleDateString('en-IN', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        timeZone: 'UTC',
    });
}