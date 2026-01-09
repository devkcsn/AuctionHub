import { Link } from 'react-router-dom';
import { FiClock, FiUsers } from 'react-icons/fi';
import CountdownTimer from './CountdownTimer';

const STATUS_STYLES = {
  ACTIVE: 'badge-active',
  PENDING: 'badge-pending',
  ENDED: 'badge-ended',
  SOLD: 'badge-sold',
  CANCELLED: 'badge-cancelled',
};

export default function AuctionCard({ auction }) {
  const {
    id, title, currentPrice, status, category,
    endTime, bidCount, imageUrls, sellerUsername, featured,
  } = auction;

  const fallback = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=300&fit=crop';
  const image = imageUrls?.length > 0 ? imageUrls[0] : fallback;

  return (
    <Link to={`/auctions/${id}`} className="card group">
      <div className="relative aspect-[4/3] overflow-hidden bg-gray-100">
        <img src={image} alt={title}
          className="w-full h-full object-cover group-hover:scale-[1.03] transition-transform duration-500"
          onError={(e) => { e.target.src = fallback; }} />
        <div className="absolute top-2 right-2">
          <span className={STATUS_STYLES[status] || 'badge'}>{status}</span>
        </div>
        {featured && (
          <div className="absolute top-2 left-2 badge bg-yellow-400 text-yellow-900">Featured</div>
        )}
      </div>

      <div className="p-3.5">
        <p className="text-[11px] uppercase tracking-wider text-gray-400 mb-1">
          {category?.replace('_', ' ')}
        </p>
        <h3 className="font-medium text-gray-900 text-sm leading-snug truncate">{title}</h3>
        <p className="text-xs text-gray-400 mt-0.5">by {sellerUsername}</p>

        <div className="mt-3 flex items-end justify-between">
          <div>
            <p className="text-[10px] uppercase tracking-wider text-gray-400">Current bid</p>
            <p className="text-lg font-bold text-gray-900 tabular-nums">
              ${parseFloat(currentPrice).toLocaleString('en-US', { minimumFractionDigits: 2 })}
            </p>
          </div>
          <div className="flex items-center gap-2.5 text-xs text-gray-400">
            <span className="flex items-center gap-1"><FiUsers size={11} /> {bidCount}</span>
            {status === 'ACTIVE' && (
              <span className="flex items-center gap-1">
                <FiClock size={11} />
                <CountdownTimer endTime={endTime} compact />
              </span>
            )}
          </div>
        </div>
      </div>
    </Link>
  );
}