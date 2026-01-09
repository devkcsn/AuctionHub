import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { notificationAPI } from '../services/api';
import wsService from '../services/websocket';
import {
  FiMenu, FiX, FiSearch, FiBell, FiUser,
  FiLogOut, FiPlus, FiGrid, FiChevronDown
} from 'react-icons/fi';
import Logo from './Logo';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const profileRef = useRef(null);
  const notifRef = useRef(null);

  useEffect(() => {
    if (isAuthenticated) {
      fetchUnreadCount();
      if (user?.username) {
        wsService.subscribeToNotifications(user.username, (msg) => {
          setUnreadCount((prev) => prev + 1);
          setNotifications((prev) => [msg, ...prev.slice(0, 9)]);
        });
      }
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    const handleClick = (e) => {
      if (profileRef.current && !profileRef.current.contains(e.target)) setProfileOpen(false);
      if (notifRef.current && !notifRef.current.contains(e.target)) setNotifOpen(false);
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const fetchUnreadCount = async () => {
    try {
      const res = await notificationAPI.getUnreadCount();
      setUnreadCount(res.data.data || 0);
    } catch (err) { /* */ }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/auctions?search=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
    }
  };

  const handleLogout = () => { logout(); navigate('/'); };

  const markAllRead = async () => {
    try { await notificationAPI.markAllAsRead(); setUnreadCount(0); } catch {}
  };

  const navLink = (path, label) => (
    <Link to={path} className={`text-sm transition-colors ${
      location.pathname === path ? 'text-white' : 'text-gray-400 hover:text-white'
    }`}>{label}</Link>
  );

  return (
    <nav className="bg-gray-950 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6">
        <div className="flex items-center justify-between h-14">
          <Link to="/" className="flex items-center">
            <span className="hidden sm:inline"><Logo variant="full" theme="light" size={26} /></span>
            <span className="sm:hidden"><Logo variant="icon" theme="light" size={26} /></span>
          </Link>

          <div className="hidden md:flex items-center gap-6">
            {navLink('/auctions', 'Browse')}
            {isAuthenticated && navLink('/create-auction', 'Sell')}
            {isAuthenticated && navLink('/dashboard', 'Dashboard')}
          </div>

          <div className="hidden md:flex items-center gap-3">
            <form onSubmit={handleSearch} className="relative">
              <FiSearch className="absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-500" size={14} />
              <input type="text" placeholder="Search..." value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="bg-gray-900 border border-gray-800 text-white text-sm rounded-md pl-8 pr-3 py-1.5 w-44
                  placeholder:text-gray-500 focus:outline-none focus:border-gray-600 focus:w-56 transition-all" />
            </form>

            {isAuthenticated ? (
              <>
                <div className="relative" ref={notifRef}>
                  <button onClick={() => setNotifOpen(!notifOpen)}
                    className="relative p-1.5 text-gray-400 hover:text-white transition-colors">
                    <FiBell size={18} />
                    {unreadCount > 0 && (
                      <span className="absolute -top-0.5 -right-0.5 bg-accent-600 text-white text-[10px] font-bold rounded-full w-4 h-4 flex items-center justify-center">
                        {unreadCount > 9 ? '9+' : unreadCount}
                      </span>
                    )}
                  </button>
                  {notifOpen && (
                    <div className="absolute right-0 mt-2 w-80 bg-white border border-gray-200 rounded-md shadow-lg z-50">
                      <div className="p-3 border-b border-gray-100 flex justify-between items-center">
                        <span className="text-sm font-semibold text-gray-900">Notifications</span>
                        {unreadCount > 0 && (
                          <button onClick={markAllRead} className="text-xs text-primary-600 hover:underline">Mark all read</button>
                        )}
                      </div>
                      <div className="max-h-64 overflow-y-auto divide-y divide-gray-50">
                        {notifications.length > 0 ? (
                          notifications.map((n, i) => (
                            <div key={i} className="px-3 py-2.5 hover:bg-gray-50 text-sm text-gray-700">{n.message}</div>
                          ))
                        ) : (
                          <p className="p-4 text-center text-gray-400 text-xs">No notifications</p>
                        )}
                      </div>
                      <Link to="/dashboard" onClick={() => setNotifOpen(false)}
                        className="block py-2 text-center text-xs text-primary-600 border-t border-gray-100 hover:bg-gray-50">View all</Link>
                    </div>
                  )}
                </div>

                <div className="relative" ref={profileRef}>
                  <button onClick={() => setProfileOpen(!profileOpen)}
                    className="flex items-center gap-1.5 text-gray-400 hover:text-white transition-colors">
                    <div className="w-6 h-6 bg-gray-800 rounded-full flex items-center justify-center text-xs font-medium text-primary-300">
                      {user?.username?.[0]?.toUpperCase() || 'U'}
                    </div>
                    <FiChevronDown size={12} />
                  </button>
                  {profileOpen && (
                    <div className="absolute right-0 mt-2 w-52 bg-white border border-gray-200 rounded-md shadow-lg z-50">
                      <div className="px-3 py-2.5 border-b border-gray-100">
                        <p className="text-sm font-medium text-gray-900">{user?.fullName || user?.username}</p>
                        <p className="text-xs text-gray-400">{user?.email}</p>
                      </div>
                      <div className="py-1">
                        <Link to="/dashboard" onClick={() => setProfileOpen(false)}
                          className="flex items-center gap-2 px-3 py-2 text-sm text-gray-600 hover:bg-gray-50"><FiGrid size={14} /> Dashboard</Link>
                        <Link to="/profile" onClick={() => setProfileOpen(false)}
                          className="flex items-center gap-2 px-3 py-2 text-sm text-gray-600 hover:bg-gray-50"><FiUser size={14} /> Profile</Link>
                        <button onClick={handleLogout}
                          className="w-full flex items-center gap-2 px-3 py-2 text-sm text-red-600 hover:bg-red-50"><FiLogOut size={14} /> Sign out</button>
                      </div>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="flex items-center gap-2">
                <Link to="/login" className="text-sm text-gray-400 hover:text-white transition-colors">Sign in</Link>
                <Link to="/register" className="text-sm bg-primary-600 text-white px-3.5 py-1.5 rounded-md hover:bg-primary-700 transition-colors">Sign up</Link>
              </div>
            )}
          </div>

          <button onClick={() => setMenuOpen(!menuOpen)} className="md:hidden text-gray-400 p-1.5">
            {menuOpen ? <FiX size={20} /> : <FiMenu size={20} />}
          </button>
        </div>

        {menuOpen && (
          <div className="md:hidden border-t border-gray-800 py-3 space-y-1">
            <form onSubmit={handleSearch} className="mb-3">
              <div className="relative">
                <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={14} />
                <input type="text" placeholder="Search auctions..." value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-800 text-white text-sm rounded-md pl-9 pr-3 py-2 placeholder:text-gray-500 focus:outline-none" />
              </div>
            </form>
            <Link to="/" className="block py-2 text-sm text-gray-300 hover:text-white" onClick={() => setMenuOpen(false)}>Home</Link>
            <Link to="/auctions" className="block py-2 text-sm text-gray-300 hover:text-white" onClick={() => setMenuOpen(false)}>Browse</Link>
            {isAuthenticated ? (
              <>
                <Link to="/create-auction" className="block py-2 text-sm text-gray-300 hover:text-white" onClick={() => setMenuOpen(false)}>Sell</Link>
                <Link to="/dashboard" className="block py-2 text-sm text-gray-300 hover:text-white" onClick={() => setMenuOpen(false)}>Dashboard</Link>
                <Link to="/profile" className="block py-2 text-sm text-gray-300 hover:text-white" onClick={() => setMenuOpen(false)}>Profile</Link>
                <button onClick={handleLogout} className="block py-2 text-sm text-red-400">Sign out</button>
              </>
            ) : (
              <div className="flex gap-3 pt-2">
                <Link to="/login" className="btn-secondary text-xs flex-1 text-center" onClick={() => setMenuOpen(false)}>Sign in</Link>
                <Link to="/register" className="btn-primary text-xs flex-1 text-center" onClick={() => setMenuOpen(false)}>Sign up</Link>
              </div>
            )}
          </div>
        )}
      </div>
    </nav>
  );
}