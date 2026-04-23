import { Link } from "react-router-dom";
import { Heart, MessageCircle, Clock3 } from "lucide-react";

function PostCard({ post }) {
  return (
    <article className="glass edge-glow rounded-3xl p-5">
      <div className="mb-4 flex items-center gap-2">
        <span className="chip rounded-full px-2.5 py-1 text-[10px] uppercase tracking-[0.08em]">
          {post.category}
        </span>
        {post.featured && (
          <span className="rounded-full bg-cyan-300/20 px-2.5 py-1 text-[10px] uppercase tracking-[0.08em] text-cyan-200">
            Featured
          </span>
        )}
      </div>
      <h3 className="text-lg font-semibold leading-tight text-white">{post.title}</h3>
      <p className="mt-3 text-sm text-[var(--muted)]">{post.excerpt}</p>
      <div className="mt-4 flex flex-wrap gap-2">
        {post.tags.map((tag) => (
          <span key={tag} className="rounded-full border border-white/10 px-2.5 py-1 text-xs text-[var(--muted)]">
            #{tag}
          </span>
        ))}
      </div>
      <div className="mt-5 flex items-center justify-between text-xs text-[var(--muted)]">
        <div className="flex items-center gap-3">
          <span>{post.author}</span>
          <span className="flex items-center gap-1">
            <Clock3 size={13} /> {post.readTime}
          </span>
        </div>
        <div className="flex items-center gap-3">
          <span className="flex items-center gap-1">
            <Heart size={13} /> {post.likes}
          </span>
          <span className="flex items-center gap-1">
            <MessageCircle size={13} /> {post.comments}
          </span>
        </div>
      </div>
      <Link
        to={`/post/${post.slug}`}
        className="btn-primary mt-5 inline-flex rounded-full px-4 py-2 text-xs"
      >
        Read Article
      </Link>
    </article>
  );
}

export default PostCard;
