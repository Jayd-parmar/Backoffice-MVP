import type { ReactNode } from 'react'
import Box from '@mui/material/Box'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { formatAmount, formatDate, formatDateTime, titleCaseToken } from '../format'
import type { Application, ApprovalDecision } from '../types'

function Field({ label, value }: { label: string; value: string }) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body1">{value || '—'}</Typography>
    </Box>
  )
}

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <Paper variant="outlined" sx={{ p: { xs: 2, sm: 3 } }}>
      <Typography variant="h6" sx={{ mb: 2 }}>
        {title}
      </Typography>
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
          gap: 2,
        }}
      >
        {children}
      </Box>
    </Paper>
  )
}

function Decision({ title, decision }: { title: string; decision: ApprovalDecision }) {
  return (
    <Box sx={{ gridColumn: '1 / -1' }}>
      <Typography variant="subtitle2">{title}</Typography>
      <Typography variant="body2" color="text.secondary">
        {decision.action ? titleCaseToken(decision.action) : 'Not reviewed yet'}
        {decision.actionAt ? ` · ${formatDateTime(decision.actionAt)}` : ''}
      </Typography>
      {decision.comment ? (
        <Typography variant="body1" sx={{ mt: 0.5 }}>
          {decision.comment}
        </Typography>
      ) : null}
    </Box>
  )
}

export function ApplicationDetails({ application }: { application: Application }) {
  const { personalDetails, forexCardDetails, addressDetails, travelDetails, approvalHistory } = application
  return (
    <Stack spacing={2}>
      <Section title="Personal details">
        <Field label="First name" value={personalDetails.firstName} />
        <Field label="Last name" value={personalDetails.lastName} />
        <Field label="Date of birth" value={formatDate(personalDetails.dateOfBirth)} />
        <Field label="Gender" value={titleCaseToken(personalDetails.gender)} />
        <Field label="PAN" value={personalDetails.panNumber} />
        <Field label="Email" value={personalDetails.email} />
        <Field label="Mobile" value={personalDetails.mobile} />
      </Section>
      <Section title="Forex card">
        <Field label="Card type" value={titleCaseToken(forexCardDetails.cardType)} />
        <Field label="Amount" value={formatAmount(forexCardDetails.amount, forexCardDetails.currency)} />
        <Field label="Purpose" value={titleCaseToken(forexCardDetails.purpose)} />
      </Section>
      <Section title="Address">
        <Field label="Address" value={addressDetails.addressLine1} />
        <Field label="City" value={addressDetails.city} />
        <Field label="State" value={addressDetails.state} />
        <Field label="Country" value={addressDetails.country} />
        <Field label="Pincode" value={addressDetails.pincode} />
      </Section>
      <Section title="Travel">
        <Field label="Destination" value={travelDetails.destinationCountry} />
        <Field label="Departure" value={formatDate(travelDetails.departureDate)} />
        <Field label="Return" value={formatDate(travelDetails.returnDate)} />
        <Field label="Purpose" value={titleCaseToken(travelDetails.purpose)} />
      </Section>
      <Section title="Approval">
        <Decision title="Approver 1" decision={approvalHistory.approver1} />
        <Decision title="Approver 2" decision={approvalHistory.approver2} />
      </Section>
    </Stack>
  )
}
