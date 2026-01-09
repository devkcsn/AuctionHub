import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auctionAPI } from '../services/api';
import toast from 'react-hot-toast';

const CATEGORIES = [
  'ELECTRONICS', 'FASHION', 'HOME_GARDEN', 'SPORTS', 'TOYS',
  'VEHICLES', 'ART', 'COLLECTIBLES', 'BOOKS', 'JEWELRY', 'OTHER',
];

export default function CreateAuctionPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    title: '', description: '', category: 'ELECTRONICS',
    startingPrice: '', reservePrice: '', minBidIncrement: '1.00',
    startTime: '', endTime: '', imageUrl: '',
  });
  const [errors, setErrors] = useState({});

  const up = (f) => (e) => { setForm((p) => ({ ...p, [f]: e.target.value })); setErrors((p) => ({ ...p, [f]: null })); };

  const validate = () => {
    const e = {};
    if (!form.title.trim()) e.title = 'Required';
    if (!form.description.trim()) e.description = 'Required';
    if (!form.startingPrice || parseFloat(form.startingPrice) <= 0) e.startingPrice = 'Enter a valid price';
    if (!form.minBidIncrement || parseFloat(form.minBidIncrement) <= 0) e.minBidIncrement = 'Required';
    if (!form.startTime) e.startTime = 'Required';
    if (!form.endTime) e.endTime = 'Required';
    if (form.startTime && form.endTime && new Date(form.startTime) >= new Date(form.endTime)) e.endTime = 'Must be after start';
    if (form.reservePrice && parseFloat(form.reservePrice) < parseFloat(form.startingPrice)) e.reservePrice = 'Must be >= starting price';
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const payload = {
        title: form.title.trim(), description: form.description.trim(), category: form.category,
        startingPrice: parseFloat(form.startingPrice),
        reservePrice: form.reservePrice ? parseFloat(form.reservePrice) : null,
        minBidIncrement: parseFloat(form.minBidIncrement),
        startTime: new Date(form.startTime).toISOString(),
        endTime: new Date(form.endTime).toISOString(),
        imageUrls: form.imageUrl ? [form.imageUrl.trim()] : [],
      };
      const res = await auctionAPI.create(payload);
      toast.success('Lot created!');
      navigate(`/auctions/${res.data.data?.id || ''}`);
    } catch (err) { toast.error(err.message || 'Failed to create'); }
    finally { setLoading(false); }
  };

  const ic = (f) => `input-field ${errors[f] ? 'border-red-400' : ''}`;
  const now = new Date();
  const defaultStart = new Date(now.getTime() + 60000).toISOString().slice(0, 16);

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Create a lot</h1>
      <p className="text-sm text-gray-500 mb-8">Fill out the details below to list your item.</p>

      <form onSubmit={handleSubmit} className="space-y-5">
        <Field label="Title" error={errors.title}>
          <input type="text" value={form.title} onChange={up('title')} className={ic('title')} placeholder="e.g. Vintage Rolex Watch 1965" />
        </Field>

        <Field label="Description" error={errors.description}>
          <textarea rows={4} value={form.description} onChange={up('description')} className={ic('description')} placeholder="Describe the item..." />
        </Field>

        <Field label="Category">
          <select value={form.category} onChange={up('category')} className="input-field">
            {CATEGORIES.map((c) => <option key={c} value={c}>{c.replace('_', ' ')}</option>)}
          </select>
        </Field>

        <div className="grid grid-cols-3 gap-4">
          <Field label="Starting price" error={errors.startingPrice}>
            <input type="number" step="0.01" min="0.01" value={form.startingPrice} onChange={up('startingPrice')} className={ic('startingPrice')} placeholder="0.00" />
          </Field>
          <Field label="Reserve" sub="optional" error={errors.reservePrice}>
            <input type="number" step="0.01" min="0" value={form.reservePrice} onChange={up('reservePrice')} className={ic('reservePrice')} placeholder="—" />
          </Field>
          <Field label="Min increment" error={errors.minBidIncrement}>
            <input type="number" step="0.01" min="0.01" value={form.minBidIncrement} onChange={up('minBidIncrement')} className={ic('minBidIncrement')} placeholder="1.00" />
          </Field>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Field label="Start time" error={errors.startTime}>
            <input type="datetime-local" value={form.startTime} onChange={up('startTime')} min={defaultStart} className={ic('startTime')} />
          </Field>
          <Field label="End time" error={errors.endTime}>
            <input type="datetime-local" value={form.endTime} onChange={up('endTime')} min={form.startTime || defaultStart} className={ic('endTime')} />
          </Field>
        </div>

        <Field label="Image URL" sub="optional">
          <input type="url" value={form.imageUrl} onChange={up('imageUrl')} className="input-field" placeholder="https://..." />
        </Field>

        {form.imageUrl && (
          <div className="rounded-md overflow-hidden bg-gray-100 max-h-48">
            <img src={form.imageUrl} alt="Preview" className="w-full h-48 object-cover" onError={(e) => { e.target.style.display = 'none'; }} />
          </div>
        )}

        <div className="flex gap-3 pt-2">
          <button type="submit" disabled={loading} className="btn-primary flex-1">
            {loading ? 'Creating...' : 'Create lot'}
          </button>
          <button type="button" onClick={() => navigate('/auctions')} className="btn-secondary">Cancel</button>
        </div>
      </form>
    </div>
  );
}

function Field({ label, sub, error, children }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1.5">
        {label} {sub && <span className="text-gray-400 font-normal text-xs">({sub})</span>}
      </label>
      {children}
      {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
    </div>
  );
}