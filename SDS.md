# Software Design Specification
## bg2ez — Personal League of Legends Companion App

**Version:** 1.0.0  
**Status:** Built — Private / Local Development  
**Author:** [ziagakis thanos]  
**Last Updated:** 2026-06-04

---

## 1. Overview

bg2ez is a personal, self-hosted web application that serves as a stat tracker, match analytics dashboard, AI-powered coaching tool, and live game scouting companion for League of Legends. It links to a single Riot Games account via manual Riot ID lookup, pulls data from the Riot Games API, stores a rich match history locally, and uses an LLM to analyse patterns in your play and scout your opponents in real time.

The system is built in Java (Spring Boot) with a React frontend. It runs entirely on localhost and is not intended for public deployment at this stage.

---

## 2. Goals

- Link a Riot Games account via Riot ID and sync match history, ranked stats, and champion performance data
- Display a rich analytics dashboard: champion stats, role stats, rank history, match history
- Provide AI-powered coaching: session analysis, last game deep dive, champion-specific macro analysis
- Provide live game scouting: display all 10 players in a live game with their ranked stats and AI-generated opponent reports
- Store all AI-generated insights persistently so they accumulate over time
- Run entirely locally with no public deployment requirements

---

## 3. Architecture Overview

A modular monolith — one Spring Boot application, one deployable unit, internally organised into well-separated packages. All services run locally via Docker Compose.

```
+------------------------------------------------------------------+
|                        Browser (Web App)                         |
|                    React -- localhost:5173                        |
+----------------------------+-------------------------------------+
                             | HTTP / REST (proxied via Vite)
             +---------------+---------------+
             |     Spring Boot Application   |
             |        localhost:8080         |
             |                               |
             |  +----------+  +----------+  |
             |  |   Auth   |  |  Riot    |  |
             |  |  Module  |  |  Module  |  |
             |  +----------+  +----------+  |
             |  +----------+  +----------+  |
             |  |Analytics |  |  Coach   |  |
             |  |  Module  |  |  Module  |  |
             |  +----------+  +----------+  |
             |  +----------+                |
             |  |   Live   |                |
             |  |  Module  |                |
             |  +----------+                |
             +---------------+---------------+
                             |
               +-------------+-------------+
               |         PostgreSQL        |
               |       localhost:5432      |
               +-------------+-------------+
                             |
                   +---------+---------+
                   |   Ollama (LLM)    |
                   | localhost:11434   |
                   +-------------------+
```

### Internal Modules

| Module | Package | Responsibility |
|---|---|---|
| **account** | `com.lolcompanion.bg2ez.account` | Account linking, summoner persistence, unlink |
| **riot** | `com.lolcompanion.bg2ez.riot` | Riot API client, rate limiting, data sync pipeline |
| **analytics** | `com.lolcompanion.bg2ez.analytics` | Champion stats, role stats, rank history computation |
| **coaching** | `com.lolcompanion.bg2ez.coaching` | LLM context building, insight generation, opponent analysis |
| **live** | `com.lolcompanion.bg2ez.live` | Live game lookup via Spectator-V5, opponent scouting |
| **match** | `com.lolcompanion.bg2ez.match` | Match entities, match history service |
| **ranked** | `com.lolcompanion.bg2ez.ranked` | Ranked entry entities and repository |
| **config** | `com.lolcompanion.bg2ez.config` | App config entity (split start date) |

---

## 4. Technology Stack

### Backend

| Concern | Choice | Notes |
|---|---|---|
| Language | Java 21 (LTS) | Virtual threads, records, pattern matching |
| Framework | Spring Boot 4.0.6 | Latest major release |
| HTTP / REST | Spring MVC | Controller/service pattern |
| Data access | Spring Data JPA + Hibernate 7 | JPA entities, custom JPQL queries |
| DB migrations | Flyway | SQL-first, runs on startup |
| AI integration | Spring AI 2.0.0-M3 | Ollama provider; Anthropic available via config swap |
| Riot HTTP client | Spring RestClient | Synchronous; two clients: regional + platform |
| Security | Spring Security | CSRF disabled; all endpoints permitted (local only) |
| Build tool | Maven | `pom.xml` with Spring AI BOM |
| Lombok | Yes | `@Data` on all entities |

