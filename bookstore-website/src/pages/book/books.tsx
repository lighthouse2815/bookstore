import { Header } from '@/components/layout/header'
import { Footer } from '@/components/layout/footer'
import { BookListing } from '@/components/book/book-listing'

export default function BooksPage() {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-8 sm:px-6 lg:px-8">
        <BookListing />
      </main>
      <Footer />
    </div>
  )
}
