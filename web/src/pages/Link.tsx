import { useState } from 'react'
import { useLinkAccount } from '../hooks/useLinkAccount'

export default function Link() {
  const [gameName, setGameName] = useState('')
  const [tagLine, setTagLine] = useState('EUNE')
  const [error, setError] = useState<string | null>(null)
  const link = useLinkAccount()

  const handleLink = async () => {
    if (!gameName || !tagLine) return
    setError(null)
    try {
      await link.mutateAsync({ gameName, tagLine })
    } catch {
      setError('Could not find that Riot ID. Check the name and tag and try again.')
    }
  }

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '48px',
    }}>
      {/* Logo */}
      <div style={{ textAlign: 'center', display: 'flex', flexDirection: 'column', gap: '10px' }}>
        <div className="ultra" style={{
          fontSize: '62px',
          color: 'var(--accent)',
          letterSpacing: '2px',
        }}>
          BG2EZ
        </div>
        <div style={{ color: 'var(--muted)', fontSize: '13px' }}>
          Your personal League of Legends companion
        </div>
      </div>

      {/* Link form */}
      <div style={{
        border: '1px solid var(--border)',
        background: 'var(--surface)',
        padding: '32px',
        display: 'flex',
        flexDirection: 'column',
        gap: '24px',
        width: '400px',
      }}>

        <div style={{ display: 'flex', gap: '8px', alignItems: 'flex-end' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', flex: 1 }}>
            <label style={{ color: 'var(--muted)', fontSize: '11px' }}>GAME NAME</label>
            <input
              value={gameName}
              onChange={e => setGameName(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleLink()}
              placeholder=" "
              style={{
                background: '#0a0a0a',
                border: '1px solid var(--border)',
                color: 'var(--text)',
                padding: '10px 12px',
                fontSize: '13px',
                fontFamily: 'Krub, sans-serif',
                outline: 'none',
                width: '100%',
              }}
            />
          </div>

          <div style={{ color: 'var(--muted)', fontSize: '20px', paddingBottom: '4px' }}>#</div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', width: '70px' }}>
            <label style={{ color: 'var(--muted)', fontSize: '11px' }}>TAG</label>
            <input
              value={tagLine}
              onChange={e => setTagLine(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleLink()}
              placeholder="EUNE"
              style={{
                background: '#0a0a0a',
                border: '1px solid var(--border)',
                color: 'var(--text)',
                padding: '10px 12px',
                fontSize: '13px',
                fontFamily: 'Krub, sans-serif',
                outline: 'none',
                width: '100%',
              }}
            />
          </div>
        </div>

        {error && (
          <div style={{ color: 'var(--loss)', fontSize: '11px' }}>{error}</div>
        )}

        <button
          onClick={handleLink}
          disabled={link.isPending || !gameName || !tagLine}
          style={{
            background: link.isPending ? 'var(--surface)' : 'var(--accent)',
            color: link.isPending ? 'var(--muted)' : '#000',
            border: '1px solid var(--accent)',
            padding: '14px',
            fontSize: '16px',
            fontWeight: 'nold',
            cursor: link.isPending ? 'not-allowed' : 'pointer',
            letterSpacing: '1px',
          }}
        >
          {link.isPending ? 'LINKING...' : 'LINK ACCOUNT'}
        </button>
      </div>
    </div>
  )
}