### Frontend

| Concern | Choice |
|---|---|
| Framework | React 18 + TypeScript |
| Build tool | Vite 5 |
| Styling | Tailwind CSS + inline styles |
| Data fetching | TanStack Query (React Query) v5 |
| Routing | React Router v6 |
| HTTP client | Axios |
| Charts | Recharts |
| Fonts | Press Start 2P (logo/nav/buttons), Krub (body), Ultra (headings/values), Inter 900 (heavy text) |

### Infrastructure

| Component | Tool |
|---|---|
| Containerisation | Docker Compose |
| Database | PostgreSQL 16 |
| Local LLM | Ollama (llama3.1 8B, CPU-only) |
| Secret management | `.env` file + IntelliJ run config env vars |

### Future / Optional

| Concern | Candidate | Trigger |
|---|---|---|
| LLM provider | Claude API (Anthropic) | Switch via `application.yml` — no code changes |
| Go microservices | Rate-limit proxy or LCU WebSocket layer | Only if specific bottleneck justifies it |

---

## 5. Riot Games API Integration

### API Key

- Personal Development API Key from developer.riotgames.com
- Rotates every 24 hours — update in IntelliJ run config env vars
- Stored as `RIOT_API_KEY` — never committed to git

### Regional Routing

| Cluster | Base URL | Used for |
|---|---|---|
| Regional | `https://europe.api.riotgames.com` | Account lookup, match list, match detail |
| Platform | `https://eun1.api.riotgames.com` | Summoner info, ranked entries, spectator, champion mastery |

Two `RestClient` instances are constructed in `RiotApiClient` — one per cluster. The API key is added per-request via `.header("X-Riot-Token", props.apiKey())`.

### Endpoints Used

| Feature | Endpoint | Cluster |
|---|---|---|
| Account by Riot ID | `GET /riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}` | Regional |
| Account by PUUID | `GET /riot/account/v1/accounts/by-puuid/{puuid}` | Regional |
| Summoner by PUUID | `GET /lol/summoner/v4/summoners/by-puuid/{puuid}` | Platform |
| Ranked entries | `GET /lol/league/v4/entries/by-puuid/{puuid}` | Platform |
| Match ID list | `GET /lol/match/v5/matches/by-puuid/{puuid}/ids` | Regional |
| Match detail | `GET /lol/match/v5/matches/{matchId}` | Regional |
| Champion mastery | `GET /lol/champion-mastery/v4/champion-masteries/by-puuid/{puuid}` | Platform |
| Live game | `GET /lol/spectator/v5/active-games/by-summoner/{puuid}` | Platform |

### Rate Limiting

Dev keys: 20 req/1s and 100 req/2min. Mitigation strategies:

- Match detail fetches are sequential (never parallel)
- Opponent analysis throttles 600ms between match detail fetches and 1500ms between players
- Default sync fetches 20 matches (configurable up to 100 via `?count=N`)
- Throttling only activates for syncs above 20 matches

### Account Linking Flow

No OAuth is used. Riot RSO requires a registered product which is out of scope for a private personal tool. Instead:

```
User enters gameName + tagLine on Link page
        |
        v
POST /api/v1/account/link/{gameName}/{tagLine}
        |
        v
RiotApiClient.getAccountByRiotId() -> PUUID, gameName, tagLine
RiotApiClient.getSummonerByPuuid() -> profileIconId, summonerLevel
        |
        v
Summoner persisted to account.summoner
Auto-sync triggered (last 20 matches)
        |
        v
Frontend navigates to Dashboard
```

Default tagLine is `EUNE` (pre-filled in the link form).

