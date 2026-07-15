import type { Metadata } from "next";
import { Inter, JetBrains_Mono } from "next/font/google";
import "./globals.css";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
});

const jetbrainsMono = JetBrains_Mono({
  subsets: ["latin"],
  variable: "--font-jetbrains-mono",
  display: "swap",
});

const baseUrl = "https://jump-droid.vercel.app";

export const metadata: Metadata = {
  metadataBase: new URL(baseUrl),
  title: {
    default: "Jump Droid — Free Android Arcade Game",
    template: "%s — Jump Droid",
  },
  description:
    "Jump Droid is a free, open-source Android arcade game. Pilot a droid-powered rocket through 8 hostile atmospheric zones. Face colossal bosses, build your fleet, and uncover the mystery of The Signal.",
  keywords: [
    "Jump Droid",
    "free Android game",
    "open source game",
    "arcade game",
    "Android game",
    "rocket game",
    "mobile game",
    "indie game",
    "Ashwath AI",
    "vertical ascent",
    "Kotlin",
    "Jetpack Compose",
  ],
  authors: [{ name: "Ashwath AI" }],
  creator: "Ashwath AI",
  publisher: "Ashwath AI",
  icons: {
    icon: "/icon.png",
    apple: "/icon.png",
  },
  openGraph: {
    type: "website",
    locale: "en_US",
    siteName: "Jump Droid",
    url: baseUrl,
    title: "Jump Droid — Free Android Arcade Game",
    description:
      "Jump Droid is a free, open-source Android arcade game. Pilot, survive, and discover.",
    images: [
      {
        url: "/icon.png",
        width: 256,
        height: 256,
        alt: "Jump Droid",
      },
    ],
  },
  twitter: {
    card: "summary",
    title: "Jump Droid — Free Android Arcade Game",
    description:
      "Jump Droid is a free, open-source Android arcade game. Pilot, survive, and discover.",
    images: ["/icon.png"],
  },
  robots: {
    index: true,
    follow: true,
  },
  alternates: {
    canonical: baseUrl,
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${inter.variable} ${jetbrainsMono.variable} h-full scroll-smooth antialiased`}
    >
      <head>
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "MobileApplication",
              name: "Jump Droid",
              applicationCategory: "GameApplication",
              operatingSystem: "Android",
              description:
                "Jump Droid is a free, open-source Android arcade game. Pilot a droid-powered rocket through 8 hostile atmospheric zones.",
              url: baseUrl,
              author: {
                "@type": "Organization",
                name: "Ashwath AI",
              },
              offers: {
                "@type": "Offer",
                price: "0",
                priceCurrency: "USD",
              },
            }),
          }}
        />
      </head>
      <body className="min-h-full bg-black text-white">
        <a
          href="#main-content"
          className="fixed -left-full top-0 z-[9999] rounded-br-lg bg-cyan-400 px-4 py-2 text-sm font-bold text-slate-950 transition-all focus:left-0"
        >
          Skip to content
        </a>
        {children}
      </body>
    </html>
  );
}
