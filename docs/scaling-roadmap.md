# {{PROJECT_NAME}} — Scaling Roadmap

> A phased plan from MVP toward very high concurrency. Each phase has a **trigger**
> (a metric you actually observe) before you advance — you do **not** move to the
> next phase on a calendar date or a hunch. Every phase is **additive** because the
> constitution (statelessness, clean architecture, bounded contexts) was honored
> from day one.
>
> **On "1M CCU":** concurrent users are not registered users. 1M *concurrent* is
> large-scale territory and usually implies tens of millions of registered users.
> Most products never need Phase 4–5. The orientation numbers below are rough — let
> the **trigger metrics** drive decisions, not the user counts.

## How to read the triggers

Watch four signals continuously from Phase 1 onward:

- **p95/p99 latency** on key endpoints
- **App CPU / memory** headroom at peak
- **DB:** connection saturation, replication lag, slow-query rate, write contention
- **Error rate / saturation** under peak traffic

A phase's trigger is "the cheaper levers in the current phase are exhausted and a
signal is still red at peak."

---

## Phase 0 — MVP / Foundation
**Goal:** Ship the product correctly on an architecture that can grow.
**Orientation:** launch → ~1k–10k registered users, low concurrency.

**Architecture:** single stateless Spring Boot modular monolith · one PostgreSQL
(with backups) · Keycloak · Angular (BFF) + Flutter clients · one environment per
stage (dev/stage/prod).

**Build:**
- Clean architecture + bounded contexts from the first commit.
- OIDC auth, BFF for web, PKCE for mobile.
- Migrations (Flyway/Liquibase), CI with tests + ArchUnit + vuln scan.
- Health checks, structured logs, basic metrics, tracing scaffolding.
- Containerized; config from environment; one-command deploy.

**Explicitly DO NOT build yet:** microservices, Kafka, read replicas, sharding,
multi-region, caching layers, autoscaling clusters, service mesh.

**Exit trigger → Phase 1:** real users in production, OR any single instance shows
< ~40% headroom at peak, OR you need zero-downtime deploys / horizontal capacity.

---

## Phase 1 — Production hardening & elastic compute
**Goal:** Run reliably and absorb growth by adding identical instances.
**Orientation:** ~10k–100k users.

**Architecture:** the same monolith, now **N stateless instances** behind a load
balancer, with autoscaling and rolling/blue-green deploys.

**Build:**
- Container orchestration (Kubernetes or managed equivalent) + horizontal autoscaling.
- Load balancer / API gateway with **rate limiting + WAF**.
- Centralized logging, dashboards, and **alerting** on the four signals.
- DB connection pooling (PgBouncer) sized to instance count.
- Backup/restore tested; basic runbooks.

**DO NOT yet:** split services, add a broker, or shard.

**Exit trigger → Phase 2:** read traffic dominates and DB CPU or query latency is
the bottleneck at peak (adding app instances no longer helps), OR repeated reads of
the same data are expensive.

---

## Phase 2 — Read scaling & caching
**Goal:** Take read load off the primary database.
**Orientation:** ~100k–500k users.

**Architecture:** add **Redis** (read-through + computed-result caching, plus shared
session/state) · **CDN** for static and cacheable responses · **PostgreSQL read
replicas** with reads routed to replicas, writes to primary.

**Build:**
- Caching strategy per bounded context with explicit invalidation rules.
- Read/write data-source routing (your code already avoided read-after-write
  assumptions, so this is config + routing, not a rewrite).
- CDN in front of the gateway; cache headers on responses.
- Capacity test to validate the new ceiling.

**DO NOT yet:** extract services or shard the primary.

**Exit trigger → Phase 3:** synchronous work in the request path (notifications,
exports, fan-out, third-party calls) inflates latency or couples failures, OR you
need to smooth large traffic spikes.

---

## Phase 3 — Asynchronous decoupling
**Goal:** Get non-critical work out of the request path; smooth spikes.
**Orientation:** ~500k+ users / rising concurrency.

**Architecture:** introduce **Kafka**. Publish domain events; move notifications,
analytics, projections, and heavy side-effects to consumers. Request path returns
fast; the rest happens asynchronously.

**Build:**
- Event schema + ownership per bounded context (events are a contract — version them).
- Idempotent consumers; outbox pattern for reliable publish.
- Backpressure and dead-letter handling; consumer autoscaling.

**DO NOT yet:** split the deployable unless a specific context's load profile
genuinely diverges (that's Phase 4).

**Exit trigger → Phase 4:** one or a few bounded contexts have a **load or scaling
profile distinct** from the rest (e.g. one is write-heavy, latency-critical, or
needs independent deploys/teams) and co-location now constrains them.

---

## Phase 4 — Selective service extraction
**Goal:** Independently scale only the contexts that need it.
**Orientation:** high concurrency; multiple teams.

**Architecture:** extract the **hottest bounded contexts** into their own
deployables. The rest stays in the monolith. Because each context was isolated
behind ports with no shared tables, extraction is surgical: lift the context,
replace in-process calls with API/events, give it its own datastore.

**Build:**
- Extract one context at a time; measure before/after.
- Service-to-service via **mTLS**; contracts versioned.
- Per-service observability and SLOs; service catalog/ownership.
- Consider a managed gateway/mesh **only now**, if cross-service concerns demand it.

**Guardrail:** Do **not** "go microservices" wholesale. Extract by evidence, leave
everything else in the monolith. A 40-service mesh you didn't need is its own outage.

**Exit trigger → Phase 5:** a single context's **dataset or write throughput**
exceeds what one primary + replicas can serve, OR you need geographic locality /
regional failover.

---

## Phase 5 — Data partitioning, sharding & multi-region
**Goal:** Scale the data tier and locality toward the top end (~1M CCU).
**Orientation:** very large scale.

**Architecture:** PostgreSQL **native partitioning** first; then **application-level
sharding** or a distributed layer (e.g. Citus) for the contexts that need it.
Optionally **multi-region** active-passive → active-active with data-locality and
replication strategy. CDN + edge caching maximized.

**Build:**
- Shard key chosen per context from real access patterns; migration executed
  online, one context at a time.
- Cross-shard query/aggregation strategy defined and bounded.
- Regional routing, replication topology, failover runbooks, chaos/DR testing.
- Cost and capacity modeling — this phase is expensive; justify per context.

**Guardrail:** Sharding is the **last** lever, not the first. It permanently raises
operational complexity. Reach it only when replicas + caching + async + extraction
are genuinely exhausted for that data.

---

## Summary

| Phase | Orientation | Key addition | Advance when… |
|---|---|---|---|
| 0 MVP | ≤10k | Stateless monolith + 1 DB + Keycloak | Real users / instance headroom low |
| 1 Hardening | 10k–100k | N instances, LB, autoscale, observability | DB becomes the bottleneck |
| 2 Read scale | 100k–500k | Redis + CDN + read replicas | Sync work hurts latency |
| 3 Async | 500k+ | Kafka, events, async consumers | A context's profile diverges |
| 4 Extract | high CCU | Split only hot contexts (mTLS) | One context outgrows one DB |
| 5 Shard/multi-region | ~1M CCU | Partition/shard, multi-region | All cheaper levers exhausted |

**The throughline:** the work in Phases 1–5 is *adding capacity*, never *rewriting
architecture* — provided every feature in every phase keeps obeying the constitution.
