import { FileText, Eye, Heart, MessageSquare, Upload, Rocket } from "lucide-react";
import { posts } from "../data/mockData.js";

function AuthorDashboardPage() {
  const cards = [
    { label: "My Posts", value: "42", icon: FileText },
    { label: "Views", value: "128K", icon: Eye },
    { label: "Likes", value: "8.9K", icon: Heart },
    { label: "Comments", value: "2.1K", icon: MessageSquare },
  ];

  return (
    <div className="container-shell pb-16">
      <header className="mb-8 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="hero-title text-3xl">Author Studio</h1>
          <p className="mt-2 text-sm text-[var(--muted)]">
            Create posts, manage media, moderate comments, and track engagement.
          </p>
        </div>
        <div className="flex gap-2">
          <button className="btn-ghost rounded-full px-4 py-2 text-sm">
            <Upload size={14} className="mr-2 inline" />
            Upload Media
          </button>
          <button className="btn-primary rounded-full px-4 py-2 text-sm">
            <Rocket size={14} className="mr-2 inline" />
            New Post
          </button>
        </div>
      </header>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {cards.map((card) => (
          <article key={card.label} className="glass edge-glow rounded-3xl p-5">
            <card.icon size={18} className="text-cyan-300" />
            <p className="mt-4 text-2xl font-semibold">{card.value}</p>
            <p className="text-sm text-[var(--muted)]">{card.label}</p>
          </article>
        ))}
      </section>

      <section className="mt-8 grid gap-6 lg:grid-cols-[1fr_360px]">
        <div className="glass rounded-3xl p-5 md:p-6">
          <h2 className="text-xl font-semibold">Recent Posts</h2>
          <div className="mt-4 space-y-3">
            {posts.map((post) => (
              <div key={post.id} className="rounded-2xl border border-white/10 bg-white/5 p-4">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <h3 className="font-medium text-white">{post.title}</h3>
                  <span className="chip rounded-full px-2 py-1 text-[10px]">{post.category}</span>
                </div>
                <p className="mt-2 text-sm text-[var(--muted)]">{post.excerpt}</p>
                <div className="mt-3 flex flex-wrap gap-2 text-xs text-[var(--muted)]">
                  <span>{post.readTime}</span>
                  <span>•</span>
                  <span>{post.likes} likes</span>
                  <span>•</span>
                  <span>{post.comments} comments</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        <aside className="glass rounded-3xl p-5 md:p-6">
          <h2 className="text-xl font-semibold">Comment Queue</h2>
          <div className="mt-4 space-y-3">
            {["Review reply thread", "Approve question comment", "Delete spam mention"].map((item) => (
              <div key={item} className="rounded-2xl border border-white/10 bg-white/5 p-3 text-sm text-[var(--muted)]">
                {item}
              </div>
            ))}
          </div>
        </aside>
      </section>
    </div>
  );
}

export default AuthorDashboardPage;
