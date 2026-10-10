import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api';
import { EmptyState, ErrorState, Loading, Pagination, StatusBadge } from '../components/ui';

export function ProjectDetail() {
  const { id } = useParams();
  const [project, setProject] = useState(null);
  const [dashboard, setDashboard] = useState(null);
  const [issues, setIssues] = useState(null);
  const [status, setStatus] = useState('');
  const [priority, setPriority] = useState('');
  const [query, setQuery] = useState('');
  const [committed, setCommitted] = useState('');
  const [page, setPage] = useState(0);
  const [error, setError] = useState('');
  const [memberEmail, setMemberEmail] = useState('');

  const load = useCallback(async () => {
    setError('');
    try {
      const params = new URLSearchParams({ projectId: id, page, size: 10 });
      if (status) params.set('status', status);
      if (priority) params.set('priority', priority);
      if (committed) params.set('query', committed);
      const [p, d, i] = await Promise.all([
        api(`/api/projects/${id}`),
        api(`/api/projects/${id}/dashboard`),
        api(`/api/issues?${params}`),
      ]);
      setProject(p);
      setDashboard(d);
      setIssues(i);
    } catch (err) {
      setError(err.message);
    }
  }, [id, page, status, priority, committed]);

  useEffect(() => { load(); }, [load]);

  async function addMember(e) {
    e.preventDefault();
    setError('');
    try {
      const updated = await api(`/api/projects/${id}/members`, { method: 'POST', body: { email: memberEmail } });
      setProject(updated);
      setMemberEmail('');
    } catch (err) {
      setError(err.message);
    }
  }

  if (error && !project) return <ErrorState message={error} onRetry={load} />;
  if (!project || !issues) return <Loading />;

  return (
    <div>
      <div className="card">
        <h2>[{project.key}] {project.name}</h2>
        <p className="muted">{project.description}</p>
        <p className="muted">Members: {project.members.join(', ')}</p>
        <form className="row" onSubmit={addMember}>
          <input type="email" value={memberEmail} onChange={(e) => setMemberEmail(e.target.value)} placeholder="Add member by email" />
          <button type="submit">Add</button>
        </form>
        {error && <p className="form-error">{error}</p>}
      </div>

      {dashboard && (
        <div className="card">
          <h3>Dashboard — {dashboard.totalIssues} issue(s)</h3>
          <div className="stats">
            {Object.entries(dashboard.byStatus).map(([s, n]) => (
              <span key={s} className="stat"><StatusBadge status={s} /> {n}</span>
            ))}
          </div>
          <p className="muted">By priority: {Object.entries(dashboard.byPriority).map(([p, n]) => `${p} ${n}`).join(' · ') || '—'}</p>
        </div>
      )}

      <div className="card">
        <div className="row space">
          <h3>Issues</h3>
          <Link to={`/projects/${id}/issues/new`}><button>New issue</button></Link>
        </div>
        <form className="toolbar" onSubmit={(e) => { e.preventDefault(); setPage(0); setCommitted(query.trim()); }}>
          <input placeholder="Search title…" value={query} onChange={(e) => setQuery(e.target.value)} />
          <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
            <option value="">All statuses</option>
            {['OPEN', 'IN_PROGRESS', 'IN_REVIEW', 'RESOLVED', 'CLOSED'].map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <select value={priority} onChange={(e) => { setPriority(e.target.value); setPage(0); }}>
            <option value="">All priorities</option>
            {['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map((p) => <option key={p} value={p}>{p}</option>)}
          </select>
          <button type="submit">Search</button>
        </form>
        {issues.content.length === 0 ? (
          <EmptyState message="No issues match." />
        ) : (
          <table className="table">
            <thead><tr><th>Title</th><th>Status</th><th>Priority</th><th>Assignee</th></tr></thead>
            <tbody>
              {issues.content.map((i) => (
                <tr key={i.id}>
                  <td><Link to={`/issues/${i.id}`}>{i.title}</Link></td>
                  <td><StatusBadge status={i.status} /></td>
                  <td>{i.priority}</td>
                  <td>{i.assigneeEmail || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <Pagination page={issues.page} totalPages={issues.totalPages} onPage={setPage} />
      </div>
    </div>
  );
}
