import React, { useState, useEffect, useRef } from 'react';
import { UserCheck, Plus, Search, Edit2, Trash2, RefreshCw, Hash, User, AlertCircle, Printer, Users } from 'lucide-react';
import { examRegistrationsApi, lookupsApi } from '../services/api';
import Modal from '../components/Modal';
import Pagination from '../components/Pagination';
import Alert from '../components/Alert';
import QuickAddLookup from '../components/QuickAddLookup';
import ExamRegistrationTools from '../components/ExamRegistrationTools';
import ClassifyExamGroup from '../components/ClassifyExamGroup';
import { printExamSlip } from '../services/examPrint';

export default function ExamRegistrationsScreen() {
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, limit: 10, total: 0, totalPages: 1 });
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState(null);

  // Filters
  const [search, setSearch] = useState('');
  const [examFilter, setExamFilter] = useState('');
  const [groupType, setGroupType] = useState('single');
  const [groupFilter, setGroupFilter] = useState('');
  const [classFilter, setClassFilter] = useState('');
  const [groups, setGroups] = useState([]);
  const [refreshKey, setRefreshKey] = useState(0);
  const [classifyOpen, setClassifyOpen] = useState(false);
  const fetchVersion = useRef(0);
  const groupLabel = id => `LG-${String(id).slice(0,8).toUpperCase()}`;
  const selectedGroup = groups.find(g => g.id === groupFilter);


  // Lookups data
  const [lookups, setLookups] = useState({
    students: [],
    exams: [],
    examRooms: [],
  });

  const classes = [...new Set(lookups.students.map(s => s.class_name).filter(Boolean))].sort((a,b) => a.localeCompare(b, 'vi'));

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [formData, setFormData] = useState({
    exam_id: '',
    exam_room_id: '',
    student_id: '',
    roll_number: '',
  });
  const [availableRooms, setAvailableRooms] = useState([]);
  const [selectedRoomInfo, setSelectedRoomInfo] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [toolMode, setToolMode] = useState(null);
  const [roomLoading, setRoomLoading] = useState(false);
  const [modalError, setModalError] = useState('');

  const loadLookups = async () => {
    try {
      const res = await lookupsApi.getAll();
      setLookups({
        students: res.data.data.students || [],
        exams: res.data.data.exams || [],
        examRooms: res.data.data.examRooms || [],
      });
    } catch (err) {
      console.error('Error loading lookups:', err);
    }
  };

  const fetchData = async (page = 1) => {
    const version = ++fetchVersion.current;
    setLoading(true);
    try {
      const res = await examRegistrationsApi.getAll({
        page,
        limit: 10,
        search,
        exam_id: examFilter || undefined,
        group_type: groupType === 'all' ? undefined : groupType,
        group_id: groupType === 'merged' ? groupFilter || undefined : undefined,
        student_class: groupType === 'single' ? classFilter || undefined : undefined,
      });
      if (version !== fetchVersion.current) return;
      setData(res.data.data);
      setPagination(res.data.pagination);
    } catch (err) {
      if (version === fetchVersion.current) setAlert({ type: 'error', message: err.message });
    } finally {
      if (version === fetchVersion.current) setLoading(false);
    }
  };

  useEffect(() => { loadLookups(); }, []);
  useEffect(() => {
    fetchData(1);
    return () => { fetchVersion.current++; };
  }, [examFilter, groupType, groupFilter, classFilter, refreshKey]);
  useEffect(() => {
    let active = true;
    examRegistrationsApi.getGroups({ exam_id: examFilter || undefined }).then(res => {
      if (active) setGroups(res.data.data || []);
    }).catch(err => { if (active) setAlert({ type: 'error', message: err.message }); });
    return () => { active = false; };
  }, [examFilter, refreshKey]);

  const refresh = () => setRefreshKey(n => n + 1);
  const changeType = type => { setGroupType(type); setGroupFilter(''); setClassFilter(''); };
  const showSavedGroup = (count, id) => {
    setToolMode(null); setClassifyOpen(false); setSearch(''); setExamFilter('');
    setClassFilter(''); setGroupType(id ? 'merged' : 'single'); setGroupFilter(id || '');
    setAlert({ type: 'success', message: id ? `Đã lưu lớp gộp ${groupLabel(id)} gồm ${count} sinh viên!` : `Đã đăng ký ${count} sinh viên vào lớp riêng!` });
    loadLookups(); refresh();
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchData(1);
  };

  // Update available rooms when exam_id changes
  const handleExamChange = (examId) => {
    const rooms = lookups.examRooms.filter((r) => r.exam_id === examId);
    setAvailableRooms(rooms);
    const defaultRoom = rooms[0] || null;
    setSelectedRoomInfo(defaultRoom);
    setFormData((prev) => ({
      ...prev,
      exam_id: examId,
      exam_room_id: defaultRoom ? defaultRoom.id : '',
    }));
  };

  const handleRoomChange = (roomId) => {
    const room = availableRooms.find((r) => r.id === roomId);
    setSelectedRoomInfo(room || null);
    setFormData((prev) => ({ ...prev, exam_room_id: roomId }));
  };

  const openCreateModal = () => {
    setEditingItem(null);
    setModalError('');
    const initialExam = lookups.exams[0]?.id || '';
    const rooms = lookups.examRooms.filter((r) => r.exam_id === initialExam);
    setAvailableRooms(rooms);
    setSelectedRoomInfo(rooms[0] || null);

    setFormData({
      exam_id: initialExam,
      exam_room_id: rooms[0]?.id || '',
      student_id: lookups.students[0]?.id || '',
      roll_number: '',
    });
    setIsModalOpen(true);
  };

  const openEditModal = async (item) => {
    setEditingItem(item);
    setModalError('');
    setAvailableRooms([]);
    setSelectedRoomInfo(null);
    setFormData({ exam_id: item.exam_id, exam_room_id: '', student_id: item.student_id, roll_number: item.roll_number, birth_date: item.birth_date || '' });
    setRoomLoading(true);
    setIsModalOpen(true);
    try {
      const res = await examRegistrationsApi.transferRooms(item.id);
      const rooms = res.data.data;
      const current = rooms.find(r => r.current);
      setAvailableRooms(rooms);
      setSelectedRoomInfo(current || null);
      setFormData(prev => ({ ...prev, exam_room_id: current?.id || '' }));
    } catch (err) { setModalError(err.message); }
    finally { setRoomLoading(false); }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.exam_room_id) {
      setAlert({ type: 'warning', message: 'Ca thi này chưa có phòng thi nào. Vui lòng gán phòng trước!' });
      return;
    }

    setSubmitting(true);
    setModalError('');
    try {
      if (editingItem) {
        await examRegistrationsApi.update(editingItem.id, {
          physical_room_id: formData.exam_room_id,
          roll_number: formData.roll_number,
          birth_date: formData.birth_date,
        });
        setAlert({ type: 'success', message: 'Cập nhật thí sinh thành công!' });
      } else {
        const res = await examRegistrationsApi.create(formData);
        setAlert({
          type: 'success',
          message: `Đăng ký thí sinh thành công! Số báo danh: ${res.data.data.roll_number}`,
        });
      }
      setIsModalOpen(false);
      if (!editingItem) { setGroupType('single'); setClassFilter(''); setGroupFilter(''); setExamFilter(''); setSearch(''); }
      loadLookups();
      refresh();
    } catch (err) {
      setModalError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id, studentName, rollNumber) => {
    if (!window.confirm(`Bạn có chắc muốn xóa thí sinh "${studentName}" (SBD: ${rollNumber})?`)) return;
    try {
      await examRegistrationsApi.delete(id);
      setAlert({ type: 'success', message: 'Đã xóa mềm thí sinh thành công!' });
      loadLookups();
      refresh();
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    }
  };

  const isSelectedRoomFull =
    selectedRoomInfo &&
    !editingItem &&
    selectedRoomInfo.current_registrations >= selectedRoomInfo.capacity;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div>
          <div className="flex items-center space-x-2 text-teal-800 font-bold text-xl">
            <UserCheck className="w-6 h-6 text-teal-700" />
            <span>Danh sách thí sinh đăng ký thi</span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Đăng ký sinh viên vào ca thi, cấp phát số báo danh và kiểm soát sĩ số phòng thi
          </p>
        </div>

        <div className="flex flex-wrap gap-2">
        <button onClick={() => setToolMode('bulk')} className="inline-flex items-center px-3 py-2.5 border border-teal-600 text-teal-700 font-medium text-sm rounded-lg hover:bg-teal-50"><Users className="w-4 h-4 mr-2"/> Gộp lớp / Đăng ký nhóm</button>
        <button onClick={() => setToolMode('print')} className="inline-flex items-center px-3 py-2.5 border border-teal-600 text-teal-700 font-medium text-sm rounded-lg hover:bg-teal-50"><Printer className="w-4 h-4 mr-2"/> In danh sách / PDF</button>
        <button
          onClick={openCreateModal}
          className="inline-flex items-center px-4 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-medium text-sm rounded-lg shadow-sm hover:shadow transition"
        >
          <Plus className="w-4 h-4 mr-2" />
          Đăng ký thí sinh
        </button>
        </div>
      </div>

      {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

      {toolMode && <ExamRegistrationTools key={toolMode} mode={toolMode} lookups={lookups} initialExam={toolMode === 'print' ? examFilter || selectedGroup?.exam_id : examFilter} initialGroupType={groupType} initialGroup={groupFilter} initialClass={classFilter} onClose={() => setToolMode(null)} onSaved={showSavedGroup} />}
      {classifyOpen && <ClassifyExamGroup lookups={lookups} initialExam={examFilter} onClose={() => setClassifyOpen(false)} onSaved={showSavedGroup} />}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm space-y-3">
        <div className="flex flex-wrap gap-2 items-center" role="group" aria-label="Loại danh sách">
          {[['single','Lớp riêng'], ['merged','Lớp gộp'], ['all','Tất cả thí sinh']].map(([value,label]) => (
            <button key={value} type="button" aria-pressed={groupType === value} onClick={() => changeType(value)} className={`px-4 py-2 rounded-lg text-sm font-semibold border transition ${groupType === value ? 'bg-teal-700 text-white border-teal-700' : 'bg-white text-slate-600 border-slate-200 hover:bg-teal-50'}`}>{label}{value === 'merged' ? ` (${groups.length} nhóm)` : ''}</button>
          ))}
          <button type="button" onClick={() => setClassifyOpen(true)} className="ml-auto px-3 py-2 rounded-lg border border-teal-600 text-teal-700 text-sm font-medium hover:bg-teal-50">Phân loại lớp gộp cũ</button>
        </div>
        <p className="text-sm text-slate-500">{groupType === 'single' ? 'Đăng ký riêng theo lớp sinh hoạt, bao gồm dữ liệu cũ chưa được đánh dấu lớp gộp.' : groupType === 'merged' ? 'Các nhóm đã gộp từ nhiều lớp sinh hoạt. Chọn một nhóm để xem danh sách riêng.' : 'Hiển thị tất cả đăng ký; lớp gộp có mã nhóm riêng.'}</p>
        {groupType === 'merged' && <div className="space-y-2">
          <label className="block text-xs font-semibold text-slate-600" htmlFor="merged-group-filter">Nhóm lớp gộp</label>
          <select id="merged-group-filter" value={groupFilter} onChange={e => setGroupFilter(e.target.value)} className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white">
            <option value="">Tất cả nhóm lớp gộp</option>
            {groups.map(g => <option key={g.id} value={g.id}>{groupLabel(g.id)} — {lookups.exams.find(ex => ex.id === g.exam_id)?.course_name || 'Ca thi'} — {g.rooms.join(', ')} — {g.count} SV</option>)}
          </select>
          {selectedGroup && <div className="bg-teal-50 border border-teal-200 rounded-lg px-4 py-3 text-sm text-teal-900"><b>{groupLabel(selectedGroup.id)}</b> · {selectedGroup.count} sinh viên · {selectedGroup.classes.length} lớp<br/>Lớp gốc: {selectedGroup.classes.join(', ')} · Phòng: {selectedGroup.rooms.join(', ')}</div>}
        </div>}
        {groupType === 'single' && <select aria-label="Lọc lớp sinh hoạt" value={classFilter} onChange={e => setClassFilter(e.target.value)} className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white max-w-full"><option value="">Tất cả lớp sinh hoạt</option>{classes.map(c => <option key={c} value={c}>{c}</option>)}</select>}
      </div>
      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row gap-3 items-stretch md:items-center justify-between">
        <form onSubmit={handleSearchSubmit} className="relative flex-1 max-w-sm">
          <input
            type="text"
            placeholder="Tìm theo SBD, mã SV, họ tên, môn học..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
        </form>

        <div className="flex flex-wrap items-center gap-3">
          <select
            value={examFilter}
            onChange={(e) => { setExamFilter(e.target.value); setGroupFilter(''); }}
            className="px-3 py-2 text-sm border border-slate-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-teal-500 max-w-xs"
          >
            <option value="">Tất cả ca thi</option>
            {lookups.exams.map((ex) => (
              <option key={ex.id} value={ex.id}>
                {ex.course_code} - {ex.course_name} ({ex.exam_date})
              </option>
            ))}
          </select>

          <button
            onClick={() => {
              setSearch('');
              setExamFilter('');
              setGroupFilter(''); setClassFilter('');
              loadLookups(); refresh();
            }}
            className="inline-flex items-center px-3 py-2 text-sm font-medium text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition"
          >
            <RefreshCw className="w-4 h-4 mr-1.5" />
            Làm mới
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600 border-collapse">
            <thead className="bg-slate-50 text-slate-700 font-semibold uppercase text-xs border-b border-slate-200 tracking-wider">
              <tr>
                <th className="px-5 py-3.5">Số báo danh (SBD)</th>
                <th className="px-5 py-3.5">Sinh viên</th>
                <th className="px-5 py-3.5">Lớp sinh hoạt</th>
                <th className="px-5 py-3.5">Học phần / Ca thi</th>
                <th className="px-5 py-3.5">Phòng thi</th>
                <th className="px-5 py-3.5">Thời gian thi</th>
                <th className="px-5 py-3.5 text-center">Kết quả</th>
                <th className="px-5 py-3.5 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="8" className="text-center py-8 text-slate-500">
                    <div className="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-teal-600 mb-2"></div>
                    <div>Đang tải danh sách thí sinh...</div>
                  </td>
                </tr>
              ) : data.length === 0 ? (
                <tr>
                  <td colSpan="8" className="text-center py-8 text-slate-400 font-medium">
                    {groupType === 'merged' ? 'Chưa có lớp gộp phù hợp. Hãy gộp lớp hoặc phân loại lớp gộp cũ.' : 'Không có thí sinh phù hợp.'}
                  </td>
                </tr>
              ) : (
                data.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50/80 transition">
                    <td className="px-5 py-3.5 whitespace-nowrap">
                      <span className="font-mono font-bold text-teal-800 bg-teal-50 px-2.5 py-1 rounded border border-teal-200 text-xs">
                        {item.roll_number}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 whitespace-nowrap">
                      <div className="font-semibold text-slate-900">{item.student_name}</div>
                      <div className="text-xs text-slate-500 font-mono">{item.student_code}</div>
                    </td>
                    <td className="px-5 py-3.5 text-slate-600 whitespace-nowrap text-xs">
                      {item.student_class || '—'}
                      {item.merged_group_id && <div className="mt-1 text-teal-700 font-semibold" title="Mã lớp gộp">{groupLabel(item.merged_group_id)}</div>}
                    </td>
                    <td className="px-5 py-3.5">
                      <div className="font-medium text-slate-900">{item.course_name}</div>
                      <div className="text-xs text-slate-500 font-mono">{item.course_code}</div>
                    </td>
                    <td className="px-5 py-3.5 whitespace-nowrap">
                      <span className="font-medium text-slate-800 bg-slate-100 px-2 py-0.5 rounded text-xs border border-slate-200">
                        {item.room_number}
                      </span>
                      <div className="text-[11px] text-slate-400 mt-0.5">{item.building}</div>
                    </td>
                    <td className="px-5 py-3.5 whitespace-nowrap text-xs">
                      <div className="font-medium text-slate-800">{item.exam_date}</div>
                      <div className="text-slate-500">{item.start_time}</div>
                    </td>
                    <td className="px-5 py-3.5 text-center whitespace-nowrap">
                      {item.result_id ? (
                        <span
                          className={`inline-block px-2.5 py-0.5 text-xs font-bold rounded-full ${
                            item.score >= 4
                              ? 'bg-emerald-100 text-emerald-800'
                              : 'bg-rose-100 text-rose-800'
                          }`}
                        >
                          {item.score !== null ? `${item.score} đ` : item.result_status}
                        </span>
                      ) : (
                        <span className="text-xs text-slate-400 italic">Chưa có điểm</span>
                      )}
                    </td>
                    <td className="px-5 py-3.5 text-right space-x-2 whitespace-nowrap">
                      <button
                        onClick={() => openEditModal(item)}
                        title="Chỉnh sửa thông tin / Đổi phòng"
                        className="inline-flex items-center p-1.5 text-teal-600 hover:text-teal-800 hover:bg-teal-50 rounded-md transition"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button title="In phiếu dự thi / PDF" aria-label="In phiếu dự thi" onClick={() => { try { printExamSlip(item, lookups.exams.find(ex => ex.id === item.exam_id)); } catch (err) { setAlert({ type: 'error', message: err.message }); } }} className="inline-flex items-center p-1.5 text-teal-600 hover:bg-teal-50 rounded-md"><Printer className="w-4 h-4"/></button>
                      <button
                        onClick={() => handleDelete(item.id, item.student_name, item.roll_number)}
                        title="Hủy đăng ký (Xóa mềm)"
                        className="inline-flex items-center p-1.5 text-rose-600 hover:text-rose-800 hover:bg-rose-50 rounded-md transition"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <Pagination pagination={pagination} onPageChange={(p) => fetchData(p)} />
      </div>

      {/* Modal Đăng Ký Thí Sinh */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => { if (!roomLoading && !submitting) setIsModalOpen(false); }}
        title={editingItem ? 'Chỉnh sửa đăng ký thí sinh' : 'Đăng ký thí sinh dự thi'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {!editingItem ? (
            <>
              {/* Chọn ca thi */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                  Ca thi / Môn thi <span className="text-rose-500">*</span>
                </label>
                <select
                  required
                  value={formData.exam_id}
                  onChange={(e) => handleExamChange(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
                >
                  <option value="">-- Chọn ca thi --</option>
                  {lookups.exams.map((ex) => (
                    <option key={ex.id} value={ex.id}>
                      {ex.course_code} - {ex.course_name} ({ex.exam_date} {ex.start_time})
                    </option>
                  ))}
                </select>
              </div>

              {/* Chọn phòng thi */}
              <div>
                <div className="flex items-center justify-between mb-1">
                  <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider">
                    Phòng thi <span className="text-rose-500">*</span>
                  </label>
                  {selectedRoomInfo && (
                    <span
                      className={`text-xs font-bold ${
                        selectedRoomInfo.current_registrations >= selectedRoomInfo.capacity
                          ? 'text-rose-600'
                          : 'text-teal-700'
                      }`}
                    >
                      Hiện có: {selectedRoomInfo.current_registrations} / {selectedRoomInfo.capacity} thí sinh
                    </span>
                  )}
                </div>
                <select
                  required
                  value={formData.exam_room_id}
                  onChange={(e) => handleRoomChange(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
                >
                  {availableRooms.length === 0 ? (
                    <option value="">-- Ca thi này chưa được gán phòng! --</option>
                  ) : (
                    availableRooms.map((r) => (
                      <option key={r.id} value={r.id}>
                        {r.room_number} - {r.building} (Đã có: {r.current_registrations}/{r.capacity})
                      </option>
                    ))
                  )}
                </select>
                {isSelectedRoomFull && (
                  <div className="flex items-center text-xs text-rose-600 mt-1.5 font-medium">
                    <AlertCircle className="w-4 h-4 mr-1 flex-shrink-0" />
                    Phòng này đã đầy sức chứa! Vui lòng chọn phòng khác hoặc tăng sức chứa.
                  </div>
                )}
              </div>

              {/* Chọn sinh viên */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                  Sinh viên <span className="text-rose-500">*</span>
                </label>
                <select
                  required
                  value={formData.student_id}
                  onChange={(e) => setFormData({ ...formData, student_id: e.target.value })}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
                >
                  <option value="">-- Chọn sinh viên --</option>
                  {lookups.students.map((st) => (
                    <option key={st.id} value={st.id}>
                      {st.student_code} - {st.full_name} ({st.class_name})
                    </option>
                  ))}
                </select>
                <QuickAddLookup kind="students" onCreated={async (id) => {
                  await loadLookups();
                  setFormData(prev => ({ ...prev, student_id: id }));
                }} />
              </div>
            </>
          ) : (
            <>
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">Phòng thi (Chuyển phòng)</label>
                <select required disabled={roomLoading} value={formData.exam_room_id} onChange={e => handleRoomChange(e.target.value)} className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none">
                  <option value="">{roomLoading ? 'Đang tải phòng...' : '-- Chọn phòng --'}</option>
                  {availableRooms.map(r => <option key={r.id} value={r.id} disabled={!r.available}>{r.room_number} - {r.building} ({r.current_registrations}/{r.capacity}){r.current ? ' – Phòng hiện tại' : r.reason ? ` – ${r.reason}` : !r.assigned ? ' – Tự gán vào ca thi' : ''}</option>)}
                </select>
                <p className="text-xs text-slate-500 mt-1">Phòng chưa gán sẽ được tự thêm vào đúng ca thi. Phòng đầy hoặc trùng giờ không thể chọn.</p>
              </div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider">Ngày sinh sinh viên (không bắt buộc)
                <input type="date" value={formData.birth_date || ''} onChange={e => setFormData(prev => ({ ...prev, birth_date: e.target.value }))} className="mt-1 w-full px-3 py-2 text-sm border border-slate-300 rounded-lg" />
              </label>
            </>
          )}

          {/* Số báo danh */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Số báo danh
            </label>
            <input
              type="text"
              value={formData.roll_number}
              onChange={(e) => setFormData({ ...formData, roll_number: e.target.value })}
              placeholder="Để trống để hệ thống TỰ ĐỘNG CẤP SỐ (VD: SBD-INT1306-005)"
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none font-mono"
            />
            <p className="text-xs text-slate-400 mt-1">
              * Nếu để trống, hệ thống sẽ tự động tạo số báo danh theo chuẩn.
            </p>
          </div>

          {modalError && <p role="alert" className="text-sm text-rose-700">{modalError}</p>}
          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-100">
            <button
              type="button"
              disabled={roomLoading || submitting}
              onClick={() => setIsModalOpen(false)}
              className="px-4 py-2 text-sm font-medium text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              disabled={submitting || roomLoading || !formData.exam_room_id || isSelectedRoomFull}
              className="px-5 py-2 text-sm font-medium text-white bg-teal-600 hover:bg-teal-700 rounded-lg shadow-sm disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
              {submitting ? 'Đang lưu...' : editingItem ? 'Lưu thay đổi' : 'Xác nhận đăng ký'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
