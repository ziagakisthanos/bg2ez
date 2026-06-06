import { useQuery } from '@tanstack/react-query'
import { useDDragonVersion } from './useDDragonVersion'
import axios from 'axios'

export function useChampionData() {
  const { data: version } = useDDragonVersion()
  return useQuery({
    queryKey: ['champion-data', version],
    queryFn: () =>
      axios.get(`https://ddragon.leagueoflegends.com/cdn/${version}/data/en_US/champion.json`)
           .then(r => r.data.data),
    enabled: !!version,
    staleTime: 1000 * 60 * 60 * 24,
  })
}