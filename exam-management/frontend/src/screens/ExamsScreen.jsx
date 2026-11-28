import React, { useState, useEffect } from 'react';
import { Calendar, CalendarPlus, Download, Plus, Search, Edit2, Trash2, Filter, RefreshCw, Clock, Users } from 'lucide-react';
import { examsApi, lookupsApi, invigilatorsApi } from '../services/api';
import { buildGoogleCalendarUrl, downloadExamsIcs } from '../services/googleCalendar';
import Modal from '../components/Modal';
import Pagination from '../components/Pagination';
import Alert from '../components/Alert';
import QuickAddLookup from '../components/QuickAddLookup';
import EditableChoice from '../components/EditableChoice';
import InvigilatorSelector from '../components/InvigilatorSelector';

export default function ExamsScreen() {
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, limit: 10, total: 0, totalPages: 1 });
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState(null);

  // Filters & Search
  const [search, setSearch] = useState('');
  const [semesterFilter, setSemesterFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  // Lookups data
  const [lookups, setLookups] = useState({
    semesters: [],
    courses: [],
    examTypes: [],
    invigilators: [],
  });

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [formData, setFormData] = useState({
    exam_type_id: '',
    course_class_id: '',
    semester_id: '',
    exam_date: '',
    start_time: '08:00',
    duration_minutes: 90,
    end_time: '09:30',
    exam_format: 'thi máy',
    exam_status: 'Đã lên lịch',
    supervisor_count: 2,
    invigilator_ids: [],
  });
  const [submitting, setSubmitting] = useState(false);

  const loadLookups = async () => {
    try {
      const [res, staffRes] = await Promise.all([lookupsApi.getAll(), invigilatorsApi.getAll()]);
      setLookups({
        semesters: res.data.data.semesters || [],
        courses: res.data.data.courses || [],
        examTypes: res.data.data.examTypes || [],
        invigilators: staffRes.data.data || [],
      });
    } catch (err) {
      console.error('Error loading lookups:', err);
    }
  };

  const fetchData = async (page = 1) => {
    setLoading(true);
    try {
      const res = await examsApi.getAll({
        page,
        limit: 10,
        search,
        semester_id: semesterFilter,
        exam_status: statusFilter,
      });
      setData(res.data.data);
      setPagination(res.data.pagination);
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadLookups();
    fetchData(1);
  }, [semesterFilter, statusFilter]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchData(1);
  };

  // Auto calculate end time
  const handleTimeChange = (startTime, duration) => {
    if (!startTime || !duration) return;
    const [h, m] = startTime.split(':').map(Number);
    const totalMins = h * 60 + m + parseInt(duration, 10);
    const endH = Math.floor(totalMins / 60) % 24;
    const endM = totalMins % 60;
    const calculated = `${String(endH).padStart(2, '0')}:${String(endM).padStart(2, '0')}`;
    setFormData((prev) => ({
      ...prev,
      start_time: startTime,
      duration_minutes: duration,
      end_time: calculated,
    }));
  };

  const openCreateModal = () => {
    setEditingItem(null);
    setFormData({
      exam_type_id: lookups.examTypes[0]?.id || '',
      course_class_id: lookups.courses[0]?.id || '',
      semester_id: lookups.semesters[0]?.id || '',
      exam_date: new Date().toISOString().split('T')[0],
      start_time: '08:00',
      duration_minutes: 90,
      end_time: '09:30',
      exam_format: 'thi máy',
      exam_status: 'Đã lên lịch',
      supervisor_count: 2,
      invigilator_ids: [],
    });
    setIsModalOpen(true);
  };

  const openEditModal = (item) => {
    setEditingItem(item);
    setFormData({
      exam_type_id: item.exam_type_id,
      course_class_id: item.course_class_id,
      semester_id: item.semester_id,
      exam_date: item.exam_date,
      start_time: item.start_time,
      duration_minutes: item.duration_minutes,
      end_time: item.end_time,
      exam_format: item.exam_format,
      exam_status: item.exam_status,
      supervisor_count: item.supervisor_count,
      invigilator_ids: item.invigilator_ids || [],
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (Number(formData.supervisor_count) < 1 || Number(formData.supervisor_count) > 10) {
      setAlert({ type: 'error', message: 'Số lượng cán bộ coi thi phải từ 1 đến 10.' });
      return;
    }
    if (formData.invigilator_ids.length > Number(formData.supervisor_count)) {
      setAlert({ type: 'error', message: 'Số cán bộ đã chọn vượt quá số lượng cán bộ coi thi.' });
      return;
    }
    setSubmitting(true);
    try {
      if (editingItem) {
        await examsApi.update(editingItem.id, { ...formData, supervisor_count: Number(formData.supervisor_count) });
        setAlert({ type: 'success', message: 'Cập nhật lịch thi thành công!' });
      } else {
        await examsApi.create({ ...formData, supervisor_count: Number(formData.supervisor_count) });
        setAlert({ type: 'success', message: 'Thêm mới lịch thi thành công!' });
      }
      setIsModalOpen(false);
      fetchData(pagination.page);
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id, courseName) => {
    if (!window.confirm(`Bạn có chắc chắn muốn xóa mềm lịch thi môn "${courseName}"?`)) return;
    try {
      await examsApi.delete(id);
      setAlert({ type: 'success', message: 'Đã xóa mềm lịch thi thành công!' });
      fetchData(pagination.page);
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'Đã lên lịch':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'Đang diễn ra':
        return 'bg-amber-100 text-amber-800 border-amber-200 animate-pulse';
      case 'Kết thúc':
        return 'bg-emerald-100 text-emerald-800 border-emerald-200';
      case 'Hoãn thi':
        return 'bg-rose-100 text-rose-800 border-rose-200';
      default:
        return 'bg-slate-100 text-slate-800 border-slate-200';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div>
          <div className="flex items-center space-x-2 text-teal-800 font-bold text-xl">
            <Calendar className="w-6 h-6 text-teal-700" />
            <span>Quản lý Lịch thi</span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Lập kế hoạch tổ chức thi, phân bổ thời gian, ca thi và hình thức thi chi tiết
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => downloadExamsIcs(data, 'lich-thi.ics')}
            disabled={data.length === 0}
            title="Tải file .ics để nhập toàn bộ lịch thi đang hiển thị vào Google Calendar"
            className="inline-flex items-center px-3.5 py-2.5 bg-white hover:bg-slate-50 text-slate-700 border border-slate-300 font-medium text-sm rounded-lg shadow-sm transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Download className="w-4 h-4 mr-2 text-blue-600" />
            Xuất Google Calendar (.ics)
          </button>

          <button
            onClick={openCreateModal}
            className="inline-flex items-center px-4 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-medium text-sm rounded-lg shadow-sm hover:shadow transition"
          >
            <Plus className="w-4 h-4 mr-2" />
            Thêm lịch thi mới
          </button>
        </div>
      </div>

      {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row gap-3 items-stretch md:items-center justify-between">
        <form onSubmit={handleSearchSubmit} className="relative flex-1 max-w-sm">
          <input
            type="text"
            placeholder="Tìm môn học, mã môn, hình thức..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
        </form>

        <div className="flex flex-wrap items-center gap-3">
          {/* Lọc học kỳ */}
          <select
            value={semesterFilter}
            onChange={(e) => setSemesterFilter(e.target.value)}
            className="px-3 py-2 text-sm border border-slate-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-teal-500"
          >
            <option value="">Tất cả học kỳ</option>
            {lookups.semesters.map((s) => (
              <option key={s.id} value={s.id}>
                {s.semester_name} ({s.academic_year})
              </option>
            ))}
          </select>

          {/* Lọc trạng thái */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 text-sm border border-slate-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-teal-500"
          >
            <option value="">Tất cả trạng thái</option>
            <option value="Đã lên lịch">Đã lên lịch</option>
            <option value="Đang diễn ra">Đang diễn ra</option>
            <option value="Kết thúc">Kết thúc</option>
            <option value="Hoãn thi">Hoãn thi</option>
          </select>

          <button
            onClick={() => {
              setSearch('');
              setSemesterFilter('');
              setStatusFilter('');
              fetchData(1);
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
                <th className="px-4 py-3.5">Học phần / Lớp HP</th>
                <th className="px-4 py-3.5">Loại kỳ thi</th>
                <th className="px-4 py-3.5">Học kỳ</th>
                <th className="px-4 py-3.5">Ngày & Giờ thi</th>
                <th className="px-4 py-3.5">Hình thức</th>
                <th className="px-4 py-3.5">Trạng thái</th>
                <th className="px-4 py-3.5 text-center">CBCT</th>
                <th className="px-4 py-3.5 text-center">Phòng/TS</th>
                <th className="px-4 py-3.5 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="9" className="text-center py-8 text-slate-500">
                    <div className="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-teal-600 mb-2"></div>
                    <div>Đang tải danh sách lịch thi...</div>
                  </td>
                </tr>
              ) : data.length === 0 ? (
                <tr>
                  <td colSpan="9" className="text-center py-8 text-slate-400 font-medium">
                    Không có lịch thi nào phù hợp với bộ lọc
                  </td>
                </tr>
              ) : (
                data.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50/80 transition">
                    <td className="px-4 py-3.5">
                      <div className="font-semibold text-slate-900">{item.course_name}</div>
                      <div className="text-xs text-slate-500 font-mono">{item.course_code}</div>
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <span className="inline-block px-2 py-0.5 text-xs font-medium rounded bg-teal-50 text-teal-700 border border-teal-200">
                        {item.exam_type_name}
                      </span>
                    </td>
                    <td className="px-4 py-3.5 text-xs text-slate-600 whitespace-nowrap">
                      <div>{item.semester_name}</div>
                      <div className="text-slate-400">{item.academic_year}</div>
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <div className="font-medium text-slate-800">{item.exam_date}</div>
                      <div className="text-xs text-slate-500 flex items-center mt-0.5">
                        <Clock className="w-3 h-3 mr-1 text-slate-400" />
                        {item.start_time} - {item.end_time} ({item.duration_minutes} phút)
                      </div>
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap capitalize text-slate-700 font-medium">
                      {item.exam_format}
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${getStatusBadge(
                          item.exam_status
                        )}`}
                      >
                        {item.exam_status}
                      </span>
                    </td>
                    <td className="px-4 py-3.5 text-center font-medium text-slate-700 whitespace-nowrap">
                      <div>{item.supervisor_count} cán bộ</div>
                      <div className="text-xs font-normal text-slate-500 mt-1 max-w-44 whitespace-normal text-left"
                        title={(item.invigilators || []).map(st => st.full_name).join(', ')}>
                        {(item.invigilators || []).length
                          ? (item.invigilators || []).map(st => st.full_name).join(', ')
                          : 'Chưa phân công tên'}
                      </div>
                    </td>
                    <td className="px-4 py-3.5 text-center text-xs whitespace-nowrap">
                      <span className="font-semibold text-teal-700">{item.room_count || 0}</span> phòng /{' '}
                      <span className="font-semibold text-slate-700">{item.registration_count || 0}</span> TS
                    </td>
                    <td className="px-4 py-3.5 text-right space-x-2 whitespace-nowrap">
                      <a
                        href={buildGoogleCalendarUrl(item)}
                        target="_blank"
                        rel="noreferrer"
                        title="Thêm lịch thi này vào Google Calendar"
                        className="inline-flex items-center p-1.5 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-md transition"
                      >
                        <CalendarPlus className="w-4 h-4" />
                      </a>
                      <button
                        onClick={() => openEditModal(item)}
                        title="Sửa lịch thi"
                        className="inline-flex items-center p-1.5 text-teal-600 hover:text-teal-800 hover:bg-teal-50 rounded-md transition"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(item.id, item.course_name)}
                        title="Xóa mềm lịch thi"
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

      {/* Modal Thêm / Sửa Lịch Thi */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingItem ? 'Cập nhật lịch thi' : 'Thêm mới lịch thi'}
        maxWidth="max-w-2xl"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Học phần */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Lớp học phần <span className="text-rose-500">*</span>
              </label>
              <select
                required
                value={formData.course_class_id}
                onChange={(e) => setFormData({ ...formData, course_class_id: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
              >
                <option value="">-- Chọn môn học --</option>
                {lookups.courses.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.course_code} - {c.course_name} ({c.credits} tín chỉ)
                  </option>
                ))}
              </select>
              <QuickAddLookup kind="courses" onCreated={async (id) => {
                await loadLookups();
                setFormData(prev => ({ ...prev, course_class_id: id }));
              }} />
            </div>

            {/* Loại kỳ thi */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Loại kỳ thi <span className="text-rose-500">*</span>
              </label>
              <select
                required
                value={formData.exam_type_id}
                onChange={(e) => setFormData({ ...formData, exam_type_id: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
              >
                <option value="">-- Chọn loại kỳ thi --</option>
                {lookups.examTypes.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.name}
                  </option>
                ))}
              </select>
              <QuickAddLookup kind="examTypes" onCreated={async (id) => {
                await loadLookups();
                setFormData(prev => ({ ...prev, exam_type_id: id }));
              }} />
            </div>

            {/* Học kỳ */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Học kỳ <span className="text-rose-500">*</span>
              </label>
              <select
                required
                value={formData.semester_id}
                onChange={(e) => setFormData({ ...formData, semester_id: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
              >
                <option value="">-- Chọn học kỳ --</option>
                {lookups.semesters.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.semester_name} ({s.academic_year})
                  </option>
                ))}
              </select>
              <QuickAddLookup kind="semesters" onCreated={async (id) => {
                await loadLookups();
                setFormData(prev => ({ ...prev, semester_id: id }));
              }} />
            </div>

            {/* Ngày thi */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Ngày tổ chức thi <span className="text-rose-500">*</span>
              </label>
              <input
                type="date"
                required
                value={formData.exam_date}
                onChange={(e) => setFormData({ ...formData, exam_date: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
              />
            </div>

            {/* Giờ bắt đầu */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Giờ bắt đầu <span className="text-rose-500">*</span>
              </label>
              <input
                type="time"
                required
                value={formData.start_time}
                onChange={(e) => handleTimeChange(e.target.value, formData.duration_minutes)}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
              />
            </div>

            {/* Thời lượng */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Thời lượng (phút) <span className="text-rose-500">*</span>
              </label>
              <input
                type="number"
                min="15"
                max="240"
                step="5"
                required
                value={formData.duration_minutes}
                onChange={(e) => handleTimeChange(formData.start_time, e.target.value)}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
              />
            </div>

            {/* Giờ kết thúc (tính tự động) */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Giờ kết thúc
              </label>
              <input
                type="time"
                value={formData.end_time}
                onChange={(e) => setFormData({ ...formData, end_time: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg bg-slate-50 focus:ring-2 focus:ring-teal-500 focus:outline-none"
              />
            </div>

            {/* Hình thức thi */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Hình thức thi
              </label>
              <EditableChoice
                value={formData.exam_format}
                onChange={(value) => setFormData(prev => ({ ...prev, exam_format: value }))}
                options={[
                  { value: 'thi máy', label: 'Thi trên máy tính' },
                  { value: 'trắc nghiệm', label: 'Trắc nghiệm giấy' },
                  { value: 'tự luận', label: 'Tự luận viết' },
                  { value: 'vấn đáp', label: 'Vấn đáp trực tiếp' },
                  { value: 'thực hành', label: 'Thực hành phòng máy' },
                ]}
                maxLength={20} placeholder="Nhập hình thức thi (tối đa 20 ký tự)"
              />
            </div>

            {/* Trạng thái */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Trạng thái thi
              </label>
              <select
                value={formData.exam_status}
                onChange={(e) => setFormData({ ...formData, exam_status: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
              >
                <option value="Đã lên lịch">Đã lên lịch</option>
                <option value="Đang diễn ra">Đang diễn ra</option>
                <option value="Kết thúc">Kết thúc</option>
                <option value="Hoãn thi">Hoãn thi</option>
              </select>
            </div>

            {/* Cán bộ coi thi */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Số lượng cán bộ coi thi
              </label>
              <input
                type="number"
                min="1"
                max="10"
                value={formData.supervisor_count}
                onChange={(e) => {
                  const count = Number(e.target.value) || 1;
                  setFormData(prev => ({ ...prev, supervisor_count: e.target.value,
                    invigilator_ids: prev.invigilator_ids.slice(0, Math.max(0, count)) }));
                }}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
              />
            </div>
            <InvigilatorSelector
              invigilators={lookups.invigilators}
              selected={formData.invigilator_ids}
              onChange={(ids) => setFormData(prev => ({ ...prev, invigilator_ids: ids }))}
              limit={formData.supervisor_count}
              onCreated={async () => { await loadLookups(); }}
            />
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="px-4 py-2 text-sm font-medium text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2 text-sm font-medium text-white bg-teal-600 hover:bg-teal-700 rounded-lg shadow-sm disabled:opacity-50 transition"
            >
              {submitting ? 'Đang lưu...' : editingItem ? 'Lưu thay đổi' : 'Tạo lịch thi'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
