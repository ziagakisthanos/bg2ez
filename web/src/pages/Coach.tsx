import { useState } from 'react'
import { useAnalyse, useInsights } from '../hooks/useInsights'
import { PageTitle, Loader } from '../components/shared'

const INSIGHT_TYPES = [
  { type: 'session', label: 'SESSION', description: 'Last N games analysis', defaultWindow: 10 },
  { type: 'last_game', label: 'LAST GAME', description: 'Deep dive on your last game', defaultWindow: 1 },
  { type: 'champion', label: 'CHAMPION', description: 'Champion-specific macro analysis', defaultWindow: 20 },
]

export default function Coach() {
  const [selectedType, setSelectedType] = useState('session')
  const [champion, setChampion] = useState('')
  const [window, setWindow] = useState(10)
  const [activeInsight, setActiveInsight] = useState<any>(null)

  const { data: insights, isLoading } = useInsights()
  const analyse = useAnalyse()

  const handleAnalyse = async () => {
    const result = await analyse.mutateAsync({
      type: selectedType,
      subject: selectedType === 'champion' ? champion : undefined,
      window,
    })
    setActiveInsight(result)
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '32px'}}>
      <PageTitle title="AI COACH MPARTZOKAS"/>

      {/* Type selector */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div style={{ color: 'var(--muted)', fontSize: '11px' }}>SELECT ANALYSIS TYPE</div>
        <div style={{ display: 'flex', gap: '8px' }}>
          {INSIGHT_TYPES.map(({ type, label, description, defaultWindow }) => (
            <button
              key={type}
              onClick={() => {
                setSelectedType(type)
                setWindow(defaultWindow)
              }}
              style={{
                flex: 1,
                padding: '16px',
                border: `1px solid ${selectedType === type ? 'var(--accent)' : 'var(--border)'}`,
                background: selectedType === type ? 'var(--surface)' : 'transparent',
                color: selectedType === type ? 'var(--accent)' : 'var(--muted)',
                cursor: 'pointer',
                textAlign: 'left',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px',
              }}
            >
              <span className="inter" style={{ fontSize: '12px' }}>{label}</span>
              <span style={{ fontSize: '11px', color: 'var(--muted)' }}>{description}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Options */}
      <div style={{ display: 'flex', gap: '16px', alignItems: 'flex-end' }}>
        {selectedType === 'champion' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <label style={{ color: 'var(--muted)', fontSize: '11px' }}>CHAMPION NAME</label>
            <input
              value={champion}
              onChange={e => setChampion(e.target.value)}
              placeholder="e.g. Anivia"
              style={{
                background: 'var(--surface)',
                border: '1px solid var(--border)',
                color: 'var(--text)',
                padding: '10px 12px',
                fontSize: '13px',
                fontFamily: 'Krub, sans-serif',
                outline: 'none',
                width: '200px',
              }}
            />
          </div>
        )}

        {selectedType === 'session' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <label style={{ color: 'var(--muted)', fontSize: '11px' }}>GAMES TO ANALYSE</label>
            <input
              type="number"
              value={window}
              onChange={e => setWindow(parseInt(e.target.value))}
              min={1}
              max={20}
              style={{
                background: 'var(--surface)',
                border: '1px solid var(--border)',
                color: 'var(--text)',
                padding: '10px 12px',
                fontSize: '13px',
                fontFamily: 'Krub, sans-serif',
                outline: 'none',
                width: '100px',
              }}
            />
          </div>
        )}

        <button
          onClick={handleAnalyse}
          disabled={analyse.isPending || (selectedType === 'champion' && !champion)}
          className="inter"
          style={{
            background: analyse.isPending ? 'var(--surface)' : 'var(--accent)',
            color: analyse.isPending ? 'var(--muted)' : '#000',
            border: '1px solid var(--accent)',
            padding: '12px 24px',
            fontSize: '10px',
            cursor: analyse.isPending ? 'not-allowed' : 'pointer',
          }}
        >
          {analyse.isPending ? 'ANALYSING...' : 'ANALYSE'}
        </button>

        {analyse.isPending && (
          <div style={{ color: 'var(--muted)', fontSize: '11px' }}>
            This may take 30-60 seconds...
          </div>
        )}
      </div>

      {/* Active insight result */}
      {activeInsight && <InsightDisplay insight={activeInsight} />}

      {/* Past insights */}
      {!isLoading && insights?.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div style={{
            color: 'var(--muted)',
            fontSize: '11px',
            letterSpacing: '2px',
            borderBottom: '1px solid var(--border)',
            paddingBottom: '8px',
          }}>
            PAST ANALYSES
          </div>
          {insights.map((insight: any, i: number) => (
            <div
              key={i}
              onClick={() => setActiveInsight(insight)}
              style={{
                border: '1px solid var(--border)',
                background: 'var(--surface)',
                padding: '12px 16px',
                cursor: 'pointer',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                fontSize: '12px',
              }}
            >
              <span style={{ color: 'var(--accent)' }}>
                {insight.insightType.toUpperCase()}
                {insight.subject ? ` — ${insight.subject}` : ''}
              </span>
              <span style={{ color: 'var(--muted)', fontSize: '11px' }}>
                {new Date(insight.generatedAt).toLocaleString()}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

function InsightDisplay({ insight }: { insight: any }) {
  const sectionColors: Record<string, string> = {
    strengths: 'var(--win)',
    weaknesses: 'var(--loss)',
    patterns: 'var(--accent)',
    advice: 'var(--text)',
    summary: 'var(--text)',
  }

  return (
    <div style={{
      border: '1px solid var(--accent)',
      background: 'var(--surface)',
      padding: '24px',
      display: 'flex',
      flexDirection: 'column',
      gap: '24px',
    }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ color: 'var(--accent)', fontFamily: 'Ultra, serif', fontSize: '14px' }}>
          {insight.insightType.toUpperCase()}
          {insight.subject ? ` — ${insight.subject}` : ''}
        </div>
        <div style={{ color: 'var(--muted)', fontSize: '11px' }}>
          {insight.matchWindow} games · {new Date(insight.generatedAt).toLocaleString()}
        </div>
      </div>

      {/* Sections */}
      {insight.sections?.map((section: any) => (
        <div key={section.sectionType} style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{
            color: sectionColors[section.sectionType] ?? 'var(--text)',
            fontSize: '11px',
            letterSpacing: '2px',
            borderBottom: `1px solid ${sectionColors[section.sectionType] ?? 'var(--border)'}`,
            paddingBottom: '6px',
          }}>
            {section.sectionType.toUpperCase()}
          </div>
          <div style={{
            color: 'var(--text)',
            fontSize: '13px',
            lineHeight: '2',
            whiteSpace: 'pre-wrap',
          }}>
            {section.content}
          </div>
        </div>
      ))}
    </div>
  )
}