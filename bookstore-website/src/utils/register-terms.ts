export type TermsSection = {
  title: string
  paragraphs: string[]
}

export type RegisterTermsCopy = {
  badge: string
  agreementLabel: string
  linkLabel: string
  requiredMessage: string
  dialogTitle: string
  closeHint: string
  closeReady: string
  intro: string
  sections: TermsSection[]
}
