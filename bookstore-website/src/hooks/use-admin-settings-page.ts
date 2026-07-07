import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { useTheme } from '@/contexts/theme-context'
import { uploadManagedFile } from '@/services/file-service'
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
  avatarFileAssetId: string
  avatarUrl: string
  gender: ProfileResponse['gender']
  dateOfBirth: string
}

type ProfileTextFieldKey = keyof Omit<ProfileFormState, 'gender'>

const initialProfileForm: ProfileFormState = {
  lastName: '',
  firstName: '',
  avatarFileAssetId: '',
  avatarUrl: '',
  gender: 'OTHER',
  dateOfBirth: '',
}

export const adminSettingsGenderOptions = ['MALE', 'FEMALE', 'OTHER'] as const

export function useAdminSettingsPage() {
  const { user, logout, refreshUser } = useAuth()
  const { t, formatDate } = useLanguage()
  const { theme, toggleTheme } = useTheme()
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
      title: t('admin.settingsPage.title'),
      description: t('admin.settingsPage.description'),
      overview: t('admin.settingsPage.overview'),
      preferences: t('admin.settingsPage.preferences'),
      role: t('admin.settingsPage.role'),
      status: t('admin.settingsPage.status'),
      active: t('admin.settingsPage.active'),
      inactive: t('admin.settingsPage.inactive'),
      accountCreated: t('admin.settingsPage.accountCreated'),
      accountUpdated: t('admin.settingsPage.accountUpdated'),
      theme: t('admin.settingsPage.theme'),
      themeDescription: t('admin.settingsPage.themeDescription'),
      language: t('admin.settingsPage.language'),
      languageDescription: t('admin.settingsPage.languageDescription'),
      avatarLabel: t('admin.settingsPage.avatarLabel'),
      accountSaved: t('admin.settingsPage.accountSaved'),
      profileSaved: t('admin.settingsPage.profileSaved'),
      profileLoadError: t('admin.settingsPage.profileLoadError'),
      logout: t('auth.profile.logout'),
    }),
    [t],
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
          avatarFileAssetId: response.avatarFileAssetId ?? '',
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

  async function handleProfileAvatarFileChange(file: File | null) {
    if (!file) {
      return
    }

    try {
      const uploadedFile = await uploadManagedFile(file, {
        purpose: 'USER_AVATAR',
        visibility: 'PUBLIC',
      })
      setProfileForm((currentForm) => ({
        ...currentForm,
        avatarFileAssetId: uploadedFile.id,
        avatarUrl: uploadedFile.publicUrl ?? URL.createObjectURL(file),
      }))
    } catch (error) {
      toast.error(getErrorMessage(error, t('checkout.error')))
    }
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
        avatarFileAssetId: profileForm.avatarFileAssetId.trim() || null,
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
    handleProfileAvatarFileChange,
    handleProfileGenderChange,
    handleLogout,
    handleSaveAccount,
    handleSaveProfile,
  }
}
