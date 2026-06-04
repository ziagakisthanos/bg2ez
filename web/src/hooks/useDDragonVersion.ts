import { useQuery } from '@tanstack/react-query'
import axios from 'axios'

export function useDDragonVersion() {
  return useQuery({
    queryKey: ['ddragon-version'],
    queryFn: () =>
      axios.get('https://ddragon.leagueoflegends.com/api/versions.json').then(r => r.data[0]),
    staleTime: 1000 * 60 * 60 * 24, // cache for 24h
  })
}