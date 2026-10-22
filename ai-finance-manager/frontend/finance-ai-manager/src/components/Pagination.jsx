export default function Pagination({ page, pageSize, total, onChange }) {
    if (total <= pageSize) return null;

    const pages = Math.ceil(total / pageSize);
    const from = (page - 1) * pageSize + 1;
    const to = Math.min(page * pageSize, total);

    return (
        <div className="flex items-center justify-between gap-3 border-t border-slate-100 px-4 py-3">
            <p className="text-xs text-slate-500">
                {from}-{to} of {total}
            </p>
            <div className="flex items-center gap-2">
                <button
                    type="button"
                    className="btn-secondary px-3 py-1.5"
                    disabled={page <= 1}
                    onClick={() => onChange(page - 1)}
                >
                    Prev
                </button>
                <span className="text-xs text-slate-600">
          {page} / {pages}
        </span>
                <button
                    type="button"
                    className="btn-secondary px-3 py-1.5"
                    disabled={page >= pages}
                    onClick={() => onChange(page + 1)}
                >
                    Next
                </button>
            </div>
        </div>
    );
}