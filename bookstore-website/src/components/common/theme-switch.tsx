import { cn } from '@/utils'

type ThemeSwitchProps = {
  checked: boolean
  label: string
  onToggle: () => void
  className?: string
}

const darkGlowBackground =
  'radial-gradient(circle at 21% 46%, rgba(93, 230, 255, 0.95) 0px, transparent 34%), radial-gradient(circle at 23% 25%, rgba(255, 135, 137, 0.9) 0px, transparent 30%), radial-gradient(circle at 20% 1%, rgba(194, 144, 255, 0.88) 0px, transparent 34%), radial-gradient(circle at 86% 87%, rgba(126, 187, 255, 0.92) 0px, transparent 34%), radial-gradient(circle at 99% 41%, rgba(150, 247, 230, 0.94) 0px, transparent 30%), radial-gradient(circle at 55% 24%, rgba(142, 225, 132, 0.92) 0px, transparent 34%)'

const lightGlowBackground =
  'radial-gradient(circle at 18% 40%, rgba(255, 244, 148, 0.98) 0px, transparent 36%), radial-gradient(circle at 34% 18%, rgba(255, 214, 102, 0.96) 0px, transparent 34%), radial-gradient(circle at 58% 24%, rgba(255, 168, 76, 0.88) 0px, transparent 30%), radial-gradient(circle at 84% 36%, rgba(255, 228, 130, 0.94) 0px, transparent 34%), radial-gradient(circle at 72% 78%, rgba(255, 187, 64, 0.82) 0px, transparent 28%), radial-gradient(circle at 42% 74%, rgba(255, 208, 88, 0.9) 0px, transparent 32%)'

const knobBackground =
  'radial-gradient(circle at 81% 39%, rgba(255, 180, 217, 0.95) 0px, transparent 45%), radial-gradient(circle at 11% 72%, rgba(201, 173, 255, 0.92) 0px, transparent 45%), radial-gradient(circle at 23% 20%, rgba(223, 255, 121, 0.95) 0px, transparent 42%)'

export function ThemeSwitch({
  checked,
  label,
  onToggle,
  className,
}: ThemeSwitchProps) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={label}
      title={label}
      onClick={onToggle}
      className={cn(
        'group relative inline-flex h-8 w-14 shrink-0 items-center rounded-full focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 focus-visible:ring-offset-2 focus-visible:ring-offset-background motion-reduce:animate-none [perspective:500px] [transform-style:preserve-3d]',
        'animate-theme-switch-float',
        className,
      )}
    >
      <span
        aria-hidden="true"
        className="absolute inset-0 -z-10 rounded-full blur-[18px]"
        style={{
          backgroundImage: checked ? darkGlowBackground : lightGlowBackground,
        }}
      />

      <span
        aria-hidden="true"
        className={cn(
          'absolute inset-0 rounded-full transition-colors duration-300',
          checked
            ? 'bg-slate-900 shadow-[inset_0_0_0_1px_rgba(255,255,255,0.08)]'
            : 'bg-white/85 shadow-[inset_0_1px_1px_rgba(255,255,255,0.45)]',
        )}
      />

      <span
        aria-hidden="true"
        className={cn(
          'absolute left-[0.3rem] top-[0.28rem] h-[1.4rem] w-[1.4rem] rounded-full transition-transform duration-300',
          'shadow-[inset_0_-10px_10px_rgba(0,0,0,0.17),0_-1px_15px_-8px_rgba(0,0,0,0.18)]',
          checked && 'translate-x-6',
        )}
        style={{ backgroundImage: knobBackground }}
      />
    </button>
  )
}
