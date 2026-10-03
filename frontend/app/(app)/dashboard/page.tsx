"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { ArrowRight, Building2, CircleDollarSign, Send, ShieldCheck, UserRound, Users } from "lucide-react";
import { useAuth } from "@/components/auth-provider";
import { PaymentTable } from "@/components/payment-table";
import { apiFetch, formatMoney } from "@/lib/api";
import type { Activity, Merchant, Payment, User, Wallet } from "@/lib/types";

export default function DashboardPage() {
  const { session } = useAuth();
  const [wallet, setWallet] = useState<Wallet>();
  const [merchant, setMerchant] = useState<Merchant>();
  const [merchants, setMerchants] = useState<Merchant[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!session) return;
    async function load() {
      try {
        if (session?.role === "CUSTOMER") {
          const [nextWallet, nextPayments, nextMerchants] = await Promise.all([
            apiFetch<Wallet>("/me/wallet", session), apiFetch<Payment[]>("/me/payments", session),
            apiFetch<Merchant[]>("/merchants", session),
          ]);
          setWallet(nextWallet); setPayments(nextPayments); setMerchants(nextMerchants);
        } else if (session?.role === "MERCHANT") {
          const [nextMerchant, nextPayments] = await Promise.all([
            apiFetch<Merchant>("/me/merchant", session), apiFetch<Payment[]>("/me/payments", session),
          ]);
          setMerchant(nextMerchant); setPayments(nextPayments);
        } else {
          const [nextUsers, nextMerchants, nextPayments, nextActivities] = await Promise.all([
            apiFetch<User[]>("/users", session), apiFetch<Merchant[]>("/merchants", session),
            apiFetch<Payment[]>("/payments", session), apiFetch<Activity[]>("/activity", session),
          ]);
          setUsers(nextUsers); setMerchants(nextMerchants); setPayments(nextPayments); setActivities(nextActivities);
        }
      } catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to load dashboard."); }
    }
    load();
  }, [session]);

  if (!session) return null;
  const volume = payments.filter((payment) => payment.status === "COMPLETED")
    .reduce((sum, payment) => sum + Number(payment.amount), 0);

  return (
    <div className="page-stack">
      <header className="page-header">
        <div><p className="eyebrow">{greeting()}, {session.fullName.split(" ")[0]}</p>
          <h1>{session.role === "ADMIN" ? "System overview" : session.role === "MERCHANT" ? "Business overview" : "Your wallet"}</h1>
          <p>See what matters now and move with confidence.</p></div>
        <span className="secure-label"><ShieldCheck size={16} /> Secure session</span>
      </header>
      {error && <p className="form-error" role="alert">{error}</p>}

      {session.role === "CUSTOMER" && (
        <>
          <section className="balance-strip">
            <div><span>Available balance</span><strong>{wallet ? formatMoney(wallet.balance, wallet.currency) : "—"}</strong>
              <small>{wallet?.status === "ACTIVE" ? "Ready to use" : "Wallet frozen"}</small></div>
            <div className="balance-actions"><Link href="/pay" className="light-button"><Send size={17} /> Pay a merchant</Link>
              <Link href="/activity" className="text-button">View activity <ArrowRight size={16} /></Link></div>
          </section>
          <section className="quick-line" aria-label="Quick wallet facts">
            <div><CircleDollarSign size={18} /><span><small>Spent in demo</small><strong>{formatMoney(volume)}</strong></span></div>
            <div><Building2 size={18} /><span><small>Active merchants</small><strong>{merchants.filter((item) => item.status === "ACTIVE").length}</strong></span></div>
            <div><UserRound size={18} /><span><small>Account</small><strong>{wallet?.owner.status ?? "—"}</strong></span></div>
          </section>
        </>
      )}

      {session.role === "MERCHANT" && (
        <section className="balance-strip merchant-strip">
          <div><span>Total received</span><strong>{merchant ? formatMoney(merchant.revenue) : "—"}</strong>
            <small>{merchant?.businessName ?? "Loading merchant…"} · {merchant?.code}</small></div>
          <div className="balance-actions"><span className="light-button static"><Building2 size={17} /> {merchant?.status ?? "—"}</span></div>
        </section>
      )}

      {session.role === "ADMIN" && (
        <section className="metric-grid">
          <div className="metric"><Users size={19} /><span>Registered users</span><strong>{users.length}</strong><small>{users.filter((item) => item.status === "ACTIVE").length} active</small></div>
          <div className="metric"><Building2 size={19} /><span>Merchants</span><strong>{merchants.length}</strong><small>{merchants.filter((item) => item.status === "ACTIVE").length} accepting payments</small></div>
          <div className="metric"><CircleDollarSign size={19} /><span>Processed volume</span><strong>{formatMoney(volume)}</strong><small>{payments.length} transactions</small></div>
          <div className="metric"><ShieldCheck size={19} /><span>Audit events</span><strong>{activities.length}</strong><small>document-store records</small></div>
        </section>
      )}

      <section className="content-section">
        <div className="section-heading"><div><h2>Recent activity</h2><p>Latest wallet payments and reversals.</p></div>
          <Link href="/activity" className="text-button">See all <ArrowRight size={16} /></Link></div>
        <PaymentTable payments={payments} role={session.role} limit={6} />
      </section>
    </div>
  );
}

function greeting() {
  const hour = new Date().getHours();
  return hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
}
