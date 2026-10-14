import Chip from '@mui/material/Chip'
import { statusLabel } from '../format'
import type { ApplicationStatus } from '../types'

const colorByStatus: Record<ApplicationStatus, 'default' | 'warning' | 'info' | 'success' | 'error'> = {
  DRAFT: 'default',
  PENDING_APPROVAL: 'warning',
  PENDING_APPROVER_1: 'warning',
  PENDING_APPROVER_2: 'info',
  APPROVED: 'success',
  REJECTED: 'error',
}

export function StatusChip({ status }: { status: ApplicationStatus }) {
  return <Chip size="small" label={statusLabel(status)} color={colorByStatus[status]} />
}
