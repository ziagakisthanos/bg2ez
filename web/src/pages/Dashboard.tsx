import { useMe } from '../hooks/useMe'
import { useChampions } from '../hooks/useChampions'
import { useMatches } from '../hooks/useMatches'
import { useRoles } from '../hooks/useRoles'
import { useRanked } from '../hooks/useRanked'
import { PageTitle, Loader } from '../components/shared.tsx'
import { useDDragonVersion } from '../hooks/useDDragonVersion'
import { championSplash, championIcon, tierEmblem, roleIcon } from '../utils/assets'

export default function Dashboard() {
  const { data: me } = useMe()
  const { data: champions } = useChampions()
  const { data: roles } = useRoles()
  const { data: ranked } = useRanked()
  const { data: version } = useDDragonVersion()
  const { data: matches } = useMatches(10)

  const solo = ranked?.find((r: any) => r.queueType === 'RANKED_SOLO_5x5')
  const topChampion = champions?.[0]
  const topRole = roles?.sort((a: any, b: any) => b.gamesPlayed - a.gamesPlayed)?.[0]

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>

      {/* Header */}
      <div>
        <div style={{ color: 'var(--muted)', fontSize: '16px', marginBottom: '8px' }}>
          Riot ID
        </div>
        <div style={{ fontSize: '30px', color: 'var(--accent)' }}>
          {me?.gameName}<span style={{ color: 'var(--muted)' }}>#{me?.tagLine}</span>
        </div>
        <div style={{ color: 'var(--muted)', fontSize: '12px', marginTop: '8px' }}>
          LVL {me?.summonerLevel}
        </div>
      </div>

      {/* Stat cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
        <RankCard solo={solo} />
        <ChampionCard champion={topChampion} version={version} />
        <RoleCard role={topRole} />
      </div>

           {/* Recent matches */}
        <div>
          <div style={{
            color: 'var(--muted)',
            fontSize: '11px',
            letterSpacing: '2px',
            borderBottom: '1px solid var(--border)',
            paddingBottom: '8px',
            marginBottom: '16px',
          }}>
            RECENT MATCHES
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
            {matches?.map((match: any) => (
              <MatchHistoryRow key={match.matchId} match={match} version={version} />
            ))}
          </div>
        </div>
    </div>
  )
}

function RankCard({ solo }: { solo: any }) {
  return (
    <div style={{
      border: '1px solid var(--border)',
      padding: '20px 16px',
      background: 'var(--surface)',
      position: 'relative',
      overflow: 'hidden',
      minHeight: '160px',
    }}>
      {solo?.tier && (
        <img
          src={tierEmblem(solo.tier)}
          style={{
            position: 'absolute',
            right: '-20%',
            bottom: '-75px',
            width: '400px',
            height: '300px',
            opacity: 0.5,
            pointerEvents: 'none',
            objectFit: 'cover',
            imageRendering: 'smooth',
          }}
        />
      )}
      <div style={{ color: 'var(--muted)', fontSize: '11px', marginBottom: '12px', position: 'relative' }}>
        SOLO RANK
      </div>
      <div style={{ fontSize: '24px', color: 'var(--text)', fontFamily: 'Ultra, serif', position: 'relative' }}>
        {solo ? `${solo.tier} ${solo.rank}` : '—'}
      </div>
      {solo && (
        <div style={{ color: 'var(--accent)', fontSize: '14px', marginTop: '8px', position: 'relative' }}>
          {solo.leaguePoints} LP
        </div>
      )}
    </div>
  )
}

function ChampionCard({ champion, version }: { champion: any; version: string }) {
  return (
    <div style={{
      border: '1px solid var(--border)',
      padding: '20px 16px',
      background: 'var(--surface)',
      position: 'relative',
      overflow: 'hidden',
      minHeight: '160px',
    }}>
      {champion && (
        <img
          src={championSplash(champion.championName)}
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            width: '100%',
            height: '100%',
            opacity: 0.22,
            filter: 'grayscale(20%)',
            pointerEvents: 'none',
            objectFit: 'cover',
            objectPosition: 'center -20px',
            imageRendering: 'smooth',
          }}
        />
      )}
      <div style={{ color: 'var(--muted)', fontSize: '11px', marginBottom: '12px', position: 'relative' }}>
        TOP CHAMPION
      </div>
      <div style={{ fontSize: '24px', color: 'var(--text)', fontFamily: 'Ultra, serif', position: 'relative' }}>
        {champion?.championName ?? '—'}
      </div>
      {champion && (
        <div style={{ color: 'var(--accent)', fontSize: '14px', marginTop: '8px', position: 'relative' }}>
          {champion.winRate}% WR
        </div>
      )}
    </div>
  )
}

