package com.lolcompanion.bg2ez.coaching.model;

public record AnalyseRequest(
        String type,      // session | last_game | champion
        String subject,   // champion if type=champion, otherwise null
        Integer window    // number of matches, null uses default
) {}