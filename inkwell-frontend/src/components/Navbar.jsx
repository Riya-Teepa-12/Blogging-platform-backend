import { useState } from "react";
import { Link, NavLink } from "react-router-dom";
import { Menu, X } from "lucide-react";
import { useAuth } from "../context/AuthContext.jsx";

function Navbar() {
  const [open, setOpen] = useState(false);
  const { isAuthenticated, role, user, logout } = useAuth();
  const normalizedRole = role ? String(role).toUpperCase() : "";
  const canAccessAuthor = normalizedRole === "AUTHOR" || normalizedRole === "ADMIN";
  const canAccessAdmin = normalizedRole === "ADMIN";

  const links = [
    { label: "Home", to: "/" },
    { label: "Feed", to: "/feed" },
  ];

  if (isAuthenticated) {
    links.push({ label: "Newsletter", to: "/newsletter" });
    links.push({ label: "Notifications", to: "/notifications" });
    links.push({ label: "Profile", to: "/profile" });
    if (canAccessAuthor) {
      links.push({ label: "Author", to: "/author" });
    }
    if (canAccessAdmin) {
      links.push({ label: "Admin", to: "/admin" });
    }
  }

  return (
    <header className="fixed left-0 right-0 top-0 z-50">
      <div className="container-shell mt-4">
        <nav className="glass edge-glow rounded-2xl px-4 py-3 md:px-6">
          <div className="flex items-center justify-between gap-4">
            <Link to="/" className="hero-title text-sm tracking-[0.16em] md:text-base flex items-center gap-2">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" className="w-5 h-5">
                <ellipse cx="12" cy="16" rx="6" ry="4"/>
                <path d="M6 16c0-2 2-4 6-4s6 2 6 4"/>
                <rect x="10" y="8" width="4" height="8" rx="2"/>
                <circle cx="12" cy="12" r="1" fill="currentColor"/>
              </svg>
              INKWELL
            </Link>

            <div className="hidden items-center gap-2 md:flex">
              {links.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  className={({ isActive }) =>
                    `rounded-full px-3 py-2 text-xs transition ${
                      isActive
                        ? "bg-white/12 text-white"
                        : "text-[var(--muted)] hover:text-white"
                    }`
                  }
                >
                  {link.label}
                </NavLink>
              ))}
            </div>

            <div className="hidden items-center gap-2 md:flex">
              {isAuthenticated ? (
                <>
                  <span className="rounded-full border border-white/15 bg-white/5 px-3 py-2 text-xs text-[var(--muted)]">
                    {user?.fullName || user?.username || "User"}
                  </span>
                  <button
                    onClick={logout}
                    className="btn-ghost rounded-full px-4 py-2 text-xs"
                  >
                    Logout
                  </button>
                </>
              ) : (
                <>
                  <Link to="/login" className="btn-ghost rounded-full px-4 py-2 text-xs">
                    Login
                  </Link>
                  <Link to="/signup" className="btn-primary rounded-full px-4 py-2 text-xs">
                    Sign up
                  </Link>
                </>
              )}
            </div>

            <button
              onClick={() => setOpen((v) => !v)}
              className="btn-ghost rounded-full p-2 md:hidden"
              aria-label="Toggle navigation"
            >
              {open ? <X size={18} /> : <Menu size={18} />}
            </button>
          </div>

          {open && (
            <div className="fade-up mt-4 space-y-2 border-t border-white/10 pt-4 md:hidden">
              {links.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  onClick={() => setOpen(false)}
                  className="block rounded-xl px-3 py-2 text-sm text-[var(--muted)] hover:bg-white/10 hover:text-white"
                >
                  {link.label}
                </NavLink>
              ))}
              {isAuthenticated ? (
                <button
                  onClick={() => {
                    logout();
                    setOpen(false);
                  }}
                  className="btn-ghost w-full rounded-xl px-3 py-2 text-center text-sm"
                >
                  Logout
                </button>
              ) : (
                <div className="grid grid-cols-2 gap-2 pt-2">
                  <Link
                    to="/login"
                    onClick={() => setOpen(false)}
                    className="btn-ghost rounded-xl px-3 py-2 text-center text-sm"
                  >
                    Login
                  </Link>
                  <Link
                    to="/signup"
                    onClick={() => setOpen(false)}
                    className="btn-primary rounded-xl px-3 py-2 text-center text-sm"
                  >
                    Sign up
                  </Link>
                </div>
              )}
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}

export default Navbar;
