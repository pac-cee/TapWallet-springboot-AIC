"use client";

import { useEffect, useMemo, useState } from "react";
import { Search } from "lucide-react";
import { useAuth } from "@/components/auth-provider";
import { Modal } from "@/components/modal";
import { PaymentTable } from "@/components/payment-table";
import { useToast } from "@/components/toast-provider";
import { apiFetch } from "@/lib/api";
import type { Payment } from "@/lib/types";

export default function ActivityPage() {
  const { session } = useAuth();
  const { notify } = useToast();
  const [payments, setPayments] = useState<Payment[]>([]);
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState("ALL");
  const [error, setError] = useState("");
  const [reversing, setReversing] = useState<Payment | null>(null);

  async function reverse() {
    if (!session || !reversing) return;
    try {
      const updated = await apiFetch<Payment>(`/payments/${reversing.id}/reverse`, session, { method: "POST" });
      setPayments((current) => current.map((payment) => payment.id === updated.id ? updated : payment));
      setReversing(null); notify("Payment reversed and funds returned to the wallet.");
    } catch (reason) {
      const message = reason instanceof Error ? reason.message : "The payment could not be reversed.";
      setError(message); setReversing(null); notify(message, "error");
    }
  }

  useEffect(() => {
    if (!session) return;
    apiFetch<Payment[]>("/me/payments", session).then(setPayments)
      .catch((reason) => setError(reason instanceof Error ? reason.message : "Unable to load payments."));
  }, [session]);

  const filtered = useMemo(() => payments.filter((payment) => {
    const matchesStatus = status === "ALL" || payment.status === status;
    const haystack = `${payment.reference} ${payment.merchant.businessName} ${payment.wallet.owner.fullName}`.toLowerCase();
    return matchesStatus && haystack.includes(query.toLowerCase());
  }), [payments, query, status]);

  if (!session) return null;
  return (
    <div className="page-stack">
      <header className="page-header"><div><p className="eyebrow">Transaction history</p><h1>Activity</h1><p>Search, verify, and follow every payment.</p></div></header>
      <section className="toolbar">
        <label className="search-field"><Search size={17} aria-hidden="true" /><span className="sr-only">Search activity</span>
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search reference or merchant…" /></label>
        <select value={status} onChange={(event) => setStatus(event.target.value)} aria-label="Filter by status">
          <option value="ALL">All statuses</option><option value="COMPLETED">Completed</option><option value="REVERSED">Reversed</option>
        </select>
      </section>
      {error && <p className="form-error" role="alert">{error}</p>}
      <section className="content-section flush"><PaymentTable payments={filtered} role={session.role}
        onReverse={session.role === "ADMIN" ? setReversing : undefined} /></section>
      {reversing && <Modal title={`Reverse ${reversing.reference}?`} description="The amount will be returned to the customer's wallet and removed from merchant revenue." onClose={() => setReversing(null)}>
        <div className="confirm-body"><p>This creates a permanent reversal record; the original payment remains in the audit trail.</p>
          <div className="modal-actions"><button className="secondary-button" onClick={() => setReversing(null)}>Cancel</button>
            <button className="danger-button" onClick={() => void reverse()}>Reverse payment</button></div></div>
      </Modal>}
    </div>
  );
}
