import { useMutation, useQueryClient } from '@tanstack/react-query'
import client from '../api/client'

export function useLinkAccount() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ gameName, tagLine }: { gameName: string; tagLine: string }) =>
      client.post(`/account/link/${gameName}/${tagLine}`).then(r => r.data),
    onSuccess: async () => {
      await client.post('/sync?count=20')
      queryClient.invalidateQueries({ queryKey: ['me'] })
      queryClient.invalidateQueries({ queryKey: ['champions'] })
      queryClient.invalidateQueries({ queryKey: ['roles'] })
      queryClient.invalidateQueries({ queryKey: ['ranked'] })
    },
  })
}