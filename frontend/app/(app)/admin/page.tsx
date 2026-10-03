"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { Edit3, Minus, PauseCircle, PlayCircle, Plus, Trash2 } from "lucide-react";
import { useRouter } from "next/navigation";
import { AdminResourceForm, type ResourceEditor } from "@/components/admin-resource-form";
import { useAuth } from "@/components/auth-provider";
import { Modal } from "@/components/modal";
import { useToast } from "@/components/toast-provider";
import { apiFetch, formatMoney } from "@/lib/api";
import type { Merchant, Session, User, Wallet } from "@/lib/types";

type Tab = "users" | "merchants" | "wallets";
type DeleteTarget = { kind: Tab; id: number; name: string };
type BalanceAction = { wallet: Wallet; mode: "top-up" | "withdraw" };

export default function AdminPage() {
  const router = useRouter();
  const { session } = useAuth();
  const { notify } = useToast();
  const [users, setUsers] = useState<User[]>([]);
  const [merchants, setMerchants] = useState<Merchant[]>([]);
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [tab, setTab] = useState<Tab>("users");
  const [editor, setEditor] = useState<ResourceEditor | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<DeleteTarget | null>(null);
  const [balanceAction, setBalanceAction] = useState<BalanceAction | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    if (!session) return;
    setError("");
    try {
      const [nextUsers, nextMerchants, nextWallets] = await Promise.all([
        apiFetch<User[]>("/users", session),
        apiFetch<Merchant[]>("/merchants", session),
        apiFetch<Wallet[]>("/wallets", session),
      ]);
      setUsers(nextUsers); setMerchants(nextMerchants); setWallets(nextWallets);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to load administration records.");
    } finally { setLoading(false); }
  }, [session]);

  useEffect(() => {
    if (session && session.role !== "ADMIN") { router.replace("/dashboard"); return; }
    const timer = window.setTimeout(() => { void load(); }, 0);
    return () => window.clearTimeout(timer);
  }, [session, router, load]);

  async function toggle(kind: "users" | "merchants", id: number, active: boolean) {
    if (!session) return;
    setError("");
    try {
      await apiFetch(`/${kind}/${id}/${active ? "freeze" : "activate"}`, session, { method: "PATCH" });
      notify(`${kind === "users" ? "User" : "Merchant"} ${active ? "frozen" : "activated"}.`);
      await load();
    } catch (reason) { handleError(reason, "The status could not be updated."); }
  }

  async function remove() {
    if (!session || !deleteTarget) return;
    try {
      await apiFetch(`/${deleteTarget.kind}/${deleteTarget.id}`, session, { method: "DELETE" });
      notify(`${deleteTarget.name} deleted.`);
      setDeleteTarget(null);
      await load();
    } catch (reason) { handleError(reason, "The record could not be deleted."); setDeleteTarget(null); }
  }

  function handleError(reason: unknown, fallback: string) {
    const message = reason instanceof Error ? reason.message : fallback;
    setError(message); notify(message, "error");
  }

  function saved(message: string) {
    setEditor(null); notify(message); void load();
  }

  const createLabel = tab === "users" ? "Add user" : tab === "merchants" ? "Add merchant" : "Open wallet";
  return (
    <div className="page-stack">
      <header className="page-header">
        <div><p className="eyebrow">Administration</p><h1>Operations</h1><p>Manage accounts, merchants, and wallet access from one place.</p></div>
        <button className="primary-button compact-button" onClick={() => setEditor({ kind: singular(tab) })}>
          <Plus size={17} /> {createLabel}
        </button>
      </header>

      <div className="tab-list" role="tablist" aria-label="Administration sections">
        <TabButton current={tab} value="users" count={users.length} onSelect={setTab}>Users</TabButton>
        <TabButton current={tab} value="merchants" count={merchants.length} onSelect={setTab}>Merchants</TabButton>
        <TabButton current={tab} value="wallets" count={wallets.length} onSelect={setTab}>Wallets</TabButton>
      </div>

      {error && <div className="inline-alert" role="alert"><span>{error}</span><button onClick={() => setError("")}>Dismiss</button></div>}
      <section className="content-section flush">
        {loading ? <div className="empty-state">Loading records…</div> : tab === "users" ? (
          <UserTable users={users} currentUserId={session?.userId} onEdit={(record) => setEditor({ kind: "user", record })}
            onToggle={(record) => void toggle("users", record.id, record.status === "ACTIVE")}
            onDelete={(record) => setDeleteTarget({ kind: "users", id: record.id, name: record.fullName })} />
        ) : tab === "merchants" ? (
          <MerchantTable merchants={merchants} onEdit={(record) => setEditor({ kind: "merchant", record })}
            onToggle={(record) => void toggle("merchants", record.id, record.status === "ACTIVE")}
            onDelete={(record) => setDeleteTarget({ kind: "merchants", id: record.id, name: record.businessName })} />
        ) : (
          <WalletTable wallets={wallets} onEdit={(record) => setEditor({ kind: "wallet", record })}
            onBalance={setBalanceAction}
            onDelete={(record) => setDeleteTarget({ kind: "wallets", id: record.id, name: `${record.owner.fullName}'s wallet` })} />
        )}
      </section>

      {editor && session && <Modal title={editor.record ? `Edit ${editor.kind}` : createLabel}
        description={editor.kind === "wallet" ? "Balances change only through controlled top-ups and withdrawals." : "Required fields are checked before saving."}
        onClose={() => setEditor(null)}>
        <AdminResourceForm editor={editor} session={session} users={users} onSaved={saved} onCancel={() => setEditor(null)} />
      </Modal>}

      {balanceAction && session && <BalanceModal action={balanceAction} session={session}
        onClose={() => setBalanceAction(null)} onSaved={(message) => { setBalanceAction(null); notify(message); void load(); }} />}

      {deleteTarget && <Modal title={`Delete ${deleteTarget.name}?`}
        description="This action is permanent. Records tied to financial history are protected."
        onClose={() => setDeleteTarget(null)}>
        <div className="confirm-body"><p>TapWallet will refuse this operation if removing the record would damage financial history.</p>
          <div className="modal-actions"><button className="secondary-button" onClick={() => setDeleteTarget(null)}>Cancel</button>
            <button className="danger-button" onClick={() => void remove()}><Trash2 size={16} /> Delete record</button></div></div>
      </Modal>}
    </div>
  );
}

