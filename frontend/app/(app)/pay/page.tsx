"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowRight, BadgeCheck, Store } from "lucide-react";
import { useAuth } from "@/components/auth-provider";
import { useToast } from "@/components/toast-provider";
import { apiFetch, formatMoney } from "@/lib/api";
import type { Merchant, Wallet } from "@/lib/types";

export default function PayPage() {
  const router = useRouter();
  const { session } = useAuth();
  const { notify } = useToast();
  const [wallet, setWallet] = useState<Wallet>();
  const [merchants, setMerchants] = useState<Merchant[]>([]);
  const [merchantId, setMerchantId] = useState("");
  const [amount, setAmount] = useState("");
  const [note, setNote] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!session) return;
    if (session.role !== "CUSTOMER") { router.replace("/dashboard"); return; }
    Promise.all([apiFetch<Wallet>("/me/wallet", session), apiFetch<Merchant[]>("/merchants", session)])
      .then(([nextWallet, nextMerchants]) => {
        const active = nextMerchants.filter((merchant) => merchant.status === "ACTIVE");
        setWallet(nextWallet); setMerchants(active); if (active[0]) setMerchantId(String(active[0].id));
      }).catch((reason) => setError(reason instanceof Error ? reason.message : "Unable to prepare payment."));
  }, [session, router]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!session) return;
    setSubmitting(true); setError("");
    try {
      await apiFetch("/me/payments", session, { method: "POST", body: JSON.stringify({ merchantId: Number(merchantId), amount: Number(amount), note }) });
      notify("Payment completed successfully.");
      router.push("/activity");
    } catch (reason) {
      const message = reason instanceof Error ? reason.message : "Payment failed.";
      setError(message); notify(message, "error");
    }
    finally { setSubmitting(false); }
  }

  const selected = merchants.find((merchant) => String(merchant.id) === merchantId);
  return (
    <div className="page-stack narrow-page">
      <header className="page-header"><div><p className="eyebrow">New payment</p><h1>Pay a merchant</h1><p>Review the details before money leaves your wallet.</p></div></header>
      <div className="payment-layout">
        <form className="payment-form" onSubmit={submit}>
          <label>Merchant<select value={merchantId} onChange={(event) => setMerchantId(event.target.value)} required>
            {merchants.map((merchant) => <option value={merchant.id} key={merchant.id}>{merchant.businessName} · {merchant.code}</option>)}
          </select></label>
          <label>Amount (RWF)<input type="number" inputMode="decimal" min="1" step="1" value={amount}
            onChange={(event) => setAmount(event.target.value)} placeholder="5,000…" required /></label>
          <label>Note <span className="optional">Optional</span><input value={note} maxLength={120}
            onChange={(event) => setNote(event.target.value)} placeholder="What is this payment for?…" /></label>
          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="primary-button" type="submit" disabled={submitting || !merchantId}>
            {submitting ? "Processing…" : "Confirm payment"}<ArrowRight size={17} />
          </button>
        </form>
        <aside className="payment-summary">
          <span className="summary-icon"><Store size={20} /></span><small>Paying</small><strong>{selected?.businessName ?? "Select a merchant"}</strong>
          <div className="summary-row"><span>Amount</span><strong>{amount ? formatMoney(Number(amount)) : "—"}</strong></div>
          <div className="summary-row"><span>Balance after</span><strong>{wallet && amount ? formatMoney(wallet.balance - Number(amount)) : "—"}</strong></div>
          <p><BadgeCheck size={16} /> Merchant status verified before payment.</p>
        </aside>
      </div>
    </div>
  );
}
