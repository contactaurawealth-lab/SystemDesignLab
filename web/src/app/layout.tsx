import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "System Design Lab — An Intelligent Laboratory for System Architecture",
  description:
    "An offline-first interactive learning environment for mastering system design through lessons, 3D simulations, failure labs, and practice. Based on karanpratapsingh/system-design.",
  keywords: [
    "system design",
    "distributed systems",
    "software architecture",
    "3d simulation",
    "offline learning",
    "system design lab",
  ],
  authors: [{ name: "System Design Lab Team" }],
  viewport: "width=device-width, initial-scale=1, maximum-scale=5",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="scroll-smooth">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link
          rel="preconnect"
          href="https://fonts.gstatic.com"
          crossOrigin="anonymous"
        />
        <link
          href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap"
          rel="stylesheet"
        />
      </head>
      <body className="bg-white text-ink antialiased selection:bg-slate-200">
        {children}
      </body>
    </html>
  );
}
