import { useState } from "react";

function NewsletterPage() {
  const [preferences, setPreferences] = useState([
    { key: "engineering", label: "Engineering", enabled: true },
    { key: "product", label: "Product", enabled: true },
    { key: "growth", label: "Growth", enabled: false },
    { key: "community", label: "Community", enabled: true },
  ]);

  const togglePreference = (key) => {
    setPreferences((prev) =>
      prev.map((item) =>
        item.key === key ? { ...item, enabled: !item.enabled } : item
      )
    );
  };

  return (
    <div className="container-shell pb-16">
      <header className="mb-8">
        <h1 className="hero-title text-3xl">Newsletter Preferences</h1>
        <p className="mt-2 text-sm text-[var(--muted)]">
          Manage double opt-in subscription preferences and campaign topics.
        </p>
      </header>

      <section className="grid gap-6 lg:grid-cols-[1fr_400px]">
        <article className="glass rounded-3xl p-5 md:p-6">
          <h2 className="text-xl font-semibold">Subscription</h2>
          <div className="mt-4 grid gap-4 md:grid-cols-2">
            <input
              type="email"
              placeholder="you@inkwell.app"
              className="rounded-2xl border border-white/15 bg-white/5 px-4 py-3 text-sm outline-none focus:border-cyan-300/70"
            />
            <button className="btn-primary rounded-2xl py-3 text-sm">
              Send Confirmation Link
            </button>
          </div>
          <p className="mt-4 text-xs text-[var(--muted)]">
            Subscription status: <span className="text-cyan-200">PENDING</span>
          </p>
        </article>

        <aside className="glass rounded-3xl p-5 md:p-6">
          <h3 className="text-lg font-semibold">Preference Tags</h3>
          <div className="mt-4 space-y-3">
            {preferences.map((item) => (
              <label
                key={item.key}
                className="flex items-center justify-between rounded-2xl border border-white/10 bg-white/5 px-4 py-3"
              >
                <span className="text-sm">{item.label}</span>
                <button
                  onClick={() => togglePreference(item.key)}
                  className={`rounded-full px-3 py-1 text-xs ${
                    item.enabled ? "btn-primary" : "btn-ghost"
                  }`}
                >
                  {item.enabled ? "On" : "Off"}
                </button>
              </label>
            ))}
          </div>
          <button className="btn-ghost mt-4 w-full rounded-2xl py-3 text-sm">
            Save Preferences
          </button>
        </aside>
      </section>
    </div>
  );
}

export default NewsletterPage;
