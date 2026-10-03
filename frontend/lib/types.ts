export type Role = "ADMIN" | "CUSTOMER" | "MERCHANT";
export type Status = "ACTIVE" | "FROZEN";

export interface Session {
  accessToken: string;
  tokenType: "Bearer";
  expiresIn: number;
  userId: number;
  fullName: string;
  email: string;
  role: Role;
  status: Status;
}

export interface User {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  role: Role;
  status: Status;
  createdAt: string;
}

export interface Wallet {
  id: number;
  owner: User;
  balance: number;
  currency: string;
  status: Status;
}

export interface Merchant {
  id: number;
  businessName: string;
  code: string;
  operator: User;
  revenue: number;
  status: Status;
}

export interface Payment {
  id: number;
  reference: string;
  wallet: Wallet;
  merchant: Merchant;
  amount: number;
  currency: string;
  note?: string;
  status: "COMPLETED" | "REVERSED";
  createdAt: string;
}

export interface Activity {
  id: string;
  eventType: string;
  actorUserId: number;
  entityType: string;
  entityId: string;
  message: string;
  occurredAt: string;
}

export interface ApiError {
  message?: string;
  fieldErrors: Record<string, string>;
}
