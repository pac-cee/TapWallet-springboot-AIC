#!/usr/bin/env python3
"""Small dependency-free API load check for the documented classroom target."""

import argparse
import concurrent.futures
import json
import math
import time
import urllib.error
import urllib.request


def request(url, method="GET", body=None, token=None, timeout=10):
    headers = {"Accept": "application/json"}
    data = None
    if body is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(body).encode("utf-8")
    if token:
        headers["Authorization"] = f"Bearer {token}"
    started = time.perf_counter()
    try:
        with urllib.request.urlopen(
            urllib.request.Request(url, data=data, headers=headers, method=method), timeout=timeout
        ) as response:
            response.read()
            return response.status, (time.perf_counter() - started) * 1000
    except urllib.error.HTTPError as error:
        error.read()
        return error.code, (time.perf_counter() - started) * 1000


def percentile(values, fraction):
    ordered = sorted(values)
    index = max(0, min(len(ordered) - 1, math.ceil(len(ordered) * fraction) - 1))
    return ordered[index]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://127.0.0.1:8080")
    parser.add_argument("--email", default="alice@tapwallet.rw")
    parser.add_argument("--password", default="Pass@123")
    parser.add_argument("--requests", type=int, default=200)
    parser.add_argument("--concurrency", type=int, default=100)
    parser.add_argument("--p95-ms", type=float, default=500)
    args = parser.parse_args()

    login_url = f"{args.base_url.rstrip('/')}/api/auth/login"
    request_body = {"email": args.email, "password": args.password}
    login_request = urllib.request.Request(
        login_url,
        data=json.dumps(request_body).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(login_request, timeout=10) as response:
        token = json.loads(response.read())["accessToken"]

    target = f"{args.base_url.rstrip('/')}/api/me"
    for _ in range(5):
        request(target, token=token)

    started = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=args.concurrency) as pool:
        results = list(pool.map(lambda _: request(target, token=token), range(args.requests)))
    elapsed = time.perf_counter() - started

    latencies = [latency for _, latency in results]
    successes = sum(1 for status, _ in results if 200 <= status < 300)
    summary = {
        "endpoint": "/api/me",
        "requests": args.requests,
        "concurrency": args.concurrency,
        "successful": successes,
        "errors": args.requests - successes,
        "p50_ms": round(percentile(latencies, 0.50), 2),
        "p95_ms": round(percentile(latencies, 0.95), 2),
        "max_ms": round(max(latencies), 2),
        "throughput_rps": round(args.requests / elapsed, 2),
        "target_p95_ms": args.p95_ms,
    }
    print(json.dumps(summary, indent=2))
    if successes != args.requests or summary["p95_ms"] > args.p95_ms:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
