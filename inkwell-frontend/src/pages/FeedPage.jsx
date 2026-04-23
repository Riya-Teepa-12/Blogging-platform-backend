import { startTransition, useDeferredValue, useState } from "react";
import { Search } from "lucide-react";
import PostCard from "../components/PostCard.jsx";
import { posts } from "../data/mockData.js";

function FeedPage() {
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("All");
  const deferredQuery = useDeferredValue(query);

  const categories = ["All", ...new Set(posts.map((post) => post.category))];

  const filteredPosts = posts.filter((post) => {
    const matchCategory = category === "All" || post.category === category;
    const q = deferredQuery.trim().toLowerCase();
    const matchQuery =
      q.length === 0 ||
      post.title.toLowerCase().includes(q) ||
      post.excerpt.toLowerCase().includes(q) ||
      post.tags.join(" ").toLowerCase().includes(q);
    return matchCategory && matchQuery;
  });

  return (
    <div className="container-shell pb-16">
      <header className="mb-8">
        <h1 className="hero-title text-3xl">Public Feed</h1>
        <p className="mt-2 text-sm text-[var(--muted)]">
          Discover published stories, filter by category, and search by keywords.
        </p>
      </header>

      <div className="glass mb-8 rounded-3xl p-4 md:p-5">
        <div className="grid gap-4 md:grid-cols-[1fr_auto] md:items-center">
          <label className="relative block">
            <Search
              size={16}
              className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[var(--muted)]"
            />
            <input
              value={query}
              onChange={(event) =>
                startTransition(() => setQuery(event.target.value))
              }
              placeholder="Search by title, content, or tags"
              className="w-full rounded-2xl border border-white/15 bg-white/5 py-3 pl-10 pr-4 text-sm outline-none focus:border-cyan-300/70"
            />
          </label>
          <div className="flex flex-wrap gap-2">
            {categories.map((item) => (
              <button
                key={item}
                onClick={() => setCategory(item)}
                className={`rounded-full px-3 py-2 text-xs transition ${
                  category === item
                    ? "btn-primary"
                    : "btn-ghost"
                }`}
              >
                {item}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {filteredPosts.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}
      </div>
    </div>
  );
}

export default FeedPage;