function TabButton({ current, value, count, onSelect, children }: {
  current: Tab; value: Tab; count: number; onSelect: (value: Tab) => void; children: React.ReactNode;
}) {
  return <button role="tab" aria-selected={current === value} className={current === value ? "active" : ""} onClick={() => onSelect(value)}>
    {children} <span>{count}</span>
  </button>;
}

function UserTable({ users, currentUserId, onEdit, onToggle, onDelete }: {
  users: User[]; currentUserId?: number; onEdit: (user: User) => void; onToggle: (user: User) => void; onDelete: (user: User) => void;
}) {
  if (!users.length) return <div className="empty-state">No users found. Add the first account to begin.</div>;
  return <div className="table-scroll"><table className="data-table"><thead><tr><th>User</th><th>Phone</th><th>Role</th><th>Status</th><th className="action-cell">Actions</th></tr></thead>
    <tbody>{users.map((user) => <tr key={user.id}><td><strong>{user.fullName}</strong><small className="block">{user.email}</small></td><td>{user.phone}</td><td>{titleCase(user.role)}</td>
      <td><Status value={user.status} /></td><td><RowActions onEdit={() => onEdit(user)} onToggle={() => onToggle(user)} active={user.status === "ACTIVE"} onDelete={() => onDelete(user)} allowDestructive={user.id !== currentUserId} /></td></tr>)}</tbody></table></div>;
}

function MerchantTable({ merchants, onEdit, onToggle, onDelete }: {
  merchants: Merchant[]; onEdit: (merchant: Merchant) => void; onToggle: (merchant: Merchant) => void; onDelete: (merchant: Merchant) => void;
}) {
  if (!merchants.length) return <div className="empty-state">No merchants found. Register a merchant to accept payments.</div>;
  return <div className="table-scroll"><table className="data-table"><thead><tr><th>Merchant</th><th>Code</th><th>Revenue</th><th>Status</th><th className="action-cell">Actions</th></tr></thead>
    <tbody>{merchants.map((merchant) => <tr key={merchant.id}><td><strong>{merchant.businessName}</strong><small className="block">{merchant.operator.email}</small></td><td>{merchant.code}</td>
      <td>{formatMoney(merchant.revenue)}</td><td><Status value={merchant.status} /></td><td><RowActions onEdit={() => onEdit(merchant)} onToggle={() => onToggle(merchant)} active={merchant.status === "ACTIVE"} onDelete={() => onDelete(merchant)} /></td></tr>)}</tbody></table></div>;
}

