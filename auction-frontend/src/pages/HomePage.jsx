import { useState, useEffect, lazy, Suspense } from 'react';
import { Link } from 'react-router-dom';
import { auctionAPI } from '../services/api';
import AuctionCard from '../components/AuctionCard';
import LoadingSpinner from '../components/LoadingSpinner';
import { FiArrowRight, FiClock, FiTrendingUp } from 'react-icons/fi';

const HeroScene = lazy(() => import('../components/HeroScene'));

const CATEGORIES = [
  { key: '', label: 'All' },
  { key: 'ELECTRONICS', label: 'Electronics' },
  { key: 'FASHION', label: 'Fashion' },
  { key: 'COLLECTIBLES', label: 'Collectibles' },
  { key: 'ART', label: 'Art' },
  { key: 'JEWELRY', label: 'Jewelry' },
  { key: 'VEHICLES', label: 'Vehicles' },
  { key: 'SPORTS', label: 'Sports' },
  { key: 'HOME_GARDEN', label: 'Home & Garden' },
];

export default function HomePage() {
  const [activeAuctions, setActiveAuctions] = useState([]);
  const [endingSoon, setEndingSoon] = useState([]);
  const [popular, setPopular] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [a, e, p] = await Promise.all([
          auctionAPI.getActive(0, 8),
          auctionAPI.getEndingSoon(0, 4),
          auctionAPI.getPopular(0, 4),
        ]);
        setActiveAuctions(a.data.data?.content || []);
        setEndingSoon(e.data.data?.content || []);
        setPopular(p.data.data?.content || []);
      } catch (err) { console.error(err); }
      finally { setLoading(false); }
    })();
  }, []);

  return (
    <div>
      {/* Hero */}
      <section className="bg-gray-950 relative overflow-hidden">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-20 md:py-28 grid grid-cols-1 lg:grid-cols-2 gap-8 items-center">
          <div className="max-w-2xl relative z-10">
            <p className="label mb-4">Live Auctions</p>
            <h1 className="text-3xl sm:text-5xl font-bold text-white leading-[1.15] mb-5">
              The auction house<br />
              <span className="text-primary-300">built for this era.</span>
            </h1>
            <p className="text-gray-400 text-base sm:text-lg mb-8 max-w-md leading-relaxed">
              Real-time bidding on curated lots — from rare collectibles to everyday finds. No middlemen.
            </p>
            <div className="flex flex-wrap gap-3">
              <Link to="/auctions" className="btn-primary">Explore auctions</Link>
              <Link to="/create-auction" className="text-sm text-gray-400 hover:text-white flex items-center gap-1 transition-colors px-2">
                Start selling <FiArrowRight size={14} />
              </Link>
            </div>
          </div>
          <div className="hidden lg:block relative h-[380px]">
            <Suspense fallback={null}>
              <HeroScene />
            </Suspense>
          </div>
        </div>
      </section>

      {/* Trust strip */}
      <section className="border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 flex flex-wrap items-center justify-center gap-x-8 gap-y-2 text-xs text-gray-400">
          <span>Real-time WebSocket bidding</span>
          <span className="hidden sm:inline text-gray-300">&middot;</span>
          <span>Snipe-protection auto-extend</span>
          <span className="hidden sm:inline text-gray-300">&middot;</span>
          <span>JWT-secured accounts</span>
          <span className="hidden sm:inline text-gray-300">&middot;</span>
          <span>Instant outbid alerts</span>
        </div>
      </section>

      {/* Categories */}
      <section className="border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-3 flex gap-2 overflow-x-auto no-scrollbar">
          {CATEGORIES.map((c) => (
            <Link key={c.key} to={c.key ? `/auctions?category=${c.key}` : '/auctions'}
              className="px-3 py-1 text-xs font-medium text-gray-500 hover:text-gray-900 border border-gray-200 rounded-full whitespace-nowrap hover:border-gray-400 transition-colors">
              {c.label}
            </Link>
          ))}
        </div>
      </section>

      {loading ? (
        <LoadingSpinner size="lg" text="Loading lots..." />
      ) : (
        <>
          {endingSoon.length > 0 && (
            <Section label="Closing soon" icon={<FiClock size={14} className="text-accent-600" />}
              title="Last chance to bid" link="/auctions?sort=ending-soon">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {endingSoon.map((a) => <AuctionCard key={a.id} auction={a} />)}
              </div>
            </Section>
          )}

          {activeAuctions.length > 0 && (
            <Section label="New lots" title="Just listed" link="/auctions">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {activeAuctions.map((a) => <AuctionCard key={a.id} auction={a} />)}
              </div>
            </Section>
          )}

          {popular.length > 0 && (
            <Section label="Trending" icon={<FiTrendingUp size={14} className="text-primary-600" />}
              title="Most active lots" link="/auctions?sort=popular">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {popular.map((a) => <AuctionCard key={a.id} auction={a} />)}
              </div>
            </Section>
          )}

          {/* CTA */}
          <section className="bg-gray-950">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 py-16 md:py-20 flex flex-col md:flex-row md:items-center md:justify-between gap-6">
              <div>
                <p className="label mb-2">Get started</p>
                <h2 className="text-2xl sm:text-3xl font-bold text-white">Ready to place your first bid?</h2>
                <p className="text-gray-400 mt-2 text-sm max-w-md">Create a free account and start bidding in under a minute.</p>
              </div>
              <Link to="/register" className="btn-primary self-start md:self-center shrink-0">Create account</Link>
            </div>
          </section>
        </>
      )}
    </div>
  );
}

function Section({ label, icon, title, link, children }) {
  return (
    <section className="py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6">
        <div className="flex items-end justify-between mb-6">
          <div>
            <p className="label flex items-center gap-1.5 mb-1">{icon} {label}</p>
            <h2 className="text-xl font-bold text-gray-900">{title}</h2>
          </div>
          {link && (
            <Link to={link} className="text-sm text-gray-500 hover:text-gray-900 flex items-center gap-1 transition-colors">
              View all <FiArrowRight size={14} />
            </Link>
          )}
        </div>
        {children}
      </div>
    </section>
  );
}