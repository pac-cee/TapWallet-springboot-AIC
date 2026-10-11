# TapWallet Performance Evidence

## Target

Typical authenticated reads should remain below 500 ms at the 95th percentile with 100 concurrent classroom users.

## Reproducible check

```bash
python3 scripts/performance_smoke.py --requests 200 --concurrency 100 --p95-ms 500
```

The script signs in through the public authentication endpoint, warms the application, sends authenticated `GET /api/me` requests using 100 worker threads, reports latency percentiles and throughput, and exits unsuccessfully when errors occur or the p95 threshold is exceeded.

## Recorded result

Recorded on 11 October 2026 against the local Docker stack:

| Measure | Result |
|---|---:|
| Requests | 200 |
| Concurrency | 100 |
| Successful | 200 |
| Errors | 0 |
| p50 | 92.97 ms |
| p95 | 153.35 ms |
| Maximum | 182.85 ms |
| Throughput | 831.01 requests/second |
| Target | p95 below 500 ms |

Result: **Pass**. This is a controlled classroom smoke measurement, not a production capacity guarantee. Network latency, database size, infrastructure limits, and external services require separate production load testing.
