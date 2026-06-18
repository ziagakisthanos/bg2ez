import { useState } from 'react'
import { useMe } from '../hooks/useMe'
import { useChampions } from '../hooks/useChampions'
import { useRoles } from '../hooks/useRoles'
import { useRanked } from '../hooks/useRanked'
import { useMatches } from '../hooks/useMatches'
import { useDDragonVersion } from '../hooks/useDDragonVersion'
import { championIcon, championSplash, tierEmblem, roleIcon } from '../utils/assets'
import { computePlayerLabels } from '../utils/playerLabels'
import { computePerformanceMetrics } from '../utils/performanceMetrics'
import {
  LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer,
  BarChart, Bar, Cell, LabelList
} from 'recharts'

const TIER_ORDER = ['IRON','BRONZE','SILVER','GOLD','PLATINUM','EMERALD','DIAMOND','MASTER','GRANDMASTER','CHALLENGER']
const RANK_ORDER = ['IV','III','II','I']
const toAbsoluteLp = (tier: string, rank: string, lp: number) => {
const tierIndex = TIER_ORDER.indexOf(tier) * 400
const rankIndex = RANK_ORDER.indexOf(rank) * 100
return tierIndex + rankIndex + lp
}

export default function Dashboard() {
  const { data: me } = useMe()
  const { data: champions } = useChampions()
  const { data: roles } = useRoles()
  const { data: ranked } = useRanked()
  const { data: matches } = useMatches(20)
  const { data: version } = useDDragonVersion()

  const solo = ranked?.find((r: any) => r.queueType === 'RANKED_SOLO_5x5')
  const topChampion = champions?.[0]
  const topRole = roles?.sort((a: any, b: any) => b.gamesPlayed - a.gamesPlayed)?.[0]
  const labels = computePlayerLabels(champions ?? [], roles ?? [], matches ?? [])
  const metrics = computePerformanceMetrics(champions ?? [], matches ?? [])

  const soloRanked = ranked
    ?.filter((r: any) => r.queueType === 'RANKED_SOLO_5x5')
    .slice()
    .reverse()
    .map((r: any) => ({
      rawLp: toAbsoluteLp(r.tier, r.rank, r.leaguePoints),
      lp: r.leaguePoints,
      tier: r.tier,
      rank: r.rank,
      date: new Date(r.recordedAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
      label: `${r.tier} ${r.rank} — ${r.leaguePoints} LP`,
    }))

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>

      {/* Summoner header */}
      <div>
        <div style={{ color: 'var(--muted)', fontSize: '11px', marginBottom: '8px' }}>
          SUMMONER
        </div>
        <div style={{ fontSize: '28px', color: 'var(--accent)', fontFamily: 'Ultra, serif' }}>
          {me?.gameName}
          <span style={{ color: 'var(--muted)' }}>#{me?.tagLine}</span>
        </div>
        <div style={{ color: 'var(--muted)', fontSize: '11px', marginTop: '8px' }}>
          LVL {me?.summonerLevel}
        </div>
      </div>

      {/* Row 1 — stat cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
        <RankCard solo={solo} />
        <ChampionCard champion={topChampion} version={version} />
        <RoleCard role={topRole} />
      </div>

      {/* Row 2 — charts and labels */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
        <LpChart data={soloRanked ?? []} ranked={ranked ?? []} />
        <PlayerLabels labels={labels} />
        <PerformanceChart metrics={metrics} />
      </div>

      {/* Row 3 — match history */}
      <div>
        <SectionTitle title="RECENT MATCHES" />
        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          {matches?.slice(0, 5).map((match: any) => (
            <DashboardMatchRow key={match.matchId} match={match} version={version} />
          ))}
        </div>
      </div>

    </div>
  )
}

// --- Row 1 Cards ---

function RankCard({ solo }: { solo: any }) {
  const total = (solo?.wins ?? 0) + (solo?.losses ?? 0)
  const wr = total > 0 ? solo.wins / total : 0

  return (
    <div style={cardStyle}>
      {solo?.tier && (
        <img src={tierEmblem(solo.tier)} style={ghostBgStyle('320px', '180px', 'cover')} />
      )}
      <CardLabel>SOLO RANK</CardLabel>
      <div style={{ fontSize: '16px', fontFamily: 'Ultra, serif', position: 'relative' }}>
        {solo ? `${solo.tier} ${solo.rank}` : '—'}
      </div>
      {solo && (
        <>
          <div style={{ color: 'var(--accent)', fontSize: '11px', position: 'relative' }}>
            {solo.leaguePoints} LP
          </div>
          <div style={{ position: 'relative', marginTop: '8px' }}>
            <div style={{
              height: '4px',
              background: 'var(--border)',
              width: '100%',
            }}>
              <div style={{
                height: '4px',
                width: `${wr * 100}%`,
                background: wr >= 0.5 ? 'var(--win)' : 'var(--loss)',
                transition: 'width 0.3s',
              }} />
            </div>
            <div style={{ color: 'var(--muted)', fontSize: '10px', marginTop: '4px' }}>
              {solo.wins}W {solo.losses}L
            </div>
          </div>
        </>
      )}
    </div>
  )
}

function ChampionCard({ champion, version }: { champion: any; version: string }) {
  return (
    <div style={cardStyle}>
      {champion && (
        <img
          src={championSplash(champion.championName)}
          style={{
            position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
            width: '100%', height: '100%',
            opacity: 0.22, filter: 'grayscale(20%)',
            pointerEvents: 'none', objectFit: 'cover',
            objectPosition: 'center -20px',
          }}
        />
      )}
      <CardLabel>TOP CHAMPION</CardLabel>
      <div style={{ fontSize: '16px', fontFamily: 'Ultra, serif', position: 'relative' }}>
        {champion?.championName ?? '—'}
      </div>
      {champion && (
        <>
          <div style={{ color: 'var(--accent)', fontSize: '11px', position: 'relative' }}>
            {champion.winRate}% WR
          </div>
          <div style={{ color: 'var(--muted)', fontSize: '11px', position: 'relative' }}>
            KDA {champion.avgKda}
          </div>
        </>
      )}
    </div>
  )
}

function RoleCard({ role }: { role: any }) {
  return (
    <div style={cardStyle}>
      {role && (
        <img
          src={roleIcon(role.role)}
          style={{
            position: 'absolute', right: '12px', bottom: '12px',
            width: '52px', height: '52px',
            opacity: 0.12, filter: 'grayscale(100%) invert(1)',
            pointerEvents: 'none', objectFit: 'contain',
          }}
        />
      )}
      <CardLabel>BEST ROLE</CardLabel>
      <div style={{ fontSize: '16px', fontFamily: 'Ultra, serif', position: 'relative' }}>
        {role ? (role.role === 'UTILITY' ? 'SUPPORT' : role.role) : '—'}
      </div>
      {role && (
        <>
          <div style={{ color: 'var(--accent)', fontSize: '11px', position: 'relative' }}>
            {role.winRate}% WR
          </div>
          <div style={{ color: 'var(--muted)', fontSize: '11px', position: 'relative' }}>
            {role.gamesPlayed} GAMES
          </div>
        </>
      )}
    </div>
  )
}

// --- Row 2 Cards ---

function LpChart({ data, ranked }: { data: any[]; ranked: any[] }) {
  const [activePoint, setActivePoint] = useState<any>(null)

  if (data.length === 0) return (
    <div style={cardStyle}>
      <CardLabel>LP HISTORY</CardLabel>
      <div style={{ color: 'var(--muted)', fontSize: '11px', marginTop: '8px' }}>No ranked data yet</div>
    </div>
  )

  // Compute header stats
  const first = data[0]
  const last = data[data.length - 1]
  const lpGained = last.rawLp - first.rawLp
  const peak = [...data].sort((a, b) => b.rawLp - a.rawLp)[0]
    return (
        <div style={cardStyle}>
          {/* Header */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px', position: 'relative' }}>
            <div>
              <CardLabel>LP HISTORY</CardLabel>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ color: 'var(--muted)', fontSize: '10px' }}>Last {data.length} syncs</span>
                <span style={{
                  color: lpGained >= 0 ? 'var(--win)' : 'var(--loss)',
                  fontSize: '10px',
                }}>
                  {lpGained >= 0 ? '▲' : '▼'} {Math.abs(lpGained)} LP
                </span>
              </div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ color: 'var(--muted)', fontSize: '9px' }}>PEAK</div>
              <div style={{ color: 'var(--accent)', fontSize: '10px', fontFamily: 'Ultra, serif' }}>
                {peak?.tier} {peak?.rank} — {peak?.lp} LP
              </div>
            </div>
          </div>

          {/* Active point tooltip */}
          {activePoint && (
            <div style={{
              position: 'absolute',
              top: '60px',
              left: '16px',
              background: 'var(--surface)',
              border: '1px solid var(--accent)',
              padding: '6px 10px',
              fontSize: '10px',
              color: 'var(--text)',
              zIndex: 10,
              pointerEvents: 'none',
            }}>
              <div style={{ color: 'var(--accent)', fontFamily: 'Ultra, serif' }}>
                {activePoint.tier} {activePoint.rank}
              </div>
              <div>{activePoint.lp} LP</div>
              <div style={{ color: 'var(--muted)' }}>{activePoint.date}</div>
            </div>
          )}

          {data.length < 2 ? (
            <div style={{ color: 'var(--muted)', fontSize: '11px' }}>Sync more to see history</div>
          ) : (
            <ResponsiveContainer width="100%" height={100}>
              <LineChart data={data}>
                <YAxis
                  stroke="transparent"
                  tick={{ fill: 'var(--muted)', fontSize: 8, fontFamily: 'Krub' }}
                  tickFormatter={(val) => {
                    const tierIndex = Math.floor(val / 400)
                    const rankIndex = Math.floor((val % 400) / 100)
                    const rank = RANK_ORDER[rankIndex] ?? ''
                    const tier = TIER_ORDER[tierIndex]?.charAt(0) ?? ''
                    return `${tier}${rank}`
                  }}
                  width={24}
                />
                <Tooltip content={() => null} />
                <Line
                  type="monotone"
                  dataKey="rawLp"
                  stroke="var(--accent)"
                  strokeWidth={2}
                  dot={(props: any) => {
                    const { cx, cy, payload } = props
                    return (
                      <circle
                        key={payload.date}
                        cx={cx}
                        cy={cy}
                        r={4}
                        fill={activePoint?.date === payload.date ? 'var(--text)' : 'var(--accent)'}
                        stroke="var(--accent)"
                        strokeWidth={1}
                        style={{ cursor: 'pointer' }}
                        onMouseEnter={() => setActivePoint(payload)}
                        onMouseLeave={() => setActivePoint(null)}
                      />
                    )
                  }}
                  activeDot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          )}

          {/* X axis dates */}
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            color: 'var(--muted)',
            fontSize: '9px',
            marginTop: '2px',
            position: 'relative',
          }}>
            <span>{data[0]?.date}</span>
            <span>{data[data.length - 1]?.date}</span>
          </div>
        </div>
      )
    }

