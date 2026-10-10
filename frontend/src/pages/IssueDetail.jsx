import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api';
import { ErrorState, Loading, StatusBadge } from '../components/ui';

const NEXT = {
  OPEN: ['IN_PROGRESS'],
  IN_PROGRESS: ['IN_REVIEW'],
  IN_REVIEW: ['IN_PROGRESS', 'RESOLVED'],
  RESOLVED: ['IN_REVIEW', 'CLOSED'],
  CLOSED: [],
};

export function IssueDetail() {
  const { id } = useParams();
  const [issue, setIssue] = useState(null);
  const [comments, setComments] = useState([]);
  const [body, setBody] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    setError('');
    try {
      const [i, c] = await Promise.all([api(`/api/issues/${id}`), api(`/api/issues/${id}/comments`)]);
      setIssue(i);
      setComments(c);
    } catch (err) {
      setError(err.message);
    }
  }, [id]);

  useEffect(() => { load(); }, [load]);

  async function transition(s) {
    setError('');
    setBusy(true);
    try {
      setIssue(await api(`/api/issues/${id}/status`, { method: 'PATCH', body: { status: s } }));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function postComment(e) {
    e.preventDefault();
    setError('');
    try {
      const created = await api(`/api/issues/${id}/comments`, { method: 'POST', body: { body } });
      setComments((c) => [...c, created]);
      setBody('');
    } catch (err) {
      setError(err.message);
    }
  }

  if (error && !issue) return <ErrorState message={error} onRetry={load} />;
  if (!issue) return <Loading />;

  return (
    <div>
      <div className="card">
        <p className="muted"><Link to={`/projects/${issue.projectId}`}>← {issue.projectKey}</Link></p>
        <h2>{issue.title}</h2>
        <p><StatusBadge status={issue.status} /> · {issue.priority} · Assignee: {issue.assigneeEmail || 'unassigned'}</p>
        <p className="muted">Reported by {issue.reporterEmail}</p>
        <p>{issue.description || 'No description.'}</p>
        {error && <p className="form-error">{error}</p>}
        <div className="actions">
          {NEXT[issue.status].map((s) => (
            <button key={s} disabled={busy} onClick={() => transition(s)}>→ {s}</button>
          ))}
        </div>
      </div>

      <div className="card">
        <h3>Comments ({comments.length})</h3>
        {comments.map((c) => (
          <div key={c.id} className="comment">
            <p className="muted">{c.authorEmail}</p>
            <p>{c.body}</p>
          </div>
        ))}
        <form className="row" onSubmit={postComment}>
          <input value={body} onChange={(e) => setBody(e.target.value)} placeholder="Write a comment…" required maxLength={2000} />
          <button type="submit">Post</button>
        </form>
      </div>
    </div>
  );
}
