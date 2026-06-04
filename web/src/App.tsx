import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import Champions from './pages/Champions'
import Roles from './pages/Roles'
import Ranked from './pages/Ranked'
import Coach from './pages/Coach'
import Link from './pages/Link'
import { useMe } from './hooks/useMe'
import Matches from './pages/Matches'
import Live from './pages/Live'



export default function App() {
  const { data: me, isLoading } = useMe()

  if (isLoading) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: 'var(--muted)',
        fontFamily: 'Press Start 2P, monospace',
        fontSize: '10px',
      }}>
        LOADING...
      </div>
    )
  }

  if (!me) {
    return <Link />
  }

  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="champions" element={<Champions />} />
        <Route path="roles" element={<Roles />} />
        <Route path="ranked" element={<Ranked />} />
        <Route path="coach" element={<Coach />} />
        <Route path="matches" element={<Matches />} />
        <Route path="live" element={<Live />} />
      </Route>
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  )
}