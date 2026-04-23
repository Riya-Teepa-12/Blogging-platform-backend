import { Route, Routes, useLocation } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import Navbar from "./components/Navbar.jsx";
import Footer from "./components/Footer.jsx";
import HomePage from "./pages/HomePage.jsx";
import FeedPage from "./pages/FeedPage.jsx";
import PostPage from "./pages/PostPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import SignupPage from "./pages/SignupPage.jsx";
import AuthorDashboardPage from "./pages/AuthorDashboardPage.jsx";
import AdminPanelPage from "./pages/AdminPanelPage.jsx";
import ProfilePage from "./pages/ProfilePage.jsx";
import NotificationsPage from "./pages/NotificationsPage.jsx";
import NewsletterPage from "./pages/NewsletterPage.jsx";
import NotFoundPage from "./pages/NotFoundPage.jsx";
import ProtectedRoute, { PublicOnlyRoute } from "./components/ProtectedRoute.jsx";

function App() {
  const location = useLocation();
  const authRoute =
    location.pathname === "/login" || location.pathname === "/signup";

  return (
    <div className="min-h-screen bg-[var(--surface)] text-[var(--text)]">
      <div className="ambient-bg" />
      {!authRoute && <Navbar />}
      <main className={authRoute ? "pt-0" : "pt-24"}>
        <AnimatePresence mode="wait">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -16 }}
            transition={{ duration: 0.4, ease: "easeOut" }}
          >
            <Routes location={location}>
              <Route path="/" element={<HomePage />} />
              <Route path="/feed" element={<FeedPage />} />
              <Route path="/post/:slug" element={<PostPage />} />
              <Route element={<PublicOnlyRoute />}>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
              </Route>
              <Route element={<ProtectedRoute />}>
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/notifications" element={<NotificationsPage />} />
                <Route path="/newsletter" element={<NewsletterPage />} />
              </Route>
              <Route element={<ProtectedRoute allowedRoles={["AUTHOR", "ADMIN"]} />}>
                <Route path="/author" element={<AuthorDashboardPage />} />
              </Route>
              <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
                <Route path="/admin" element={<AdminPanelPage />} />
              </Route>
              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </motion.div>
        </AnimatePresence>
      </main>
      {!authRoute && <Footer />}
    </div>
  );
}

export default App;
