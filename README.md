# 🔍 RuntimeLens

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3+-brightgreen.svg)](https://spring.io/projects/spring-boot)

**Zero-Code Observability & Diagnostics for Spring Boot.**

RuntimeLens is an "invisible" diagnostic engine that lives inside your Spring Boot application. It observes your app's behavior in real-time and provides instant feedback when things go wrong—without you writing a single line of instrumentation code.

---

## ✨ Features

*   **🚀 Zero-Code Activation**: Just add the dependency. No `@EnableRuntimeLens`, no configurations, no boilerplate.
*   **🌐 HTTP Insight**: Monitors request latency, status codes, and identifies slow endpoints (>1000ms).
*   **🗄️ N+1 Query Detector**: Automatically detects when a single request triggers excessive database queries.
*   **🔌 External API Monitoring**: Intercepts `RestTemplate` calls to track performance and failures of third-party services.
*   **⚠️ Exception Catchment**: Captures unhandled exceptions and logs them with full context and stack traces.
*   **🧠 JVM Health**: Monitors Heap memory and CPU load, alerting you to resource pressure before it's too late.
*   **📝 Human-Readable Reports**: Beautifully formatted diagnostic logs in your console and `runtimelens.log`.

---

## 🛠️ Getting Started

### 1. Add the Dependency

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.rudramadhab22</groupId>
    <artifactId>runtimelens-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Run Your App

That's it. RuntimeLens is now active.

---

## 📊 Real-Time Diagnostics

When RuntimeLens detects an issue, it generates a report like this in your `runtimelens.log`:

```text
--- RuntimeLens Diagnostic [a83c5c00-9165-4f43-8250-5fe1dc9534c5] ---
Summary: GET /slow -> 200 (1207ms)
  [!] SLOW_REQUEST: Request took longer than 1000ms
---------------------------------------------------
Timestamp: 2026-07-22T23:29:47.892225900
Request ID: 8bba1131-088c-4a16-b70e-15000595942e
Summary: GET /nplus1 -> 200 (720ms)
  [!] POTENTIAL_N_PLUS_ONE: Request triggered 10 database queries
---------------------------------------------------
Timestamp: 2026-07-22T23:29:51.291691
Request ID: f66ce9ba-6fac-4712-bed4-03d5b20de990
Summary: GET /proxy -> 200 (3394ms)
  [!] SLOW_REQUEST: Request took longer than 1000ms
  [!] SLOW_EXTERNAL_API: External call to https://api.example.com/data took 3316ms
---------------------------------------------------
```

---

## 🏗️ Architecture

RuntimeLens uses **Dynamic Proxies** and **Spring BeanPostProcessors** to hook into:
- `Servlet Filter` for HTTP requests.
- `javax.sql.DataSource` for database operations.
- `ClientHttpRequestInterceptor` for external API calls.
- `@ControllerAdvice` for global exception handling.

It correlates all events using a `ThreadLocal` context, ensuring that every database query and external call is tied back to the specific HTTP request that triggered it.

---

## 📜 License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.
