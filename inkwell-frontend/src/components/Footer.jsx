import { Link } from "react-router-dom";

function Footer() {
  return (
    <footer className="border-t border-white/10 py-10">
      <div className="container-shell grid gap-8 md:grid-cols-4">
        <div>
          <h3 className="hero-title text-sm tracking-[0.14em]">INKWELL</h3>
          <p className="mt-3 text-sm text-[var(--muted)]">
            Write. Publish. Connect. Inspire.
          </p>
        </div>
        <div className="space-y-2 text-sm text-[var(--muted)]">
          <p className="text-white">Platform</p>
          <Link to="/feed" className="block hover:text-white">
            Feed
          </Link>
          <Link to="/author" className="block hover:text-white">
            Author Studio
          </Link>
          <Link to="/admin" className="block hover:text-white">
            Admin Panel
          </Link>
        </div>
        <div className="space-y-2 text-sm text-[var(--muted)]">
          <p className="text-white">Account</p>
          <Link to="/login" className="block hover:text-white">
            Login
          </Link>
          <Link to="/signup" className="block hover:text-white">
            Signup
          </Link>
          <Link to="/profile" className="block hover:text-white">
            Profile
          </Link>
        </div>
        <div className="space-y-2 text-sm text-[var(--muted)]">
          <p className="text-white">Engagement</p>
          <Link to="/notifications" className="block hover:text-white">
            Notifications
          </Link>
          <Link to="/newsletter" className="block hover:text-white">
            Newsletter
          </Link>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
