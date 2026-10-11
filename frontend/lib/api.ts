import type { ApiError, Session } from "@/lib/types";

export const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api";

export class ApiClientError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly fieldErrors: Record<string, string> = {},
  ) {
    super(message);
    this.name = "ApiClientError";
  }
}

export async function apiFetch<T>(path: string, session?: Session | null, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(session ? { Authorization: `Bearer ${session.accessToken}` } : {}),
      ...init?.headers,
    },
    cache: "no-store",
  });

  if (!response.ok) {
    let error: Partial<ApiError> = {};
    try {
      error = await response.json();
    } catch {
      // Keep the stable fallback below for non-JSON infrastructure errors.
    }
    if (response.status === 401) window.dispatchEvent(new Event("tapwallet:unauthorized"));
    throw new ApiClientError(
      error.message ?? friendlyStatusMessage(response.status),
      response.status,
      error.fieldErrors ?? {},
    );
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export async function apiDownload(path: string, session: Session): Promise<Blob> {
  const response = await fetch(`${API_URL}${path}`, {
    headers: { Authorization: `Bearer ${session.accessToken}` },
    cache: "no-store",
  });
  if (!response.ok) {
    let error: Partial<ApiError> = {};
    try { error = await response.json(); } catch { /* Use the fallback below. */ }
    if (response.status === 401) window.dispatchEvent(new Event("tapwallet:unauthorized"));
    throw new ApiClientError(error.message ?? friendlyStatusMessage(response.status), response.status);
  }
  return response.blob();
}

function friendlyStatusMessage(status: number) {
  if (status === 401) return "Your session has expired. Please sign in again.";
  if (status === 403) return "You do not have permission to perform this action.";
  if (status === 404) return "The requested record could not be found.";
  if (status >= 500) return "TapWallet is temporarily unavailable. Please try again shortly.";
  return "We could not complete your request. Please check the details and try again.";
}

export function formatMoney(amount: number, currency = "RWF") {
  return new Intl.NumberFormat("en-RW", {
    style: "currency",
    currency,
    maximumFractionDigits: currency === "RWF" ? 0 : 2,
  }).format(amount);
}

export function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-RW", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}
