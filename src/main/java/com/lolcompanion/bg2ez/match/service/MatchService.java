package com.lolcompanion.bg2ez.match.service;

import com.lolcompanion.bg2ez.account.repository.SummonerRepository;
import com.lolcompanion.bg2ez.match.converter.MatchConverter;
import com.lolcompanion.bg2ez.match.entity.MatchParticipant;
import com.lolcompanion.bg2ez.match.model.MatchSummaryModel;
import com.lolcompanion.bg2ez.match.repository.MatchParticipantRepository;
import com.lolcompanion.bg2ez.match.repository.MatchSummaryRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final MatchSummaryRepository matchSummaryRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final SummonerRepository summonerRepository;
    private final MatchConverter matchConverter;

    public MatchService(MatchSummaryRepository matchSummaryRepository,
                        MatchParticipantRepository matchParticipantRepository,
                        SummonerRepository summonerRepository,
                        MatchConverter matchConverter) {
        this.matchSummaryRepository = matchSummaryRepository;
        this.matchParticipantRepository = matchParticipantRepository;
        this.summonerRepository = summonerRepository;
        this.matchConverter = matchConverter;
    }

    public List<MatchSummaryModel> getMatchHistory(int limit) {
        String puuid = summonerRepository.findAll()
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No account linked"))
                .getPuuid();

        // Get all participant rows for this player
        Map<String, MatchParticipant> participantByMatchId =
                matchParticipantRepository.findByPuuid(puuid)
                        .stream()
                        .collect(Collectors.toMap(
                                MatchParticipant::getMatchId,
                                p -> p,
                                (a, b) -> a));

        return matchSummaryRepository.findAll()
                .stream()
                .filter(s -> participantByMatchId.containsKey(s.getMatchId()))
                .sorted(Comparator.comparing(
                        s -> s.getGameStart() == null
                                ? java.time.OffsetDateTime.MIN
                                : s.getGameStart(),
                        Comparator.reverseOrder()))
                .limit(limit)
                .map(s -> matchConverter.toModel(s, participantByMatchId.get(s.getMatchId())))
                .toList();
    }
}