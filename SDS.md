# Software Design Specification
## LoL Companion — Personal League of Legends Companion App

**Version:** 0.4.0  
**Status:** Draft — Private / Local Development  
**Author:** [Your Name]  
**Last Updated:** 2026-06-01

---

## 1. Overview

LoL Companion is a personal, self-hosted web application that serves as a stat tracker, match analytics dashboard, and AI-powered coaching tool for League of Legends. It links to a single Riot Games account via OAuth, pulls data from the Riot Games API, stores a rich match history locally, and uses an LLM to analyse patterns in your play — surfacing strengths, weaknesses, and concrete improvement advice over time.

The system is built primarily in **Java (Spring Boot)**. This is intentional: the goal is to develop deep, production-grade Java skills across the full backend stack — HTTP, data access, scheduling, and AI integration — in a single coherent codebase. Go is noted as a possible future option for specific microservices (e.g. a low-latency Riot rate-limit proxy or a live game overlay WebSocket layer) if a concrete need arises, but is not part of the initial build.

The system runs entirely on localhost and is not intended for public deployment at this stage.

---

## 2. Goals & Non-Goals

### Goals
- Authenticate with a Riot Games account via the official Riot OAuth flow
- Fetch and persistently store match history, ranked stats, and champion performance data
- Display a rich analytics dashboard in the browser
- Provide AI-powered coaching: strength/weakness analysis, pattern detection, and personalised improvement advice derived from match history
- Store AI-generated insights so they accumulate and improve over time
- Build real, production-grade Java skills: Spring Boot, JPA, dependency injection, scheduled jobs, Spring AI
- Keep the system modular so services can be extracted later if needed

### Non-Goals (for now)
- Multi-user support
- Live in-game overlay (deferred — requires LCU API)
- Public deployment
- Mobile app
- Monetisation
- Go services (possible future addition, not planned for v1)

---

## 3. Architecture Overview

A single Java monolith structured as a **modular monolith** — one deployable unit, internally organised into well-separated packages that each own their domain. This avoids the operational overhead of microservices while preserving the option to extract services later.

```
+------------------------------------------------------------------+
|                        Browser (Web App)                         |
|                    React -- localhost:3000                        |
+----------------------------+-------------------------------------+
                             | HTTP / REST
             +---------------+---------------+
             |     Spring Boot Application   |
             |        localhost:8080         |
             |                               |
             |  +----------+  +----------+  |
             |  |   Auth   |  | Riot     |  |
             |  |  Module  |  | Module   |  |
             |  +----------+  +----------+  |
             |  +----------+  +----------+  |
             |  |Analytics |  | Coach    |  |
             |  |  Module  |  | Module   |  |
             |  +----------+  +----------+  |
             +---------------+---------------+
                             |
               +-------------+-------------+
               |         PostgreSQL        |
               |       localhost:5432      |
               +-------------+-------------+
                             |
                   +---------+---------+
                   |   LLM Provider    |
                   | Claude / Ollama   |
                   +-------------------+
```

### Internal Modules

| Module | Responsibility |
|---|---|
| **auth** | Riot OAuth 2.0 + PKCE, JWT session management, account linking |
| **riot** | Riot API client, rate limiting, data sync, raw match ingestion |
| **analytics** | Champion stats, role stats, rank history, trend computation |
| **coach** | LLM context building, prompt management, insight generation and storage |
| **api** | REST controllers, request/response DTOs, shared middleware |

Each module has its own package tree, its own JPA entities, and its own service layer. Modules communicate through service interfaces — never by reaching into another module's repository directly.

---

## 4. Technology Stack

### Backend

| Concern | Choice | Rationale |
|---|---|---|
| Language | Java 21 (LTS) | Virtual threads (Project Loom), modern records and pattern matching |
| Framework | Spring Boot 3.x | Industry standard; excellent ecosystem for data, scheduling, security, AI |
| HTTP / REST | Spring MVC | Built-in; clean controller/service separation |
| Data access | Spring Data JPA + Hibernate | Entity mapping + standard queries; custom JPQL/SQL for analytics |
| DB migrations | Flyway | SQL-first; runs automatically on startup |
| AI integration | Spring AI | Abstracts over Claude, OpenAI, Ollama — provider swappable via config |
| Riot HTTP client | Spring RestClient (Spring 6.1+) | Synchronous, clean API; switch to WebClient if async sync needed |
| Auth | Spring Security + OAuth2 client | Handles Riot RSO PKCE flow natively |
| Scheduling | Spring @Scheduled | Periodic rank snapshots, stale analytics refresh |
| Build tool | Maven | Ubiquitous in enterprise Java; excellent Spring documentation alignment |
| Testing | JUnit 5, Mockito, Spring Boot Test, Testcontainers | Unit + integration; Testcontainers for real Postgres in tests |

