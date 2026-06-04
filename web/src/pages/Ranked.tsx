import { useRanked } from '../hooks/useRanked'
import { tierEmblem } from '../utils/assets'
import { PageTitle, Loader } from '../components/shared'

export default function Ranked() {
  const { data: ranked, isLoading } = useRanked()

  if (isLoading) return <Loader />

  const solo = ranked?.filter((r: any) => r.queueType === 'RANKED_SOLO_5x5')
  const flex = ranked?.filter((r: any) => r.queueType === 'RANKED_FLEX_SR')

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
      <PageTitle title="RANK HISTORY" />
      <QueueSection title="SOLO / DUO" entries={solo} />
      <QueueSection title="FLEX" entries={flex} />
    </div>
  )
}

function QueueSection({ title, entries }: { title: string; entries: any[] }) {
  if (!entries?.length) return null
  return (
    <div>
      <div style={{
        color: 'var(--muted)',
        fontSize: '11px',
        letterSpacing: '2px',
        borderBottom: '1px solid var(--border)',
        paddingBottom: '8px',
        marginBottom: '16px',
      }}>
        {title}
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
        {entries.map((e: any, i: number) => (
          <RankRow key={i} e={e} />
        ))}
      </div>
    </div>
  )
}

function RankRow({ e }: { e: any }) {
  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: '180px 80px 80px 80px 120px',
      gap: '16px',
      padding: '10px 12px',
      border: '1px solid var(--border)',
      background: 'var(--surface)',
      fontSize: '13px',
      alignItems: 'center',
      position: 'relative',
      overflow: 'hidden',
    }}>
      {/* Ghost background emblem */}
      {e.tier && (
        <img
          src={tierEmblem(e.tier)}
          style={{
            position: 'absolute',
            right: '-10px',
            top: '50%',
            transform: 'translateY(-48%)',
            width: '700px',
            height: '700px',
            opacity: 0.2,
//             filter: 'grayscale(100%)',
            pointerEvents: 'none',
            objectFit: 'cover',
          }}
        />
      )}

      <span style={{ color: 'var(--accent)', fontFamily: 'Ultra, serif', fontSize: '18px' }}>
        {e.tier} {e.rank}
      </span>
      <span>{e.leaguePoints} LP</span>
      <span style={{ color: 'var(--win)' }}>{e.wins}W</span>
      <span style={{ color: 'var(--loss)' }}>{e.losses}L</span>
      <span style={{ color: 'var(--muted)' }}>
        {new Date(e.recordedAt).toLocaleDateString()}
      </span>
    </div>
  )
}