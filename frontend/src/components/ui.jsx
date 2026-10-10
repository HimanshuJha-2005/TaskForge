import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';

export function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  return (
    <nav className="nav">
      <Link to="/" className="brand">TaskForge</Link>
      <div className="nav-links">
        <Link to="/">Projects</Link>
        {user ? (
          <>
            <span className="nav-user">{user.email} ({user.role})</span>
            <button onClick={() => { logout(); navigate('/login'); }}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export function Loading() {
  return <p className="state">Loading…</p>;
}

export function ErrorState({ message, onRetry }) {
  return (
    <div className="state error">
      <p>Something went wrong: {message}</p>
      {onRetry && <button onClick={onRetry}>Retry</button>}
    </div>
  );
}

export function EmptyState({ message }) {
  return <p className="state">{message}</p>;
}

export function Pagination({ page, totalPages, onPage }) {
  if (totalPages <= 1) return null;
  return (
    <div className="pagination">
      <button disabled={page === 0} onClick={() => onPage(page - 1)}>← Prev</button>
      <span>Page {page + 1} of {totalPages}</span>
      <button disabled={page >= totalPages - 1} onClick={() => onPage(page + 1)}>Next →</button>
    </div>
  );
}

export const STATUS_COLORS = {
  OPEN: '#1a56db',
  IN_PROGRESS: '#c27803',
  IN_REVIEW: '#7c3aed',
  RESOLVED: '#057a55',
  CLOSED: '#65686d',
};

export function StatusBadge({ status }) {
  return (
    <span className="badge" style={{ background: STATUS_COLORS[status] || '#65686d' }}>
      {status}
    </span>
  );
}
