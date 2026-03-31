"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "../../components/auth/AuthProvider";

export default function LoginPage() {
  const { status, login, error } = useAuth();
  const router = useRouter();
  const [nextPath, setNextPath] = useState("/home");

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (typeof window !== "undefined") {
      const params = new URLSearchParams(window.location.search);
      const next = params.get("next");
      if (next) {
        setNextPath(next);
      }
    }
  }, []);

  useEffect(() => {
    if (status === "authenticated") {
      router.replace(nextPath);
    }
  }, [status, nextPath, router]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setFormError(null);
    setSubmitting(true);
    try {
      await login(email.trim(), password);
    } catch {
      setFormError("Unable to sign in. Check your credentials and try again.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-brand">
          <div className="auth-logo">PhoenixTask®</div>
          <p className="auth-subtitle">Sign in to access your workspace.</p>
        </div>

        {error ? <div className="auth-alert">{error}</div> : null}
        {formError ? <div className="auth-alert auth-alert-error">{formError}</div> : null}

        <form className="form" onSubmit={handleSubmit}>
          <label className="form-field">
            <span>Email</span>
            <input
              type="email"
              name="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              required
            />
          </label>

          <label className="form-field">
            <span>Password</span>
            <input
              type="password"
              name="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
            />
          </label>

          <div className="form-actions">
            <button type="submit" className="button-primary" disabled={submitting}>
              {submitting ? "Signing in..." : "Sign in"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
