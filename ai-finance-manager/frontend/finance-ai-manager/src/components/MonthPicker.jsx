import { shiftMonth } from '../utils/format.js';

export default function MonthPicker({ month, onChange }) {
    return (
        <div className="flex items-center gap-2">
            <button
                type="button"
                onClick={() => onChange(shiftMonth(month, -1))}
                className="btn-secondary px-3 py-2"
                aria-label="Previous month"
            >
                ‹
            </button>
            <input
                type="month"
                value={month}
                onChange={(e) => onChange(e.target.value)}
                className="input w-auto py-2"
                aria-label="Select month"
            />
            <button
                type="button"
                onClick={() => onChange(shiftMonth(month, 1))}
                className="btn-secondary px-3 py-2"
                aria-label="Next month"
            >
                ›
            </button>
        </div>
    );
}