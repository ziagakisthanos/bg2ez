const CHAMPION_NAME_OVERRIDES: Record<string, string> = {
  'Wukong': 'MonkeyKing',
  'Nunu & Willump': 'Nunu',
  'Renata Glasc': 'Renata',
  'Bel\'Veth': 'Belveth',
  'Cho\'Gath': 'Chogath',
  'Kai\'Sa': 'Kaisa',
  'Kha\'Zix': 'Khazix',
  'Kog\'Maw': 'KogMaw',
  'LeBlanc': 'Leblanc',
  'Lee Sin': 'LeeSin',
  'Master Yi': 'MasterYi',
  'Miss Fortune': 'MissFortune',
  'Twisted Fate': 'TwistedFate',
  'Vel\'Koz': 'Velkoz',
  'Xin Zhao': 'XinZhao',
  'Rek\'Sai': 'RekSai',
}

export const formatRole = (role: string) =>
  role === 'UTILITY' ? 'SUPPORT' : role

export const championIcon = (version: string, championName: string) => {
  const name = CHAMPION_NAME_OVERRIDES[championName] ?? championName.replace(/\s/g, '')
  return `https://ddragon.leagueoflegends.com/cdn/${version}/img/champion/${name}.png`
}

export const championSplash = (championName: string) => {
  const name = CHAMPION_NAME_OVERRIDES[championName] ?? championName.replace(/\s/g, '')
  return `https://ddragon.leagueoflegends.com/cdn/img/champion/splash/${name}_0.jpg`
}

export const tierEmblem = (tier: string) =>
  `https://raw.communitydragon.org/14.6/plugins/rcp-fe-lol-static-assets/global/default/ranked-emblem/emblem-${tier.toLowerCase()}.png`

export const roleIcon = (role: string) =>
  `https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-champ-select/global/default/svg/position-${role.toLowerCase()}.svg`

