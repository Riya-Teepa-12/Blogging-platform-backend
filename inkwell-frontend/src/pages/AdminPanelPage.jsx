import { adminStats } from "../data/mockData.js";

function AdminPanelPage() {
  return (
    <div className="container-shell pb-16">
      <header className="mb-8">
        <h1 className="hero-title text-3xl">Admin Panel</h1>
        <p className="mt-2 text-sm text-[var(--muted)]">
          Manage users, roles, posts, categories, tags, comments, newsletter and analytics.
        </p>
      </header>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {adminStats.map((stat) => (
          <article key={stat.label} className="glass edge-glow rounded-3xl p-5">
            <p className="text-2xl font-semibold">{stat.value}</p>
            <p className="text-sm text-[var(--muted)]">{stat.label}</p>
          </article>
        ))}
      </section>

      <section className="mt-8 grid gap-6 lg:grid-cols-2">
        <article className="glass rounded-3xl p-5 md:p-6">
          <h2 className="text-xl font-semibold">User Management</h2>
          <div className="mt-4 space-y-3">
            {["Change role", "Suspend account", "Reactivate account", "Delete account"].map((action) => (
              <div key={action} className="rounded-2xl border border-white/10 bg-white/5 p-3 text-sm text-[var(--muted)]">
                {action}
              </div>
            ))}
          </div>
        </article>

        <article className="glass rounded-3xl p-5 md:p-6">
          <h2 className="text-xl font-semibold">Content Governance</h2>
          <div className="mt-4 space-y-3">
            {["Feature post", "Delete violating post", "Approve/reject comment", "Manage media assets"].map((action) => (
              <div key={action} className="rounded-2xl border border-white/10 bg-white/5 p-3 text-sm text-[var(--muted)]">
                {action}
              </div>
            ))}
          </div>
        </article>
      </section>

      <section className="mt-6 grid gap-6 lg:grid-cols-2">
        <article className="glass rounded-3xl p-5 md:p-6">
          <h2 className="text-xl font-semibold">Taxonomy + Newsletter</h2>
          <div className="mt-4 space-y-3">
            {["Create hierarchical categories", "Manage tags and trending list", "Segment subscribers", "Dispatch campaigns"].map((action) => (
              <div key={action} className="rounded-2xl border border-white/10 bg-white/5 p-3 text-sm text-[var(--muted)]">
                {action}
              </div>
            ))}
          </div>
        </article>

        <article className="glass rounded-3xl p-5 md:p-6">
          <h2 className="text-xl font-semibold">Audit + Broadcast</h2>
          <div className="mt-4 space-y-3">
            {["Broadcast notification by role", "View audit logs", "Track admin actions", "Observe SLA health metrics"].map((action) => (
              <div key={action} className="rounded-2xl border border-white/10 bg-white/5 p-3 text-sm text-[var(--muted)]">
                {action}
              </div>
            ))}
          </div>
        </article>
      </section>
    </div>
  );
}

export default AdminPanelPage;