const LABEL_DESCRIPTIONS: Record<string, string> = {
  'CARRY THREAT': 'Deals high damage — a primary win condition in teamfights',
  'MECHANICAL': 'Maintains high KDA — efficient with resources and positioning',
  'GOOD VISION': 'Places wards consistently — strong map awareness',
  'TEAM PLAYER': 'High kill participation — always present in fights',
  'ROAM EXPERT': 'Frequently kills in lanes outside their primary role',
  'GANK EXPERT': 'Jungle win rate above 55% — effective at creating pressure',
  'CONSISTENT': 'High win rate on main champion with significant games played',
  'EARLY HUNTER': 'Secures kills before 10 minutes — aggressive early game',
  'OBJ FOCUSED': 'Participates in dragon and baron kills regularly',
  'VISION DENIER': 'Destroys enemy wards consistently — active vision control',
  'TOO CONFIDENT': 'Low KDA suggests taking too many risks',
  'BAD MENTAL': 'Lost 4 or more of the last 5 games — possible tilt',
  'VISION PROBLEM': 'Below average vision score — needs more ward placement',
  'WARD NEGLECT': 'Placing very few wards per game',
  'OBJ AVOIDER': 'Rarely participates in objective kills',
  'ONE TRICK': 'Over 80% of games on one champion — limited champion pool',
  'FARMING MACHINE': 'Above average CS — strong laning and wave management',
  'ROAM MACHINE': 'Extremely high roam kill rate — map presence is a key strength',
}

