import { Link } from 'react-router-dom';
import Logo from './Logo';

export default function Footer() {
  return (
    <footer className="border-t border-gray-200 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-10">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
          <div className="col-span-2 md:col-span-1">
            <Link to="/" className="inline-block mb-3">
              <Logo variant="full" theme="dark" size={24} />
            </Link>
            <p className="text-xs text-gray-500 leading-relaxed max-w-[220px]">
              Live bidding on curated lots. Built for collectors, sellers, and everyone in between.
            </p>
          </div>

          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-3">Platform</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/auctions" className="text-gray-600 hover:text-gray-900 transition-colors">Browse</Link></li>
              <li><Link to="/create-auction" className="text-gray-600 hover:text-gray-900 transition-colors">Sell an item</Link></li>
              <li><Link to="/auctions?sort=ending-soon" className="text-gray-600 hover:text-gray-900 transition-colors">Ending soon</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-3">Categories</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/auctions?category=ELECTRONICS" className="text-gray-600 hover:text-gray-900 transition-colors">Electronics</Link></li>
              <li><Link to="/auctions?category=COLLECTIBLES" className="text-gray-600 hover:text-gray-900 transition-colors">Collectibles</Link></li>
              <li><Link to="/auctions?category=ART" className="text-gray-600 hover:text-gray-900 transition-colors">Art</Link></li>
              <li><Link to="/auctions?category=JEWELRY" className="text-gray-600 hover:text-gray-900 transition-colors">Jewelry</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-3">Company</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="text-gray-600 hover:text-gray-900 transition-colors">Help</a></li>
              <li><a href="#" className="text-gray-600 hover:text-gray-900 transition-colors">Terms</a></li>
              <li><a href="#" className="text-gray-600 hover:text-gray-900 transition-colors">Privacy</a></li>
              <li><a href="#" className="text-gray-600 hover:text-gray-900 transition-colors">Contact</a></li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-100 mt-8 pt-6 text-xs text-gray-400">
          &copy; {new Date().getFullYear()} AuctionHub
        </div>
      </div>
    </footer>
  );
}