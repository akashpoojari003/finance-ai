import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

const NAV_ITEMS = [
    { to: '/', label: 'Dashboard', end: true, icon: 'M3 12l9-9 9 9M5 10v10h5v-6h4v6h5V10' },
    {
        to: '/expenses',
        label: 'Expenses',
        icon: 'M3 7h18M3 12h18M3 17h18',
    },
    {
        to: '/settings',
        label: 'Settings',
        icon: 'M12 15a3 3 0 100-6 3 3 0 000 6zM19.4 15a1.7 1.7 0 00.3 1.8l.1.1a2 2 0 11-2.8 2.8l-.1-.1a1.7 1.7 0 00-1.8-.3 1.7 1.7 0 00-1 1.5V21a2 2 0 11-4 0v-.1a1.7 1.7 0 00-1.1-1.5 1.7 1.7 0 00-1.8.3l-.1.1a2 2 0 11-2.8-2.8l.1-.1a1.7 1.7 0 00.3-1.8 1.7 1.7 0 00-1.5-1H3a2 2 0 110-4h.1a1.7 1.7 0 001.5-1.1 1.7 1.7 0 00-.3-1.8l-.1-.1a2 2 0 112.8-2.8l.1.1a1.7 1.7 0 001.8.3H9a1.7 1.7 0 001-1.5V3a2 2 0 114 0v.1a1.7 1.7 0 001 1.5 1.7 1.7 0 001.8-.3l.1-.1a2 2 0 112.8 2.8l-.1.1a1.7 1.7 0 00-.3 1.8V9a1.7 1.7 0 001.5 1H21a2 2 0 110 4h-.1a1.7 1.7 0 00-1.5 1z',
    },
];

function Icon({ path }) {
    return (
        <svg
            viewBox="0 0 24 24"
            className="h-5 w-5"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
        >
            <path d={path} />
        </svg>
    );
}

const desktopLink = ({ isActive }) =>
    `rounded-lg px-3 py-2 text-sm font-medium transition ${
        isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-600 hover:bg-slate-100'
    }`;

const mobileLink = ({ isActive }) =>
    `flex flex-1 flex-col items-center gap-0.5 py-2 text-xs font-medium transition ${
        isActive ? 'text-brand-600' : 'text-slate-500'
    }`;

export default function Layout() {
    const { user, logout } = useAuth();

    return (
        <div className="min-h-screen bg-slate-50">
            <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/90 backdrop-blur">
                <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
                    <div className="flex items-center gap-6">
                        <span className="text-lg font-bold text-brand-600">FinanceAI</span>
                        <nav className="hidden items-center gap-1 md:flex">
                            {NAV_ITEMS.map((item) => (
                                <NavLink key={item.to} to={item.to} end={item.end} className={desktopLink}>
                                    {item.label}
                                </NavLink>
                            ))}
                        </nav>
                    </div>
                    <div className="flex items-center gap-3">
                        <span className="max-w-[8rem] truncate text-sm text-slate-600">{user?.name}</span>
                        <button type="button" onClick={logout} className="btn-secondary px-3 py-1.5">
                            Log out
                        </button>
                    </div>
                </div>
            </header>

            <main className="mx-auto max-w-5xl px-4 pb-24 pt-5 md:pb-10">
                <Outlet />
            </main>

            <nav className="fixed inset-x-0 bottom-0 z-20 flex border-t border-slate-200 bg-white md:hidden">
                {NAV_ITEMS.map((item) => (
                    <NavLink key={item.to} to={item.to} end={item.end} className={mobileLink}>
                        <Icon path={item.icon} />
                        {item.label}
                    </NavLink>
                ))}
            </nav>
        </div>
    );
}