---

## 6. Database Design

Single PostgreSQL 16 instance. Organised using Postgres schemas as domain namespaces. All migrations managed by Flyway from `src/main/resources/db/migration/`.

Cascade deletes are configured so that deleting a summoner automatically removes all related data in the correct order.

### Migration History

| Version | Description |
|---|---|
| V1 | Create schemas (account, ranked, match, analytics, coaching) |
| V2 | Create account.summoner table |
| V3 | Remove summoner_id column (Riot no longer returns it) |
| V4 | Create match.summary and match.participant tables |
| V5 | Create public.app_config table with current_split_start seed |
| V6 | Create ranked.entry table |
| V7 | Create analytics.champion_stats and analytics.role_stats tables |
| V8 | Create coaching.insight, insight_section, insight_match tables |
| V9 | Create coaching.opponent_insight table |
| V10 | Add ON DELETE CASCADE to all FK constraints referencing account.summoner |

### Schema: account

```sql
CREATE TABLE account.summoner (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    puuid           TEXT UNIQUE NOT NULL,
    game_name       TEXT NOT NULL,
    tag_line        TEXT NOT NULL,
    profile_icon_id INT,
    summoner_level  INT,
    linked_at       TIMESTAMPTZ DEFAULT NOW(),
    last_synced_at  TIMESTAMPTZ
);
```

Note: `summoner_id` was removed in V3 — Riot's summoner endpoint no longer returns it. PUUID is the primary identifier throughout the system.

### Schema: ranked

```sql
-- Append-only; never updated — full LP history is the point
CREATE TABLE ranked.entry (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    puuid           TEXT NOT NULL REFERENCES account.summoner(puuid) ON DELETE CASCADE,
    queue_type      TEXT NOT NULL,   -- RANKED_SOLO_5x5 | RANKED_FLEX_SR
    tier            TEXT,
    rank            TEXT,
    league_points   INT,
    wins            INT,
    losses          INT,
    recorded_at     TIMESTAMPTZ DEFAULT NOW()
);
```

### Schema: match

```sql
CREATE TABLE match.summary (
    match_id        TEXT PRIMARY KEY,
    game_mode       TEXT,
    game_duration   INT,
    game_start      TIMESTAMPTZ,
    queue_id        INT
);

CREATE TABLE match.participant (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id            TEXT NOT NULL REFERENCES match.summary(match_id) ON DELETE CASCADE,
    puuid               TEXT NOT NULL,
    champion_id         INT,
    champion_name       TEXT,
    kills               INT,
    deaths              INT,
    assists             INT,
    win                 BOOLEAN,
    total_damage        INT,
    gold_earned         INT,
    cs                  INT,
    vision_score        INT,
    role                TEXT,        -- teamPosition from Riot API (UTILITY not SUPPORT)
    lane                TEXT,
    kill_participation  NUMERIC(5,2),
    solo_kills          INT,
    turret_damage       INT
);
```

Note: `role` stores Riot's `teamPosition` field (TOP, JUNGLE, MIDDLE, BOTTOM, UTILITY). The frontend displays UTILITY as SUPPORT using a `formatRole()` helper. `lane` is kept for reference but `role` is used for all analytics.

Note: `raw_json` was deliberately excluded. If future AI features need additional fields, re-fetching from Riot is preferred over storing bloated JSONB.

### Schema: analytics

```sql
CREATE TABLE analytics.champion_stats (
    puuid           TEXT NOT NULL,
    champion_name   TEXT NOT NULL,
    games_played    INT,
    wins            INT,
    losses          INT,
    avg_kda         NUMERIC(5,2),
    avg_cs          NUMERIC(6,2),
    avg_damage      INT,
    avg_vision      NUMERIC(5,2),
    last_computed   TIMESTAMPTZ,
    PRIMARY KEY (puuid, champion_name)
);

CREATE TABLE analytics.role_stats (
    puuid           TEXT NOT NULL,
    role            TEXT NOT NULL,
    games_played    INT,
    wins            INT,
    losses          INT,
    avg_kda         NUMERIC(5,2),
    last_computed   TIMESTAMPTZ,
    PRIMARY KEY (puuid, role)
);
```

