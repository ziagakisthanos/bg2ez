import { useQuery } from '@tanstack/react-query'
import client from '../api/client'

export function useMatches(limit = 20) {
  return useQuery({
    queryKey: ['matches', limit],
    queryFn: () => client.get(`/me/matches?limit=${limit}`).then(r => r.data),
  })
}