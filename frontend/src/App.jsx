import { BrowserRouter, Link, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth';
import { Navbar } from './components/ui';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Login, Register } from './pages/Auth';
import { Projects } from './pages/Projects';
import { ProjectDetail } from './pages/ProjectDetail';
import { NewIssue } from './pages/NewIssue';
import { IssueDetail } from './pages/IssueDetail';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />
        <main className="container">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/" element={<ProtectedRoute><Projects /></ProtectedRoute>} />
            <Route path="/projects/:id" element={<ProtectedRoute><ProjectDetail /></ProtectedRoute>} />
            <Route path="/projects/:id/issues/new" element={<ProtectedRoute><NewIssue /></ProtectedRoute>} />
            <Route path="/issues/:id" element={<ProtectedRoute><IssueDetail /></ProtectedRoute>} />
            <Route path="*" element={<p className="state">Not found. <Link to="/">Home</Link></p>} />
          </Routes>
        </main>
      </AuthProvider>
    </BrowserRouter>
  );
}