Analytics are computed after every sync via a Spring `ApplicationEvent` (`SyncCompletedEvent`). Scoped to ranked queues (queue_id IN (420, 440)) within the current season split.

### Schema: coaching

```sql
CREATE TABLE coaching.insight (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    puuid               TEXT NOT NULL REFERENCES account.summoner(puuid) ON DELETE CASCADE,
    insight_type        TEXT NOT NULL,   -- session | last_game | champion
    subject             TEXT,            -- champion name for type=champion
    match_window        INT,
    generated_at        TIMESTAMPTZ DEFAULT NOW(),
    model_used          TEXT,
    prompt_tokens       INT,
    completion_tokens   INT
);

CREATE TABLE coaching.insight_section (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    insight_id      UUID NOT NULL REFERENCES coaching.insight(id) ON DELETE CASCADE,
    section_type    TEXT NOT NULL,   -- strengths | weaknesses | patterns | advice | summary
    content         TEXT NOT NULL,
    display_order   INT
);

CREATE TABLE coaching.insight_match (
    insight_id      UUID NOT NULL REFERENCES coaching.insight(id) ON DELETE CASCADE,
    match_id        TEXT NOT NULL REFERENCES match.summary(match_id) ON DELETE CASCADE,
    PRIMARY KEY (insight_id, match_id)
);

CREATE TABLE coaching.opponent_insight (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    puuid           TEXT NOT NULL,
    game_id         TEXT NOT NULL,
    champion_name   TEXT,
    generated_at    TIMESTAMPTZ DEFAULT NOW(),
    content         TEXT NOT NULL,
    UNIQUE (puuid, game_id)       -- one analysis per opponent per game
);
```

### Schema: public (config)

```sql
CREATE TABLE public.app_config (
    key         TEXT PRIMARY KEY,
    value       TEXT NOT NULL,
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Seeded with current split start (epoch seconds)
INSERT INTO public.app_config (key, value) VALUES ('current_split_start', '1741168800');
```

The split start date is stored here rather than hardcoded. Update it manually at the start of each new ranked split. Analytics queries use this value to scope stats to the current season.

---

## 7. Data Sync Strategy

Sync is triggered manually via `POST /api/v1/sync?count=N` (default 20, max 100). Auto-sync is triggered once on initial account link.

```
POST /api/v1/sync?count=20
        |
        v
SyncService.sync()
        |
        v
1. Fetch match IDs from Riot (last N, since current_split_start)
2. Diff against match.summary — keep only IDs not already stored
3. For each new match ID:
   - Fetch match detail from Riot
   - Persist match.summary row
   - Persist match.participant rows (all 10 players)
   - Throttle if count > 20 (1200ms delay between calls)
4. Fetch ranked entries -> append to ranked.entry (append-only)
5. Update summoner.last_synced_at
6. Publish SyncCompletedEvent
        |
        v
AnalyticsService (listens for SyncCompletedEvent)
        |
        v
7. Recompute analytics.champion_stats for linked PUUID
8. Recompute analytics.role_stats for linked PUUID
        |
        v
Return SyncResult { found, newMatches, fetched }
```

The sync button on the frontend has a 120-second cooldown after each sync to prevent API abuse.

---

## 8. Module Design

### 8.1 Account Module

Handles summoner linking and unlinking.

Key classes:
- `AccountService` — links account (calls Riot API, persists summoner), unlinks account (cascade delete via FK), exposes `Optional<Summoner> getLinkedAccount()`
- `AccountController` — `POST /account/link`, `GET /me`, `DELETE /account/unlink`
- `SummonerConverter` — `entityToModel()` / `modelToEntity()`

