import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { auctionAPI, bidAPI, notificationAPI } from '../services/api';
import { useAuthStore } from '../store/authStore';
import AuctionCard from '../components/AuctionCard';
import LoadingSpinner from '../components/LoadingSpinner';
import toast from 'react-hot-toast';
import { FiPackage, FiTrendingUp, FiAward, FiBell, FiPlus, FiCheck } from 'react-icons/fi';

const TABS = [
  { key: 'myAuctions', label: 'My lots', icon: FiPackage },
  { key: 'myBids', label: 'My bids', icon: FiTrendingUp },
  { key: 'won', label: 'Won', icon: FiAward },
  { key: 'notifications', label: 'Notifications', icon: FiBell },
];

export default function DashboardPage() {
  const { user } = useAuthStore();
  const [tab, setTab] = useState('myAuctions');
  const [data, setData] = useState([]);
  const [notifs, setNotifs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchData(); }, [tab]);

  const fetchData = async () => {
    setLoading(true);
    try {
      if (tab === 'myAuctions') { const r = await auctionAPI.getMyAuctions(0, 20); setData(r.data.data?.content || []); }
      else if (tab === 'myBids') { const r = await auctionAPI.getBiddingAuctions(0, 20); setData(r.data.data?.content || []); }
      else if (tab === 'won') { const r = await auctionAPI.getWonAuctions(0, 20); setData(r.data.data?.content || []); }
      else { const r = await notificationAPI.getAll(); setNotifs(r.data.data || []); }
    } catch { toast.error('Failed to load'); }
    finally { setLoading(false); }
  };

  const markAllRead = async () => {
    try { await notificationAPI.markAllAsRead(); setNotifs((p) => p.map((n) => ({ ...n, read: true }))); toast.success('Done'); }
    catch { toast.error('Failed'); }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-sm text-gray-500 mt-0.5">Welcome back, {user?.fullName || user?.username}</p>
        </div>
        <Link to="/create-auction" className="btn-primary flex items-center gap-1.5 text-sm"><FiPlus size={14} /> New lot</Link>
      </div>

      <div className="flex gap-1 border-b border-gray-200 mb-6 overflow-x-auto">
        {TABS.map((t) => {
          const Icon = t.icon;
          const active = tab === t.key;
          return (
            <button key={t.key} onClick={() => setTab(t.key)}
              className={`flex items-center gap-1.5 px-4 py-2.5 text-sm font-medium border-b-2 transition-colors whitespace-nowrap -mb-px ${
                active ? 'border-primary-600 text-primary-700' : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}>
              <Icon size={14} /> {t.label}
            </button>
          );
        })}
      </div>

      {loading ? <LoadingSpinner /> : (
        <>
          {tab !== 'notifications' && (
            data.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                {data.map((a) => <AuctionCard key={a.id} auction={a} />)}
              </div>
            ) : (
              <div className="text-center py-20">
                <p className="text-gray-400 text-sm mb-4">
                  {tab === 'myAuctions' && "No lots yet."}
                  {tab === 'myBids' && "No bids placed."}
                  {tab === 'won' && "No wins yet."}
                </p>
                {tab === 'myAuctions' && <Link to="/create-auction" className="btn-primary text-sm">Create your first lot</Link>}
                {tab === 'myBids' && <Link to="/auctions" className="btn-primary text-sm">Browse auctions</Link>}
              </div>
            )
          )}

          {tab === 'notifications' && (
            <>
              {notifs.length > 0 && (
                <div className="flex justify-end mb-3">
                  <button onClick={markAllRead} className="text-xs text-primary-600 hover:underline flex items-center gap-1">
                    <FiCheck size={12} /> Mark all read
                  </button>
                </div>
              )}
              {notifs.length > 0 ? (
                <div className="space-y-2">
                  {notifs.map((n) => (
                    <div key={n.id} className={`flex items-start gap-3 p-3 rounded-md border text-sm transition-colors ${
                      n.read ? 'bg-white border-gray-100' : 'bg-primary-50/40 border-primary-100'
                    }`}>
                      {!n.read && <span className="w-1.5 h-1.5 bg-primary-500 rounded-full mt-1.5 shrink-0" />}
                      <div className="flex-1 min-w-0">
                        <p className="font-medium text-gray-900 text-sm">{n.title}</p>
                        <p className="text-gray-500 text-xs mt-0.5">{n.message}</p>
                        <p className="text-gray-400 text-[11px] mt-1">{new Date(n.createdAt).toLocaleString()}</p>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-20">
                  <p className="text-gray-400 text-sm">No notifications</p>
                </div>
              )}
            </>
          )}
        </>
      )}
    </div>
  );
}