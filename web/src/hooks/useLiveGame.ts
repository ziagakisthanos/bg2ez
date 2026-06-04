import { useQuery } from '@tanstack/react-query'
import client from '../api/client'
import { useMe } from './useMe'

export function useLiveGame() {
      const { data: me } = useMe()

  return useQuery({
    queryKey: ['live'],
    queryFn: () => client.get('/live').then(r => r.data).catch(() => null),
    refetchInterval: 30000,
    retry: false,
    enabled: !!me,
  })
}