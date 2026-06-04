import { useState, useEffect } from 'react'
import client from '../api/client'
import { useQueryClient } from '@tanstack/react-query'

const COOLDOWN_SECONDS = 120

export default function SyncButton() {
  const [syncing, setSyncing] = useState(false)
  const [lastSynced, setLastSynced] = useState<Date | null>(null)
  const [secondsAgo, setSecondsAgo] = useState<number | null>(null)
  const [cooldown, setCooldown] = useState(0)
  const queryClient = useQueryClient()

  // tick the "X seconds ago" counter
  useEffect(() => {
    if (!lastSynced) return
    const interval = setInterval(() => {
      setSecondsAgo(Math.floor((Date.now() - lastSynced.getTime()) / 1000))
    }, 1000)
    return () => clearInterval(interval)
  }, [lastSynced])

  // tick the cooldown counter
  useEffect(() => {
    if (cooldown <= 0) return
    const interval = setInterval(() => {
      setCooldown(c => {
        if (c <= 1) {
          clearInterval(interval)
          return 0
        }
        return c - 1
      })
    }, 1000)
    return () => clearInterval(interval)
  }, [cooldown])

  const sync = async () => {
    if (syncing || cooldown > 0) return
    setSyncing(true)
    try {
      await client.post('/sync?count=20')
      setLastSynced(new Date())
      setSecondsAgo(0)
      setCooldown(COOLDOWN_SECONDS)
      queryClient.invalidateQueries()
    } catch {
      // silent fail — user can try again after cooldown
    } finally {
      setSyncing(false)
    }
  }

  const formatSecondsAgo = (s: number) => {
    if (s < 60) return `${s}s ago`
    if (s < 3600) return `${Math.floor(s / 60)}m ago`
    return `${Math.floor(s / 3600)}h ago`
  }

  const isDisabled = syncing || cooldown > 0

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
      <button
        onClick={sync}
        disabled={isDisabled}
        className="inter"
        style={{
          background: isDisabled ? 'var(--surface)' : 'var(--accent)',
          color: isDisabled ? 'var(--muted)' : '#000',
          border: `1px solid ${isDisabled ? 'var(--border)' : 'var(--accent)'}`,
          padding: '10px 12px',
          fontSize: '12px',
          letterSpacing: '1px',
          cursor: isDisabled ? 'not-allowed' : 'pointer',
          width: '100%',
        }}
      >
        {syncing ? 'SYNCING...' : cooldown > 0 ? `SYNCED` : 'SYNC'}
      </button>

      {lastSynced && secondsAgo !== null && (
        <div style={{
          color: 'var(--muted)',
          fontSize: '9px',
          textAlign: 'center',
          fontFamily: 'Krub, sans-serif',
        }}>
          synced {formatSecondsAgo(secondsAgo)}
        </div>
      )}
    </div>
  )
}