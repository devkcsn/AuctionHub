import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { auctionAPI } from '../services/api';
import AuctionCard from '../components/AuctionCard';
import LoadingSpinner from '../components/LoadingSpinner';
import { FiSearch } from 'react-icons/fi';

const CATEGORIES = [
  { key: '', label: 'All' },
  { key: 'ELECTRONICS', label: 'Electronics' },
  { key: 'FASHION', label: 'Fashion' },
  { key: 'HOME_GARDEN', label: 'Home & Garden' },
  { key: 'SPORTS', label: 'Sports' },
  { key: 'COLLECTIBLES', label: 'Collectibles' },
  { key: 'ART', label: 'Art' },
  { key: 'VEHICLES', label: 'Vehicles' },
  { key: 'JEWELRY', label: 'Jewelry' },
  { key: 'BOOKS', label: 'Books' },
  { key: 'TOYS', label: 'Toys' },
  { key: 'MUSIC', label: 'Music' },
  { key: 'OTHER', label: 'Other' },
];

const SORT_OPTIONS = [
  { key: 'createdAt-desc', label: 'Newest' },
  { key: 'createdAt-asc', label: 'Oldest' },
  { key: 'currentPrice-asc', label: 'Price: low to high' },
  { key: 'currentPrice-desc', label: 'Price: high to low' },
  { key: 'endTime-asc', label: 'Ending soon' },
  { key: 'bidCount-desc', label: 'Most bids' },
];

export default function AuctionListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [auctions, setAuctions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchQuery, setSearchQuery] = useState(searchParams.get('search') || '');
  const [category, setCategory] = useState(searchParams.get('category') || '');
  const [sortKey, setSortKey] = useState('createdAt-desc');

  useEffect(() => { fetchAuctions(); }, [page, category, sortKey, searchParams]);

  const fetchAuctions = async () => {
    setLoading(true);
    try {
      const [sortBy, sortDir] = sortKey.split('-');
      const search = searchParams.get('search');
      let res;
      if (search) res = await auctionAPI.search(search, page, 12);
      else if (category) res = await auctionAPI.getByCategory(category, page, 12);
      else res = await auctionAPI.getActive(page, 12, sortBy, sortDir);
      setAuctions(res.data.data?.content || []);
      setTotalPages(res.data.data?.totalPages || 0);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    searchQuery.trim() ? setSearchParams({ search: searchQuery.trim() }) : setSearchParams({});
  };

  const handleCategory = (c) => {
    setCategory(c);
    setPage(0);
    c ? setSearchParams({ category: c }) : setSearchParams({});
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Browse auctions</h1>
      <p className="text-sm text-gray-500 mb-6">Find something worth bidding on.</p>

      <div className="flex flex-col sm:flex-row gap-3 mb-8">
        <form onSubmit={handleSearch} className="flex-1">
          <div className="relative">
            <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={15} />
            <input type="text" placeholder="Search lots..." value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)} className="input-field pl-9" />
          </div>
        </form>
        <select value={category} onChange={(e) => handleCategory(e.target.value)} className="input-field sm:w-40">
          {CATEGORIES.map((c) => <option key={c.key} value={c.key}>{c.label}</option>)}
        </select>
        <select value={sortKey} onChange={(e) => { setSortKey(e.target.value); setPage(0); }} className="input-field sm:w-44">
          {SORT_OPTIONS.map((o) => <option key={o.key} value={o.key}>{o.label}</option>)}
        </select>
      </div>

      {loading ? (
        <LoadingSpinner size="lg" text="Loading lots..." />
      ) : auctions.length > 0 ? (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {auctions.map((a) => <AuctionCard key={a.id} auction={a} />)}
          </div>
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-3 mt-10">
              <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0} className="btn-secondary text-sm">Previous</button>
              <span className="text-sm text-gray-500 tabular-nums">{page + 1} / {totalPages}</span>
              <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))} disabled={page >= totalPages - 1} className="btn-secondary text-sm">Next</button>
            </div>
          )}
        </>
      ) : (
        <div className="text-center py-20">
          <p className="text-gray-400 text-sm">No lots found. Try a different search or filter.</p>
        </div>
      )}
    </div>
  );
}