### Frontend

| Concern | Choice |
|---|---|
| Framework | React 18 + TypeScript |
| Build tool | Vite |
| Charting | Recharts |
| Styling | Tailwind CSS |
| Data fetching | TanStack Query (React Query) |
| Routing | React Router v6 |

### Infrastructure (Local)

| Component | Tool |
|---|---|
| Containerisation | Docker Compose |
| Database | PostgreSQL 16 |
| Secret management | .env files (gitignored) |
| Optional local LLM | Ollama (runs on host machine) |

### Future / Optional

| Concern | Candidate | Trigger |
|---|---|---|
| Rate-limit proxy | Go microservice | If Riot API rate limiting becomes painful to manage in-JVM |
| Live game overlay | Go + WebSocket microservice | If LCU API integration is pursued |

---

## 5. Riot Games API Integration

### API Key & Registration

1. Register at developer.riotgames.com
2. Use a Personal API Key (Development key -- rotates every 24h, sufficient for personal use)
3. Store as RIOT_API_KEY in .env -- never commit to git
4. Register http://localhost:8080/auth/callback as the OAuth redirect URI

### Endpoints Used

| Feature | Riot API Endpoint |
|---|---|
| Account linking | GET /riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine} |
| Summoner info | GET /lol/summoner/v4/summoners/by-puuid/{encryptedPUUID} |
| Ranked stats | GET /lol/league/v4/entries/by-summoner/{encryptedSummonerId} |
| Match list | GET /lol/match/v5/matches/by-puuid/{puuid}/ids |
| Match detail | GET /lol/match/v5/matches/{matchId} |
| Champion mastery | GET /lol/champion-mastery/v4/champion-masteries/by-puuid/{encryptedPUUID} |

### Rate Limit Handling

Dev keys: 20 req/1s and 100 req/2min. The Riot module manages this with:
- A token-bucket counter tracking requests against both windows
- Automatic backoff when approaching limits
- Respect for Retry-After headers on 429 responses
- Match detail fetches batched and throttled (never parallel)

### OAuth / Account Linking Flow

```
User clicks "Link Account"
        |
        v
Spring Security redirects to Riot RSO authorize URL (PKCE)
        |
        v
User authenticates on Riot's site
        |
        v
Riot redirects to localhost:8080/auth/callback?code=...
        |
        v
Spring Security exchanges code for access_token + id_token
        |
        v
Auth module extracts PUUID from id_token claims
        |
        v
Summoner stored in DB -> JWT session cookie issued
```

---

## 6. Database Design

Single PostgreSQL 16 instance. Organised using Postgres schemas as domain namespaces. Flyway manages all migrations from src/main/resources/db/migration/.

The raw_json JSONB column on match.summary stores the full Riot API response. This means no data fidelity is lost -- when new AI features need fields that were not initially parsed into typed columns, they are already available for reprocessing without re-fetching from Riot.

### Schema: account

```sql
CREATE TABLE account.summoner (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    puuid           TEXT UNIQUE NOT NULL,
    summoner_id     TEXT NOT NULL,
    game_name       TEXT NOT NULL,
    tag_line        TEXT NOT NULL,
    profile_icon_id INT,
    summoner_level  INT,
    linked_at       TIMESTAMPTZ DEFAULT NOW(),
    last_synced_at  TIMESTAMPTZ
);
```

### Schema: ranked

