;(() => {
  const storageKey = 'bookstore-theme'

  try {
    const storedTheme = window.localStorage.getItem(storageKey)
    const theme = storedTheme === 'dark' ? 'dark' : 'light'

    document.documentElement.classList.toggle('dark', theme === 'dark')
  } catch {
    document.documentElement.classList.remove('dark')
  }
})()
