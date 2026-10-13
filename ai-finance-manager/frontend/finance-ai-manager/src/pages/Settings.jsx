import { useAuth } from '../auth/AuthContext.jsx';

export default function Settings() {
    const { user, logout } = useAuth();

    return (
        <div className="space-y-4">
            <h1 className="text-xl font-semibold text-slate-900">Settings</h1>

            <div className="card">
                <p className="text-sm text-slate-500">Signed in as</p>
                <p className="mt-1 text-lg font-semibold text-slate-900">{user?.name}</p>
            </div>

            <div className="card">
                <button type="button" onClick={logout} className="btn-danger w-full sm:w-auto">
                    Log out
                </button>
            </div>
        </div>
    );
}