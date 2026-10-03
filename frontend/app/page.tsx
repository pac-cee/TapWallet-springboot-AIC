"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowRight, Check, Eye, EyeOff, ShieldCheck } from "lucide-react";
import { Logo } from "@/components/logo";
import { useAuth } from "@/components/auth-provider";

const demos = [
  { label: "Customer", email: "alice@tapwallet.rw", password: "Pass@123" },
  { label: "Merchant", email: "cafe@tapwallet.rw", password: "Pass@123" },
  { label: "Admin", email: "admin@tapwallet.rw", password: "Admin@123" },
];

export default function LoginPage() {
  const router = useRouter();
  const { session, ready, login } = useAuth();
  const [email, setEmail] = useState(demos[0].email);
  const [password, setPassword] = useState(demos[0].password);
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (ready && session) router.replace("/dashboard");
  }, [ready, session, router]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try { await login(email, password); }
    catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to sign in."); }
    finally { setSubmitting(false); }
  }

  return (
    <main className="login-page">
      <section className="login-story" aria-labelledby="story-title">
        <Logo />
        <div className="story-copy">
          <p className="eyebrow">Everyday payments</p>
          <h1 id="story-title">Send, spend, and stay in control.</h1>
          <p>Keep your wallet, merchant payments, and transaction history together in one secure place.</p>
          <ul className="proof-list">
            <li><Check size={17} aria-hidden="true" /> Pay registered merchants in moments</li>
            <li><Check size={17} aria-hidden="true" /> Follow every payment from one timeline</li>
            <li><Check size={17} aria-hidden="true" /> Keep account access private and protected</li>
          </ul>
        </div>
        <div className="trust-note"><ShieldCheck size={18} aria-hidden="true" /> Protected access and a clear record of every transaction.</div>
      </section>

      <section className="login-panel" aria-labelledby="login-title">
        <div className="login-box">
          <div>
            <p className="eyebrow">Welcome back</p>
            <h2 id="login-title">Sign in to TapWallet</h2>
            <p className="muted">Enter your details or choose a quick-access account.</p>
          </div>

          <div className="role-switch" aria-label="Quick-access accounts">
            {demos.map((demo) => (
              <button key={demo.label} type="button" className={email === demo.email ? "active" : ""}
                onClick={() => { setEmail(demo.email); setPassword(demo.password); setError(""); }}>
                {demo.label}
              </button>
            ))}
          </div>

          <form onSubmit={submit} className="stack-form">
            <label>Email address
              <input type="email" name="email" autoComplete="email" spellCheck={false} value={email}
                onChange={(event) => setEmail(event.target.value)} placeholder="name@tapwallet.rw…" required />
            </label>
            <label>Password
              <span className="password-field">
                <input type={showPassword ? "text" : "password"} name="password" autoComplete="current-password"
                  value={password} onChange={(event) => setPassword(event.target.value)} required />
                <button type="button" className="icon-button" onClick={() => setShowPassword((value) => !value)}
                  aria-label={showPassword ? "Hide password" : "Show password"}>
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </span>
            </label>
            {error && <p className="form-error" role="alert">{error}</p>}
            <button className="primary-button" type="submit" disabled={submitting}>
              {submitting ? "Signing in…" : "Continue"}<ArrowRight size={17} aria-hidden="true" />
            </button>
          </form>
        </div>
      </section>
    </main>
  );
}
