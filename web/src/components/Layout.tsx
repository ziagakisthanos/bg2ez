import { Outlet, NavLink } from 'react-router-dom'
import { useState } from 'react'
import SyncButton from './SyncButton'
import { useUnlinkAccount } from '../hooks/useUnlinkAccount'

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
    const unlink = useUnlinkAccount()
    const [confirmUnlink, setConfirmUnlink] = useState(false)
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

        {/* Unlink */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          {!confirmUnlink ? (
            <button
              onClick={() => setConfirmUnlink(true)}
              style={{
                background: 'transparent',
                border: '1px solid var(--border)',
                color: 'var(--muted)',
                padding: '8px 12px',
                fontSize: '11px',
                fontFamily: 'Krub, sans-serif',
                cursor: 'pointer',
                width: '100%',
              }}
            >
              UNLINK ACCOUNT
            </button>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div style={{ color: 'var(--muted)', fontSize: '10px', textAlign: 'center' }}>
                Are you sure?
              </div>
              <div style={{ display: 'flex', gap: '6px' }}>
                <button
                  onClick={async () => {
                    await unlink.mutateAsync()
                    setConfirmUnlink(false)
                  }}
                  style={{
                    flex: 1,
                    background: 'var(--loss)',
                    border: 'none',
                    color: '#fff',
                    padding: '8px',
                    fontSize: '10px',
                    fontFamily: 'Krub, sans-serif',
                    cursor: 'pointer',
                  }}
                >
                  YES
                </button>
                <button
                  onClick={() => setConfirmUnlink(false)}
                  style={{
                    flex: 1,
                    background: 'transparent',
                    border: '1px solid var(--border)',
                    color: 'var(--muted)',
                    padding: '8px',
                    fontSize: '10px',
                    fontFamily: 'Krub, sans-serif',
                    cursor: 'pointer',
                  }}
                >
                  NO
                </button>
              </div>
            </div>
          )}
        </div>
      </aside>

      {/* Main */}
      <main style={{ flex: 1, padding: '32px', overflow: 'auto' }}>
        <Outlet />
      </main>

    </div>
  )
}