function WalletTable({ wallets, onEdit, onBalance, onDelete }: {
  wallets: Wallet[]; onEdit: (wallet: Wallet) => void; onBalance: (action: BalanceAction) => void; onDelete: (wallet: Wallet) => void;
}) {
  if (!wallets.length) return <div className="empty-state">No wallets found. Open a wallet for an active customer.</div>;
  return <div className="table-scroll"><table className="data-table"><thead><tr><th>Owner</th><th>Balance</th><th>Status</th><th className="action-cell">Actions</th></tr></thead>
    <tbody>{wallets.map((wallet) => <tr key={wallet.id}><td><strong>{wallet.owner.fullName}</strong><small className="block">Wallet #{wallet.id}</small></td>
      <td><strong>{formatMoney(wallet.balance, wallet.currency)}</strong></td><td><Status value={wallet.status} /></td><td><div className="row-actions">
        <button className="row-action" onClick={() => onBalance({ wallet, mode: "top-up" })}><Plus size={15} /> Top up</button>
        <button className="icon-row-action" onClick={() => onBalance({ wallet, mode: "withdraw" })} aria-label={`Withdraw from ${wallet.owner.fullName}`}><Minus size={16} /></button>
        <button className="icon-row-action" onClick={() => onEdit(wallet)} aria-label={`Edit ${wallet.owner.fullName}'s wallet`}><Edit3 size={16} /></button>
        <button className="icon-row-action danger" onClick={() => onDelete(wallet)} aria-label={`Delete ${wallet.owner.fullName}'s wallet`}><Trash2 size={16} /></button>
      </div></td></tr>)}</tbody></table></div>;
}

function RowActions({ active, onEdit, onToggle, onDelete, allowDestructive = true }: { active: boolean; onEdit: () => void; onToggle: () => void; onDelete: () => void; allowDestructive?: boolean }) {
  return <div className="row-actions">{allowDestructive && <button className="row-action" onClick={onToggle}>
    {active ? <><PauseCircle size={15} /> Freeze</> : <><PlayCircle size={15} /> Activate</>}</button>}
    <button className="icon-row-action" onClick={onEdit} aria-label="Edit record"><Edit3 size={16} /></button>
    {allowDestructive && <button className="icon-row-action danger" onClick={onDelete} aria-label="Delete record"><Trash2 size={16} /></button>}</div>;
}

function Status({ value }: { value: string }) { return <span className={`status ${value.toLowerCase()}`}>{value}</span>; }

function BalanceModal({ action, session, onClose, onSaved }: {
  action: BalanceAction; session: Session; onClose: () => void; onSaved: (message: string) => void;
}) {
  const [amount, setAmount] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  async function submit(event: FormEvent) {
    event.preventDefault(); setSubmitting(true); setError("");
    try {
      await apiFetch(`/wallets/${action.wallet.id}/${action.mode}`, session, { method: "POST", body: JSON.stringify({ amount: Number(amount) }) });
      onSaved(action.mode === "top-up" ? "Wallet topped up successfully." : "Withdrawal completed successfully.");
    } catch (reason) { setError(reason instanceof Error ? reason.message : "The balance could not be updated."); }
    finally { setSubmitting(false); }
  }
  return <Modal title={action.mode === "top-up" ? "Top up wallet" : "Withdraw funds"}
    description={`${action.wallet.owner.fullName} · Available ${formatMoney(action.wallet.balance, action.wallet.currency)}`} onClose={onClose}>
    <form className="modal-form" onSubmit={submit}><label>Amount ({action.wallet.currency})<input autoFocus type="number" min="1" step="1" value={amount} onChange={(event) => setAmount(event.target.value)} required /></label>
      {error && <p className="form-error" role="alert">{error}</p>}<div className="modal-actions"><button type="button" className="secondary-button" onClick={onClose}>Cancel</button>
        <button className="primary-button" disabled={submitting}>{submitting ? "Processing…" : action.mode === "top-up" ? "Add funds" : "Withdraw"}</button></div></form>
  </Modal>;
}

function singular(tab: Tab): ResourceEditor["kind"] { return tab === "users" ? "user" : tab === "merchants" ? "merchant" : "wallet"; }
function titleCase(value: string) { return value.charAt(0) + value.slice(1).toLowerCase(); }
