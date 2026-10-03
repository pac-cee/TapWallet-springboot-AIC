"use client";

import { useEffect } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Activity, Gauge, LogOut, Send, Settings2, Users } from "lucide-react";
import { useAuth } from "@/components/auth-provider";
import { Logo } from "@/components/logo";

const baseLinks = [
  { href: "/dashboard", label: "Overview", icon: Gauge, roles: ["ADMIN", "CUSTOMER", "MERCHANT"] },
  { href: "/pay", label: "Pay", icon: Send, roles: ["CUSTOMER"] },
  { href: "/activity", label: "Activity", icon: Activity, roles: ["ADMIN", "CUSTOMER", "MERCHANT"] },
  { href: "/admin", label: "Manage", icon: Users, roles: ["ADMIN"] },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { session, ready, logout } = useAuth();

  useEffect(() => {
    if (ready && !session) router.replace("/");
  }, [ready, session, router]);

  if (!ready || !session) return <div className="page-loader">Opening TapWallet…</div>;
  const links = baseLinks.filter((link) => link.roles.includes(session.role));

  return (
    <div className="app-frame">
      <aside className="sidebar">
        <Logo />
        <nav aria-label="Main navigation">
          {links.map(({ href, label, icon: Icon }) => (
            <Link href={href} key={href} className={pathname === href ? "active" : ""}>
              <Icon size={18} strokeWidth={pathname === href ? 2 : 1.7} aria-hidden="true" />{label}
            </Link>
          ))}
        </nav>
        <div className="sidebar-user">
          <span className="avatar" aria-hidden="true">{session.fullName.slice(0, 1)}</span>
          <span><strong>{session.fullName}</strong><small>{session.role.toLowerCase()}</small></span>
          <button className="icon-button" onClick={logout} aria-label="Sign out"><LogOut size={18} /></button>
        </div>
      </aside>

      <div className="app-body">
        <header className="mobile-header"><Logo /><span className="role-pill">{session.role}</span></header>
        <main className="main-content">{children}</main>
        <nav className="bottom-nav" aria-label="Mobile navigation">
          {links.map(({ href, label, icon: Icon }) => (
            <Link href={href} key={href} className={pathname === href ? "active" : ""}>
              <Icon size={20} aria-hidden="true" /><span>{label}</span>
            </Link>
          ))}
          <button onClick={logout}><Settings2 size={20} aria-hidden="true" /><span>Account</span></button>
        </nav>
      </div>
    </div>
  );
}
