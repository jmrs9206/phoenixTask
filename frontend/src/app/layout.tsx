import type { ReactNode } from "react";
import { Sora } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "../components/auth/AuthProvider";

const sora = Sora({ subsets: ["latin"], weight: ["400", "500", "600"] });

export const metadata = {
  title: "PhoenixTask®",
  description: "PhoenixTask® frontend base"
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en" className={sora.className}>
      <body>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