function PlayerLabels({ labels }: { labels: any[] }) {
  const [hoveredLabel, setHoveredLabel] = useState<string | null>(null)

  return (
    <div style={cardStyle}>
      <CardLabel>PLAYER PROFILE</CardLabel>
      {labels.length === 0 ? (
        <div style={{ color: 'var(--muted)', fontSize: '11px', marginTop: '8px' }}>
          Not enough data
        </div>
      ) : (
        <div style={{ position: 'relative' }}>
          <div style={{
            display: 'flex',
            flexWrap: 'wrap',
            gap: '6px',
            marginTop: '8px',
          }}>
            {labels.map((label: any) => (
              <span
                key={label.label}
                onMouseEnter={() => setHoveredLabel(label.label)}
                onMouseLeave={() => setHoveredLabel(null)}
                style={{
                  border: `1px solid ${label.color}`,
                  color: label.color,
                  fontSize: '10px',
                  padding: '4px 8px',
                  fontFamily: 'Krub, sans-serif',
                  letterSpacing: '0.5px',
                  cursor: 'default',
                  position: 'relative',
                }}
              >
                {label.label}

                {/* Tooltip */}
                {hoveredLabel === label.label && LABEL_DESCRIPTIONS[label.label] && (
                  <div style={{
                    position: 'absolute',
                    bottom: 'calc(100% + 6px)',
                    left: 0,
                    background: 'var(--surface)',
                    border: `1px solid ${label.color}`,
                    padding: '6px 10px',
                    fontSize: '10px',
                    color: 'var(--text)',
                    whiteSpace: 'nowrap',
                    zIndex: 100,
                    pointerEvents: 'none',
                    lineHeight: '1.5',
                    minWidth: '180px',
                    maxWidth: '240px',
                    whiteSpace: 'normal',
                  }}>
                    {LABEL_DESCRIPTIONS[label.label]}
                  </div>
                )}
              </span>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

function PerformanceChart({ metrics }: { metrics: any }) {
  if (!metrics) return (
    <div style={cardStyle}>
      <CardLabel>PERFORMANCE</CardLabel>
      <div style={{ color: 'var(--muted)', fontSize: '11px', marginTop: '8px' }}>
        Not enough data
      </div>
    </div>
  )

  const bars = [
    {
      key: 'KP',
      value: metrics.killParticipation,
      max: 100,
      threshold: 65,
      label: 'TEAM PLAYER',
    },
    {
      key: 'CS',
      value: metrics.csPerGame,
      max: 300,
      threshold: 180,
      label: 'FARMING',
    },
    {
      key: 'VISION',
      value: metrics.visionScore,
      max: 60,
      threshold: 25,
      label: 'VISION',
    },
    {
      key: 'WARDS',
      value: metrics.wardsPlaced,
      max: 30,
      threshold: 8,
      label: 'WARDS/G',
    },
    {
      key: 'ROAM',
      value: metrics.roamKills,
      max: 5,
      threshold: 1.5,
      label: 'ROAMING',
    },
  ]

  const data = bars.map(b => ({
    name: b.label,
    value: Math.min(b.value, b.max),
    pct: Math.min((b.value / b.max) * 100, 100),
    color: b.value >= b.threshold ? 'var(--win)' : 'var(--loss)',
    display: b.value,
  }))

  return (
    <div style={cardStyle}>
      <CardLabel>PERFORMANCE</CardLabel>
      <ResponsiveContainer width="100%" height={130}>
        <BarChart data={data} barCategoryGap="20%">
          <XAxis
            dataKey="name"
            stroke="transparent"
            tick={{ fill: 'var(--muted)', fontSize: 8, fontFamily: 'Krub' }}
          />
          <Tooltip
            contentStyle={{
              background: 'var(--surface)',
              border: '1px solid var(--border)',
              borderRadius: 0,
              fontSize: 11,
              fontFamily: 'Krub, sans-serif',
              color: 'var(--text)',
            }}
            formatter={(value: any, _: any, props: any) => [props.payload.display, props.payload.name]}
          />
          <Bar dataKey="pct" radius={0}>
            {data.map((entry, index) => (
              <Cell key={index} fill={entry.color} />
            ))}
            <LabelList
              dataKey="display"
              position="top"
              style={{ fill: 'var(--muted)', fontSize: 9, fontFamily: 'Krub' }}
            />
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}

// --- Match Row ---

function DashboardMatchRow({ match, version }: { match: any; version: string }) {
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
      <div style={{ alignSelf: 'stretch', background: p.win ? 'var(--win)' : 'var(--loss)' }} />
      {version && (
        <img
          src={championSplash(p.championName)}
          style={{
            position: 'absolute', right: 0, top: 0, bottom: 0,
            width: '180px', height: '100%',
            opacity: 0.04, filter: 'grayscale(30%)',
            objectFit: 'cover', objectPosition: 'center top',
            pointerEvents: 'none',
          }}
        />
      )}
      {version && (
        <img
          src={championIcon(version, p.championName)}
          style={{ width: '36px', height: '36px', objectFit: 'cover', imageRendering: 'pixelated' }}
        />
      )}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
        <span style={{ fontSize: '13px' }}>{p.championName}</span>
        <span style={{ fontSize: '11px', color: 'var(--muted)' }}>{match.queueLabel}</span>
      </div>
      <span style={{ color: p.win ? 'var(--win)' : 'var(--loss)', fontFamily: 'Ultra, serif', fontSize: '13px' }}>
        {p.win ? 'WIN' : 'LOSS'}
      </span>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
        <span style={{ fontSize: '13px' }}>{p.kills}/{p.deaths}/{p.assists}</span>
        <span style={{ fontSize: '11px', color: 'var(--muted)' }}>{kda} KDA</span>
      </div>
      <span style={{ fontSize: '13px', color: 'var(--muted)' }}>{p.cs} CS</span>
      <span style={{ fontSize: '11px', color: 'var(--muted)' }}>{duration}</span>
      {p.role && (
        <img
          src={roleIcon(p.role)}
          style={{ width: '18px', height: '18px', filter: 'invert(1)', opacity: 0.4 }}
        />
      )}
    </div>
  )
}

// --- Shared helpers ---

function SectionTitle({ title }: { title: string }) {
  return (
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
  )
}

function CardLabel({ children }: { children: React.ReactNode }) {
  return (
    <div style={{
      color: 'var(--muted)',
      fontSize: '11px',
      letterSpacing: '2px',
      marginBottom: '12px',
      position: 'relative',
    }}>
      {children}
    </div>
  )
}

const cardStyle: React.CSSProperties = {
  border: '1px solid var(--border)',
  padding: '20px 16px',
  background: 'var(--surface)',
  position: 'relative',
  overflow: 'hidden',
  minHeight: '180px',
  display: 'flex',
  flexDirection: 'column',
}

function ghostBgStyle(width: string, height: string, fit: 'cover' | 'contain'): React.CSSProperties {
  return {
    position: 'absolute',
    right: '-20px',
    bottom: '-20px',
    width,
    height,
    opacity: 0.18,
    pointerEvents: 'none',
    objectFit: fit,
  }
}