import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { useAuth } from "../context/AuthContext.jsx";

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const redirectPath = location.state?.from?.pathname || "/feed";

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      const response = await login(form);
      const role = response?.user?.role ? String(response.user.role).toUpperCase() : "";
      if (redirectPath === "/login" || redirectPath === "/signup") {
        if (role === "ADMIN") {
          navigate("/admin", { replace: true });
          return;
        }
        if (role === "AUTHOR") {
          navigate("/author", { replace: true });
          return;
        }
        navigate("/feed", { replace: true });
        return;
      }
      navigate(redirectPath, { replace: true });
    } catch (err) {
      setError(err.message || "Login failed");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-10">
      <motion.div
        initial={{ opacity: 0, y: 28 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.45 }}
        className="glass edge-glow w-full max-w-md rounded-4xl p-6 md:p-8"
      >
        <h1 className="hero-title text-2xl">Welcome Back</h1>
        <p className="mt-2 text-sm text-[var(--muted)]">
          Continue to your reader, author, or admin workspace.
        </p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="mb-1 block text-xs uppercase tracking-[0.08em] text-[var(--muted)]">
              Email
            </label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              placeholder="you@inkwell.app"
              required
              className="w-full rounded-2xl border border-white/15 bg-white/5 px-4 py-3 text-sm outline-none focus:border-cyan-300/70"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs uppercase tracking-[0.08em] text-[var(--muted)]">
              Password
            </label>
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              placeholder="********"
              required
              className="w-full rounded-2xl border border-white/15 bg-white/5 px-4 py-3 text-sm outline-none focus:border-cyan-300/70"
            />
          </div>
          {error && (
            <p className="rounded-xl border border-red-300/30 bg-red-400/10 px-3 py-2 text-sm text-red-200">
              {error}
            </p>
          )}
          <button
            type="submit"
            disabled={submitting}
            className="btn-primary w-full rounded-2xl py-3 text-sm disabled:cursor-not-allowed disabled:opacity-60"
          >
            {submitting ? "Logging in..." : "Login"}
          </button>
          <button type="button" className="btn-ghost w-full rounded-2xl py-3 text-sm" disabled>
            Continue with Google
          </button>
          <button type="button" className="btn-ghost w-full rounded-2xl py-3 text-sm" disabled>
            Continue with GitHub
          </button>
        </form>

        <p className="mt-5 text-center text-sm text-[var(--muted)]">
          New to InkWell?{" "}
          <Link to="/signup" className="text-cyan-200 hover:text-white">
            Create account
          </Link>
        </p>
      </motion.div>
    </div>
  );
}

export default LoginPage;