```sql
-- Append-only; never update rows -- full LP history is the point
CREATE TABLE ranked.entry (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    puuid           TEXT NOT NULL REFERENCES account.summoner(puuid),
    queue_type      TEXT NOT NULL,
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
    queue_id        INT,
    raw_json        JSONB
);

CREATE TABLE match.participant (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id            TEXT REFERENCES match.summary(match_id),
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
    role                TEXT,
    lane                TEXT,
    kill_participation  NUMERIC(5,2),
    solo_kills          INT,
    turret_damage       INT
);
```

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
    win_rate        NUMERIC(5,2),
    avg_kda         NUMERIC(5,2),
    last_computed   TIMESTAMPTZ,
    PRIMARY KEY (puuid, role)
);
```

### Schema: coaching

```sql
CREATE TABLE coaching.insight (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    puuid               TEXT NOT NULL REFERENCES account.summoner(puuid),
    insight_type        TEXT NOT NULL,   -- overall | champion | role | session
    subject             TEXT,            -- champion name or role; null for overall
    match_window        INT,
    generated_at        TIMESTAMPTZ DEFAULT NOW(),
    model_used          TEXT,
    prompt_tokens       INT,
    completion_tokens   INT
);

CREATE TABLE coaching.insight_section (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    insight_id      UUID NOT NULL REFERENCES coaching.insight(id),
    section_type    TEXT NOT NULL,   -- strengths | weaknesses | patterns | advice | summary
    content         TEXT NOT NULL,
    display_order   INT
);

CREATE TABLE coaching.insight_match (
    insight_id      UUID NOT NULL REFERENCES coaching.insight(id),
    match_id        TEXT NOT NULL REFERENCES match.summary(match_id),
    PRIMARY KEY (insight_id, match_id)
);
```

---

## 7. Module Design

### 7.1 Auth Module (com.lolcompanion.auth)

Spring Security handles the Riot OAuth2 PKCE flow natively with minimal custom code. On successful login, PUUID is extracted from the id_token and the summoner record is created or updated. A JWT is issued in an HTTP-only cookie.

Key classes: AuthController, RiotOAuthSuccessHandler, JwtService, SessionFilter

### 7.2 Riot Module (com.lolcompanion.riot)

Owns all Riot API interaction and the data sync pipeline.

Key classes:
- RiotApiClient -- RestClient wrapper; all Riot HTTP calls go through here
- RateLimitGuard -- tracks request counts against both rate limit windows; blocks or delays when approaching limits
- SyncService -- orchestrates sync: fetch match IDs -> diff against DB -> fetch new details -> write to DB -> publish SyncCompletedEvent
- MatchIngestionService -- maps Riot API response POJOs to JPA entities

### 7.3 Analytics Module (com.lolcompanion.analytics)

Reads from match.* and ranked.*, writes to analytics.*. Computations are SQL/JPQL queries, not application-level aggregation loops. Listens for SyncCompletedEvent to trigger refresh.

Key classes: ChampionStatsService, RoleStatsService, RankHistoryService, AnalyticsController

### 7.4 Coach Module (com.lolcompanion.coach)

Builds LLM prompts from stored match data, calls the configured provider via Spring AI, and persists structured insight results.

Key classes:
- ContextBuilder -- assembles a structured match digest from DB (never passes raw JSON to the model)
- PromptTemplateService -- loads system prompt templates from resources/prompts/
- InsightGenerationService -- orchestrates: build context -> render prompt -> call LLM -> parse -> persist
- InsightParser -- splits LLM response into named sections; falls back to raw storage if parsing fails
- CoachController -- exposes /api/v1/coach/*

Spring AI provider selection via application.properties -- no code changes needed to switch between Claude and Ollama.

### 7.5 API Module (com.lolcompanion.api)

Global exception handler (@ControllerAdvice), request logging filter, CORS config, shared DTO records.

---

## 8. AI Coaching Design

### Context Digest (passed to LLM -- never raw JSON)

```
== Player Profile ==
Game: {gameName}#{tagLine}
Current rank: {tier} {rank} {LP} LP
Season record: {wins}W {losses}L ({winRate}%)

== Recent Form (last {window} games) ==
Win rate: {X}%
Most played: {champion} ({games}g, {wr}% WR), ...
Best KDA: {champion} ({kda})
Struggling on: {champion} ({kda}, {wr}% WR)
Strongest role: {role} ({wr}% WR)

== Notable Patterns ==
- Kill participation avg: {X}%
- Vision score avg: {X}
- Most common losing condition: {derived}

