import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { useTheme } from '@/contexts/theme-context'
import { updateCurrentUser } from '@/services/auth-service'
import {
  getCurrentProfile,
  updateCurrentProfile,
} from '@/services/profile-service'
import type { ProfileResponse } from '@/types/profile'
import { getErrorMessage } from '@/utils'

type AccountFormState = {
  username: string
  email: string
  phoneNumber: string
}

type ProfileFormState = {
  lastName: string
  firstName: string
  avatarUrl: string
  gender: ProfileResponse['gender']
  dateOfBirth: string
}

type ProfileTextFieldKey = keyof Omit<ProfileFormState, 'gender'>

const initialProfileForm: ProfileFormState = {
  lastName: '',
  firstName: '',
  avatarUrl: '',
  gender: 'OTHER',
  dateOfBirth: '',
}

export const adminSettingsGenderOptions = ['MALE', 'FEMALE', 'OTHER'] as const

export function useAdminSettingsPage() {
  const { user, logout, refreshUser } = useAuth()
  const { language, t, formatDate } = useLanguage()
  const { theme, toggleTheme } = useTheme()
  const isVietnamese = language === 'vi'
  const [profile, setProfile] = useState<ProfileResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSavingAccount, setIsSavingAccount] = useState(false)
  const [isSavingProfile, setIsSavingProfile] = useState(false)
  const [accountForm, setAccountForm] = useState<AccountFormState>({
    username: user?.username ?? '',
    email: user?.email ?? '',
    phoneNumber: user?.phoneNumber ?? '',
  })
  const [profileForm, setProfileForm] = useState<ProfileFormState>(
    initialProfileForm,
  )

  const labels = useMemo(
    () => ({
      title: isVietnamese ? 'Cai dat tai khoan quan tri' : 'Admin account settings',
      description: isVietnamese
        ? 'Xem thong tin tai khoan, cap nhat profile va tuy chinh khong gian lam viec quan tri.'
        : 'Review account details, update your profile, and adjust your admin workspace preferences.',
      overview: isVietnamese ? 'Tong quan tai khoan' : 'Account overview',
      preferences: isVietnamese ? 'Tuy chon giao dien' : 'Workspace preferences',
      role: isVietnamese ? 'Vai tro' : 'Role',
      status: isVietnamese ? 'Trang thai' : 'Status',
      active: isVietnamese ? 'Dang hoat dong' : 'Active',
      inactive: isVietnamese ? 'Khong hoat dong' : 'Inactive',
      accountCreated: isVietnamese ? 'Ngay tao tai khoan' : 'Account created',
      accountUpdated: isVietnamese ? 'Cap nhat gan nhat' : 'Last updated',
      theme: isVietnamese ? 'Che do sang toi' : 'Light and dark mode',
      themeDescription: isVietnamese
        ? 'Chuyen giao dien admin ma khong can roi khoi bang dieu khien.'
        : 'Switch the admin interface theme without leaving the dashboard.',
      language: isVietnamese ? 'Ngon ngu hien thi' : 'Display language',
      languageDescription: isVietnamese
        ? 'Ap dung ngay cho toan bo giao dien quan tri.'
        : 'Applies immediately across the admin interface.',
      accountSaved: isVietnamese
        ? 'Da cap nhat thong tin tai khoan'
        : 'Account information updated',
      profileSaved: isVietnamese ? 'Da cap nhat profile' : 'Profile updated',
      profileLoadError: isVietnamese
        ? 'Khong tai duoc thong tin profile'
        : 'Unable to load profile information',
      logout: t('auth.profile.logout'),
    }),
    [isVietnamese, t],
  )

  useEffect(() => {
    if (!user) {
      return
    }

    setAccountForm({
      username: user.username,
      email: user.email,
      phoneNumber: user.phoneNumber,
    })
  }, [user])

  useEffect(() => {
    let isCancelled = false

    async function loadProfile() {
      try {
        const response = await getCurrentProfile()

        if (isCancelled) {
          return
        }

        setProfile(response)
        setProfileForm({
          lastName: response.lastName,
          firstName: response.firstName,
          avatarUrl: response.avatarUrl ?? '',
          gender: response.gender,
          dateOfBirth: response.dateOfBirth,
        })
      } catch (error) {
        if (!isCancelled) {
          toast.error(getErrorMessage(error, labels.profileLoadError))
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadProfile()

    return () => {
      isCancelled = true
    }
  }, [labels.profileLoadError])

  function handleAccountChange<K extends keyof AccountFormState>(
    key: K,
    value: AccountFormState[K],
  ) {
    setAccountForm((currentForm) => ({
      ...currentForm,
      [key]: value,
    }))
  }

  function handleProfileChange<K extends ProfileTextFieldKey>(
    key: K,
    value: ProfileFormState[K],
  ) {
    setProfileForm((currentForm) => ({
      ...currentForm,
      [key]: value,
    }))
  }

  function handleProfileGenderChange(value: ProfileResponse['gender']) {
    setProfileForm((currentForm) => ({
      ...currentForm,
      gender: value,
    }))
  }

  async function handleLogout() {
    await logout()
  }

  async function handleSaveAccount(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSavingAccount(true)

    try {
      await updateCurrentUser({
        username: accountForm.username.trim(),
        email: accountForm.email.trim(),
        phoneNumber: accountForm.phoneNumber.trim(),
      })
      await refreshUser()
      toast.success(labels.accountSaved)
    } catch (error) {
      toast.error(getErrorMessage(error, t('checkout.error')))
    } finally {
      setIsSavingAccount(false)
    }
  }

  async function handleSaveProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSavingProfile(true)

    try {
      const response = await updateCurrentProfile({
        lastName: profileForm.lastName.trim(),
        firstName: profileForm.firstName.trim(),
        avatarUrl: profileForm.avatarUrl.trim() || null,
        gender: profileForm.gender,
        dateOfBirth: profileForm.dateOfBirth,
      })

      setProfile(response)
      await refreshUser()
      toast.success(labels.profileSaved)
    } catch (error) {
      toast.error(getErrorMessage(error, t('checkout.error')))
    } finally {
      setIsSavingProfile(false)
    }
  }

  return {
    user,
    profile,
    theme,
    t,
    formatDate,
    labels,
    isLoading,
    isSavingAccount,
    isSavingProfile,
    accountForm,
    profileForm,
    toggleTheme,
    handleAccountChange,
    handleProfileChange,
    handleProfileGenderChange,
    handleLogout,
    handleSaveAccount,
    handleSaveProfile,
  }
}
