import { useLiveGame } from '../hooks/useLiveGame'
import { PageTitle, Loader } from '../components/shared'
import { tierEmblem, championIcon } from '../utils/assets'
import { useDDragonVersion } from '../hooks/useDDragonVersion'

export default function Live() {
  const { data: game, isLoading, refetch } = useLiveGame()
  const { data: version } = useDDragonVersion()

  if (isLoading) return <Loader />

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <PageTitle title="LIVE GAME" />
        <button
          onClick={() => refetch()}
          style={{
            background: 'transparent',
            border: '1px solid var(--border)',
            color: 'var(--muted)',
            padding: '8px 16px',
            fontSize: '11px',
            fontFamily: 'Krub, sans-serif',
            cursor: 'pointer',
          }}
        >
          REFRESH
        </button>
      </div>

      {!game ? (
        <NotInGame />
      ) : (
        <GameView game={game} version={version} />
      )}
    </div>
  )
}

function NotInGame() {
  return (
    <div style={{
      border: '1px solid var(--border)',
      background: 'var(--surface)',
      padding: '48px',
      textAlign: 'center',
      display: 'flex',
      flexDirection: 'column',
      gap: '16px',
      alignItems: 'center',
    }}>
      <div style={{ fontSize: '32px', opacity: 0.2 }}>⚔</div>
      <div style={{ color: 'var(--muted)', fontSize: '13px' }}>
        Not currently in a game
      </div>
      <div style={{ color: 'var(--muted)', fontSize: '11px' }}>
        Start a game and refresh to see live opponent data
      </div>
    </div>
  )
}

function GameView({ game, version }: { game: any; version: string }) {
  const minutes = game.gameLength ? Math.floor(game.gameLength / 60) : 0
  const seconds = game.gameLength ? game.gameLength % 60 : 0

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>

      {/* Game info */}
      <div style={{
        display: 'flex',
        gap: '24px',
        fontSize: '12px',
        color: 'var(--muted)',
        borderBottom: '1px solid var(--border)',
        paddingBottom: '16px',
      }}>
        <span style={{ color: 'var(--accent)' }}>{game.queueLabel}</span>
        <span>{minutes}:{seconds.toString().padStart(2, '0')} elapsed</span>
      </div>

      {/* Teams */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        <TeamPanel title="YOUR TEAM" players={game.allies} version={version} isEnemy={false} />
        <TeamPanel title="ENEMIES" players={game.enemies} version={version} isEnemy={true} />
      </div>
    </div>
  )
}

function TeamPanel({ title, players, version, isEnemy }: {
  title: string
  players: any[]
  version: string
  isEnemy: boolean
}) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
      <div style={{
        color: isEnemy ? 'var(--loss)' : 'var(--win)',
        fontSize: '11px',
        letterSpacing: '2px',
        borderBottom: `1px solid ${isEnemy ? 'var(--loss)' : 'var(--win)'}`,
        paddingBottom: '8px',
        marginBottom: '8px',
        opacity: 0.8,
      }}>
        {title}
      </div>
      {players.map((player: any) => (
        <PlayerCard key={player.puuid} player={player} version={version} isEnemy={isEnemy} />
      ))}
    </div>
  )
}

function PlayerCard({ player, version, isEnemy }: {
  player: any
  version: string
  isEnemy: boolean
}) {
  const winRate = player.winRate ? `${player.winRate.toFixed(1)}%` : null
  const wr = player.winRate ?? 0
  const totalGames = (player.wins ?? 0) + (player.losses ?? 0)

  return (
    <div style={{
      border: `1px solid ${player.isYou ? 'var(--accent)' : 'var(--border)'}`,
      background: 'var(--surface)',
      padding: '12px',
      display: 'flex',
      flexDirection: 'column',
      gap: '8px',
      position: 'relative',
      overflow: 'hidden',
    }}>
      {/* Tier emblem background */}
      {player.tier && (
        <img
          src={tierEmblem(player.tier)}
          style={{
            position: 'absolute',
            right: '-20px',
            bottom: '-20px',
            width: '120px',
            height: '68px',
            opacity: 0.06,
            pointerEvents: 'none',
            objectFit: 'cover',
          }}
        />
      )}

      {/* Top row — champion + name */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
        {version && player.championId && (
          <img
            src={`https://ddragon.leagueoflegends.com/cdn/${version}/img/champion/${player.championId}.png`}
            style={{ width: '36px', height: '36px', objectFit: 'cover' }}
            onError={e => (e.currentTarget.style.display = 'none')}
          />
        )}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
          <span style={{
            fontSize: '13px',
            color: player.isYou ? 'var(--accent)' : 'var(--text)',
            fontWeight: player.isYou ? 600 : 400,
          }}>
            {player.summonerName || 'Unknown'}
            {player.isYou && (
              <span style={{ color: 'var(--accent)', fontSize: '10px', marginLeft: '6px' }}>
                YOU
              </span>
            )}
          </span>
        </div>
      </div>

      {/* Rank row */}
      <div style={{ display: 'flex', gap: '16px', fontSize: '12px' }}>
        {player.tier ? (
          <>
            <span style={{ color: 'var(--accent)', fontFamily: 'Ultra, serif' }}>
              {player.tier} {player.rank}
            </span>
            <span style={{ color: 'var(--muted)' }}>{player.leaguePoints} LP</span>
            {winRate && (
              <span style={{ color: wr >= 50 ? 'var(--win)' : 'var(--loss)' }}>
                {winRate} WR
              </span>
            )}
            {totalGames > 0 && (
              <span style={{ color: 'var(--muted)' }}>{totalGames}G</span>
            )}
          </>
        ) : (
          <span style={{ color: 'var(--muted)' }}>Unranked</span>
        )}
      </div>
    </div>
  )
}