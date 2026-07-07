import { EbookListing } from '@/components/book/ebook-listing'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'

export default function EbooksPage() {
  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="mx-auto w-full max-w-[1380px] flex-1 px-4 py-8 sm:px-6 lg:px-8">
        <EbookListing />
      </main>
      <Footer />
    </div>
  )
}
