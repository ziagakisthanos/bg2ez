import { useChampions } from '../hooks/useChampions'
import { useDDragonVersion } from '../hooks/useDDragonVersion'
import { championSplash } from '../utils/assets'
import { PageTitle, Loader } from '../components/shared'

export default function Champions() {
  const { data: champions, isLoading } = useChampions()
  const { data: version } = useDDragonVersion()

  if (isLoading) return <Loader />

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <PageTitle title="CHAMPIONS" />

      <div style={{
        display: 'grid',
        gridTemplateColumns: '48px 160px 60px 70px 70px 80px 80px 80px',
        gap: '16px',
        padding: '8px 12px',
        color: 'var(--muted)',
        fontSize: '14px',
        letterSpacing: '1px',
        borderBottom: '1px solid var(--border)',
      }}>
        <span />
        <span>CHAMPION</span>
        <span>GAMES</span>
        <span>WIN%</span>
        <span>KDA</span>
        <span>AVG CS</span>
        <span>AVG DMG</span>
        <span>W / L</span>
      </div>

      {champions?.map((c: any) => (
        <ChampionRow key={c.championName} c={c} version={version} />
      ))}
    </div>
  )
}

function ChampionRow({ c, version }: { c: any; version: string }) {
  const wr = parseFloat(c.winRate)
  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: '48px 160px 60px 70px 70px 80px 80px 80px',
      gap: '16px',
      padding: '34px 12px',
      border: '1px solid var(--border)',
      background: 'var(--surface)',
      alignItems: 'center',
      position: 'relative',
      overflow: 'hidden',
    }}>
      {/* Ghost background */}
      {version && (
        <img
          src={championSplash(c.championName)}
          style={{
            position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              width: '100%',
              height: '100%',
              opacity: 0.12,
              filter: 'grayscale(20%)',
              pointerEvents: 'none',
              objectFit: 'cover',
              objectPosition: 'center -99px',
              imageRendering: 'smooth',
          }}
        />
      )}
        <span />
      <span style={{ color: 'var(--accent)' }}>{c.championName}</span>
      <span style={{ color: 'var(--muted)' }}>{c.gamesPlayed}</span>
      <span style={{ color: wr >= 50 ? 'var(--win)' : 'var(--loss)' }}>{c.winRate}%</span>
      <span>{c.avgKda}</span>
      <span style={{ color: 'var(--muted)' }}>{c.avgCs}</span>
      <span style={{ color: 'var(--muted)' }}>{c.avgDamage?.toLocaleString()}</span>
      <span>
        <span style={{ color: 'var(--win)' }}>{c.wins}W</span>
        {' '}<span style={{ color: 'var(--muted)' }}>/</span>{' '}
        <span style={{ color: 'var(--loss)' }}>{c.losses}L</span>
      </span>
    </div>
  )
}