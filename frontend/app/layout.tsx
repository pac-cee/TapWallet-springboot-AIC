import type { Metadata, Viewport } from "next";
import "./globals.css";
import { AuthProvider } from "@/components/auth-provider";
import { ToastProvider } from "@/components/toast-provider";

export const metadata: Metadata = {
  title: "TapWallet",
  description: "A secure digital wallet for everyday Rwandan payments.",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#f6f7f5",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body><ToastProvider><AuthProvider>{children}</AuthProvider></ToastProvider></body>
    </html>
  );
}