On unlink: `matchParticipantRepository.deleteAll()`, `matchSummaryRepository.deleteAll()`, `championStatsRepository.deleteAll()`, `roleStatsRepository.deleteAll()`, `opponentInsightRepository.deleteAll()` are called manually (these have no FK to summoner). Then `summonerRepository.delete(summoner)` triggers cascade for ranked entries and coaching insights.

### 8.2 Riot Module

Owns all Riot API interaction and the sync pipeline.

Key classes:
- `RiotApiClient` — two `RestClient` instances (regional + platform); all Riot HTTP calls go here; key added per-request
- `RiotProperties` — `@ConfigurationProperties(prefix = "riot")` record for apiKey and region
- `SyncService` — full sync orchestration; publishes `SyncCompletedEvent` on completion
- `MatchIngestionService` — maps `MatchDetailDto` to JPA entities; computes kill participation

### 8.3 Analytics Module

Reads from `match.*` and `ranked.*`, writes to `analytics.*`.

Key classes:
- `AnalyticsService` — `computeForPlayer(puuid)` triggered by `SyncCompletedEvent`; computes champion and role stats; filters by ranked queue IDs and current split start date
- `AnalyticsController` — `GET /me/champions`, `GET /me/roles`, `POST /me/analytics/compute`
- `ChampionStatsConverter`, `RoleStatsConverter` — entity/model conversion

Win rate is computed during conversion (not stored) to always reflect current wins/losses.

### 8.4 Coaching Module

Builds LLM prompts from stored match data, calls Ollama, persists and serves insights.

Key classes:
- `ContextBuilder` — assembles structured match digests: `buildSessionContext()`, `buildLastGameContext()`, `buildChampionContext()`, `buildOpponentContext()`
- `InsightGenerationService` — orchestrates: build context → load prompt template → call LLM via Spring AI → parse sections → persist
- `InsightParser` (inline in `InsightGenerationService`) — splits response on section headers (STRENGTHS, WEAKNESSES, PATTERNS, ADVICE); falls back to storing raw text if parsing fails
- `OpponentAnalysisService` — fetches opponent match history (10 games), builds context, calls LLM, caches result per (puuid, gameId)
- `CoachController` — `POST /coach/analyse`, `GET /coach/insights`, `GET /coach/insights/latest`
- `InsightConverter` — entity/model conversion; strips internal fields (model_used, id) from API response

### 8.5 Live Module

Key classes:
- `LiveGameService` — calls Spectator-V5, splits participants into allies/enemies by teamId, fetches ranked stats per player, resolves display names (riotId → account API fallback)
- `LiveGameController` — `GET /live` (returns 204 if not in game), `POST /live/analyse`
- Anonymous player handling: if PUUID is null (Riot anonymity feature), returns player with null stats rather than crashing

### 8.6 Match Module

Key classes:
- `MatchService` — `getMatchHistory(limit)`: joins match.summary with match.participant for the linked player, sorted by game_start desc
- `MatchController` — `GET /me/matches?limit=N`
- `MatchConverter` — maps to `MatchSummaryModel` including computed `queueLabel` and nested `MatchParticipantModel`

---

## 9. AI Coaching Design

### LLM Configuration

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.1
```

To switch to Claude API, replace with:

```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      chat:
        options:
          model: claude-sonnet-4-20250514
          max-tokens: 2000
