import React, { useEffect, useState } from 'react';
import Modal from './Modal';
import { examRegistrationsApi } from '../services/api';
const field = 'w-full border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white';
export default function ClassifyExamGroup({ lookups, initialExam, onClose, onSaved }) {
  const [exam, setExam] = useState(initialExam || lookups.exams[0]?.id || '');
  const [room, setRoom] = useState('');
  const [rows, setRows] = useState([]);
  const [selected, setSelected] = useState([]);
  const [loading, setLoading] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const rooms = lookups.examRooms.filter(r => r.exam_id === exam);
  const chosen = rows.filter(r => selected.includes(r.id));
  const classes = [...new Set(chosen.map(r => (r.student_class || '').trim().toUpperCase()).filter(Boolean))];
  useEffect(() => {
    let active = true;
    setRows([]); setSelected([]); setError('');
    if (!exam || !room) { setLoading(false); return; }
    setLoading(true);
    (async () => {
      let all = [], page = 1, totalPages = 1;
      do {
        const res = await examRegistrationsApi.getAll({exam_id: exam, exam_room_id: room, group_type: 'single', page, limit: 200});
        if (!Array.isArray(res.data.data)) throw Error('Không lấy được danh sách đăng ký.');
        all.push(...res.data.data.filter(r => !r.merged_group_id));
        totalPages = res.data.pagination?.totalPages || 1; page++;
      } while (active && page <= totalPages);
      if (active) setRows(all.sort((a,b) => (a.roll_number || '').localeCompare(b.roll_number || '', 'vi', {numeric:true})));
    })().catch(err => { if (active) setError(err.message); }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [exam, room]);
  const submit = async e => {
    e.preventDefault(); setError('');
    if (busy || loading) return;
    if (!room || classes.length < 2) { setError('Chọn phòng và ít nhất hai sinh viên thuộc hai lớp khác nhau.'); return; }
    setBusy(true);
    try {
      const res = await examRegistrationsApi.classifyGroup({exam_id: exam, exam_room_id: room, registration_ids: selected});
      if (!res.data.success) throw Error(res.data.message || 'Không lưu được lớp gộp.');
      onSaved(res.data.data.count, res.data.data.merged_group_id);
    } catch (err) { setError(err.message); } finally { setBusy(false); }
  };
  return <Modal isOpen title="Phân loại lớp gộp cũ" maxWidth="max-w-3xl" onClose={() => { if (!busy) onClose(); }}>
    <form onSubmit={submit} className="space-y-4">
      <p className="text-sm text-slate-600">Chọn các thí sinh đã được gộp trước đây trong cùng ca và phòng. Thao tác chỉ đánh dấu nhóm, không đăng ký lại, không đổi SBD, phòng hoặc điểm thi.</p>
      <label className="block text-sm" htmlFor="classify-exam">Ca thi</label><select id="classify-exam" className={field} value={exam} disabled={busy} onChange={e => { setExam(e.target.value); setRoom(''); }}><option value="">-- Chọn ca thi --</option>{lookups.exams.map(ex => <option key={ex.id} value={ex.id}>{ex.course_code} – {ex.course_name} ({ex.exam_date} {ex.start_time})</option>)}</select>
      <label className="block text-sm" htmlFor="classify-room">Phòng thi</label><select id="classify-room" className={field} value={room} disabled={busy} onChange={e => setRoom(e.target.value)}><option value="">-- Chọn phòng thi --</option>{rooms.map(r => <option key={r.id} value={r.id}>{r.room_number} – {r.building}</option>)}</select>
      <div className="flex gap-4 text-sm"><button type="button" disabled={busy || loading} onClick={() => setSelected(rows.map(r => r.id))} className="font-semibold text-teal-700">Chọn tất cả</button><button type="button" disabled={busy} onClick={() => setSelected([])} className="text-rose-600">Bỏ chọn tất cả</button></div>
      <div className="max-h-64 overflow-y-auto border rounded-lg divide-y">{loading ? <p className="p-3 text-sm">Đang tải...</p> : !rows.length ? <p className="p-3 text-sm text-slate-500">{room ? 'Không có đăng ký riêng trong phòng này.' : 'Chọn phòng để xem danh sách.'}</p> : rows.map(r => <label key={r.id} className="flex items-center gap-3 p-2 text-sm cursor-pointer hover:bg-teal-50"><input type="checkbox" disabled={busy} checked={selected.includes(r.id)} onChange={e => { const checked = e.target.checked; setSelected(prev => checked ? [...prev,r.id] : prev.filter(id => id !== r.id)); }}/><span>{r.roll_number} – {r.student_name} <span className="text-slate-500">({r.student_class || 'Chưa có lớp'})</span></span></label>)}</div>
      <p className="text-sm">Đã chọn <b>{selected.length}</b> sinh viên từ <b>{classes.length}</b> lớp.</p>
      {error && <p role="alert" className="text-sm text-rose-700">{error}</p>}
      <div className="flex gap-3 justify-end border-t pt-4"><button type="button" disabled={busy} onClick={onClose} className="px-4 py-2 bg-slate-100 rounded-lg text-sm">Đóng</button><button type="submit" disabled={busy || loading} className="px-4 py-2 bg-teal-600 rounded-lg text-white text-sm font-medium">{busy ? 'Đang lưu...' : 'Lưu lớp gộp'}</button></div>
    </form>
  </Modal>;
}
