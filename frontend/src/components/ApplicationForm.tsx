import { useState, type FormEvent, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import MenuItem from '@mui/material/MenuItem'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { ApiError, createApplication, updateApplication } from '../api'
import { useAuth } from '../useAuth'
import {
  CARD_TYPES,
  CURRENCIES,
  FOREX_PURPOSES,
  DESTINATIONS,
  GENDERS,
  TRAVEL_PURPOSES,
  toRequest,
  validateForm,
  type FormValues,
} from '../form'
import { rememberApplication } from '../session'
import { titleCaseToken } from '../format'

type SectionKey = 'personalDetails' | 'forexCardDetails' | 'addressDetails' | 'travelDetails'

function withCurrent(options: readonly string[], current: string): string[] {
  if (current && !options.includes(current)) return [current, ...options]
  return [...options]
}

function SectionCard({ title, children }: { title: string; children: ReactNode }) {
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

export function ApplicationForm({
  mode,
  initial,
  applicationId,
}: {
  mode: 'create' | 'edit'
  initial: FormValues
  applicationId?: string
}) {
  const navigate = useNavigate()
  const { session } = useAuth()
  const [values, setValues] = useState(initial)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [serverError, setServerError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  function update(section: SectionKey, key: string, value: string) {
    setValues((current) => ({
      ...current,
      [section]: { ...current[section], [key]: value },
    }))
    const field = `${section}.${key}`
    setErrors((current) => {
      if (!current[field]) return current
      const next = { ...current }
      delete next[field]
      return next
    })
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    const nextErrors = validateForm(values)
    setErrors(nextErrors)
    const firstInvalid = Object.keys(nextErrors)[0]
    if (firstInvalid) {
      document.getElementById(firstInvalid)?.focus()
      return
    }
    if (!session) return
    if (mode === 'edit' && !applicationId) {
      setServerError('This application is missing an id.')
      return
    }

    setSaving(true)
    setServerError(null)
    try {
      const body = toRequest(values)
      const saved =
        mode === 'create'
          ? await createApplication(body)
          : await updateApplication(applicationId as string, body)
      rememberApplication(session.userId, saved.id)
      navigate(`/applications/${saved.id}`, { replace: true })
    } catch (error) {
      setServerError(error instanceof ApiError ? error.message : 'Could not save the application.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <Stack component="form" spacing={2} onSubmit={onSubmit} noValidate sx={{ maxWidth: 880 }}>
      <Box>
        <Typography variant="h4">{mode === 'create' ? 'Forex card application' : 'Update application'}</Typography>
        <Typography color="text.secondary" sx={{ mt: 1 }}>
          {mode === 'create'
            ? 'Enter personal, card, address, and travel details. Saving stores a draft you can review before submitting.'
            : 'Update the saved details, then return to the review page.'}
        </Typography>
      </Box>
      {serverError ? <Alert severity="error">{serverError}</Alert> : null}

      <SectionCard title="Personal details">
        <TextField
          id="personalDetails.firstName"
          label="First name"
          value={values.personalDetails.firstName}
          onChange={(event) => update('personalDetails', 'firstName', event.target.value)}
          error={Boolean(errors['personalDetails.firstName'])}
          helperText={errors['personalDetails.firstName']}
          slotProps={{ htmlInput: { maxLength: 80 } }}
          fullWidth
        />
        <TextField
          id="personalDetails.lastName"
          label="Last name"
          value={values.personalDetails.lastName}
          onChange={(event) => update('personalDetails', 'lastName', event.target.value)}
          error={Boolean(errors['personalDetails.lastName'])}
          helperText={errors['personalDetails.lastName']}
          slotProps={{ htmlInput: { maxLength: 80 } }}
          fullWidth
        />
        <TextField
          id="personalDetails.dateOfBirth"
          label="Date of birth"
          type="date"
          value={values.personalDetails.dateOfBirth}
          onChange={(event) => update('personalDetails', 'dateOfBirth', event.target.value)}
          error={Boolean(errors['personalDetails.dateOfBirth'])}
          helperText={errors['personalDetails.dateOfBirth']}
          slotProps={{ inputLabel: { shrink: true } }}
          fullWidth
        />
        <TextField
          id="personalDetails.gender"
          select
          label="Gender"
          value={values.personalDetails.gender}
          onChange={(event) => update('personalDetails', 'gender', event.target.value)}
          error={Boolean(errors['personalDetails.gender'])}
          helperText={errors['personalDetails.gender']}
          fullWidth
        >
          <MenuItem value="">Select</MenuItem>
          {withCurrent(GENDERS, values.personalDetails.gender).map((gender) => (
            <MenuItem key={gender} value={gender}>
              {titleCaseToken(gender)}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          id="personalDetails.panNumber"
          label="PAN"
          value={values.personalDetails.panNumber}
          onChange={(event) => update('personalDetails', 'panNumber', event.target.value.toUpperCase())}
          error={Boolean(errors['personalDetails.panNumber'])}
          helperText={errors['personalDetails.panNumber'] ?? 'Example ABCDE1234F'}
          slotProps={{ htmlInput: { maxLength: 10 } }}
          fullWidth
        />
        <TextField
          id="personalDetails.aadhaarNumber"
          label="Aadhaar"
          value={values.personalDetails.aadhaarNumber}
          onChange={(event) => update('personalDetails', 'aadhaarNumber', event.target.value)}
          error={Boolean(errors['personalDetails.aadhaarNumber'])}
          helperText={errors['personalDetails.aadhaarNumber'] ?? 'Optional'}
          slotProps={{ htmlInput: { maxLength: 40 } }}
          fullWidth
        />
        <TextField
          id="personalDetails.passportNumber"
          label="Passport"
          value={values.personalDetails.passportNumber}
          onChange={(event) => update('personalDetails', 'passportNumber', event.target.value.toUpperCase())}
          error={Boolean(errors['personalDetails.passportNumber'])}
          helperText={errors['personalDetails.passportNumber'] ?? 'Optional'}
          slotProps={{ htmlInput: { maxLength: 40 } }}
          fullWidth
        />
        <TextField
          id="personalDetails.voterId"
          label="Voter ID"
          value={values.personalDetails.voterId}
          onChange={(event) => update('personalDetails', 'voterId', event.target.value.toUpperCase())}
          error={Boolean(errors['personalDetails.voterId'])}
          helperText={errors['personalDetails.voterId'] ?? 'Optional'}
          slotProps={{ htmlInput: { maxLength: 40 } }}
          fullWidth
        />
        <TextField
          id="personalDetails.drivingLicense"
          label="Driving licence"
          value={values.personalDetails.drivingLicense}
          onChange={(event) => update('personalDetails', 'drivingLicense', event.target.value.toUpperCase())}
          error={Boolean(errors['personalDetails.drivingLicense'])}
          helperText={errors['personalDetails.drivingLicense'] ?? 'Optional'}
          slotProps={{ htmlInput: { maxLength: 40 } }}
          fullWidth
        />
        <TextField
          id="personalDetails.email"
          label="Email"
          type="email"
          value={values.personalDetails.email}
          onChange={(event) => update('personalDetails', 'email', event.target.value)}
          error={Boolean(errors['personalDetails.email'])}
          helperText={errors['personalDetails.email']}
          fullWidth
        />
        <TextField
          id="personalDetails.mobile"
          label="Mobile"
          value={values.personalDetails.mobile}
          onChange={(event) => update('personalDetails', 'mobile', event.target.value)}
          error={Boolean(errors['personalDetails.mobile'])}
          helperText={errors['personalDetails.mobile'] ?? '10 digits'}
          slotProps={{ htmlInput: { maxLength: 10, inputMode: 'numeric' } }}
          fullWidth
        />
      </SectionCard>

      <SectionCard title="Forex card">
        <TextField
          id="forexCardDetails.cardType"
          select
          label="Card type"
          value={values.forexCardDetails.cardType}
          onChange={(event) => update('forexCardDetails', 'cardType', event.target.value)}
          error={Boolean(errors['forexCardDetails.cardType'])}
          helperText={errors['forexCardDetails.cardType']}
          fullWidth
        >
          <MenuItem value="">Select</MenuItem>
          {withCurrent(CARD_TYPES, values.forexCardDetails.cardType).map((cardType) => (
            <MenuItem key={cardType} value={cardType}>
              {titleCaseToken(cardType)}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          id="forexCardDetails.currency"
          select
          label="Currency"
          value={values.forexCardDetails.currency}
          onChange={(event) => update('forexCardDetails', 'currency', event.target.value)}
          error={Boolean(errors['forexCardDetails.currency'])}
          helperText={errors['forexCardDetails.currency']}
          fullWidth
        >
          <MenuItem value="">Select</MenuItem>
          {withCurrent(CURRENCIES, values.forexCardDetails.currency).map((currency) => (
            <MenuItem key={currency} value={currency}>
              {currency}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          id="forexCardDetails.amount"
          label="Amount"
          value={values.forexCardDetails.amount}
          onChange={(event) => update('forexCardDetails', 'amount', event.target.value)}
          error={Boolean(errors['forexCardDetails.amount'])}
          helperText={errors['forexCardDetails.amount']}
          slotProps={{ htmlInput: { inputMode: 'decimal' } }}
          fullWidth
        />
        <TextField
          id="forexCardDetails.purpose"
          select
          label="Purpose"
          value={values.forexCardDetails.purpose}
          onChange={(event) => update('forexCardDetails', 'purpose', event.target.value)}
          error={Boolean(errors['forexCardDetails.purpose'])}
          helperText={errors['forexCardDetails.purpose']}
          fullWidth
        >
          <MenuItem value="">Select</MenuItem>
          {withCurrent(FOREX_PURPOSES, values.forexCardDetails.purpose).map((purpose) => (
            <MenuItem key={purpose} value={purpose}>
              {titleCaseToken(purpose)}
            </MenuItem>
          ))}
        </TextField>
      </SectionCard>

      <SectionCard title="Address">
        <TextField
          id="addressDetails.addressLine1"
          label="Address line 1"
          value={values.addressDetails.addressLine1}
          onChange={(event) => update('addressDetails', 'addressLine1', event.target.value)}
          error={Boolean(errors['addressDetails.addressLine1'])}
          helperText={errors['addressDetails.addressLine1']}
          slotProps={{ htmlInput: { maxLength: 200 } }}
          sx={{ gridColumn: { sm: '1 / -1' } }}
          fullWidth
        />
        <TextField
          id="addressDetails.city"
          label="City"
          value={values.addressDetails.city}
          onChange={(event) => update('addressDetails', 'city', event.target.value)}
          error={Boolean(errors['addressDetails.city'])}
          helperText={errors['addressDetails.city']}
          fullWidth
        />
        <TextField
          id="addressDetails.state"
          label="State"
          value={values.addressDetails.state}
          onChange={(event) => update('addressDetails', 'state', event.target.value)}
          error={Boolean(errors['addressDetails.state'])}
          helperText={errors['addressDetails.state']}
          fullWidth
        />
        <TextField
          id="addressDetails.country"
          label="Country"
          value={values.addressDetails.country}
          onChange={(event) => update('addressDetails', 'country', event.target.value)}
          error={Boolean(errors['addressDetails.country'])}
          helperText={errors['addressDetails.country']}
          fullWidth
        />
        <TextField
          id="addressDetails.pincode"
          label="Pincode"
          value={values.addressDetails.pincode}
          onChange={(event) => update('addressDetails', 'pincode', event.target.value)}
          error={Boolean(errors['addressDetails.pincode'])}
          helperText={errors['addressDetails.pincode']}
          slotProps={{ htmlInput: { maxLength: 12 } }}
          fullWidth
        />
      </SectionCard>

      <SectionCard title="Travel">
        <TextField
          id="travelDetails.destinationCountry"
          select
          label="Destination country"
          value={values.travelDetails.destinationCountry}
          onChange={(event) => update('travelDetails', 'destinationCountry', event.target.value)}
          error={Boolean(errors['travelDetails.destinationCountry'])}
          helperText={errors['travelDetails.destinationCountry']}
          fullWidth
        >
          <MenuItem value="">Select</MenuItem>
          {DESTINATIONS.map((destination) => (
            <MenuItem key={destination.value} value={destination.value}>
              {destination.label}
            </MenuItem>
          ))}
          {values.travelDetails.destinationCountry &&
          !DESTINATIONS.some((destination) => destination.value === values.travelDetails.destinationCountry) ? (
            <MenuItem value={values.travelDetails.destinationCountry}>{values.travelDetails.destinationCountry}</MenuItem>
          ) : null}
        </TextField>
        <TextField
          id="travelDetails.purpose"
          select
          label="Purpose"
          value={values.travelDetails.purpose}
          onChange={(event) => update('travelDetails', 'purpose', event.target.value)}
          error={Boolean(errors['travelDetails.purpose'])}
          helperText={errors['travelDetails.purpose']}
          fullWidth
        >
          <MenuItem value="">Select</MenuItem>
          {withCurrent(TRAVEL_PURPOSES, values.travelDetails.purpose).map((purpose) => (
            <MenuItem key={purpose} value={purpose}>
              {titleCaseToken(purpose)}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          id="travelDetails.departureDate"
          label="Departure date"
          type="date"
          value={values.travelDetails.departureDate}
          onChange={(event) => update('travelDetails', 'departureDate', event.target.value)}
          error={Boolean(errors['travelDetails.departureDate'])}
          helperText={errors['travelDetails.departureDate']}
          slotProps={{ inputLabel: { shrink: true } }}
          fullWidth
        />
        <TextField
          id="travelDetails.returnDate"
          label="Return date"
          type="date"
          value={values.travelDetails.returnDate}
          onChange={(event) => update('travelDetails', 'returnDate', event.target.value)}
          error={Boolean(errors['travelDetails.returnDate'])}
          helperText={errors['travelDetails.returnDate']}
          slotProps={{ inputLabel: { shrink: true } }}
          fullWidth
        />
      </SectionCard>

      <Box>
        <Button type="submit" variant="contained" size="large" disabled={saving}>
          {saving ? 'Saving…' : mode === 'create' ? 'Save draft' : 'Save changes'}
        </Button>
      </Box>
    </Stack>
  )
}