```

No code changes required — Spring AI abstracts the provider.

### Insight Types

| Type | Scope | Default window | Prompt template |
|---|---|---|---|
| `session` | Last N games across all champions | 10 | `system-coaching.txt` |
| `last_game` | Most recent single game | 1 | `system-coaching.txt` |
| `champion` | Games on a specific champion | 20 | `system-champion.txt` |

Champion analysis includes macro strategy: champion role in teamfights, win conditions, wave management, split push vs grouping, flank potential, objective priority.

### Opponent Analysis

Triggered by `POST /api/v1/live/analyse` with a list of enemy PUUIDs and champion names. For each enemy:

1. Check `coaching.opponent_insight` cache (keyed by puuid + gameId) — return cached if exists
2. Fetch last 10 match IDs from Riot
3. Fetch match details sequentially (600ms throttle between calls)
4. Build opponent context digest
5. Call LLM with `system-opponent.txt` prompt
6. Persist result
7. 1500ms delay before next player

### Prompt Templates

Located in `src/main/resources/prompts/`:

- `system-coaching.txt` — general coaching; instructs model to produce exactly 4 sections: STRENGTHS, WEAKTHS, PATTERNS, ADVICE
- `system-champion.txt` — champion-specific; adds macro strategy instruction
- `system-opponent.txt` — opponent scouting; 3-4 bullet points, no headers, focused on actionable counter-play

Context passed to the model is a structured text digest — never raw JSON. Keeps prompts within token limits and focuses the model on signal.

---

## 10. REST API Surface

```
-- Account
POST  /api/v1/account/link/{gameName}/{tagLine}   link account
GET   /api/v1/me                                   summoner profile
DELETE /api/v1/account/unlink                      unlink and delete all data

-- Sync
POST  /api/v1/sync?count=20                        trigger data sync

-- Analytics
GET   /api/v1/me/champions                         champion stats
GET   /api/v1/me/roles                             role stats
GET   /api/v1/me/ranked                            ranked history
GET   /api/v1/me/matches?limit=20                  match history
POST  /api/v1/me/analytics/compute                 manual analytics recompute

-- AI Coaching
POST  /api/v1/coach/analyse                        trigger insight generation
GET   /api/v1/coach/insights                       list all insights
GET   /api/v1/coach/insights/latest?type=session   latest insight by type

-- Live Game
GET   /api/v1/live                                 current live game (204 if not in game)
POST  /api/v1/live/analyse                         scout enemies with AI

-- Actuator
GET   /actuator/health                             health check
```

---

## 11. Frontend Structure

```
web/
  src/
    pages/
      Dashboard.tsx      LP snapshot, stat cards, recent matches
      Champions.tsx       Champion stats table with splash art backgrounds
      Roles.tsx           Role stat cards with role icons
      Ranked.tsx          Rank history with tier emblem backgrounds
      Matches.tsx         Match history list (configurable limit)
      Coach.tsx           AI coaching hub: type selector, analyse button, past insights
      Live.tsx            Live game: teams, ranked info, AI scouting reports
      Link.tsx            Account linking landing page (shown when no account linked)
    components/
      Layout.tsx          Sidebar (sticky), nav, sync button, unlink button
      SyncButton.tsx      Sync with 120s cooldown, live countdown, synced X ago
      shared.tsx          PageTitle, Loader
    hooks/
      useMe.ts
      useLinkAccount.ts
      useUnlinkAccount.ts
      useChampions.ts
      useRoles.ts
      useRanked.ts
      useMatches.ts
      useInsights.ts
      useAnalyse.ts
      useLiveGame.ts      polls every 30s, disabled when no account linked
      useAnalyseEnemies.ts
      useDDragonVersion.ts
      useChampionData.ts  full Data Dragon champion list for ID-to-name resolution
    api/
      client.ts           Axios instance, baseURL /api/v1
    utils/
      assets.ts           championIcon, championSplash, tierEmblem, roleIcon,
                          championIconById, formatRole (UTILITY -> SUPPORT)
    types/                (implicit via TypeScript inference)
```

### Design System

- **Background:** `#0a0a0a` with subtle 32px grid overlay
- **Surface:** `#111111`
- **Border:** `#222222` (1px, no border-radius anywhere)
- **Accent:** `#c89b3c` (League gold)
- **Win:** `#4a9b6f` | **Loss:** `#9b4a4a`
- **Text:** `#e8e0d0` | **Muted:** `#666666`
- Zero border-radius enforced via `* { border-radius: 0 !important }`
- Pixel grid background texture on body
- Champion splash arts used as low-opacity card backgrounds
- Tier emblems from Community Dragon as ghost backgrounds
- Role icons from Community Dragon SVGs

