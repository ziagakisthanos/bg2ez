import { Outlet, NavLink } from 'react-router-dom'
import SyncButton from './SyncButton'

const navItems = [
  { to: '/', label: '> DASHBOARD', end: true },
  { to: '/matches', label: '> MATCH HISTORY' },
  { to: '/champions', label: '> CHAMPIONS' },
  { to: '/roles', label: '> ROLES' },
  { to: '/ranked', label: '> RANKED' },
  { to: '/coach', label: '> COACH' },
  { to: '/live', label: '> LIVE' },

]

export default function Layout() {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>

      {/* Sidebar */}
      <aside style={{
        width: '220px',
        height: '100vh',
        position: 'sticky',
        top: 0,
        borderRight: '1px solid var(--border)',
        padding: '24px 16px',
        display: 'flex',
        flexDirection: 'column',
        gap: '32px',
        flexShrink: 0,
        overflowY: 'auto',
      }}>
        {/* Logo */}
        <div className="ultra" style={{
          color: 'var(--accent)',
          fontSize: '36px',
          letterSpacing: '2px',
          borderBottom: '1px solid var(--border)',
          padding: '0 0 0 16px',
        }}>
          BG2EZ
        </div>

        {/* Nav */}
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {navItems.map(({ to, label, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              style={({ isActive }) => ({
                color: isActive ? 'var(--accent)' : 'var(--muted)',
                fontSize: '14px',
                letterSpacing: '1px',
                transition: 'color 0.1s',
                padding: '0 0 0 16px',

              })}
            >
              {label}
            </NavLink>
          ))}
        </nav>

        {/* Sync */}
        <div style={{ marginTop: 'auto' }}>
          <SyncButton />
        </div>
      </aside>

      {/* Main */}
      <main style={{ flex: 1, padding: '32px', overflow: 'auto' }}>
        <Outlet />
      </main>

    </div>
  )
}