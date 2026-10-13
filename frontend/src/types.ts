export type Role = 'CUSTOMER' | 'APPROVER_1' | 'APPROVER_2'

export type ApplicationStatus =
  | 'DRAFT'
  | 'PENDING_APPROVER_1'
  | 'PENDING_APPROVER_2'
  | 'APPROVED'
  | 'REJECTED'

export type ApprovalAction = 'APPROVED' | 'REJECTED'

export type PersonalDetails = {
  firstName: string
  lastName: string
  dateOfBirth: string
  gender: string
  panNumber: string
  email: string
  mobile: string
}

export type ForexCardDetails = {
  cardType: string
  currency: string
  amount: number
  purpose: string
}

export type AddressDetails = {
  addressLine1: string
  city: string
  state: string
  country: string
  pincode: string
}

export type TravelDetails = {
  destinationCountry: string
  departureDate: string
  returnDate: string
  purpose: string
}

export type ApprovalDecision = {
  userId: string | null
  action: ApprovalAction | null
  comment: string | null
  actionAt: string | null
}

export type Application = {
  id: string
  customerId: string
  status: ApplicationStatus
  personalDetails: PersonalDetails
  forexCardDetails: ForexCardDetails
  addressDetails: AddressDetails
  travelDetails: TravelDetails
  approvalHistory: {
    approver1: ApprovalDecision
    approver2: ApprovalDecision
  }
  createdAt: string
  updatedAt: string
}

export type ApplicationRequest = {
  personalDetails: PersonalDetails
  forexCardDetails: ForexCardDetails
  addressDetails: AddressDetails
  travelDetails: TravelDetails
}

export type Session = {
  accessToken: string
  refreshToken: string
  userId: string
  role: Role
  email: string
}
