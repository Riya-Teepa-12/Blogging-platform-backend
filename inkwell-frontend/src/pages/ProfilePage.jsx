import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";

function ProfilePage() {
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState({
    fullName: user?.fullName || "",
    username: user?.username || "",
    bio: user?.bio || "",
    avatarUrl: user?.avatarUrl || "",
  });
  const [avatarPreview, setAvatarPreview] = useState(user?.avatarUrl || "");
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    setForm({
      fullName: user?.fullName || "",
      username: user?.username || "",
      bio: user?.bio || "",
      avatarUrl: user?.avatarUrl || "",
    });
    setAvatarPreview(user?.avatarUrl || "");
  }, [user]);

  const initials = useMemo(() => {
    const names = user?.fullName?.split(" ")?.filter(Boolean) ?? [];
    if (names.length === 0) {
      return user?.username?.slice(0, 2).toUpperCase() || "UI";
    }
    if (names.length === 1) {
      return names[0].slice(0, 2).toUpperCase();
    }
    return `${names[0][0]}${names[1][0]}`.toUpperCase();
  }, [user]);

  const avatarSrc = avatarPreview || form.avatarUrl || user?.avatarUrl || "";

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleAvatarChange = (event) => {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }
    const reader = new FileReader();
    reader.onload = (loadEvent) => {
      const url = loadEvent.target?.result;
      if (typeof url === "string") {
        setAvatarPreview(url);
        setForm((prev) => ({ ...prev, avatarUrl: url }));
      }
    };
    reader.readAsDataURL(file);
  };

  const handleRemoveAvatar = () => {
    setAvatarPreview("");
    setForm((prev) => ({ ...prev, avatarUrl: "" }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    setSubmitting(true);
    setMessage("");

    try {
      updateUser(form);
      setMessage("Profile updated successfully.");
    } finally {
      setSubmitting(false);
      window.setTimeout(() => setMessage(""), 3000);
    }
  };

  return (
    <div className="container-shell pb-16">
      <header className="mb-8">
        <h1 className="hero-title text-3xl">Profile Settings</h1>
        <p className="mt-2 text-sm text-[var(--muted)]">
          Update your identity, bio, avatar, and password.
        </p>
      </header>

      <div className="grid gap-6 lg:grid-cols-[320px_1fr_420px]">
        <div className="glass rounded-3xl p-5 md:p-6">
          <h2 className="text-lg font-semibold">Your Avatar</h2>
          <p className="mt-2 text-sm text-[var(--muted)]">
            This is the only avatar displayed in your profile section.
          </p>

          <div className="mt-6 flex flex-col items-center gap-4 text-center">
            <div className="flex h-28 w-28 items-center justify-center overflow-hidden rounded-full bg-white/5 text-4xl font-semibold text-white/90">
              {avatarSrc ? (
                <img src={avatarSrc} alt="Avatar preview" className="h-full w-full object-cover" />
              ) : (
                initials
              )}
            </div>
            <div className="grid gap-2">
              <label className="btn-ghost inline-flex cursor-pointer rounded-full px-4 py-2 text-sm">
                Choose Avatar
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleAvatarChange}
                  className="hidden"
                />
              </label>
              <button
                type="button"
                onClick={handleRemoveAvatar}
                className="btn-ghost rounded-full px-4 py-2 text-sm"
              >
                Remove Avatar
              </button>
            </div>
            <p className="text-sm text-[var(--muted)]">
              Best results come from a square image. The avatar will be saved locally with your profile data.
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="glass rounded-3xl p-5 md:p-6">
          <div>
            <h2 className="text-lg font-semibold">Profile Details</h2>
            <p className="mt-1 text-sm text-[var(--muted)]">
              Add or update your display name, username, and short bio.
            </p>
          </div>

          <div className="mt-6 grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-1 block text-xs uppercase tracking-[0.08em] text-[var(--muted)]">
                Full Name
              </label>
              <input
                name="fullName"
                value={form.fullName}
                onChange={handleChange}
                placeholder="Your full name"
                className="w-full rounded-2xl border border-white/15 bg-white/5 px-4 py-3 text-sm outline-none focus:border-cyan-300/70"
              />
            </div>
            <div>
              <label className="mb-1 block text-xs uppercase tracking-[0.08em] text-[var(--muted)]">
                Username
              </label>
              <input
                name="username"
                value={form.username}
                onChange={handleChange}
                placeholder="@username"
                className="w-full rounded-2xl border border-white/15 bg-white/5 px-4 py-3 text-sm outline-none focus:border-cyan-300/70"
              />
            </div>
            <div className="md:col-span-2">
              <label className="mb-1 block text-xs uppercase tracking-[0.08em] text-[var(--muted)]">
                Bio
              </label>
              <textarea
                name="bio"
                rows={4}
                value={form.bio}
                onChange={handleChange}
                placeholder="Tell others a bit about yourself"
                className="w-full rounded-2xl border border-white/15 bg-white/5 px-4 py-3 text-sm outline-none focus:border-cyan-300/70"
              />
            </div>
          </div>

          <div className="mt-5 flex flex-col gap-3 sm:flex-row sm:items-center">
            <button
              type="submit"
              disabled={submitting}
              className="btn-primary rounded-full px-5 py-2 text-sm disabled:cursor-not-allowed disabled:opacity-60"
            >
              {submitting ? "Saving..." : "Save Profile"}
            </button>
            {message ? (
              <span className="rounded-2xl border border-emerald-400/20 bg-emerald-400/10 px-4 py-2 text-sm text-emerald-100">
                {message}
              </span>
            ) : null}
          </div>
        </form>

        <aside className="glass rounded-3xl p-5 md:p-6">
          <h2 className="text-lg font-semibold">Change Password</h2>
          <div className="mt-4 space-y-3">
            <input placeholder="Current password" type="password" className="w-full rounded-2xl border border-white/15 bg-white/5 px-4 py-3 text-sm outline-none focus:border-cyan-300/70" />
            <input placeholder="New password" type="password" className="w-full rounded-2xl border border-white/15 bg-white/5 px-4 py-3 text-sm outline-none focus:border-cyan-300/70" />
            <button className="btn-ghost w-full rounded-2xl py-3 text-sm">Update Password</button>
          </div>
        </aside>
      </div>
    </div>
  );
}

export default ProfilePage;
