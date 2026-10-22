import { useEffect } from 'react';

export default function Modal({ title, onClose, children }) {
    useEffect(() => {
        const onKey = (e) => {
            if (e.key === 'Escape') onClose();
        };
        document.addEventListener('keydown', onKey);
        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.removeEventListener('keydown', onKey);
            document.body.style.overflow = previousOverflow;
        };
    }, [onClose]);

    return (
        <div
            className="fixed inset-0 z-40 flex items-end justify-center sm:items-center"
            role="dialog"
            aria-modal="true"
            aria-label={title}
        >
            <div className="absolute inset-0 bg-slate-900/50" onClick={onClose} />
            <div className="relative z-10 max-h-[90vh] w-full max-w-md overflow-y-auto rounded-t-2xl bg-white p-5 shadow-card sm:rounded-2xl">
                <div className="mb-4 flex items-center justify-between">
                    <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-lg px-2 py-1 text-slate-500 hover:bg-slate-100"
                        aria-label="Close"
                    >
                        ✕
                    </button>
                </div>
                {children}
            </div>
        </div>
    );
}