---

## 12. Data Assets

All static assets served from external CDNs — no local storage.

| Asset | Source |
|---|---|
| Champion icons | `ddragon.leagueoflegends.com/cdn/{version}/img/champion/{name}.png` |
| Champion splash art | `ddragon.leagueoflegends.com/cdn/img/champion/splash/{name}_0.jpg` |
| Champion data (ID map) | `ddragon.leagueoflegends.com/cdn/{version}/data/en_US/champion.json` |
| DDragon version | `ddragon.leagueoflegends.com/api/versions.json` (index 0) |
| Tier emblems | `raw.communitydragon.org/14.6/plugins/rcp-fe-lol-static-assets/global/default/ranked-emblem/emblem-{tier}.png` |
| Role icons | `raw.communitydragon.org/latest/plugins/rcp-fe-lol-champ-select/global/default/svg/position-{role}.svg` |

Champion name overrides are handled in `assets.ts` for cases where Data Dragon names differ from Riot API names (e.g. Wukong → MonkeyKing, Nunu & Willump → Nunu).

---

## 13. Local Development Setup

### Prerequisites

- Java 21 (via SDKMAN: `sdk install java 21-tem`)
- Maven (via wrapper `./mvnw`)
- Node.js 20+
- Docker + Docker Compose
- Ollama installed and running (`http://localhost:11434`)
- Ollama model pulled: `ollama pull llama3.1`

### Environment Variables

Set in IntelliJ run configuration (not in `.env` file — Spring Boot does not auto-read `.env`):

```
RIOT_API_KEY=RGAPI-xxxx-xxxx-xxxx
```

Riot dev keys expire every 24 hours. Regenerate at developer.riotgames.com and update the run config.

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lolcompanion
    username: lol
    password: localdev
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
    schemas: account, ranked, match, analytics, coaching
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.1

management:
  endpoints:
    web:
      exposure:
        include: health, info

riot:
  api-key: ${RIOT_API_KEY}
  region: eun1
```

### Docker Compose

```yaml
services:
  postgres:
    image: postgres:16
    container_name: lol-companion-db
    environment:
      POSTGRES_DB: lolcompanion
      POSTGRES_USER: lol
      POSTGRES_PASSWORD: localdev
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

Run Postgres only via Docker. Run Spring Boot from IntelliJ (green run button). Run React via `npm run dev` in the `web/` directory.

### Vite Proxy

`web/vite.config.ts` proxies API calls to the backend:

```ts
server: {
  proxy: {
    '/api': 'http://localhost:8080',
    '/auth': 'http://localhost:8080'
  }
}
```

---

## 14. Project Structure

