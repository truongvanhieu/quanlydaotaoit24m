import React, { useState } from 'react';
import { Plus, X, Search, UserCheck } from 'lucide-react';
import { invigilatorsApi } from '../services/api';

const initial = { staff_code: '', full_name: '', department: '', phone: '' };

/** Chọn nhiều cán bộ hiện có hoặc tạo cán bộ mới, không rời biểu mẫu lịch thi. */
export default function InvigilatorSelector({ invigilators = [], selected = [], onChange, onCreated, limit = 1 }) {
  const [open, setOpen] = useState(false);
  const [fields, setFields] = useState(initial);
  const [search, setSearch] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const numberLimit = Math.max(1, Number(limit) || 1);
  const matching = invigilators.filter(item =>
    `${item.full_name} ${item.staff_code} ${item.department || ''}`.toLocaleLowerCase('vi')
      .includes(search.trim().toLocaleLowerCase('vi'))
  );
  const toggle = (id) => {
    if (selected.includes(id)) onChange(selected.filter(value => value !== id));
    else if (selected.length < numberLimit) {
      setError('');
      onChange([...selected, id]);
    } else setError(`Chỉ có thể chọn tối đa ${numberLimit} cán bộ theo số lượng đã cấu hình.`);
  };

  const create = async () => {
    if (!fields.full_name.trim()) { setError('Vui lòng nhập họ và tên cán bộ.'); return; }
    if (selected.length >= numberLimit) {
      setError('Hãy tăng số lượng cán bộ hoặc bỏ chọn một cán bộ trước khi thêm.');
      return;
    }
    setSaving(true);
    setError('');
    try {
      const response = await invigilatorsApi.create({
        staff_code: fields.staff_code.trim(),
        full_name: fields.full_name.trim(),
        department: fields.department.trim(),
        phone: fields.phone.trim(),
      });
      const item = response.data?.data;
      if (!item?.id) throw new Error('Chưa nhận được mã cán bộ vừa tạo.');
      await onCreated(item);
      onChange([...selected, item.id]);
      setFields(initial);
      setOpen(false);
      setSearch('');
    } catch (err) { setError(err.message || 'Không thể lưu cán bộ.'); }
    finally { setSaving(false); }
  };

  return (
    <div className="md:col-span-2 rounded-lg border border-slate-200 p-4 bg-slate-50/50 space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div>
          <label className="text-xs font-semibold text-slate-700 uppercase tracking-wider">Họ tên cán bộ coi thi</label>
          <p className="text-xs text-slate-500 mt-1">
            Đã chọn {selected.length}/{numberLimit} cán bộ. Có thể thêm cán bộ mới và chọn ngay tại đây.
          </p>
        </div>
        <button type="button" onClick={() => { setOpen(v => !v); setError(''); }}
          className="inline-flex items-center gap-1 text-xs font-semibold text-teal-700 hover:text-teal-900">
          {open ? <X className="w-3.5 h-3.5" /> : <Plus className="w-3.5 h-3.5" />}
          {open ? 'Đóng nhập tay' : 'Tự nhập cán bộ mới'}
        </button>
      </div>

      {open && (
        <div className="rounded-lg border border-teal-200 bg-teal-50 p-3 space-y-3">
          <strong className="text-sm text-teal-800">Thêm cán bộ coi thi vào SQL Server</strong>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {[
              ['staff_code', 'Mã cán bộ (để trống sẽ tự tạo)', 'CB001', 30],
              ['full_name', 'Họ và tên *', 'Nguyễn Văn A', 150],
              ['department', 'Khoa / Bộ môn', 'Công nghệ thông tin', 150],
              ['phone', 'Số điện thoại (không bắt buộc)', '09...', 20],
            ].map(([key, label, placeholder, maxLength]) => (
              <label key={key} className="block text-xs font-medium text-slate-700">
                {label}
                <input value={fields[key]} type="text" maxLength={maxLength} placeholder={placeholder}
                  onChange={(e) => setFields(prev => ({ ...prev, [key]: e.target.value }))}
                  className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm font-normal focus:outline-none focus:ring-2 focus:ring-teal-500" />
              </label>
            ))}
          </div>
          <div className="flex justify-end">
            <button type="button" disabled={saving} onClick={create}
              className="rounded-lg bg-teal-600 text-white px-4 py-2 text-xs font-semibold hover:bg-teal-700 disabled:opacity-50">
              {saving ? 'Đang lưu...' : 'Lưu cán bộ và chọn'}
            </button>
          </div>
        </div>
      )}

      <div className="relative">
        <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
        <input type="search" value={search} onChange={(e) => setSearch(e.target.value)}
          placeholder="Tìm cán bộ theo họ tên, mã hoặc khoa..."
          className="w-full rounded-lg border border-slate-300 bg-white pl-9 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500" />
      </div>

      <div className="max-h-48 overflow-y-auto rounded-lg border border-slate-200 bg-white divide-y divide-slate-100">
        {matching.length === 0 ? (
          <p className="px-3 py-3 text-xs text-slate-500">Chưa có cán bộ phù hợp. Chọn “Tự nhập cán bộ mới” để thêm.</p>
        ) : matching.map(person => {
          const checked = selected.includes(person.id);
          return (
            <label key={person.id} className={`flex gap-3 items-start px-3 py-2.5 text-sm cursor-pointer hover:bg-teal-50 ${checked ? 'bg-teal-50/60' : ''}`}>
              <input type="checkbox" checked={checked}
                disabled={!checked && selected.length >= numberLimit}
                onChange={() => toggle(person.id)} className="accent-teal-600 mt-1" />
              <span className="flex-1 min-w-0">
                <span className="font-medium text-slate-800">{person.full_name}</span>
                <span className="text-xs text-slate-500"> — {person.staff_code}{person.department ? ` • ${person.department}` : ''}</span>
              </span>
              {checked && <UserCheck className="w-4 h-4 text-teal-600 shrink-0" />}
            </label>
          );
        })}
      </div>
      {error && <p role="alert" className="text-xs text-rose-700">{error}</p>}
      <p className="text-xs text-slate-500">Bạn có thể lưu lịch thi trước rồi bổ sung cán bộ sau. Số người được chọn không vượt quá số lượng cán bộ coi thi.</p>
    </div>
  );
}
