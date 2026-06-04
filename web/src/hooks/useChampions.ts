import { useQuery } from '@tanstack/react-query'
import client from '../api/client'

export function useChampions() {
  return useQuery({
    queryKey: ['champions'],
    queryFn: () => client.get('/me/champions').then(r => r.data),
  })
}