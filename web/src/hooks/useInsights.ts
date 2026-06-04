import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import client from '../api/client'

export function useInsights() {
  return useQuery({
    queryKey: ['insights'],
    queryFn: () => client.get('/coach/insights').then(r => r.data),
  })
}

export function useLatestInsight(type: string) {
  return useQuery({
    queryKey: ['insights', 'latest', type],
    queryFn: () => client.get(`/coach/insights/latest?type=${type}`).then(r => r.data),
    retry: false,
  })
}

export function useAnalyse() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: { type: string; subject?: string; window?: number }) =>
      client.post('/coach/analyse', body).then(r => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['insights'] })
    },
  })
}