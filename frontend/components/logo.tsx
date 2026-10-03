import { WalletCards } from "lucide-react";

export function Logo({ compact = false }: { compact?: boolean }) {
  return (
    <span className="logo" aria-label="TapWallet">
      <span className="logo-mark"><WalletCards size={19} strokeWidth={2} aria-hidden="true" /></span>
      {!compact && <span>TapWallet</span>}
    </span>
  );
}
