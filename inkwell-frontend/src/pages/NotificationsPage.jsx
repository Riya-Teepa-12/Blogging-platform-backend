import { useEffect, useEffectEvent, useState } from "react";
import { notifications as initialNotifications } from "../data/mockData.js";

function NotificationsPage() {
  const [items, setItems] = useState(initialNotifications);

  const markAllRead = useEffectEvent(() => {
    setItems((prev) => prev.map((item) => ({ ...item, read: true })));
  });

  useEffect(() => {
    const onKeyDown = (event) => {
      if (event.key.toLowerCase() === "r" && (event.ctrlKey || event.metaKey)) {
        event.preventDefault();
        markAllRead();
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [markAllRead]);

  return (
    <div className="container-shell pb-16">
      <header className="mb-8 flex items-center justify-between gap-3">
        <div>
          <h1 className="hero-title text-3xl">Notification Center</h1>
          <p className="mt-2 text-sm text-[var(--muted)]">
            Track replies, mentions, and platform alerts.
          </p>
        </div>
        <button onClick={markAllRead} className="btn-primary rounded-full px-4 py-2 text-xs">
          Mark all read
        </button>
      </header>

      <div className="space-y-3">
        {items.map((item) => (
          <article
            key={item.id}
            className={`glass rounded-2xl p-4 ${
              item.read ? "opacity-65" : "edge-glow"
            }`}
          >
            <p className="text-sm">{item.text}</p>
            <p className="mt-2 text-xs text-[var(--muted)]">{item.time}</p>
          </article>
        ))}
      </div>
    </div>
  );
}

export default NotificationsPage;
