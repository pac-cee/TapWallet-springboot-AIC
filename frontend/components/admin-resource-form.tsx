"use client";

import { FormEvent, useState } from "react";
import { ApiClientError, apiFetch } from "@/lib/api";
import type { Merchant, Session, User, Wallet } from "@/lib/types";

export type ResourceEditor =
  | { kind: "user"; record?: User }
  | { kind: "merchant"; record?: Merchant }
  | { kind: "wallet"; record?: Wallet };

export function AdminResourceForm({ editor, session, users, onSaved, onCancel }: {
  editor: ResourceEditor;
  session: Session;
  users: User[];
  onSaved: (message: string) => void;
  onCancel: () => void;
}) {
  if (editor.kind === "user") return <UserForm {...{ editor, session, onSaved, onCancel }} />;
  if (editor.kind === "merchant") return <MerchantForm {...{ editor, session, users, onSaved, onCancel }} />;
  return <WalletForm {...{ editor, session, users, onSaved, onCancel }} />;
}

function UserForm({ editor, session, onSaved, onCancel }: {
  editor: Extract<ResourceEditor, { kind: "user" }>;
  session: Session;
  onSaved: (message: string) => void;
  onCancel: () => void;
}) {
  const record = editor.record;
  const [fullName, setFullName] = useState(record?.fullName ?? "");
  const [email, setEmail] = useState(record?.email ?? "");
  const [phone, setPhone] = useState(record?.phone ?? "+250");
  const [role, setRole] = useState(record?.role ?? "CUSTOMER");
  const [password, setPassword] = useState("");
  const form = useAdminForm();

  async function submit(event: FormEvent) {
    event.preventDefault();
    await form.submit(async () => {
      await apiFetch(`/users${record ? `/${record.id}` : ""}`, session, {
        method: record ? "PUT" : "POST",
        body: JSON.stringify({ fullName, email, phone, role, password: password || undefined }),
      });
      onSaved(record ? "User details updated." : "User account created.");
    });
  }

  return <form className="modal-form" onSubmit={submit} noValidate>
    <FormField label="Full name" name="fullName" error={form.fields.fullName}>
      <input value={fullName} onChange={(event) => setFullName(event.target.value)} autoFocus required />
    </FormField>
    <div className="form-grid">
      <FormField label="Email address" name="email" error={form.fields.email}>
        <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
      </FormField>
      <FormField label="Phone number" name="phone" error={form.fields.phone}>
        <input value={phone} onChange={(event) => setPhone(event.target.value)} placeholder="+250788123456" required />
      </FormField>
    </div>
    <div className="form-grid">
      <FormField label="Role" name="role" error={form.fields.role}>
        <select value={role} onChange={(event) => setRole(event.target.value as User["role"])}>
          <option value="CUSTOMER">Customer</option><option value="MERCHANT">Merchant</option><option value="ADMIN">Administrator</option>
        </select>
      </FormField>
      <FormField label={record ? "New password (optional)" : "Temporary password"} name="password" error={form.fields.password}>
        <input type="password" minLength={8} value={password} onChange={(event) => setPassword(event.target.value)} placeholder={record ? "Leave unchanged" : "At least 8 characters"} required={!record} />
      </FormField>
    </div>
    <FormFooter {...form} onCancel={onCancel} submitLabel={record ? "Save changes" : "Create user"} />
  </form>;
}

function MerchantForm({ editor, session, users, onSaved, onCancel }: {
  editor: Extract<ResourceEditor, { kind: "merchant" }>;
  session: Session;
  users: User[];
  onSaved: (message: string) => void;
  onCancel: () => void;
}) {
  const record = editor.record;
  const operators = users.filter((user) => user.role === "MERCHANT");
  const [businessName, setBusinessName] = useState(record?.businessName ?? "");
  const [code, setCode] = useState(record?.code ?? "");
  const [operatorId, setOperatorId] = useState(String(record?.operator.id ?? operators[0]?.id ?? ""));
  const form = useAdminForm();

  async function submit(event: FormEvent) {
    event.preventDefault();
    await form.submit(async () => {
      await apiFetch(`/merchants${record ? `/${record.id}` : ""}`, session, {
        method: record ? "PUT" : "POST",
        body: JSON.stringify({ businessName, code: code.toUpperCase(), operatorId: Number(operatorId) }),
      });
      onSaved(record ? "Merchant details updated." : "Merchant registered.");
    });
  }

  return <form className="modal-form" onSubmit={submit} noValidate>
    <FormField label="Business name" name="businessName" error={form.fields.businessName}>
      <input value={businessName} onChange={(event) => setBusinessName(event.target.value)} autoFocus required />
    </FormField>
    <div className="form-grid">
      <FormField label="Merchant code" name="code" error={form.fields.code}>
        <input value={code} onChange={(event) => setCode(event.target.value.toUpperCase())} placeholder="CAF-001" required />
      </FormField>
      <FormField label="Operator" name="operatorId" error={form.fields.operatorId}>
        <select value={operatorId} onChange={(event) => setOperatorId(event.target.value)} required>
          <option value="" disabled>Select an operator</option>
          {operators.map((user) => <option key={user.id} value={user.id}>{user.fullName}</option>)}
        </select>
      </FormField>
    </div>
    {!operators.length && <p className="form-error">Create a user with the Merchant role first.</p>}
    <FormFooter {...form} onCancel={onCancel} submitLabel={record ? "Save changes" : "Register merchant"} disabled={!operators.length} />
  </form>;
}

