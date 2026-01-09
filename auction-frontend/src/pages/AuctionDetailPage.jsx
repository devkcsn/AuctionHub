import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import { auctionAPI, bidAPI } from '../services/api';
import { useAuthStore } from '../store/authStore';
import wsService from '../services/websocket';
import CountdownTimer from '../components/CountdownTimer';
import LoadingSpinner from '../components/LoadingSpinner';
import toast from 'react-hot-toast';
import { FiClock, FiEye, FiTrendingUp, FiArrowLeft, FiAlertCircle, FiUsers } from 'react-icons/fi';

export default function AuctionDetailPage() {
  const { id } = useParams();
  const { user, isAuthenticated } = useAuthStore();
  const [auction, setAuction] = useState(null);
  const [bids, setBids] = useState([]);
  const [loading, setLoading] = useState(true);
  const [bidAmount, setBidAmount] = useState('');
  const [bidding, setBidding] = useState(false);
  const [selectedImage, setSelectedImage] = useState(0);
  const priceRef = useRef(null);

  useEffect(() => {
    fetchAuction();
    fetchBids();
    const unsub = wsService.subscribeToAuction(id, handleWs);
    return () => unsub?.();
  }, [id]);

  const handleWs = useCallback((msg) => {
    if (msg.type === 'NEW_BID') {
      setAuction((p) => p ? { ...p, currentPrice: msg.currentPrice, bidCount: msg.bidCount, timeRemainingSeconds: msg.timeRemainingSeconds } : p);
      setBids((p) => [{ bidderUsername: msg.bidderUsername, amount: msg.bidAmount, createdAt: msg.timestamp }, ...p.slice(0, 19)]);
      if (priceRef.current) {
        priceRef.current.classList.add('bid-pulse');
        setTimeout(() => priceRef.current?.classList.remove('bid-pulse'), 250);
      }
      if (user && msg.bidderUsername !== user.username) {
        toast('New bid: $' + parseFloat(msg.bidAmount).toLocaleString(), { icon: '\uD83D\uDD28' });
      }
    } else if (msg.type === 'AUCTION_ENDED') {
      setAuction((p) => p ? { ...p, status: 'ENDED' } : p);
      toast('Auction ended', { icon: '\u23F0' });
    }
  }, [user]);

  const fetchAuction = async () => {
    try {
      const res = await auctionAPI.getById(id);
      setAuction(res.data.data);
      if (res.data.data) {
        const min = parseFloat(res.data.data.currentPrice) + parseFloat(res.data.data.minBidIncrement);
        setBidAmount(min.toFixed(2));
      }
    } catch { toast.error('Failed to load auction'); }
    finally { setLoading(false); }
  };

  const fetchBids = async () => {
    try { const r = await bidAPI.getAuctionBids(id, 0, 20); setBids(r.data.data?.content || []); } catch {}
  };

  const handlePlaceBid = async () => {
    if (!isAuthenticated) { toast.error('Sign in to bid'); return; }
    const amount = parseFloat(bidAmount);
    if (isNaN(amount) || amount <= 0) { toast.error('Enter a valid amount'); return; }
    setBidding(true);
    try {
      await bidAPI.placeBid({ auctionItemId: parseInt(id), amount });
      toast.success('Bid placed!');
      setBidAmount((amount + parseFloat(auction.minBidIncrement)).toFixed(2));
    } catch (err) { toast.error(err.message || 'Failed to place bid'); }
    finally { setBidding(false); }
  };

  if (loading) return <LoadingSpinner size="lg" text="Loading lot..." />;
  if (!auction) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-20 text-center">
        <p className="text-gray-400 mb-2">Auction not found</p>
        <Link to="/auctions" className="text-primary-600 text-sm hover:underline">Browse auctions</Link>
      </div>
    );
  }

  const fallback = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&h=400&fit=crop';
  const images = auction.imageUrls?.length > 0 ? auction.imageUrls : [fallback];
  const isOwner = user?.id === auction.sellerId;
  const isActive = auction.status === 'ACTIVE';
  const minBid = auction.bidCount > 0
    ? parseFloat(auction.currentPrice) + parseFloat(auction.minBidIncrement)
    : parseFloat(auction.startingPrice);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6">
      <Link to="/auctions" className="inline-flex items-center gap-1 text-sm text-gray-400 hover:text-gray-700 mb-5 transition-colors">
        <FiArrowLeft size={14} /> Back
      </Link>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
        {/* Images */}
        <div className="lg:col-span-3">
          <div className="rounded-md overflow-hidden bg-gray-100 aspect-[4/3]">
            <img src={images[selectedImage]} alt={auction.title} className="w-full h-full object-cover" onError={(e) => { e.target.src = fallback; }} />
          </div>
          {images.length > 1 && (
            <div className="flex gap-2 mt-2 overflow-x-auto">
              {images.map((img, i) => (
                <button key={i} onClick={() => setSelectedImage(i)}
                  className={`w-14 h-14 rounded-md overflow-hidden flex-shrink-0 border-2 transition-colors ${selectedImage === i ? 'border-primary-500' : 'border-transparent hover:border-gray-300'}`}>
                  <img src={img} alt="" className="w-full h-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Details */}
        <div className="lg:col-span-2">
          <div className="flex items-center gap-2 mb-2">
            <span className={`badge-${auction.status.toLowerCase()}`}>{auction.status}</span>
            <span className="text-[11px] uppercase tracking-wider text-gray-400">{auction.category?.replace('_', ' ')}</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1">{auction.title}</h1>
          <p className="text-sm text-gray-400 mb-5">by <span className="text-gray-600">{auction.sellerUsername}</span></p>

          {/* Price */}
          <div className="bg-gray-50 rounded-md p-5 mb-5">
            <p className="text-[10px] uppercase tracking-widest text-gray-400 mb-1">Current bid</p>
            <p ref={priceRef} className="text-3xl font-bold text-gray-900 tabular-nums transition-transform">
              ${parseFloat(auction.currentPrice).toLocaleString('en-US', { minimumFractionDigits: 2 })}
            </p>
            <div className="flex gap-4 mt-2 text-xs text-gray-400">
              <span className="flex items-center gap-1"><FiUsers size={12} /> {auction.bidCount} bids</span>
              <span className="flex items-center gap-1"><FiEye size={12} /> {auction.viewCount} views</span>
            </div>
          </div>

          {/* Timer */}
          {isActive && (
            <div className="mb-5">
              <p className="text-[10px] uppercase tracking-widest text-gray-400 mb-2 flex items-center gap-1"><FiClock size={12} /> Time remaining</p>
              <CountdownTimer endTime={auction.endTime} onEnd={() => setAuction((p) => p ? { ...p, status: 'ENDED' } : p)} />
            </div>
          )}

          {/* Bid form */}
          {isActive && !isOwner && (
            <div className="border border-gray-200 rounded-md p-5 mb-5">
              <h3 className="text-sm font-semibold text-gray-900 mb-1">Place a bid</h3>
              <p className="text-xs text-gray-400 mb-3">Min: ${minBid.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
              <div className="flex gap-2">
                <div className="relative flex-1">
                  <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm">$</span>
                  <input type="number" step="0.01" min={minBid} value={bidAmount}
                    onChange={(e) => setBidAmount(e.target.value)} className="input-field pl-7" placeholder={minBid.toFixed(2)} />
                </div>
                <button onClick={handlePlaceBid} disabled={bidding || !isAuthenticated} className="btn-primary whitespace-nowrap">
                  {bidding ? 'Bidding...' : 'Bid'}
                </button>
              </div>
              {!isAuthenticated && (
                <p className="text-xs text-gray-400 mt-2"><Link to="/login" className="text-primary-600 underline">Sign in</Link> to bid</p>
              )}
            </div>
          )}

          {isOwner && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-md p-3 mb-5 text-xs text-yellow-800">
              <FiAlertCircle className="inline mr-1" /> You cannot bid on your own lot.
            </div>
          )}

          {auction.status === 'SOLD' && auction.winnerUsername && (
            <div className="bg-emerald-50 border border-emerald-200 rounded-md p-3 mb-5 text-xs text-emerald-800">
              Won by <span className="font-bold">{auction.winnerUsername}</span> — ${parseFloat(auction.currentPrice).toLocaleString()}
            </div>
          )}

          {/* Description */}
          <div className="mb-5">
            <h3 className="text-sm font-semibold text-gray-900 mb-1.5">Description</h3>
            <p className="text-sm text-gray-600 whitespace-pre-wrap leading-relaxed">{auction.description}</p>
          </div>

          {/* Details table */}
          <div className="bg-gray-50 rounded-md p-4 text-sm">
            <h3 className="font-semibold text-gray-900 mb-2 text-sm">Lot details</h3>
            <dl className="grid grid-cols-2 gap-x-4 gap-y-1.5 text-sm">
              <dt className="text-gray-400">Starting price</dt>
              <dd className="font-medium text-gray-700">${parseFloat(auction.startingPrice).toFixed(2)}</dd>
              {auction.reservePrice && (
                <><dt className="text-gray-400">Reserve</dt><dd className="font-medium text-gray-700">${parseFloat(auction.reservePrice).toFixed(2)}</dd></>
              )}
              <dt className="text-gray-400">Min increment</dt>
              <dd className="font-medium text-gray-700">${parseFloat(auction.minBidIncrement).toFixed(2)}</dd>
              <dt className="text-gray-400">Starts</dt>
              <dd className="font-medium text-gray-700">{new Date(auction.startTime).toLocaleString()}</dd>
              <dt className="text-gray-400">Ends</dt>
              <dd className="font-medium text-gray-700">{new Date(auction.endTime).toLocaleString()}</dd>
            </dl>
          </div>
        </div>
      </div>

      {/* Bid history */}
      <div className="mt-10">
        <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
          <FiTrendingUp size={16} /> Bid history <span className="text-sm font-normal text-gray-400">({bids.length})</span>
        </h2>
        {bids.length > 0 ? (
          <div className="border border-gray-200 rounded-md overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 text-left">
                  <th className="py-2.5 px-4 font-semibold text-gray-500 text-xs uppercase tracking-wider">Bidder</th>
                  <th className="py-2.5 px-4 font-semibold text-gray-500 text-xs uppercase tracking-wider">Amount</th>
                  <th className="py-2.5 px-4 font-semibold text-gray-500 text-xs uppercase tracking-wider">Time</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {bids.map((b, i) => (
                  <tr key={i} className={i === 0 ? 'bg-primary-50/50' : ''}>
                    <td className="py-2.5 px-4">
                      <span className="flex items-center gap-2">
                        <span className="w-5 h-5 bg-gray-200 rounded-full flex items-center justify-center text-[10px] font-semibold text-gray-600">
                          {b.bidderUsername?.[0]?.toUpperCase()}
                        </span>
                        {b.bidderUsername}
                        {i === 0 && <span className="badge-active ml-1">Highest</span>}
                      </span>
                    </td>
                    <td className="py-2.5 px-4 font-medium tabular-nums">${parseFloat(b.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                    <td className="py-2.5 px-4 text-gray-400">{new Date(b.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-12 border border-dashed border-gray-200 rounded-md">
            <p className="text-gray-400 text-sm">No bids yet. Be the first.</p>
          </div>
        )}
      </div>
    </div>
  );
}