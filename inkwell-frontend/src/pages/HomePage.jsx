import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { ArrowRight, Sparkles, ShieldCheck, BellRing, BarChart3 } from "lucide-react";
import AnimatedSection from "../components/AnimatedSection.jsx";
import { features, metrics, posts } from "../data/mockData.js";

function HomePage() {
  return (
    <div className="pb-16">
      <section className="container-shell">
        <div className="grid gap-8 rounded-4xl border border-white/10 bg-[var(--surface-2)]/70 p-6 md:p-10 lg:grid-cols-[1.1fr_0.9fr]">
          <div className="space-y-6">
            <span className="chip inline-flex rounded-full px-4 py-2 text-xs uppercase tracking-[0.1em]">
              AI-Powered Blogging Platform
            </span>
            <h1 className="hero-title text-3xl leading-tight md:text-5xl">
              Write smarter. <span className="gradient-text">Publish faster.</span> Build
              loyal readership.
            </h1>
            <p className="max-w-xl text-sm text-[var(--muted)] md:text-base">
              InkWell merges content production, engagement, moderation, and
              analytics into one futuristic workspace for readers, authors, and
              admins.
            </p>
            <div className="flex flex-wrap gap-3">
              <Link to="/signup" className="btn-primary inline-flex items-center rounded-full px-5 py-2.5 text-sm">
                Start Free <ArrowRight className="ml-2" size={16} />
              </Link>
              <Link to="/feed" className="btn-ghost inline-flex items-center rounded-full px-5 py-2.5 text-sm">
                Explore Feed
              </Link>
            </div>
            <div className="grid grid-cols-2 gap-3 pt-2 md:grid-cols-4">
              <div className="glass rounded-2xl p-3 text-xs text-[var(--muted)]">
                <Sparkles size={14} className="mb-2 text-cyan-300" />
                Rich editor + media
              </div>
              <div className="glass rounded-2xl p-3 text-xs text-[var(--muted)]">
                <ShieldCheck size={14} className="mb-2 text-cyan-300" />
                RBAC security
              </div>
              <div className="glass rounded-2xl p-3 text-xs text-[var(--muted)]">
                <BellRing size={14} className="mb-2 text-cyan-300" />
                Smart notifications
              </div>
              <div className="glass rounded-2xl p-3 text-xs text-[var(--muted)]">
                <BarChart3 size={14} className="mb-2 text-cyan-300" />
                Platform analytics
              </div>
            </div>
          </div>

          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.2, duration: 0.5 }}
            className="motion-float glass edge-glow rounded-3xl p-4 md:p-6"
          >
            <div className="rounded-2xl bg-gradient-to-br from-cyan-300/20 via-indigo-300/10 to-fuchsia-300/10 p-5">
              <p className="text-xs uppercase tracking-[0.09em] text-[var(--muted)]">
                Live workspace preview
              </p>
              <h3 className="mt-3 text-xl font-semibold">Author Command Center</h3>
              <div className="mt-5 space-y-3 text-sm text-[var(--muted)]">
                <div className="glass rounded-xl p-3">Draft readiness score: 91%</div>
                <div className="glass rounded-xl p-3">Newsletter sync queued</div>
                <div className="glass rounded-xl p-3">New comments requiring attention: 6</div>
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      <AnimatedSection className="container-shell mt-14">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {features.map((feature) => (
            <div key={feature.title} className="glass edge-glow rounded-3xl p-5">
              <h3 className="text-lg font-semibold text-white">{feature.title}</h3>
              <p className="mt-2 text-sm text-[var(--muted)]">{feature.desc}</p>
            </div>
          ))}
        </div>
      </AnimatedSection>

      <AnimatedSection className="container-shell mt-14">
        <div className="grid gap-4 rounded-4xl border border-white/10 bg-white/5 p-5 md:grid-cols-4">
          {metrics.map((metric) => (
            <div key={metric.label} className="glass rounded-2xl p-4">
              <p className="text-2xl font-semibold text-white">{metric.value}</p>
              <p className="mt-1 text-xs uppercase tracking-[0.08em] text-[var(--muted)]">
                {metric.label}
              </p>
            </div>
          ))}
        </div>
      </AnimatedSection>

      <AnimatedSection className="container-shell mt-14">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="hero-title text-2xl">Trending Articles</h2>
          <Link to="/feed" className="btn-ghost rounded-full px-4 py-2 text-xs">
            View all
          </Link>
        </div>
        <div className="grid gap-4 md:grid-cols-3">
          {posts.map((post) => (
            <article key={post.id} className="glass rounded-3xl p-5">
              <span className="chip rounded-full px-2 py-1 text-[10px]">{post.category}</span>
              <h3 className="mt-3 text-lg font-semibold text-white">{post.title}</h3>
              <p className="mt-2 text-sm text-[var(--muted)]">{post.excerpt}</p>
              <Link to={`/post/${post.slug}`} className="btn-primary mt-4 inline-flex rounded-full px-4 py-2 text-xs">
                Open Post
              </Link>
            </article>
          ))}
        </div>
      </AnimatedSection>

      <AnimatedSection className="container-shell mt-16">
        <div className="rounded-4xl border border-cyan-200/20 bg-gradient-to-r from-cyan-500/20 to-indigo-500/20 px-6 py-10 text-center">
          <h2 className="hero-title text-2xl md:text-4xl">Launch your next content system in style</h2>
          <p className="mx-auto mt-4 max-w-2xl text-sm text-[var(--muted)] md:text-base">
            InkWell combines author productivity, reader engagement, and admin control in one
            unified futuristic platform.
          </p>
          <div className="mt-7 flex justify-center gap-3">
            <Link to="/signup" className="btn-primary rounded-full px-5 py-2.5 text-sm">
              Create Account
            </Link>
            <Link to="/admin" className="btn-ghost rounded-full px-5 py-2.5 text-sm">
              Preview Admin
            </Link>
          </div>
        </div>
      </AnimatedSection>
    </div>
  );
}

export default HomePage;
