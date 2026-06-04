import { useRoles } from '../hooks/useRoles'
import { roleIcon, formatRole } from '../utils/assets'
import { PageTitle, Loader } from '../components/shared'

export default function Roles() {
  const { data: roles, isLoading } = useRoles()

  if (isLoading) return <Loader />

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <PageTitle title="ROLES" />
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
        {roles?.map((r: any) => <RoleCard key={r.role} r={r} />)}
      </div>
    </div>
  )
}

function RoleCard({ r }: { r: any }) {
  const wr = parseFloat(r.winRate)
  return (
    <div style={{
      border: '1px solid var(--border)',
      background: 'var(--surface)',
      padding: '24px 20px',
      display: 'flex',
      flexDirection: 'column',
      gap: '16px',
      position: 'relative',
      overflow: 'hidden',
    }}>
      {/* Ghost background icon */}
      <img
        src={roleIcon(r.role)}
        style={{
          position: 'absolute',
          right: '-10px',
          bottom: '-10px',
          width: '100px',
          height: '100px',
          opacity: 0.06,
          filter: 'grayscale(100%) invert(1)',
          pointerEvents: 'none',
        }}
      />
      {/* Role icon small */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <img
          src={roleIcon(r.role)}
          style={{
            width: '24px',
            height: '24px',
            filter: 'invert(1)',
            opacity: 0.6,
          }}
        />
        <div style={{ color: 'var(--accent)', fontSize: '14px', fontFamily: 'Ultra, serif' }}>
          {formatRole(r.role)}
        </div>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', position: 'relative' }}>
        <Stat label="GAMES" value={r.gamesPlayed} />
        <Stat label="WIN RATE" value={`${r.winRate}%`} color={wr >= 50 ? 'var(--win)' : 'var(--loss)'} />
        <Stat label="AVG KDA" value={r.avgKda} />
        <Stat label="RECORD" value={`${r.wins}W / ${r.losses}L`} />
      </div>
    </div>
  )
}

function Stat({ label, value, color }: { label: string; value: any; color?: string }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
      <span style={{ color: 'var(--muted)' }}>{label}</span>
      <span style={{ color: color ?? 'var(--text)' }}>{value}</span>
    </div>
  )
}