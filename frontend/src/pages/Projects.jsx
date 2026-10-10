import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';
import { EmptyState, ErrorState, Loading } from '../components/ui';

export function Projects() {
  const { user } = useAuth();
  const [projects, setProjects] = useState(null);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ name: '', key: '', description: '' });
  const canCreate = user && (user.role === 'ADMIN' || user.role === 'PROJECT_MANAGER');

  const load = useCallback(async () => {
    setError('');
    try {
      setProjects(await api('/api/projects'));
    } catch (err) {
      setError(err.message);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  async function create(e) {
    e.preventDefault();
    setError('');
    try {
      await api('/api/projects', { method: 'POST', body: { ...form, key: form.key.toUpperCase() } });
      setForm({ name: '', key: '', description: '' });
      load();
    } catch (err) {
      setError(err.message);
    }
  }

  if (error && !projects) return <ErrorState message={error} onRetry={load} />;
  if (!projects) return <Loading />;

  return (
    <div>
      {canCreate && (
        <form className="card form wide" onSubmit={create}>
          <h3>New project</h3>
          {error && <p className="form-error">{error}</p>}
          <div className="row">
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Name" required />
            <input value={form.key} onChange={(e) => setForm({ ...form, key: e.target.value.toUpperCase() })} placeholder="KEY" required pattern="[A-Z0-9]{2,12}" title="2-12 uppercase letters/digits" />
            <button type="submit">Create</button>
          </div>
        </form>
      )}
      {projects.length === 0 ? (
        <EmptyState message="No projects yet. Ask a manager to add you." />
      ) : (
        <div className="grid">
          {projects.map((p) => (
            <Link key={p.id} to={`/projects/${p.id}`} className="card product">
              <h3>[{p.key}] {p.name}</h3>
              <p className="muted">{p.members.length} member(s)</p>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
