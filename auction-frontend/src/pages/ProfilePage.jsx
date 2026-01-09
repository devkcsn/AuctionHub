import { useState, useEffect } from 'react';
import { useAuthStore } from '../store/authStore';
import { authAPI } from '../services/api';
import toast from 'react-hot-toast';
import { FiEdit3, FiSave, FiLock } from 'react-icons/fi';

export default function ProfilePage() {
  const { user, updateUser } = useAuthStore();
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [changingPw, setChangingPw] = useState(false);
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', username: '' });
  const [pw, setPw] = useState({ current: '', next: '', confirm: '' });
  const [pwLoading, setPwLoading] = useState(false);

  useEffect(() => {
    if (user) setForm({ firstName: user.firstName || '', lastName: user.lastName || '', email: user.email || '', username: user.username || '' });
  }, [user]);

  const saveProfile = async () => {
    setSaving(true);
    try {
      const r = await authAPI.updateProfile({ firstName: form.firstName, lastName: form.lastName });
      updateUser(r.data.data);
      toast.success('Saved');
      setEditing(false);
    } catch (err) { toast.error(err.message || 'Failed'); }
    finally { setSaving(false); }
  };

  const changePw = async () => {
    if (!pw.current || !pw.next) { toast.error('Fill all fields'); return; }
    if (pw.next !== pw.confirm) { toast.error('Passwords do not match'); return; }
    if (pw.next.length < 6) { toast.error('Min 6 characters'); return; }
    setPwLoading(true);
    try {
      await authAPI.changePassword({ currentPassword: pw.current, newPassword: pw.next });
      toast.success('Password changed');
      setPw({ current: '', next: '', confirm: '' });
      setChangingPw(false);
    } catch (err) { toast.error(err.message || 'Failed'); }
    finally { setPwLoading(false); }
  };

  return (
    <div className="max-w-xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Profile</h1>

      <div className="flex items-center gap-4 mb-8">
        <div className="w-14 h-14 bg-gray-900 rounded-full flex items-center justify-center">
          <span className="text-xl font-bold text-primary-300">
            {user?.firstName?.[0]?.toUpperCase() || user?.username?.[0]?.toUpperCase() || '?'}
          </span>
        </div>
        <div>
          <p className="font-semibold text-gray-900">{user?.firstName} {user?.lastName}</p>
          <p className="text-sm text-gray-400">@{user?.username}</p>
        </div>
      </div>

      <div className="border border-gray-200 rounded-md p-5 mb-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-semibold text-gray-900">Personal information</h3>
          {!editing ? (
            <button onClick={() => setEditing(true)} className="text-xs text-primary-600 hover:underline flex items-center gap-1"><FiEdit3 size={12} /> Edit</button>
          ) : (
            <button onClick={saveProfile} disabled={saving} className="text-xs text-primary-600 hover:underline flex items-center gap-1"><FiSave size={12} /> {saving ? 'Saving...' : 'Save'}</button>
          )}
        </div>
        <div className="space-y-3">
          <div className="grid grid-cols-2 gap-4">
            <InfoField label="First Name" value={form.firstName} editing={editing} onChange={(v) => setForm((f) => ({ ...f, firstName: v }))} />
            <InfoField label="Last Name" value={form.lastName} editing={editing} onChange={(v) => setForm((f) => ({ ...f, lastName: v }))} />
          </div>
          <div>
            <label className="text-xs text-gray-400 block mb-1">Email</label>
            <p className="text-sm text-gray-900">{form.email}</p>
          </div>
          <div>
            <label className="text-xs text-gray-400 block mb-1">Username</label>
            <p className="text-sm text-gray-900">{form.username}</p>
          </div>
        </div>
      </div>

      <div className="border border-gray-200 rounded-md p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-semibold text-gray-900 flex items-center gap-1.5"><FiLock size={13} /> Password</h3>
          {!changingPw && <button onClick={() => setChangingPw(true)} className="text-xs text-primary-600 hover:underline">Change</button>}
        </div>
        {changingPw ? (
          <div className="space-y-3">
            <input type="password" placeholder="Current password" value={pw.current}
              onChange={(e) => setPw((p) => ({ ...p, current: e.target.value }))} className="input-field" />
            <input type="password" placeholder="New password" value={pw.next}
              onChange={(e) => setPw((p) => ({ ...p, next: e.target.value }))} className="input-field" />
            <input type="password" placeholder="Confirm new password" value={pw.confirm}
              onChange={(e) => setPw((p) => ({ ...p, confirm: e.target.value }))} className="input-field" />
            <div className="flex gap-2">
              <button onClick={changePw} disabled={pwLoading} className="btn-primary text-sm">{pwLoading ? 'Updating...' : 'Update'}</button>
              <button onClick={() => { setChangingPw(false); setPw({ current: '', next: '', confirm: '' }); }} className="btn-secondary text-sm">Cancel</button>
            </div>
          </div>
        ) : (
          <p className="text-sm text-gray-400">••••••••</p>
        )}
      </div>
    </div>
  );
}

function InfoField({ label, value, editing, onChange }) {
  return (
    <div>
      <label className="text-xs text-gray-400 block mb-1">{label}</label>
      {editing ? (
        <input type="text" value={value} onChange={(e) => onChange(e.target.value)} className="input-field" />
      ) : (
        <p className="text-sm text-gray-900">{value || '\u2014'}</p>
      )}
    </div>
  );
}