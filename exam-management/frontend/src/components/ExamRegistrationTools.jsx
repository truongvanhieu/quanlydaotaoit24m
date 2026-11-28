import React, { useEffect, useRef, useState } from 'react';
import { examRegistrationsApi } from '../services/api';
import Modal from './Modal';
import { rosterDocument, rosterClass, rosterGroupLabel, selectRosterRows } from '../services/examPrint';

const field = 'w-full border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white';
const button = 'px-4 py-2 rounded-lg text-sm font-semibold bg-teal-600 text-white hover:bg-teal-700 disabled:opacity-50 cursor-pointer';
const classOf = st => st.class_name || '(Chưa có lớp)';
const idOf = id => String(id ?? '');
const sameId = (a,b) => idOf(a) === idOf(b);
const freeSeats = r => r ? Math.max(0, Number(r.capacity || 0) - Number(r.current_registrations || 0)) : 0;
const roomsFor = (lookups, id) => lookups.examRooms.filter(r => sameId(r.exam_id, id));
const preferredRoom = (lookups, id) => idOf(roomsFor(lookups, id).find(r => freeSeats(r) > 0)?.id);

async function allRegistrations(examId, roomId = '') {
  const all = [];
  let page = 1, totalPages = 1;
  do {
    const res = await examRegistrationsApi.getAll({ exam_id: examId, exam_room_id: roomId || undefined, page, limit: 200 });
    if (res.data.success === false || !Array.isArray(res.data.data)) throw new Error(res.data.message || 'Không lấy được danh sách thí sinh.');
    all.push(...res.data.data);
    totalPages = Number(res.data.pagination?.totalPages ?? res.data.pagination?.total_pages ?? 1);
    page++;
  } while (page <= totalPages);
  return all;
}

