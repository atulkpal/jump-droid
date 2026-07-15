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
    default: "Jump Droid — The Signal",
    template: "%s — Jump Droid",
  },
  description:
    "A mysterious transmission from the upper atmosphere. Jump Droid is a free, open-source Android arcade game built by Ashwath AI.",
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
  ],
  authors: [{ name: "Ashwath AI" }],
  creator: "Ashwath AI",
  publisher: "Ashwath AI",
  manifest: "/manifest.webmanifest",
  icons: {
    icon: [
      { url: "/icon.png", sizes: "256x256", type: "image/png" },
      { url: "/favicon.ico", sizes: "any" },
    ],
    apple: "/apple-touch-icon.png",
  },
  openGraph: {
    type: "website",
    locale: "en_US",
    siteName: "Jump Droid",
    url: baseUrl,
    title: "Jump Droid — The Signal",
    description:
      "A mysterious transmission from the upper atmosphere. A free, open-source Android arcade game.",
    images: [
      {
        url: "/og-image.png",
        width: 1200,
        height: 630,
        alt: "Jump Droid",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "Jump Droid — The Signal",
    description:
      "A mysterious transmission from the upper atmosphere. A free, open-source Android arcade game.",
    images: ["/twitter-image.png"],
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
                "A mysterious transmission from the upper atmosphere. A free, open-source Android arcade game.",
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