== Previous coaching summary ==
{Condensed text from most recent insight -- omitted on first run}
```

### System Prompt Template (stored in resources/prompts/)

You are a personal League of Legends coach with deep knowledge of the game at all skill levels.
Analyse the player data honestly and constructively. Be specific -- always reference champion names,
concrete stats, and observable patterns. Never give generic advice. Structure your response into
exactly four sections labelled: Strengths, Weaknesses, Patterns, Actionable Advice.
Each section should be 2-4 concise bullet points.

### Insight Types

| Type | Scope | Recommended window |
|---|---|---|
| overall | All champions and roles | Last 50 matches |
| champion | One specific champion | Last 20 games on that champion |
| role | One role (e.g. ADC) | Last 30 games in that role |
| session | Recent session summary | Last 5-10 games |

### Staleness and Regeneration

Analysis is always user-triggered -- never automatic. The frontend shows when the last analysis ran and how many new matches have been played since, prompting a refresh when meaningful new data exists.

---

## 9. REST API Surface

```
-- Auth
GET  /auth/login                   redirect to Riot OAuth
GET  /auth/callback                OAuth code exchange + session
POST /auth/logout                  clear session cookie

-- Account
GET  /api/v1/me                    summoner profile + sync status

-- Sync
POST /api/v1/sync                  trigger manual sync

-- Analytics
GET  /api/v1/me/ranked             LP history + current rank
GET  /api/v1/me/champions          champion stats table
GET  /api/v1/me/roles              role stats breakdown
GET  /api/v1/me/matches            paginated match list
GET  /api/v1/me/matches/{id}       single match detail

-- AI Coaching
GET  /api/v1/coach/insights        list all stored insights
GET  /api/v1/coach/insights/{id}   full insight with sections
GET  /api/v1/coach/latest          most recent insight per type
POST /api/v1/coach/analyse         trigger new analysis
                                   body: { type, subject?, window? }
```

---

## 10. Project Structure

```
lol-companion/
  src/
    main/
      java/com/lolcompanion/
        auth/
          AuthController.java
          JwtService.java
          RiotOAuthSuccessHandler.java
          SessionFilter.java
        riot/
          RiotApiClient.java
          RateLimitGuard.java
          SyncService.java
          MatchIngestionService.java
          model/                  Riot API response POJOs
        analytics/
          ChampionStatsService.java
          RoleStatsService.java
          RankHistoryService.java
          AnalyticsController.java
          entity/
        coach/
          ContextBuilder.java
          PromptTemplateService.java
          InsightGenerationService.java
          InsightParser.java
          CoachController.java
          entity/
        api/
          GlobalExceptionHandler.java
          CorsConfig.java
          dto/
        match/
          entity/
        account/
          entity/
      resources/
        application.properties
        application-local.properties
        prompts/
          system-overall.txt
          system-champion.txt
          system-role.txt
          system-session.txt
        db/migration/             Flyway SQL files (V1__, V2__, ...)
    test/
      java/com/lolcompanion/
        riot/RiotApiClientTest.java
        analytics/ChampionStatsServiceTest.java
        coach/ContextBuilderTest.java
        coach/InsightParserTest.java
        integration/SyncIntegrationTest.java   Testcontainers
  web/
    src/
      pages/
        Dashboard.tsx
        Champions.tsx
        Matches.tsx
        MatchDetail.tsx
        Profile.tsx
        Coach.tsx
        InsightDetail.tsx
      components/
        MatchCard.tsx
        ChampionRow.tsx
        RankBadge.tsx
        StatChart.tsx
        InsightCard.tsx
        AnalyseButton.tsx
        InsightSection.tsx
      hooks/
        useMe.ts
        useMatches.ts
        useChampions.ts
        useInsights.ts
      api/client.ts
      types/
        riot.ts
        coaching.ts
  docker-compose.yml
  .env.example
  pom.xml
  SDS.md
```

---

## 11. Local Development Setup

### Prerequisites
- Java 21 (via SDKMAN: sdk install java 21-tem)
- Maven wrapper included (mvnw); generated by start.spring.io
- Node.js 20+
- Docker + Docker Compose
- Ollama (optional, for local LLM)

### Environment Variables (.env)

```
RIOT_API_KEY=RGAPI-xxxx-xxxx-xxxx
RIOT_REGION=euw1
RIOT_OAUTH_CLIENT_ID=...
RIOT_OAUTH_CLIENT_SECRET=...

POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=lolcompanion
POSTGRES_USER=lol
POSTGRES_PASSWORD=localdev

JWT_SECRET=some-long-random-secret-at-least-32-chars

AI_PROVIDER=claude
ANTHROPIC_API_KEY=sk-ant-...
AI_MODEL=claude-sonnet-4-20250514
```

### Docker Compose

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: lolcompanion
      POSTGRES_USER: lol
      POSTGRES_PASSWORD: localdev
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    depends_on: [postgres]
    env_file: .env

  web:
    build: ./web
    ports:
      - "3000:3000"

volumes:
  pgdata:
```

In early development: run Spring Boot with mvn spring-boot:run and only Docker-compose Postgres. Faster iteration, no image rebuild cycle.

---

## 12. Development Phases

### Phase 1 -- Foundation
- [ ] Maven project setup with Spring Boot 3.x dependencies
- [ ] Docker Compose with Postgres only
- [ ] Flyway migrations for account, ranked, match schemas
- [ ] Health check endpoint (GET /actuator/health)
- [ ] application-local.properties for dev overrides

### Phase 2 -- Account Linking
- [ ] Spring Security + Riot OAuth2 PKCE configuration
- [ ] RiotOAuthSuccessHandler: extract PUUID, persist summoner
- [ ] JWT generation and SessionFilter middleware
- [ ] GET /api/v1/me returns summoner profile

### Phase 3 -- Data Sync
- [ ] RiotApiClient with RestClient + RateLimitGuard
- [ ] Match list fetch -> diff -> match detail fetch pipeline
- [ ] Ranked snapshot on each sync
- [ ] POST /api/v1/sync endpoint
- [ ] SyncCompletedEvent published on success

### Phase 4 -- Analytics
- [ ] analytics schema Flyway migration
- [ ] ChampionStatsService + RoleStatsService (SyncCompletedEvent listener)
- [ ] RankHistoryService
- [ ] Analytics REST endpoints

### Phase 5 -- Frontend (Core)
- [ ] React + Vite + Tailwind setup
- [ ] Dashboard, Champions, Matches, MatchDetail, Profile pages
- [ ] LP history chart, champion stats table, role breakdown

### Phase 6 -- AI Coaching
- [ ] Spring AI dependency + provider config
- [ ] coaching schema Flyway migration
- [ ] ContextBuilder: match digest assembly
- [ ] Prompt templates in resources/prompts/
- [ ] InsightGenerationService + InsightParser
- [ ] Coach REST endpoints
- [ ] Frontend: Coach page, InsightDetail, AnalyseButton

### Phase 7 -- Polish
- [ ] Structured logging (Logback + JSON)
- [ ] Global exception handler with consistent error shape
- [ ] Testcontainers integration tests for sync and analytics
- [ ] README with full setup instructions
- [ ] Caching review

---

## 13. Key Risks & Mitigations

| Risk | Mitigation |
|---|---|
| Riot dev key expires every 24h | Documented clearly; .env update + app restart is the reload path |
| Riot rate limits | RateLimitGuard tracks both windows; match fetches are sequential and throttled |
| OAuth PKCE complexity | Spring Security handles PKCE natively -- minimal custom code |
| Flyway migration conflicts | Never edit existing migration files; always add new versioned files |
| LLM prompt token limits | ContextBuilder caps match window and summarises prior insights; no raw JSON in prompts |
| LLM response parsing failures | InsightParser falls back to storing raw text if section parsing fails |
| Spring Boot startup time | Use mvn spring-boot:run for dev; Docker only for Postgres in early phases |

---

## 14. Out of Scope (Future Considerations)

- Go microservices -- possible for a Riot rate-limit proxy or live overlay WebSocket layer if a specific bottleneck justifies it
- Live in-game overlay -- requires LCU API (local WebSocket from the game client); highest effort, deferred
- Build suggestions -- needs community data sources; Riot API does not expose recommended builds
- Multi-account support -- DB is PUUID-keyed and structurally ready; auth would need extending
- Fine-tuned model -- interesting once enough personal match data accumulates
- Public deployment -- requires HTTPS, secret management, Riot production key approval

---

*This document will evolve as the project progresses. Update version and last-updated fields on each significant revision.*
