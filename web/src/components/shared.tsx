export function PageTitle({ title }: { title: string }) {
  return (
    <div style={{
      fontSize: '14px',
      color: 'var(--accent)',
      borderBottom: '1px solid var(--border)',
      paddingBottom: '16px',
      marginBottom: '8px',
      letterSpacing: '3px',
    }}>
      {title}
    </div>
  )
}

export function Loader() {
  return (
    <div style={{ color: 'var(--muted)', fontSize: '8px', padding: '32px' }}>
      LOADING...
    </div>
  )
}