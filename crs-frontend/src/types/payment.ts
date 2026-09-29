// Khớp PaymentResponse của payment-service

export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'CANCELLED';
export type PaymentMethod = 'COD' | 'BANK_TRANSFER';

export interface BankTransferInfo {
  bankBin: string;
  bankName: string;
  accountNo: string;
  accountName: string;
  transferContent: string;
}

export interface Payment {
  id: number;
  orderId: number;
  userId: number;
  amount: number;
  paymentMethod: PaymentMethod;
  status: PaymentStatus;
  transactionId?: string;
  paidAt?: string;
  createdAt: string;
  bankTransfer?: BankTransferInfo;
}
