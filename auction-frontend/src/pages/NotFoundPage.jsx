import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="min-h-[60vh] flex items-center justify-center px-4">
      <div className="text-center">
        <p className="text-6xl font-bold text-gray-200 mb-3">404</p>
        <h2 className="text-lg font-semibold text-gray-900 mb-1">Page not found</h2>
        <p className="text-sm text-gray-500 mb-6">The page you are looking for does not exist or has been moved.</p>
        <div className="flex gap-3 justify-center">
          <Link to="/" className="btn-primary text-sm">Home</Link>
          <Link to="/auctions" className="btn-secondary text-sm">Browse auctions</Link>
        </div>
      </div>
    </div>
  );
}