function RoleCard({ role }: { role: any }) {
  return (
    <div style={{
      border: '1px solid var(--border)',
      padding: '20px 16px',
      background: 'var(--surface)',
      position: 'relative',
      overflow: 'hidden',
      minHeight: '160px',
    }}>
      {role && (
        <img
          src={roleIcon(role.role)}
          style={{
            position: 'absolute',
            right: '10%',
            bottom: '20%',
            width: '100px',
            height: '100px',
            opacity: 0.3,
            pointerEvents: 'none',
            objectFit: 'contain',
            imageRendering: 'smooth',

          }}
        />
      )}
      <div style={{ color: 'var(--muted)', fontSize: '11px', marginBottom: '12px', position: 'relative' }}>
        BEST LANE
      </div>
      <div style={{ fontSize: '24px', color: 'var(--text)', fontFamily: 'Ultra, serif', position: 'relative' }}>
        {role?.role ?? '—'}
      </div>
      {role && (
        <div style={{ color: 'var(--accent)', fontSize: '14px', marginTop: '8px', position: 'relative' }}>
          {role.winRate}% WR
        </div>
      )}
    </div>
  )
}

function SectionTitle({ title }: { title: string }) {
  return (
    <div style={{
      color: 'var(--muted)',
      fontSize: '12px',
      letterSpacing: '2px',
      borderBottom: '1px solid var(--border)',
      paddingBottom: '8px',
      marginBottom: '16px',
    }}>
      {title}
    </div>
  )
}

function MatchHistoryRow({ match, version }: { match: any; version: string }) {
  const p = match.player
  const kda = p.deaths === 0
    ? (p.kills + p.assists).toFixed(2)
    : ((p.kills + p.assists) / p.deaths).toFixed(2)
  const duration = match.gameDuration
    ? `${Math.floor(match.gameDuration / 60)}m`
    : '—'

  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: '4px 44px 160px 80px 100px 70px 70px 80px',
      alignItems: 'center',
      gap: '12px',
      border: '1px solid var(--border)',
      background: 'var(--surface)',
      position: 'relative',
      overflow: 'hidden',
      minHeight: '60px',
      paddingRight: '16px',
    }}>
      {/* Win/loss bar */}
      <div style={{
        alignSelf: 'stretch',
        background: p.win ? 'var(--win)' : 'var(--loss)',
      }} />

      {/* Splash background */}
      {version && (
        <img
          src={championSplash(p.championName)}
          style={{
            position: 'absolute',
            right: 0,
            top: 0,
            bottom: 0,
            width: '180px',
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
          style={{ width: '36px', height: '36px', objectFit: 'cover', imageRendering: 'pixelated' }}
        />
      )}

      {/* Champion + queue */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
        <span style={{ fontSize: '13px', color: 'var(--text)' }}>{p.championName}</span>
        <span style={{ fontSize: '11px', color: 'var(--muted)' }}>{match.queueLabel}</span>
      </div>

      {/* Result */}
      <span style={{
        color: p.win ? 'var(--win)' : 'var(--loss)',
        fontFamily: 'Ultra, serif',
        fontSize: '13px',
      }}>
        {p.win ? 'WIN' : 'LOSS'}
      </span>

      {/* KDA */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
        <span style={{ fontSize: '13px' }}>{p.kills}/{p.deaths}/{p.assists}</span>
        <span style={{ fontSize: '11px', color: 'var(--muted)' }}>{kda} KDA</span>
      </div>

      {/* CS */}
      <span style={{ fontSize: '13px', color: 'var(--muted)' }}>{p.cs} CS</span>

      {/* Duration */}
      <span style={{ fontSize: '11px', color: 'var(--muted)' }}>{duration}</span>

      {/* Role */}
      {p.role && (
        <img
          src={roleIcon(p.role)}
          style={{ width: '18px', height: '18px', filter: 'invert(1)', opacity: 0.4 }}
        />
      )}
    </div>
  )
}