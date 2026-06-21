// Timezone choices for the form's `timezone` select. Static reference data —
// kept out of the page component so index.vue stays focused on UI wiring.
export interface TimezoneOption {
  value: string
  label: string
}

export const timezones: TimezoneOption[] = [
  { value: 'UTC', label: 'UTC - Coordinated Universal Time' },
  { value: 'Europe/Berlin', label: 'Europe/Berlin (CET/CEST)' },
  { value: 'Europe/London', label: 'Europe/London (GMT/BST)' },
  { value: 'Europe/Paris', label: 'Europe/Paris (CET/CEST)' },
  { value: 'Europe/Amsterdam', label: 'Europe/Amsterdam (CET/CEST)' },
  { value: 'America/New_York', label: 'America/New_York (EST/EDT - US East Coast)' },
  { value: 'America/Chicago', label: 'America/Chicago (CST/CDT - US Central)' },
  { value: 'America/Denver', label: 'America/Denver (MST/MDT - US Mountain)' },
  { value: 'America/Los_Angeles', label: 'America/Los_Angeles (PST/PDT - US West Coast)' },
  { value: 'America/Toronto', label: 'America/Toronto (EST/EDT - Canada)' },
  { value: 'America/Sao_Paulo', label: 'America/Sao_Paulo (BRT - Brazil)' },
  { value: 'Asia/Tokyo', label: 'Asia/Tokyo (JST - Japan)' },
  { value: 'Asia/Shanghai', label: 'Asia/Shanghai (CST - China)' },
  { value: 'Asia/Hong_Kong', label: 'Asia/Hong_Kong (HKT)' },
  { value: 'Asia/Singapore', label: 'Asia/Singapore (SGT)' },
  { value: 'Asia/Dubai', label: 'Asia/Dubai (GST - UAE)' },
  { value: 'Asia/Kolkata', label: 'Asia/Kolkata (IST - India)' },
  { value: 'Australia/Sydney', label: 'Australia/Sydney (AEDT/AEST)' },
  { value: 'Pacific/Auckland', label: 'Pacific/Auckland (NZDT/NZST - New Zealand)' },
  { value: 'Africa/Johannesburg', label: 'Africa/Johannesburg (SAST - South Africa)' }
]