export default function ExamRegistrationTools({ mode, lookups, initialExam, initialGroupType = 'all', initialGroup = '', initialClass = '', onClose, onSaved }) {
  const bulk = mode === 'bulk';
  const firstExam = idOf(initialExam || lookups.exams[0]?.id);
  const [examId, setExamId] = useState(firstExam);
  const [roomId, setRoomId] = useState(() => bulk ? preferredRoom(lookups, firstExam) : '');
  const [classFilter, setClassFilter] = useState(() => !bulk && initialGroupType === 'single' ? initialClass : '');
  const [printType, setPrintType] = useState(initialGroupType);
  const [printGroup, setPrintGroup] = useState(initialGroupType === 'merged' ? initialGroup : '');
  const [printRows, setPrintRows] = useState([]);
  const [printLoading, setPrintLoading] = useState(!bulk);
  const [printError, setPrintError] = useState('');
  const [selected, setSelected] = useState([]);
  const [query, setQuery] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [existingIds, setExistingIds] = useState([]);
  const [checking, setChecking] = useState(bulk);
  const [checkError, setCheckError] = useState('');
  const [retry, setRetry] = useState(0);
  const [preview, setPreview] = useState('');
  const [previewReady, setPreviewReady] = useState(false);
  const frame = useRef(null);
  const rooms = roomsFor(lookups, examId);
  const room = rooms.find(r => sameId(r.id, roomId));
  const exam = lookups.exams.find(e => sameId(e.id, examId));
  const classes = [...new Set(lookups.students.map(classOf))].sort((a,b) => a.localeCompare(b, 'vi'));
  const visible = lookups.students.filter(st => (!classFilter || classOf(st) === classFilter) && `${st.student_code} ${st.full_name}`.toLowerCase().includes(query.toLowerCase()));
  const registered = new Set(existingIds);
  const available = visible.filter(st => !registered.has(idOf(st.id)));
  const selectedClasses = [...new Set(lookups.students.filter(s => selected.includes(idOf(s.id))).map(classOf))];
  const seats = freeSeats(room);
  const roomRows = printRows.filter(r => !roomId || sameId(r.exam_room_id, roomId));
  const printClasses = [...new Set(roomRows.filter(r => !r.merged_group_id).map(rosterClass))].sort((a,b) => a.localeCompare(b, 'vi'));
  const printGroups = new Map();
  for (const row of roomRows) {
    if (!row.merged_group_id) continue;
    const id = idOf(row.merged_group_id);
    if (!printGroups.has(id)) printGroups.set(id, []);
    printGroups.get(id).push(row);
  }
  const matchingRows = selectRosterRows(roomRows, printType, classFilter, printGroup);

  useEffect(() => {
    if (bulk || !examId) { setPrintLoading(false); return; }
    let active = true;
    setPrintLoading(true); setPrintError(''); setPrintRows([]);
    allRegistrations(examId).then(rows => { if (active) setPrintRows(rows); })
      .catch(err => { if (active) setPrintError(err.message); })
      .finally(() => { if (active) setPrintLoading(false); });
    return () => { active = false; };
  }, [bulk, examId, retry]);

  // Refresh the registered IDs for the selected exam; stale responses are ignored.
  useEffect(() => {
    if (!bulk || !examId) { setChecking(false); return; }
    let active = true;
    setChecking(true); setCheckError(''); setExistingIds([]);
    allRegistrations(examId).then(rows => {
      if (!active) return;
      const ids = rows.map(r => idOf(r.student_id));
      setExistingIds(ids);
      setSelected(prev => prev.filter(id => !ids.includes(id)));
    }).catch(err => { if (active) setCheckError(err.message); })
      .finally(() => { if (active) setChecking(false); });
    return () => { active = false; };
  }, [bulk, examId, retry]);

  useEffect(() => {
    if (!examId && lookups.exams.length) {
      const id = idOf(lookups.exams[0].id);
      setExamId(id); setRoomId(bulk ? preferredRoom(lookups, id) : '');
    }
  }, [lookups, examId, bulk]);

  const changeExam = id => {
    setExamId(id); setRoomId(bulk ? preferredRoom(lookups, id) : '');
    setSelected([]); setExistingIds([]); setChecking(bulk && !!id); setError(''); setCheckError('');
    if (!bulk) { setClassFilter(''); setPrintGroup(''); setPrintRows([]); setPrintLoading(!!id); setRetry(n => n + 1); }
  };
  const submit = async e => {
    e.preventDefault(); setError('');
    if (busy) return;
    if (!exam) { setError('Vui lòng chọn ca thi.'); return; }
    if (bulk) {
      if (checking) { setError('Đang kiểm tra sinh viên đã đăng ký. Vui lòng thử lại khi hoàn tất.'); return; }
      if (checkError) { setError('Chưa kiểm tra được đăng ký hiện có. Hãy bấm Thử lại.'); return; }
      if (!room) { setError('Vui lòng chọn phòng thi. Nếu chưa có phòng, hãy gán phòng tại mục 3. Phòng thi.'); return; }
      if (!selected.length) { setError('Vui lòng tích chọn ít nhất một sinh viên.'); return; }
      if (selected.length > seats) { setError(`Phòng chỉ còn ${seats} chỗ. Hãy chọn ít sinh viên hơn hoặc đổi phòng.`); return; }
    }
    setBusy(true);
    try {
      if (bulk) {
        const res = await examRegistrationsApi.bulkCreate({ exam_id: examId, exam_room_id: roomId, student_ids: selected });
        if (res.data.success === false) throw new Error(res.data.message || 'Đăng ký nhóm thất bại.');
        onSaved(res.data.data.count, res.data.data.merged_group_id);
      } else {
        const all = await allRegistrations(examId, roomId);
        const rows = selectRosterRows(all, printType, classFilter, printGroup);
        if (!rows.length) throw new Error('Không có thí sinh phù hợp để in. Hãy kiểm tra ca thi, phòng và lớp hoặc mã lớp gộp đã chọn.');
        setPreviewReady(false);
        setPreview(rosterDocument(exam, rows));
      }
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  };
  const print = () => {
    const win = frame.current?.contentWindow;
    if (!win || !previewReady) { setError('Bản xem trước chưa tải xong.'); return; }
    try { win.focus(); win.print(); } catch (err) { setError('Không mở được hộp thoại in: ' + err.message); }
  };
  return <Modal isOpen onClose={() => { if (!busy) onClose(); }} title={preview ? 'Xem trước danh sách ký tên' : bulk ? 'Gộp lớp / đăng ký thi theo nhóm' : 'In danh sách ký tên / PDF'} maxWidth={preview ? 'max-w-6xl' : 'max-w-3xl'}>
    {preview ? <div className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <button type="button" onClick={() => { setPreview(''); setError(''); }} className="px-4 py-2 rounded-lg bg-slate-100 text-sm cursor-pointer">← Quay lại bộ lọc</button>
        <button type="button" onClick={print} disabled={!previewReady} className={button}>In / Lưu PDF</button>
      </div>
      <p className="text-sm text-slate-600">Chọn máy in hoặc “Lưu dưới dạng PDF” trong hộp thoại in của trình duyệt.</p>
      {error && <p role="alert" className="text-sm text-rose-700">{error}</p>}
      <iframe ref={frame} title="Bản xem trước danh sách ký tên" srcDoc={preview} onLoad={() => setPreviewReady(true)} className="w-full border border-slate-300 rounded-lg bg-white" style={{height:'60vh'}} />
    </div> : <form className="space-y-4" onSubmit={submit} noValidate>
      <div><label htmlFor="tool-exam" className="block text-sm font-medium">Ca thi</label><select id="tool-exam" className={field} value={examId} onChange={e => changeExam(e.target.value)} disabled={busy}><option value="">-- Chọn ca thi --</option>{lookups.exams.map(ex => <option key={ex.id} value={idOf(ex.id)}>{ex.course_code} - {ex.course_name} ({ex.exam_date} {ex.start_time})</option>)}</select></div>
      <div><label htmlFor="tool-room" className="block text-sm font-medium">Phòng thi</label><select id="tool-room" className={field} value={roomId} onChange={e => { setRoomId(e.target.value); setError(''); }} disabled={busy}><option value="">{bulk ? '-- Chọn phòng --' : 'Tất cả phòng (mỗi phòng một danh sách)'}</option>{rooms.map(r => <option key={r.id} value={idOf(r.id)}>{r.room_number} - {r.building} ({freeSeats(r)} chỗ trống / {r.capacity})</option>)}</select>{bulk && !rooms.length && <p className="text-sm text-amber-700 mt-1">Ca này chưa được gán phòng. Vào mục 3. Phòng thi để gán phòng trước.</p>}</div>
      {bulk ? <div><label htmlFor="tool-class" className="block text-sm font-medium">{bulk ? 'Lọc lớp để chọn sinh viên' : 'Lớp sinh hoạt (tùy chọn)'}</label><select id="tool-class" className={field} value={classFilter} onChange={e => { setClassFilter(e.target.value); setError(''); }} disabled={busy}><option value="">Tất cả lớp</option>{classes.map(c => <option key={c} value={c}>{c}</option>)}</select></div> : <>
        <div><label htmlFor="tool-print-type" className="block text-sm font-medium">Loại danh sách in</label><select id="tool-print-type" className={field} value={printType} onChange={e => { setPrintType(e.target.value); setClassFilter(''); setPrintGroup(''); setError(''); }} disabled={busy}><option value="single">Lớp riêng</option><option value="merged">Lớp gộp</option><option value="all">Tất cả (tách riêng từng lớp / nhóm)</option></select></div>
        {printType === 'single' && <div><label htmlFor="tool-class" className="block text-sm font-medium">Lớp sinh hoạt</label><select id="tool-class" className={field} value={classFilter} onChange={e => { setClassFilter(e.target.value); setError(''); }} disabled={busy || printLoading}><option value="">Tất cả lớp riêng</option>{classFilter && !printClasses.includes(classFilter) && <option value={classFilter}>{classFilter} (không có trong bộ lọc hiện tại)</option>}{printClasses.map(c => <option key={c} value={c}>{c}</option>)}</select></div>}
        {printType === 'merged' && <div><label htmlFor="tool-print-group" className="block text-sm font-medium">Mã lớp gộp</label><select id="tool-print-group" className={field} value={printGroup} onChange={e => { setPrintGroup(e.target.value); setError(''); }} disabled={busy || printLoading}><option value="">Tất cả lớp gộp</option>{printGroup && !printGroups.has(printGroup) && <option value={printGroup}>{rosterGroupLabel(printGroup)} (không có trong bộ lọc hiện tại)</option>}{[...printGroups.entries()].sort(([a],[b]) => a.localeCompare(b)).map(([id, rows]) => <option key={id} value={id}>{rosterGroupLabel(id)} – {rows.length} SV – {[...new Set(rows.map(rosterClass))].join(', ')}</option>)}</select></div>}
        {printLoading ? <p className="text-sm text-slate-500">Đang tải danh sách lớp...</p> : printError ? <p role="alert" className="text-sm text-rose-700">{printError} <button type="button" className="underline font-semibold" onClick={() => setRetry(n => n+1)}>Thử lại</button></p> : <p className="text-sm text-teal-800 bg-teal-50 rounded-lg p-3">Có <b>{matchingRows.length}</b> thí sinh phù hợp. Mỗi lớp riêng hoặc mã lớp gộp trong từng phòng được in thành danh sách riêng.</p>}
      </>}
      {bulk ? <>
        <p className="text-sm text-slate-600">Chọn sinh viên từ nhiều lớp. Đổi bộ lọc vẫn giữ lựa chọn; SBD được cấp xen kẽ theo lớp. Nếu chỉ chọn một lớp, đăng ký sẽ nằm ở mục Lớp riêng.</p>
        {checking && <p className="text-sm text-slate-600">Đang kiểm tra sinh viên đã đăng ký...</p>}
        {checkError && <div role="alert" className="text-sm text-rose-700">{checkError} <button type="button" className="underline font-semibold" onClick={() => setRetry(n => n+1)}>Thử lại</button></div>}
        <input aria-label="Tìm sinh viên" className={field} placeholder="Tìm mã sinh viên, họ tên..." value={query} onChange={e => setQuery(e.target.value)} disabled={busy} />
        <div className="flex flex-wrap gap-3 text-sm"><button type="button" className="text-teal-700 font-semibold cursor-pointer" disabled={busy} onClick={() => { setSelected(prev => [...new Set([...prev, ...available.map(s => idOf(s.id))])]); setError(''); }}>Chọn tất cả đang hiển thị</button><button type="button" className="text-rose-600 cursor-pointer" disabled={busy} onClick={() => { setSelected([]); setError(''); }}>Bỏ chọn tất cả</button></div>
        <div className="border rounded-lg max-h-64 overflow-y-auto divide-y">{visible.map(st => { const id = idOf(st.id), already = registered.has(id); return <label className={`flex gap-3 items-center p-2 text-sm ${already ? 'bg-slate-50 text-slate-400' : 'cursor-pointer hover:bg-teal-50'}`} key={id}><input type="checkbox" style={{width:16,height:16,accentColor:'#0d9488',flexShrink:0}} disabled={busy || already} checked={selected.includes(id)} onChange={e => { const checked = e.currentTarget.checked; setSelected(prev => checked ? [...new Set([...prev,id])] : prev.filter(x => x !== id)); setError(''); }}/><span>{st.student_code} – {st.full_name} <span className="text-slate-500">({classOf(st)})</span>{already && <span className="ml-2 text-xs text-amber-700">Đã đăng ký ca này</span>}</span></label>; })}{!visible.length && <p className="p-3 text-sm text-slate-500">Không tìm thấy sinh viên.</p>}</div>
        <div className="text-sm text-slate-700">Đã chọn <b>{selected.length}</b> sinh viên thuộc <b>{selectedClasses.length}</b> lớp: {selectedClasses.join(', ') || '—'}. {room ? <>Phòng còn <b>{seats}</b> chỗ.</> : 'Chưa chọn phòng.'}</div>
        {room && selected.length > seats && <p className="text-sm text-rose-600">Số sinh viên vượt số chỗ trống.</p>}
      </> : <p className="text-sm text-slate-600">Danh sách lấy toàn bộ thí sinh theo ca/phòng và loại lớp đã chọn, không giới hạn trang đang xem. Bản xem trước mở ngay tại đây, không cần cửa sổ bật lên. Các cột chưa có dữ liệu để trống ghi tay.</p>}
      {error && <p role="alert" className="text-sm text-rose-700 bg-rose-50 border border-rose-200 rounded-lg p-3">{error}</p>}
      <div className="flex justify-end gap-3 border-t pt-4"><button type="button" disabled={busy} onClick={onClose} className="px-4 py-2 rounded-lg bg-slate-100 text-sm cursor-pointer">Đóng</button><button type="submit" className={button} disabled={busy || (!bulk && (printLoading || !!printError))}>{busy ? 'Đang xử lý...' : bulk ? 'Đăng ký nhóm' : 'Xem trước / Xuất PDF'}</button></div>
    </form>}
  </Modal>;
}
