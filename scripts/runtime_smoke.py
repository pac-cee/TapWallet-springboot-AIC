#!/usr/bin/env python3
"""Verify the running TapWallet stack without printing access tokens."""

import argparse
import json
import urllib.error
import urllib.request


def call(url, method="GET", payload=None, token=None, accept="application/json"):
    headers = {"Accept": accept}
    body = None
    if payload is not None:
        headers["Content-Type"] = "application/json"
        body = json.dumps(payload).encode("utf-8")
    if token:
        headers["Authorization"] = f"Bearer {token}"
    request = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request, timeout=10) as response:
            return response.status, response.headers, response.read()
    except urllib.error.HTTPError as error:
        return error.code, error.headers, error.read()


def login(base_url, email, password):
    status, _, body = call(
        f"{base_url}/api/auth/login",
        method="POST",
        payload={"email": email, "password": password},
    )
    if status != 200:
        raise RuntimeError(f"Login failed for {email}: HTTP {status}")
    return json.loads(body)["accessToken"]


def require(label, actual, expected):
    if actual != expected:
        raise RuntimeError(f"{label}: expected HTTP {expected}, received {actual}")
    print(f"PASS  {label}: HTTP {actual}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--api-url", default="http://127.0.0.1:8080")
    parser.add_argument("--web-url", default="http://127.0.0.1:3000")
    parser.add_argument("--admin-email", default="admin@tapwallet.rw")
    parser.add_argument("--admin-password", default="Admin@123")
    parser.add_argument("--customer-email", default="alice@tapwallet.rw")
    parser.add_argument("--customer-password", default="Pass@123")
    args = parser.parse_args()
    api_url = args.api_url.rstrip("/")
    web_url = args.web_url.rstrip("/")

    health_status, _, _ = call(f"{api_url}/actuator/health")
    require("backend health", health_status, 200)
    web_status, _, _ = call(web_url, accept="text/html")
    require("frontend", web_status, 200)

    admin_token = login(api_url, args.admin_email, args.admin_password)
    customer_token = login(api_url, args.customer_email, args.customer_password)

    summary_status, _, summary_body = call(
        f"{api_url}/api/reports/summary", token=admin_token
    )
    require("admin report summary", summary_status, 200)
    summary = json.loads(summary_body)
    expected_keys = {"totalUsers", "totalMerchants", "totalPayments", "dailyVolume"}
    if not expected_keys.issubset(summary):
        raise RuntimeError("Report summary is missing required fields")
    print(
        "PASS  report shape: "
        f"payments={summary['totalPayments']}, merchants={summary['totalMerchants']}"
    )

    csv_status, csv_headers, csv_body = call(
        f"{api_url}/api/reports/payments.csv", token=admin_token, accept="text/csv"
    )
    require("admin CSV export", csv_status, 200)
    if "text/csv" not in csv_headers.get("Content-Type", "") or not csv_body.startswith(b"Reference,"):
        raise RuntimeError("CSV response has an unexpected content type or header row")
    print(f"PASS  CSV format: {len(csv_body.splitlines()) - 1} data rows")

    forbidden_status, _, _ = call(
        f"{api_url}/api/reports/summary", token=customer_token
    )
    require("customer report restriction", forbidden_status, 403)

    removed_topup_status, _, _ = call(
        f"{api_url}/api/me/wallet/top-up",
        method="POST",
        payload={"amount": 1000},
        token=customer_token,
    )
    require("customer self-top-up unavailable", removed_topup_status, 404)


if __name__ == "__main__":
    main()
