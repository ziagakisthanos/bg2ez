import { useMutation, useQueryClient } from '@tanstack/react-query'
import client from '../api/client'

export function useUnlinkAccount() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => client.delete('/account/unlink').then(r => r.data),
    onSuccess: () => {
      queryClient.setQueryData(['me'], null)
      queryClient.invalidateQueries({ queryKey: ['me'] })
    },
  })
}