import { ArrowDownLeft, RotateCcw } from "lucide-react";
import { formatDate, formatMoney } from "@/lib/api";
import type { Payment, Role } from "@/lib/types";

export function PaymentTable({ payments, role, limit, onReverse }: { payments: Payment[]; role: Role; limit?: number; onReverse?: (payment: Payment) => void }) {
  const rows = limit ? payments.slice(0, limit) : payments;
  if (!rows.length) return <div className="empty-state">No payments yet. New activity will appear here.</div>;
  return (
    <div className="table-scroll">
      <table className="data-table">
        <thead><tr><th>Transaction</th><th>Counterparty</th><th>Status</th><th className="amount-cell">Amount</th>{onReverse && <th className="action-cell">Action</th>}</tr></thead>
        <tbody>
          {rows.map((payment) => (
            <tr key={payment.id}>
              <td><span className="transaction-cell"><span className="transaction-icon">
                {payment.status === "REVERSED" ? <RotateCcw size={17} /> : <ArrowDownLeft size={17} />}
              </span><span><strong>{payment.reference}</strong><small>{formatDate(payment.createdAt)}</small></span></span></td>
              <td>{role === "MERCHANT" ? payment.wallet.owner.fullName : payment.merchant.businessName}</td>
              <td><span className={`status ${payment.status.toLowerCase()}`}>{payment.status}</span></td>
              <td className="amount-cell"><strong>{role === "MERCHANT" ? "+" : "−"}{formatMoney(payment.amount, payment.currency)}</strong></td>
              {onReverse && <td className="action-cell">{payment.status === "COMPLETED" ?
                <button className="row-action" onClick={() => onReverse(payment)}><RotateCcw size={15} /> Reverse</button> : <span className="muted">—</span>}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
