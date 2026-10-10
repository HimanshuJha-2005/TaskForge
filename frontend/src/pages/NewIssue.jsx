import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api';

export function NewIssue() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({ title: '', description: '', priority: 'MEDIUM', assigneeEmail: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const created = await api('/api/issues', {
        method: 'POST',
        body: {
          projectId: Number(id),
          title: form.title,
          description: form.description || null,
          priority: form.priority,
          assigneeEmail: form.assigneeEmail || null,
        },
      });
      navigate(`/issues/${created.id}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="card form" onSubmit={submit}>
      <h2>New issue</h2>
      {error && <p className="form-error">{error}</p>}
      <label>Title<input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required maxLength={200} /></label>
      <label>Description<textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} rows={4} /></label>
      <label>Priority
        <select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
          {['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map((p) => <option key={p} value={p}>{p}</option>)}
        </select>
      </label>
      <label>Assignee email (optional)<input type="email" value={form.assigneeEmail} onChange={(e) => setForm({ ...form, assigneeEmail: e.target.value })} /></label>
      <button type="submit" disabled={busy}>{busy ? 'Creating…' : 'Create issue'}</button>
    </form>
  );
}
