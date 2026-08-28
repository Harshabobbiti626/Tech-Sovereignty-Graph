---
title: Tech Sovereignty Graph
emoji: 🕸️
colorFrom: red
colorTo: indigo
sdk: docker
app_port: 7860
pinned: false
---

**Shadow IT & Governance Tracker** — the whole app in one container: nginx serves the React UI on `$PORT` and proxies `/api` to the Spring Boot JVM on the same origin (no CORS). Graph data lives on CognoDB; the circuit breaker + outage screen handle a sleeping free-tier DB gracefully.

Source: [Harshabobbiti626/Tech-Sovereignty-Graph](https://github.com/Harshabobbiti626/Tech-Sovereignty-Graph)
