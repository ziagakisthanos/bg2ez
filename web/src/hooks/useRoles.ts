import { useQuery } from '@tanstack/react-query'
import client from '../api/client'

export function useRoles() {
  return useQuery({
    queryKey: ['roles'],
    queryFn: () => client.get('/me/roles').then(r => r.data),
  })
}