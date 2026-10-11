"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { BarChart3, CalendarDays, Download, RefreshCw, Users } from "lucide-react";
import { useAuth } from "@/components/auth-provider";
import { useToast } from "@/components/toast-provider";
import { apiDownload, apiFetch, formatDate, formatMoney } from "@/lib/api";
import type { ReportSummary } from "@/lib/types";

export default function ReportsPage() {
  const { session } = useAuth();
  const { notify } = useToast();
  const initialPeriod = useMemo(() => defaultPeriod(), []);
  const [from, setFrom] = useState(initialPeriod.from);
  const [to, setTo] = useState(initialPeriod.to);
  const [appliedPeriod, setAppliedPeriod] = useState(initialPeriod);
  const [report, setReport] = useState<ReportSummary>();
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!session || session.role !== "ADMIN") return;
    async function load() {
      try {
        const data = await apiFetch<ReportSummary>(`/reports/summary?${query(appliedPeriod)}`, session);
        setReport(data); setError("");
      } catch (reason) {
        setError(reason instanceof Error ? reason.message : "Unable to generate the report.");
      } finally { setLoading(false); }
    }
    void load();
  }, [appliedPeriod, session]);

  function apply(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setAppliedPeriod({ from, to });
  }

  async function exportCsv() {
    if (!session) return;
    setDownloading(true);
    try {
      const blob = await apiDownload(`/reports/payments.csv?${query(appliedPeriod)}`, session);
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = `tapwallet-payments-${appliedPeriod.from}-to-${appliedPeriod.to}.csv`;
      anchor.click();
      URL.revokeObjectURL(url);
      notify("Payment report downloaded as CSV.");
    } catch (reason) {
      const message = reason instanceof Error ? reason.message : "Unable to download the report.";
      setError(message); notify(message, "error");
    } finally { setDownloading(false); }
  }

  if (!session || session.role !== "ADMIN") return null;
  const maxDailyVolume = Math.max(...(report?.dailyVolume.map((item) => Number(item.volume)) ?? [0]), 1);

  return (
    <div className="page-stack reports-page">
      <header className="page-header">
        <div><p className="eyebrow">Operational intelligence</p><h1>Reports</h1>
          <p>Measure payment activity, balances, merchant performance, and reversals.</p></div>
        <button className="secondary-button compact-button" onClick={() => void exportCsv()} disabled={downloading || loading}>
          <Download size={17} /> {downloading ? "Preparing…" : "Export CSV"}
        </button>
      </header>

      <form className="report-filter" onSubmit={apply}>
        <div className="report-filter-copy"><CalendarDays size={19} /><span><strong>Reporting period</strong><small>Maximum range: 366 days</small></span></div>
        <label>From<input type="date" value={from} max={to} onChange={(event) => setFrom(event.target.value)} required /></label>
        <label>To<input type="date" value={to} min={from} onChange={(event) => setTo(event.target.value)} required /></label>
        <button className="primary-button compact-button" type="submit" disabled={loading}><RefreshCw size={16} /> Apply</button>
      </form>

      {error && <p className="form-error" role="alert">{error}</p>}
      {loading && !report && <div className="report-loading">Generating report…</div>}

      {report && <>
        <section className="report-hero">
          <div><span>Completed payment volume</span><strong>{formatMoney(report.completedVolume)}</strong>
            <small>{report.completedPayments} completed of {report.totalPayments} total payments</small></div>
          <div className="report-period"><small>{report.from}</small><span>to</span><small>{report.to}</small>
            <em>Generated {formatDate(report.generatedAt)}</em></div>
        </section>

        <section className="report-metrics" aria-label="Report summary">
          <ReportMetric label="Registered users" value={String(report.totalUsers)} detail={`${report.activeUsers} active`} icon={<Users size={18} />} />
          <ReportMetric label="Active merchants" value={String(report.activeMerchants)} detail={`${report.totalMerchants} registered`} icon={<BarChart3 size={18} />} />
          <ReportMetric label="Wallet balances" value={formatMoney(report.walletBalances)} detail="Current available funds" />
          <ReportMetric label="Merchant revenue" value={formatMoney(report.merchantRevenue)} detail="Current recorded revenue" />
          <ReportMetric label="Reversed payments" value={String(report.reversedPayments)} detail={formatMoney(report.reversedVolume)} tone={report.reversedPayments ? "warning" : undefined} />
        </section>

        <div className="report-grid">
          <section className="content-section report-panel">
            <div className="section-heading"><div><h2>Daily payment volume</h2><p>Completed payments during the selected period.</p></div></div>
            <div className="volume-chart">
              {report.dailyVolume.length ? report.dailyVolume.map((item) => (
                <div className="volume-row" key={item.date}>
                  <time dateTime={item.date}>{shortDate(item.date)}</time>
                  <div className="volume-track"><span style={{ width: `${Math.max(3, Number(item.volume) / maxDailyVolume * 100)}%` }} /></div>
                  <strong>{formatMoney(item.volume)}</strong><small>{item.payments}</small>
                </div>
              )) : <div className="empty-state">No completed payments in this period.</div>}
            </div>
          </section>

          <section className="content-section flush report-panel">
            <div className="section-heading"><div><h2>Top merchants</h2><p>Ranked by completed payment volume.</p></div></div>
            {report.topMerchants.length ? <div className="table-scroll"><table className="data-table"><thead><tr><th>Merchant</th><th>Payments</th><th className="amount-cell">Volume</th></tr></thead>
              <tbody>{report.topMerchants.map((merchant, index) => <tr key={merchant.merchantId}>
                <td><span className="rank">{index + 1}</span><strong>{merchant.businessName}</strong><small className="block">{merchant.code}</small></td>
                <td>{merchant.payments}</td><td className="amount-cell"><strong>{formatMoney(merchant.volume)}</strong></td>
              </tr>)}</tbody></table></div> : <div className="empty-state">No merchant activity in this period.</div>}
          </section>
        </div>
      </>}
    </div>
  );
}

function ReportMetric({ label, value, detail, icon, tone }: { label: string; value: string; detail: string; icon?: React.ReactNode; tone?: string }) {
  return <div className={`report-metric ${tone ?? ""}`}><span>{icon}{label}</span><strong>{value}</strong><small>{detail}</small></div>;
}

function defaultPeriod() {
  const to = new Date();
  const from = new Date(to); from.setDate(from.getDate() - 29);
  return { from: localDate(from), to: localDate(to) };
}

function localDate(date: Date) {
  const offset = date.getTimezoneOffset();
  return new Date(date.getTime() - offset * 60_000).toISOString().slice(0, 10);
}

function query(period: { from: string; to: string }) {
  return new URLSearchParams(period).toString();
}

function shortDate(value: string) {
  return new Intl.DateTimeFormat("en-RW", { day: "2-digit", month: "short" }).format(new Date(`${value}T12:00:00`));
}
