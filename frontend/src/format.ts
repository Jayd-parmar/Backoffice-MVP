import type { ApplicationStatus, Role } from './types'

export function roleLabel(role: Role): string {
  switch (role) {
    case 'CUSTOMER':
      return 'Customer'
    case 'APPROVER_1':
      return 'Approver 1'
    case 'APPROVER_2':
      return 'Approver 2'
    case 'APPROVER_3':
      return 'Approver 3'
  }
}

export function statusLabel(status: ApplicationStatus): string {
  switch (status) {
    case 'DRAFT':
      return 'Draft'
    case 'PENDING_APPROVAL':
      return 'Pending approval'
    case 'PENDING_APPROVER_1':
      return 'Pending approver 1'
    case 'PENDING_APPROVER_2':
      return 'Pending approver 2'
    case 'APPROVED':
      return 'Approved'
    case 'REJECTED':
      return 'Rejected'
  }
}

export function destinationLabel(value: string): string {
  switch (value.trim().toUpperCase()) {
    case 'INDIA':
      return 'India'
    case 'US':
      return 'United States'
    case 'UK':
      return 'United Kingdom'
    case 'NEPAL':
      return 'Nepal'
    default:
      return value
  }
}

export function formatDate(value: string): string {
  if (!value) return '—'
  const date = new Date(`${value.slice(0, 10)}T00:00:00`)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

export function formatDateTime(value: string | null): string {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

export function formatAmount(amount: number, currency: string): string {
  try {
    return new Intl.NumberFormat(undefined, {
      style: 'currency',
      currency,
      maximumFractionDigits: 2,
    }).format(amount)
  } catch {
    return `${currency} ${amount.toLocaleString(undefined, { maximumFractionDigits: 2 })}`
  }
}

export function titleCaseToken(value: string): string {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => (part ? part.charAt(0).toUpperCase() + part.slice(1) : part))
    .join(' ')
}
