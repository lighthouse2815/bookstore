import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { updateCurrentUser } from '@/services/auth-service'
import { getMyOrders } from '@/services/order-service'
import {
  getCurrentProfile,
  updateCurrentProfile,
} from '@/services/profile-service'
import type { OrderResponse } from '@/types/order'
import type { ProfileResponse } from '@/types/profile'
import {
  compressAvatarFile,
  getAvatarFileErrorMessage,
} from '@/utils/avatar-image'
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

export function useProfilePage() {
  const { user, logout, refreshUser } = useAuth()
  const { t, language } = useLanguage()
  const isVietnamese = language === 'vi'
  const navigate = useNavigate()
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [profile, setProfile] = useState<ProfileResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSavingAccount, setIsSavingAccount] = useState(false)
  const [isSavingProfile, setIsSavingProfile] = useState(false)
  const [accountForm, setAccountForm] = useState<AccountFormState>({
    username: user?.username ?? '',
    email: user?.email ?? '',
    phoneNumber: user?.phoneNumber ?? '',
  })
  const [profileForm, setProfileForm] = useState<ProfileFormState>({
    lastName: '',
    firstName: '',
    avatarUrl: '',
    gender: 'OTHER',
    dateOfBirth: '',
  })

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

    async function loadProfileData() {
      try {
        const [profileResponse, ordersResponse] = await Promise.all([
          getCurrentProfile(),
          getMyOrders(),
        ])

        if (isCancelled) {
          return
        }

        setProfile(profileResponse)
        setProfileForm({
          lastName: profileResponse.lastName,
          firstName: profileResponse.firstName,
          avatarUrl: profileResponse.avatarUrl ?? '',
          gender: profileResponse.gender,
          dateOfBirth: profileResponse.dateOfBirth,
        })
        setOrders(ordersResponse)
      } catch (error) {
        if (!isCancelled) {
          toast.error(getErrorMessage(error, t('checkout.error')))
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadProfileData()

    return () => {
      isCancelled = true
    }
  }, [t])

  function handleAccountChange<K extends keyof AccountFormState>(
    key: K,
    value: AccountFormState[K],
  ) {
    setAccountForm((currentForm) => ({
      ...currentForm,
      [key]: value,
    }))
  }

  function handleProfileInputChange<K extends keyof Omit<ProfileFormState, 'gender'>>(
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
      const avatarUrl = await compressAvatarFile(file)
      setProfileForm((currentForm) => ({
        ...currentForm,
        avatarUrl,
      }))
    } catch (error) {
      toast.error(
        getAvatarFileErrorMessage(error, isVietnamese, t('checkout.error')),
      )
    }
  }

  async function handleLogout() {
    await logout()
    navigate('/')
  }

  async function handleSaveAccount(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSavingAccount(true)

    try {
      await updateCurrentUser(accountForm)
      await refreshUser()
      toast.success(t('auth.profile.accountUpdated'))
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
        lastName: profileForm.lastName,
        firstName: profileForm.firstName,
        avatarUrl: profileForm.avatarUrl || null,
        gender: profileForm.gender,
        dateOfBirth: profileForm.dateOfBirth,
      })
      setProfile(response)
      await refreshUser()
      toast.success(t('auth.profile.profileUpdated'))
    } catch (error) {
      toast.error(getErrorMessage(error, t('checkout.error')))
    } finally {
      setIsSavingProfile(false)
    }
  }

  return {
    user,
    orders,
    profile,
    isLoading,
    isSavingAccount,
    isSavingProfile,
    accountForm,
    profileForm,
    handleAccountChange,
    handleProfileInputChange,
    handleProfileAvatarFileChange,
    handleProfileGenderChange,
    handleLogout,
    handleSaveAccount,
    handleSaveProfile,
    avatarLabel: isVietnamese ? 'Ảnh đại diện' : 'Avatar image',
  }
}
