import { Link, useParams } from "react-router-dom";
import { Heart, MessageCircle, Clock3, SendHorizontal } from "lucide-react";
import { posts } from "../data/mockData.js";

const sampleComments = [
  {
    id: 1,
    author: "Ritika",
    body: "This architecture breakdown is exactly what I needed for my own content platform.",
    likes: 12,
    replies: [{ id: 11, author: "Author", body: "Glad it helped. I can share event flow details too." }],
  },
  {
    id: 2,
    author: "Sahil",
    body: "Would love a follow-up on newsletter segmentation strategy.",
    likes: 7,
    replies: [],
  },
];

function PostPage() {
  const { slug } = useParams();
  const post = posts.find((item) => item.slug === slug) ?? posts[0];

  return (
    <div className="container-shell pb-16">
      <article className="glass edge-glow rounded-4xl p-6 md:p-9">
        <span className="chip rounded-full px-3 py-1 text-xs uppercase">{post.category}</span>
        <h1 className="mt-4 text-3xl font-semibold leading-tight md:text-5xl">
          {post.title}
        </h1>
        <div className="mt-4 flex flex-wrap gap-4 text-sm text-[var(--muted)]">
          <span>{post.author}</span>
          <span className="flex items-center gap-1">
            <Clock3 size={14} /> {post.readTime}
          </span>
          <span className="flex items-center gap-1">
            <Heart size={14} /> {post.likes}
          </span>
          <span className="flex items-center gap-1">
            <MessageCircle size={14} /> {post.comments}
          </span>
        </div>

        <div className="mt-8 space-y-4 text-[15px] leading-relaxed text-[var(--muted)]">
          <p>
            InkWell is designed around an end-to-end publishing lifecycle where
            content creation, moderation, audience engagement, and analytics
            operate as one cohesive loop.
          </p>
          <p>
            Authors can iterate in draft mode, publish at the optimal moment,
            and evaluate engagement through post-level metrics. Readers interact
            through likes, threaded comments, and notifications.
          </p>
          <p>
            The admin layer enforces platform quality by managing users,
            taxonomy, moderation workflows, and campaign delivery with full
            visibility into operational signals.
          </p>
        </div>
      </article>

      <section className="mt-8 grid gap-6 lg:grid-cols-[1fr_320px]">
        <div className="glass rounded-3xl p-5 md:p-6">
          <h2 className="text-xl font-semibold">Discussion</h2>
          <div className="mt-4 space-y-4">
            {sampleComments.map((comment) => (
              <div key={comment.id} className="rounded-2xl border border-white/10 bg-white/5 p-4">
                <p className="text-sm font-medium text-white">{comment.author}</p>
                <p className="mt-2 text-sm text-[var(--muted)]">{comment.body}</p>
                <p className="mt-2 text-xs text-[var(--muted)]">Likes: {comment.likes}</p>
                {comment.replies.length > 0 && (
                  <div className="mt-3 rounded-xl border border-white/10 bg-black/20 p-3">
                    {comment.replies.map((reply) => (
                      <div key={reply.id}>
                        <p className="text-xs font-medium text-white">{reply.author}</p>
                        <p className="mt-1 text-xs text-[var(--muted)]">{reply.body}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        <aside className="glass rounded-3xl p-5 md:p-6">
          <h3 className="text-lg font-semibold">Add Comment</h3>
          <textarea
            rows={5}
            placeholder="Write your thoughts..."
            className="mt-3 w-full rounded-2xl border border-white/15 bg-white/5 p-3 text-sm outline-none focus:border-cyan-300/70"
          />
          <button className="btn-primary mt-3 inline-flex items-center rounded-full px-4 py-2 text-sm">
            Send <SendHorizontal size={14} className="ml-2" />
          </button>
          <Link to="/feed" className="btn-ghost mt-3 block rounded-full px-4 py-2 text-center text-sm">
            Back to Feed
          </Link>
        </aside>
      </section>
    </div>
  );
}

export default PostPage;
