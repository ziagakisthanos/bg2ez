import { useState } from 'react'
import { useMatches } from '../hooks/useMatches'
import { useDDragonVersion } from '../hooks/useDDragonVersion'
import { championIcon, championSplash, roleIcon } from '../utils/assets'
import { PageTitle, Loader } from '../components/shared'

export default function Matches() {
  const [limit, setLimit] = useState(20)
  const { data: matches, isLoading } = useMatches(limit)
  const { data: version } = useDDragonVersion()

  if (isLoading) return <Loader />

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <PageTitle title="MATCH HISTORY" />
        <select
          value={limit}
          onChange={e => setLimit(parseInt(e.target.value))}
          style={{
            background: 'var(--surface)',
            border: '1px solid var(--border)',
            color: 'var(--text)',
            padding: '8px 12px',
            fontSize: '12px',
            fontFamily: 'Krub, sans-serif',
            cursor: 'pointer',
            outline: 'none',
          }}
        >
          <option value={10}>Last 10</option>
          <option value={20}>Last 20</option>
          <option value={50}>Last 50</option>
        </select>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
        {matches?.map((match: any) => (
          <MatchRow key={match.matchId} match={match} version={version} />
        ))}
      </div>
    </div>
  )
}

function MatchRow({ match, version }: { match: any; version: string }) {
  const p = match.player
  const wr = p.win
  const kda = p.deaths === 0
    ? (p.kills + p.assists).toFixed(2)
    : ((p.kills + p.assists) / p.deaths).toFixed(2)
  const duration = match.gameDuration
    ? `${Math.floor(match.gameDuration / 60)}m ${match.gameDuration % 60}s`
    : '—'

  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: '4px 56px 180px 100px 100px 80px 80px 80px 80px 100px',
      alignItems: 'center',
      gap: '12px',
      padding: '0 16px 0 0',
      border: '1px solid var(--border)',
      background: 'var(--surface)',
      position: 'relative',
      overflow: 'hidden',
      minHeight: '72px',
    }}>
      {/* Win/loss bar */}
      <div style={{
        alignSelf: 'stretch',
        background: wr ? 'var(--win)' : 'var(--loss)',
      }} />

      {/* Champion splash background */}
      {version && (
        <img
          src={championSplash(p.championName)}
          style={{
            position: 'absolute',
            right: 0,
            top: 0,
            bottom: 0,
            width: '200px',
            height: '100%',
            opacity: 0.04,
            filter: 'grayscale(30%)',
            objectFit: 'cover',
            objectPosition: 'center top',
            pointerEvents: 'none',
          }}
        />
      )}

      {/* Champion icon */}
      {version && (
        <img
          src={championIcon(version, p.championName)}
          style={{
            width: '44px',
            height: '44px',
            objectFit: 'cover',
            imageRendering: 'pixelated',
          }}
        />
      )}

      {/* Champion + queue */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
        <span style={{ color: 'var(--text)', fontSize: '13px' }}>{p.championName}</span>
        <span style={{ color: 'var(--muted)', fontSize: '11px' }}>{match.queueLabel}</span>
      </div>

      {/* Result */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
        <span style={{
          color: wr ? 'var(--win)' : 'var(--loss)',
          fontSize: '13px',
          fontFamily: 'Ultra, serif',
        }}>
          {wr ? 'WIN' : 'LOSS'}
        </span>
        <span style={{ color: 'var(--muted)', fontSize: '11px' }}>{duration}</span>
      </div>

      {/* KDA */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
        <span style={{ fontSize: '13px' }}>
          {p.kills}/{p.deaths}/{p.assists}
        </span>
        <span style={{ color: 'var(--muted)', fontSize: '11px' }}>{kda} KDA</span>
      </div>

      {/* CS */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
        <span style={{ fontSize: '13px' }}>{p.cs}</span>
        <span style={{ color: 'var(--muted)', fontSize: '11px' }}>CS</span>
      </div>

      {/* Damage */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
        <span style={{ fontSize: '13px' }}>{p.totalDamage?.toLocaleString()}</span>
        <span style={{ color: 'var(--muted)', fontSize: '11px' }}>DMG</span>
      </div>

      {/* Vision */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
        <span style={{ fontSize: '13px' }}>{p.visionScore}</span>
        <span style={{ color: 'var(--muted)', fontSize: '11px' }}>VISION</span>
      </div>

      {/* Role + KP */}
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '4px',
      }}>
        {p.role && (
          <img
            src={roleIcon(p.role)}
            style={{
              width: '20px',
              height: '20px',
              filter: 'invert(1)',
              opacity: 0.5,
            }}
          />
        )}
        <span style={{ color: 'var(--muted)', fontSize: '11px' }}>
          {p.killParticipation}% KP
        </span>
      </div>
    </div>
  )
}