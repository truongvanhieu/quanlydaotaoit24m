import React, { useState } from 'react';
import { Plus, X } from 'lucide-react';
import { lookupsApi, examTypesApi } from '../services/api';

const definitions = {
  courses: { title: 'môn học', fields: [
    ['course_code', 'Mã môn học', 'text', 50, 'INT1501'],
    ['course_name', 'Tên môn học', 'text', 200, 'Lập trình Java nâng cao'],
    ['credits', 'Số tín chỉ', 'number', 2, '3'],
  ] },
  semesters: { title: 'học kỳ', fields: [
    ['semester_code', 'Mã học kỳ', 'text', 50, 'HK3-2026'],
    ['semester_name', 'Tên học kỳ', 'text', 100, 'Học kỳ 3'],
    ['academic_year', 'Năm học', 'text', 20, '2025-2026'],
  ] },
  rooms: { title: 'phòng thi vật lý', fields: [
    ['room_number', 'Mã / tên phòng', 'text', 50, 'P.501-C'],
    ['building', 'Tòa nhà / địa điểm', 'text', 100, 'Tòa nhà C'],
    ['capacity', 'Sức chứa', 'number', 5, '40'],
  ] },
  students: { title: 'sinh viên', fields: [
    ['student_code', 'Mã sinh viên', 'text', 20, 'SV2026001'],
    ['full_name', 'Họ tên', 'text', 150, 'Nguyễn Văn A'],
    ['class_name', 'Lớp', 'text', 50, 'IT24M'],
    ['birth_date', 'Ngày sinh (không bắt buộc)', 'date', 10, ''],
    ['email', 'Email (không bắt buộc)', 'email', 100, 'sv@example.com'],
    ['phone', 'Số điện thoại (không bắt buộc)', 'tel', 20, ''],
  ] },
  examTypes: { title: 'loại kỳ thi', fields: [
    ['name', 'Tên loại kỳ thi', 'text', 150, 'Thi giữa kỳ'],
    ['description', 'Mô tả (không bắt buộc)', 'text', 500, ''],
  ] },
};

export default function QuickAddLookup({ kind, onCreated }) {
  const config = definitions[kind];
  const [open, setOpen] = useState(false);
  const [values, setValues] = useState({});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  if (!config) return null;

  const save = async () => {
    const required = config.fields.filter(([key]) => !['email', 'phone', 'class_name', 'description', 'birth_date'].includes(key));
    const missing = required.find(([key]) => !String(values[key] ?? '').trim());
    if (missing) { setError(`Vui lòng nhập ${missing[1].toLowerCase()}.`); return; }
    setSaving(true);
    setError('');
    try {
      const response = kind === 'examTypes'
        ? await examTypesApi.create(values)
        : await lookupsApi.create(kind, values);
      const createdId = response.data?.data?.id;
      if (!createdId) throw new Error('Máy chủ chưa trả về ID của bản ghi mới.');
      await onCreated(createdId);
      setValues({});
      setOpen(false);
    } catch (err) {
      setError(err.message || 'Không thể lưu dữ liệu.');
    } finally { setSaving(false); }
  };

  return (
    <div className="mt-2">
      {!open ? (
        <button type="button" onClick={() => { setError(''); setOpen(true); }}
          className="inline-flex items-center gap-1 text-xs font-semibold text-teal-700 hover:text-teal-900">
          <Plus className="w-3.5 h-3.5" /> Tự nhập {config.title} mới
        </button>
      ) : (
        <div className="rounded-lg border border-teal-200 bg-teal-50 p-3 space-y-3">
          <div className="flex justify-between items-center gap-2">
            <strong className="text-sm text-teal-800">Thêm {config.title} trực tiếp</strong>
            <button type="button" className="text-slate-500 hover:text-slate-800" aria-label="Đóng nhập tay"
              onClick={() => { setOpen(false); setError(''); }}><X className="w-4 h-4" /></button>
          </div>
          <div className="grid grid-cols-1 gap-2">
            {config.fields.map(([key, label, type, max, placeholder]) => (
              <label key={key} className="block text-xs font-medium text-slate-700">
                {label}
                <input type={type} maxLength={type === 'number' ? undefined : max}
                  min={type === 'number' ? 1 : undefined} max={type === 'number' ? (kind === 'courses' ? 30 : 10000) : undefined}
                  value={values[key] ?? ''} placeholder={placeholder}
                  onChange={(e) => setValues(prev => ({ ...prev, [key]: e.target.value }))}
                  onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); if (!saving) save(); } }}
                  className="mt-1 w-full rounded-md border border-slate-300 bg-white px-2.5 py-2 font-normal focus:outline-none focus:ring-2 focus:ring-teal-500" />
              </label>
            ))}
          </div>
          {error && <p role="alert" className="text-xs text-red-700">{error}</p>}
          <div className="flex items-center justify-end gap-2">
            <button type="button" onClick={() => setOpen(false)} className="px-3 py-1.5 text-xs rounded-md bg-white border border-slate-300">Hủy</button>
            <button type="button" disabled={saving} onClick={save} className="px-3 py-1.5 text-xs font-semibold rounded-md bg-teal-600 text-white disabled:opacity-50">
              {saving ? 'Đang lưu...' : 'Lưu vào SQL Server'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
