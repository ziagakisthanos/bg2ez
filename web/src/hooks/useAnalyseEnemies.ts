import { useMutation } from '@tanstack/react-query'
import client from '../api/client'

export function useAnalyseEnemies() {
  return useMutation({
    mutationFn: (body: { gameId: string; enemies: { puuid: string; championName: string }[] }) =>
      client.post('/live/analyse', body).then(r => r.data),
  })
}