function WalletForm({ editor, session, users, onSaved, onCancel }: {
  editor: Extract<ResourceEditor, { kind: "wallet" }>;
  session: Session;
  users: User[];
  onSaved: (message: string) => void;
  onCancel: () => void;
}) {
  const record = editor.record;
  const customers = users.filter((user) => user.role === "CUSTOMER");
  const [ownerId, setOwnerId] = useState(String(record?.owner.id ?? customers[0]?.id ?? ""));
  const [openingBalance, setOpeningBalance] = useState("0");
  const currency = "RWF";
  const [status, setStatus] = useState(record?.status ?? "ACTIVE");
  const form = useAdminForm();

  async function submit(event: FormEvent) {
    event.preventDefault();
    await form.submit(async () => {
      await apiFetch(`/wallets${record ? `/${record.id}` : ""}`, session, {
        method: record ? "PUT" : "POST",
        body: JSON.stringify(record ? { currency: currency.toUpperCase(), status } : {
          ownerId: Number(ownerId), openingBalance: Number(openingBalance), currency: currency.toUpperCase(),
        }),
      });
      onSaved(record ? "Wallet settings updated." : "Wallet opened.");
    });
  }

  return <form className="modal-form" onSubmit={submit} noValidate>
    {!record && <FormField label="Customer" name="ownerId" error={form.fields.ownerId}>
      <select value={ownerId} onChange={(event) => setOwnerId(event.target.value)} required autoFocus>
        <option value="" disabled>Select a customer</option>
        {customers.map((user) => <option key={user.id} value={user.id}>{user.fullName}</option>)}
      </select>
    </FormField>}
    <div className="form-grid">
      {!record && <FormField label="Opening balance" name="openingBalance" error={form.fields.openingBalance}>
        <input type="number" min="0" step="1" value={openingBalance} onChange={(event) => setOpeningBalance(event.target.value)} />
      </FormField>}
      <FormField label="Currency" name="currency" error={form.fields.currency}>
        <input value={currency} readOnly aria-readonly="true" />
      </FormField>
      {record && <FormField label="Status" name="status" error={form.fields.status}>
        <select value={status} onChange={(event) => setStatus(event.target.value as Wallet["status"])}>
          <option value="ACTIVE">Active</option><option value="FROZEN">Frozen</option>
        </select>
      </FormField>}
    </div>
    {!customers.length && !record && <p className="form-error">Create a customer account first.</p>}
    <FormFooter {...form} onCancel={onCancel} submitLabel={record ? "Save changes" : "Open wallet"} disabled={!record && !customers.length} />
  </form>;
}

function useAdminForm() {
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [fields, setFields] = useState<Record<string, string>>({});
  async function submit(action: () => Promise<void>) {
    setSubmitting(true); setError(""); setFields({});
    try { await action(); }
    catch (reason) {
      setError(reason instanceof Error ? reason.message : "The change could not be saved.");
      if (reason instanceof ApiClientError) setFields(reason.fieldErrors);
    } finally { setSubmitting(false); }
  }
  return { submitting, error, fields, submit };
}

function FormField({ label, name, error, children }: { label: string; name: string; error?: string; children: React.ReactNode }) {
  return <label htmlFor={name}>{label}{children}{error && <small className="field-error">{error}</small>}</label>;
}

function FormFooter({ submitting, error, onCancel, submitLabel, disabled = false }: {
  submitting: boolean; error: string; onCancel: () => void; submitLabel: string; disabled?: boolean;
}) {
  return <>
    {error && <p className="form-error" role="alert">{error}</p>}
    <div className="modal-actions">
      <button className="secondary-button" type="button" onClick={onCancel}>Cancel</button>
      <button className="primary-button" type="submit" disabled={submitting || disabled}>{submitting ? "Saving…" : submitLabel}</button>
    </div>
  </>;
}
