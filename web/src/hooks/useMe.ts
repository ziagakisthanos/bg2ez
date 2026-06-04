import { useQuery } from '@tanstack/react-query'
import client from '../api/client'

export function useMe() {
  return useQuery({
    queryKey: ['me'],
    queryFn: () => client.get('/me').then(r => r.data).catch(() => null),
    retry: false,
  })
}