```
bg2ez/
  src/
    main/
      java/com/lolcompanion/bg2ez/
        account/
          controller/AccountController.java
          converter/SummonerConverter.java
          entity/Summoner.java
          model/SummonerModel.java
          repository/SummonerRepository.java
          service/AccountService.java
        analytics/
          controller/AnalyticsController.java
          converter/ChampionStatsConverter.java
          converter/RoleStatsConverter.java
          entity/ChampionStats.java
          entity/ChampionStatsId.java
          entity/RoleStats.java
          entity/RoleStatsId.java
          model/ChampionStatsModel.java
          model/RoleStatsModel.java
          repository/ChampionStatsRepository.java
          repository/RoleStatsRepository.java
          service/AnalyticsService.java
        coaching/
          controller/CoachController.java
          converter/InsightConverter.java
          entity/Insight.java
          entity/InsightSection.java
          entity/OpponentInsight.java
          model/AnalyseRequest.java
          model/InsightModel.java
          model/InsightSectionModel.java
          model/OpponentInsightModel.java
          repository/InsightRepository.java
          repository/OpponentInsightRepository.java
          service/ContextBuilder.java
          service/InsightGenerationService.java
          service/OpponentAnalysisService.java
        config/
          entity/AppConfig.java
          repository/AppConfigRepository.java
          SecurityConfig.java
        live/
          controller/LiveGameController.java
          model/LiveAnalyseRequest.java
          model/LiveGameModel.java
          model/LivePlayerModel.java
          service/LiveGameService.java
        match/
          controller/MatchController.java
          converter/MatchConverter.java
          entity/MatchParticipant.java
          entity/MatchSummary.java
          model/MatchParticipantModel.java
          model/MatchSummaryModel.java
          repository/MatchParticipantRepository.java
          repository/MatchSummaryRepository.java
          service/MatchService.java
        ranked/
          converter/RankedEntryConverter.java
          entity/RankedEntry.java
          model/RankedEntryModel.java
          repository/RankedEntryRepository.java
        riot/
          client/RiotApiClient.java
          config/RiotProperties.java
          controller/SyncController.java
          model/AccountDto.java
          model/LiveGameDto.java
          model/MatchDetailDto.java
          model/RankedEntryDto.java
          model/SummonerDto.java
          service/SyncService.java
        Bg2ezApplication.java
      resources/
        application.yml
        db/migration/
          V1__create_schemas.sql
          V2__create_summoner_table.sql
          V3__remove_summoner_id.sql
          V4__create_match_tables.sql
          V5__create_config_table.sql
          V6__create_ranked_table.sql
          V7__create_analytics_tables.sql
          V8__create_coaching_tables.sql
          V9__create_opponent_insight_table.sql
          V10__add_cascade_deletes.sql
        prompts/
          system-coaching.txt
          system-champion.txt
          system-opponent.txt
    test/
      java/com/lolcompanion/bg2ez/
        Bg2ezApplicationTests.java
  web/
    src/
      api/client.ts
      components/Layout.tsx
      components/SyncButton.tsx
      components/shared.tsx
      hooks/...
      pages/...
      utils/assets.ts
    index.html
    vite.config.ts
    package.json
  docker-compose.yml
  pom.xml
  SDS.md
```

---

## 15. Known Risks and Limitations

| Risk | Status | Mitigation |
|---|---|---|
| Riot Spectator-V5 deprecation | Active risk — Riot announced intent to deprecate for anonymity | Built knowing the risk; manual scouting (Option B) is fallback |
| Riot dev key expires every 24h | Ongoing | Documented; update in IntelliJ run config |
| Riot anonymity feature | Partially affects live game | PUUID and summonerName may be null; handled with anonymous player fallback |
| Ollama response quality | Lower than Claude for nuanced analysis | Acceptable for personal use; Claude API is a one-line config swap |
| Ollama speed on CPU | 30-90 seconds per analysis | Acceptable for manually-triggered analysis; user informed in UI |
| Analytics scoped to current split only | By design | `current_split_start` in app_config must be updated manually each split |
| Single account only | By design | DB is PUUID-keyed; multi-account would require auth layer |
| No HTTPS | Local only | Not needed until public deployment |

---

## 16. Out of Scope (Future Considerations)

- **Public deployment** — requires HTTPS, Vault/secret management, Riot production key approval, multi-tenancy
- **Multi-account support** — DB schema supports it; auth layer needed
- **LCU (League Client API)** — local WebSocket from the game client; could replace Spectator-V5 for live game detection; potential Go microservice
- **Fine-tuned model** — once enough personal match data accumulates, a LoL-specific fine-tuned model becomes interesting
- **Build suggestions** — requires community data sources (OP.GG, U.GG); Riot API does not expose recommended builds
- **Match timeline data** — Riot provides per-minute timeline for each match; would enable CS-at-10, gold-at-15, early game analysis
- **Mobile app** — out of scope; the web app is mobile-friendly enough for personal use
- **Go microservices** — only if a specific bottleneck justifies the added complexity

---

*Update version and last-updated on each significant revision.*
