import { useQuery } from '@tanstack/react-query'
import client from '../api/client'

export function useRanked() {
  return useQuery({
    queryKey: ['ranked'],
    queryFn: () => client.get('/me/ranked').then(r => r.data),
  })
}