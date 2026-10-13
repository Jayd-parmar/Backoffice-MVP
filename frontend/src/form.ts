import type { Application, ApplicationRequest } from './types'

export type FormValues = {
  personalDetails: {
    firstName: string
    lastName: string
    dateOfBirth: string
    gender: string
    panNumber: string
    email: string
    mobile: string
  }
  forexCardDetails: {
    cardType: string
    currency: string
    amount: string
    purpose: string
  }
  addressDetails: {
    addressLine1: string
    city: string
    state: string
    country: string
    pincode: string
  }
  travelDetails: {
    destinationCountry: string
    departureDate: string
    returnDate: string
    purpose: string
  }
}

export const GENDERS = ['MALE', 'FEMALE', 'OTHER'] as const
export const CARD_TYPES = ['MULTI_CURRENCY', 'SINGLE_CURRENCY'] as const
export const CURRENCIES = ['USD', 'EUR', 'GBP', 'AED', 'SGD', 'AUD', 'CAD', 'JPY'] as const
export const FOREX_PURPOSES = ['TRAVEL', 'BUSINESS', 'EDUCATION', 'MEDICAL'] as const
export const TRAVEL_PURPOSES = ['BUSINESS', 'LEISURE', 'EDUCATION', 'MEDICAL', 'TOURISM'] as const

export function emptyForm(email = ''): FormValues {
  return {
    personalDetails: {
      firstName: '',
      lastName: '',
      dateOfBirth: '',
      gender: '',
      panNumber: '',
      email,
      mobile: '',
    },
    forexCardDetails: {
      cardType: '',
      currency: '',
      amount: '',
      purpose: '',
    },
    addressDetails: {
      addressLine1: '',
      city: '',
      state: '',
      country: '',
      pincode: '',
    },
    travelDetails: {
      destinationCountry: '',
      departureDate: '',
      returnDate: '',
      purpose: '',
    },
  }
}

export function applicationToForm(application: Application): FormValues {
  return {
    personalDetails: { ...application.personalDetails },
    forexCardDetails: {
      cardType: application.forexCardDetails.cardType,
      currency: application.forexCardDetails.currency,
      amount: Number.isFinite(application.forexCardDetails.amount)
        ? String(application.forexCardDetails.amount)
        : '',
      purpose: application.forexCardDetails.purpose,
    },
    addressDetails: { ...application.addressDetails },
    travelDetails: { ...application.travelDetails },
  }
}

function todayIso(): string {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

function parseAmount(value: string): number | null {
  const trimmed = value.trim()
  if (!/^\d+(\.\d{1,2})?$/.test(trimmed)) return null
  const amount = Number(trimmed)
  if (!Number.isFinite(amount) || amount <= 0) return null
  return amount
}

export function validateForm(values: FormValues): Record<string, string> {
  const errors: Record<string, string> = {}
  const personal = values.personalDetails
  const forex = values.forexCardDetails
  const address = values.addressDetails
  const travel = values.travelDetails

  if (!personal.firstName.trim()) errors['personalDetails.firstName'] = 'First name is required'
  else if (personal.firstName.trim().length > 80) errors['personalDetails.firstName'] = 'First name must be at most 80 characters'
  if (!personal.lastName.trim()) errors['personalDetails.lastName'] = 'Last name is required'
  else if (personal.lastName.trim().length > 80) errors['personalDetails.lastName'] = 'Last name must be at most 80 characters'
  if (!personal.dateOfBirth) errors['personalDetails.dateOfBirth'] = 'Date of birth is required'
  else if (personal.dateOfBirth >= todayIso()) errors['personalDetails.dateOfBirth'] = 'Date of birth must be in the past'
  if (!personal.gender.trim()) errors['personalDetails.gender'] = 'Gender is required'
  if (!/^[A-Za-z]{5}[0-9]{4}[A-Za-z]$/.test(personal.panNumber.trim())) {
    errors['personalDetails.panNumber'] = 'PAN number is invalid'
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(personal.email.trim())) {
    errors['personalDetails.email'] = 'Email is invalid'
  }
  if (!/^[0-9]{10}$/.test(personal.mobile.trim())) {
    errors['personalDetails.mobile'] = 'Mobile must be 10 digits'
  }

  if (!forex.cardType.trim()) errors['forexCardDetails.cardType'] = 'Card type is required'
  if (!/^[A-Za-z]{3}$/.test(forex.currency.trim())) {
    errors['forexCardDetails.currency'] = 'Currency must be a 3-letter code'
  }
  if (!forex.amount.trim()) errors['forexCardDetails.amount'] = 'Amount is required'
  else if (parseAmount(forex.amount) == null) errors['forexCardDetails.amount'] = 'Amount must be a positive number'
  if (!forex.purpose.trim()) errors['forexCardDetails.purpose'] = 'Purpose is required'

  if (!address.addressLine1.trim()) errors['addressDetails.addressLine1'] = 'Address line 1 is required'
  else if (address.addressLine1.trim().length > 200) {
    errors['addressDetails.addressLine1'] = 'Address line 1 must be at most 200 characters'
  }
  if (!address.city.trim()) errors['addressDetails.city'] = 'City is required'
  if (!address.state.trim()) errors['addressDetails.state'] = 'State is required'
  if (!address.country.trim()) errors['addressDetails.country'] = 'Country is required'
  if (!address.pincode.trim()) errors['addressDetails.pincode'] = 'Pincode is required'
  else if (address.pincode.trim().length > 12) errors['addressDetails.pincode'] = 'Pincode must be at most 12 characters'

  if (!travel.destinationCountry.trim()) {
    errors['travelDetails.destinationCountry'] = 'Destination country is required'
  }
  if (!travel.departureDate) errors['travelDetails.departureDate'] = 'Departure date is required'
  if (!travel.returnDate) errors['travelDetails.returnDate'] = 'Return date is required'
  else if (travel.departureDate && travel.returnDate < travel.departureDate) {
    errors['travelDetails.returnDate'] = 'Return date must be on or after departure date'
  }
  if (!travel.purpose.trim()) errors['travelDetails.purpose'] = 'Purpose is required'

  return errors
}

export function toRequest(values: FormValues): ApplicationRequest {
  const amount = parseAmount(values.forexCardDetails.amount)
  return {
    personalDetails: {
      firstName: values.personalDetails.firstName.trim(),
      lastName: values.personalDetails.lastName.trim(),
      dateOfBirth: values.personalDetails.dateOfBirth,
      gender: values.personalDetails.gender.trim(),
      panNumber: values.personalDetails.panNumber.trim(),
      email: values.personalDetails.email.trim(),
      mobile: values.personalDetails.mobile.trim(),
    },
    forexCardDetails: {
      cardType: values.forexCardDetails.cardType.trim(),
      currency: values.forexCardDetails.currency.trim(),
      amount: amount ?? 0,
      purpose: values.forexCardDetails.purpose.trim(),
    },
    addressDetails: {
      addressLine1: values.addressDetails.addressLine1.trim(),
      city: values.addressDetails.city.trim(),
      state: values.addressDetails.state.trim(),
      country: values.addressDetails.country.trim(),
      pincode: values.addressDetails.pincode.trim(),
    },
    travelDetails: {
      destinationCountry: values.travelDetails.destinationCountry.trim(),
      departureDate: values.travelDetails.departureDate,
      returnDate: values.travelDetails.returnDate,
      purpose: values.travelDetails.purpose.trim(),
    },
  }
}

export function customerDestination(_status: Application['status'], id: string): string {
  return `/applications/${id}`
}
