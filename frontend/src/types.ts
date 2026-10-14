export type Role = 'CUSTOMER' | 'APPROVER_1' | 'APPROVER_2' | 'APPROVER_3'

export type ApplicationStatus =
  | 'DRAFT'
  | 'PENDING_APPROVAL'
  | 'PENDING_APPROVER_1'
  | 'PENDING_APPROVER_2'
  | 'APPROVED'
  | 'REJECTED'

export type ApprovalAction = 'APPROVED' | 'REJECTED'

export type DocumentCode = 'AADHAAR' | 'PAN' | 'PASSPORT' | 'VOTER_ID' | 'DRIVING_LICENSE'

export type PersonalDetails = {
  firstName: string
  lastName: string
  dateOfBirth: string
  gender: string
  panNumber: string
  aadhaarNumber: string
  passportNumber: string
  voterId: string
  drivingLicense: string
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

export type DocumentVerification = {
  code: DocumentCode
  label: string
  verified: boolean
}

export type VerificationGroup = {
  code: string
  label: string
  documents: DocumentVerification[]
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
    approver3: ApprovalDecision
  }
  verificationGroups: VerificationGroup[]
  currentGroupIndex: number
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
