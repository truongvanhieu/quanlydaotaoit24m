import React, { useState, useEffect } from 'react';
import { FileCode, Plus, Search, Edit2, Trash2, ExternalLink, RefreshCw, FileText } from 'lucide-react';
import { examPapersApi, lookupsApi, examsApi } from '../services/api';
import Modal from '../components/Modal';
import Pagination from '../components/Pagination';
import Alert from '../components/Alert';

export default function ExamPapersScreen() {
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, limit: 10, total: 0, totalPages: 1 });
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState(null);

  // Filters
  const [search, setSearch] = useState('');
  const [examFilter, setExamFilter] = useState('');

  // Lookups data
  const [lookups, setLookups] = useState({ exams: [], courses: [], examTypes: [], semesters: [] });
  const [quickOpen, setQuickOpen] = useState(false);
  const [quickSaving, setQuickSaving] = useState(false);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [formData, setFormData] = useState({
    exam_id: '',
    paper_code: '',
    file_url: '',
  });
  const [submitting, setSubmitting] = useState(false);

  const loadLookups = async () => {
    try {
      const res = await lookupsApi.getAll();
      const { exams = [], courses = [], examTypes = [], semesters = [] } = res.data.data;
      setLookups({ exams, courses, examTypes, semesters });
    } catch (err) {
      console.error('Error loading lookups:', err);
    }
  };

  const fetchData = async (page = 1) => {
    setLoading(true);
    try {
      const res = await examPapersApi.getAll({
        page,
        limit: 10,
        search,
        exam_id: examFilter,
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
  }, [examFilter]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchData(1);
  };

  const openCreateModal = () => {
    setQuickOpen(false);
    setEditingItem(null);
    setFormData({
      exam_id: lookups.exams[0]?.id || '',
      paper_code: '',
      file_url: '',
    });
    setIsModalOpen(true);
  };

  const openEditModal = (item) => {
    setQuickOpen(false);
    setEditingItem(item);
    setFormData({
      exam_id: item.exam_id,
      paper_code: item.paper_code,
      file_url: item.file_url,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (quickOpen || quickSaving) return;
    if (!formData.paper_code.trim() || !formData.file_url.trim()) {
      setAlert({ type: 'warning', message: 'Vui lòng nhập mã đề thi và đường dẫn file đề!' });
      return;
    }

    setSubmitting(true);
    try {
      if (editingItem) {
        await examPapersApi.update(editingItem.id, formData);
        setAlert({ type: 'success', message: 'Cập nhật đề thi thành công!' });
      } else {
        await examPapersApi.create(formData);
        setAlert({ type: 'success', message: 'Thêm mới đề thi thành công!' });
      }
      setIsModalOpen(false);
      fetchData(pagination.page);
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id, paperCode) => {
    if (!window.confirm(`Bạn có chắc chắn muốn xóa mềm đề thi "${paperCode}"?`)) return;
    try {
      await examPapersApi.delete(id);
      setAlert({ type: 'success', message: 'Đã xóa mềm đề thi thành công!' });
      fetchData(pagination.page);
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div>
          <div className="flex items-center space-x-2 text-teal-800 font-bold text-xl">
            <FileCode className="w-6 h-6 text-teal-700" />
            <span>Quản lý Đề thi</span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Lưu trữ mã đề thi và đường dẫn tài liệu đề thi gắn liền với từng ca thi
          </p>
        </div>

        <button
          onClick={openCreateModal}
          className="inline-flex items-center px-4 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-medium text-sm rounded-lg shadow-sm hover:shadow transition"
        >
          <Plus className="w-4 h-4 mr-2" />
          Thêm đề thi mới
        </button>
      </div>

      {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row gap-3 items-stretch md:items-center justify-between">
        <form onSubmit={handleSearchSubmit} className="relative flex-1 max-w-sm">
          <input
            type="text"
            placeholder="Tìm theo mã đề, môn học, file..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
        </form>

        <div className="flex flex-wrap items-center gap-3">
          <select
            value={examFilter}
            onChange={(e) => setExamFilter(e.target.value)}
            className="px-3 py-2 text-sm border border-slate-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-teal-500 max-w-xs"
          >
            <option value="">Tất cả ca thi</option>
            {lookups.exams.map((ex) => (
              <option key={ex.id} value={ex.id}>
                {ex.course_code} - {ex.course_name}
              </option>
            ))}
          </select>

          <button
            onClick={() => {
              setSearch('');
              setExamFilter('');
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
                <th className="px-5 py-3.5">Mã đề thi</th>
                <th className="px-5 py-3.5">Ca thi / Môn học</th>
                <th className="px-5 py-3.5">Ngày tổ chức</th>
                <th className="px-5 py-3.5">Đường dẫn file đề (file_url)</th>
                <th className="px-5 py-3.5">Ngày tạo</th>
                <th className="px-5 py-3.5 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="6" className="text-center py-8 text-slate-500">
                    <div className="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-teal-600 mb-2"></div>
                    <div>Đang tải danh sách đề thi...</div>
                  </td>
                </tr>
              ) : data.length === 0 ? (
                <tr>
                  <td colSpan="6" className="text-center py-8 text-slate-400 font-medium">
                    Chưa có đề thi nào trong hệ thống
                  </td>
                </tr>
              ) : (
                data.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50/80 transition">
                    <td className="px-5 py-3.5 whitespace-nowrap">
                      <span className="font-mono font-bold text-teal-800 bg-teal-50 px-2.5 py-1 rounded border border-teal-200">
                        {item.paper_code}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <div className="font-medium text-slate-900">{item.course_name}</div>
                      <div className="text-xs text-slate-500 font-mono">{item.course_code}</div>
                    </td>
                    <td className="px-5 py-3.5 whitespace-nowrap text-xs text-slate-600">
                      <div>{item.exam_date}</div>
                      <div className="text-slate-400">{item.start_time}</div>
                    </td>
                    <td className="px-5 py-3.5">
                      <div className="flex items-center space-x-2">
                        <span className="font-mono text-xs text-slate-600 max-w-xs truncate" title={item.file_url}>
                          {item.file_url}
                        </span>
                        <a
                          href={item.file_url}
                          target="_blank"
                          rel="noreferrer"
                          title="Mở đường dẫn"
                          className="inline-flex items-center p-1 text-teal-600 hover:text-teal-800 hover:bg-teal-50 rounded"
                        >
                          <ExternalLink className="w-3.5 h-3.5" />
                        </a>
                      </div>
                    </td>
                    <td className="px-5 py-3.5 text-xs text-slate-500 whitespace-nowrap">
                      {item.created_at ? new Date(item.created_at).toLocaleDateString('vi-VN') : '—'}
                    </td>
                    <td className="px-5 py-3.5 text-right space-x-2 whitespace-nowrap">
                      <button
                        onClick={() => openEditModal(item)}
                        title="Chỉnh sửa đề thi"
                        className="inline-flex items-center p-1.5 text-teal-600 hover:text-teal-800 hover:bg-teal-50 rounded-md transition"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(item.id, item.paper_code)}
                        title="Xóa mềm đề thi"
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

      {/* Modal Thêm / Sửa Đề Thi */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => { if (!quickSaving && !submitting) setIsModalOpen(false); }}
        title={editingItem ? 'Cập nhật đề thi' : 'Thêm mới đề thi'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Chọn ca thi */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Ca thi / Môn thi áp dụng <span className="text-rose-500">*</span>
            </label>
            <select
              required={!quickOpen}
              disabled={quickOpen}
              value={formData.exam_id}
              onChange={(e) => setFormData({ ...formData, exam_id: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
            >
              <option value="">-- Chọn ca thi --</option>
              {lookups.exams.map((ex) => (
                <option key={ex.id} value={ex.id}>
                  {ex.course_code} - {ex.course_name} ({ex.exam_date} {ex.start_time})
                </option>
              ))}
            </select>
            {!quickOpen ? (
              <button type="button" disabled={submitting} onClick={() => { setQuickOpen(true); loadLookups(); }}
                className="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-teal-700 hover:text-teal-900">
                <Plus className="w-3.5 h-3.5" /> Tự nhập môn học mới
              </button>
            ) : (
              <QuickCreateExam lookups={lookups} onSaving={setQuickSaving}
                onCourseCreated={(course) => setLookups(prev => ({ ...prev, courses: [...prev.courses.filter(c => c.id !== course.id), course] }))}
                onCancel={() => setQuickOpen(false)}
                onCreated={(exam) => {
                  setLookups(prev => ({ ...prev, exams: [exam, ...prev.exams.filter(ex => ex.id !== exam.id)] }));
                  setFormData(prev => ({ ...prev, exam_id: exam.id }));
                  setQuickOpen(false);
                }} />
            )}
          </div>

          {/* Mã đề thi */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Mã đề thi <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              value={formData.paper_code}
              onChange={(e) => setFormData({ ...formData, paper_code: e.target.value })}
              placeholder="VD: DE-WEB-01, DE-CSDL-101..."
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none font-mono"
            />
          </div>

          {/* Đường dẫn file đề thi */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Đường dẫn file đề thi <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              value={formData.file_url}
              onChange={(e) => setFormData({ ...formData, file_url: e.target.value })}
              placeholder="VD: /uploads/papers/DE-WEB-01.pdf hoặc https://drive.google.com/..."
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none font-mono text-xs"
            />
            <p className="text-xs text-slate-400 mt-1">
              Lưu trữ trực tiếp đường dẫn file đề thi trong hệ thống.
            </p>
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              disabled={quickSaving || submitting}
              className="px-4 py-2 text-sm font-medium text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              disabled={submitting || quickOpen || quickSaving}
              className="px-5 py-2 text-sm font-medium text-white bg-teal-600 hover:bg-teal-700 rounded-lg shadow-sm disabled:opacity-50 transition"
            >
              {submitting ? 'Đang lưu...' : editingItem ? 'Lưu thay đổi' : 'Lưu đề thi'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}


// A paper references an exam, not a course. Create the course (if needed),
// then create its exam with the existing APIs and select that exam in the parent.
function QuickCreateExam({ lookups, onCourseCreated, onCreated, onCancel, onSaving }) {
  const [courseId, setCourseId] = useState('');
  const [courseCode, setCourseCode] = useState('');
  const [courseName, setCourseName] = useState('');
  const [credits, setCredits] = useState('3');
  const [examType, setExamType] = useState('');
  const [semester, setSemester] = useState('');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('08:00');
  const [duration, setDuration] = useState('90');
  const [format, setFormat] = useState('thi máy');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const fieldClass = 'mt-1 w-full rounded-md border border-slate-300 bg-white px-2.5 py-2 font-normal text-sm focus:outline-none focus:ring-2 focus:ring-teal-500';

  const save = async () => {
    if (saving) return;
    setError('');
    const code = courseCode.trim();
    const name = courseName.trim();
    const creditCount = Number(credits);
    const minutes = Number(duration);
    const selectedCourse = lookups.courses.find(c => c.id === courseId);
    if (courseId && !selectedCourse) { setError('Môn học đã chọn không còn trong danh sách.'); return; }
    if (!courseId && (!code || !name || !Number.isInteger(creditCount) || creditCount < 1 || creditCount > 30)) {
      setError('Nhập mã môn, tên môn và số tín chỉ nguyên từ 1 đến 30.'); return;
    }
    if (!courseId && lookups.courses.some(c => c.course_code.toLowerCase() === code.toLowerCase())) {
      setError('Mã môn học đã tồn tại. Hãy chọn môn đó trong danh sách bên trên.'); return;
    }
    if (!examType || !semester || !date || !/^\d{2}:\d{2}$/.test(time) || !Number.isInteger(minutes) || minutes < 15 || minutes > 240) {
      setError('Chọn loại kỳ thi, học kỳ, ngày, giờ thi và thời lượng từ 15 đến 240 phút.'); return;
    }
    const [h, m] = time.split(':').map(Number);
    if (h > 23 || m > 59 || h * 60 + m + minutes >= 1440) {
      setError('Ca thi phải bắt đầu và kết thúc trong cùng một ngày.'); return;
    }
    setSaving(true);
    onSaving(true);
    try {
      let course = selectedCourse;
      if (!course) {
        const response = await lookupsApi.create('courses', { course_code: code, course_name: name, credits: creditCount });
        const id = response.data?.data?.id;
        if (!id) throw new Error('Máy chủ chưa trả về mã định danh môn học.');
        course = { id, course_code: code, course_name: name, credits: creditCount };
        onCourseCreated(course);
        setCourseId(id);
        setNotice('Đã lưu môn học vào SQL Server. Nếu lưu ca thi chưa thành công, có thể thử lại mà không tạo trùng môn.');
      }
      const response = await examsApi.create({
        course_class_id: course.id, exam_type_id: examType, semester_id: semester,
        exam_date: date, start_time: time, duration_minutes: minutes,
        exam_format: format, exam_status: 'Đã lên lịch', supervisor_count: 2, invigilator_ids: [],
      });
      const created = response.data?.data;
      if (!created?.id) throw new Error('Máy chủ chưa trả về mã định danh ca thi.');
      onCreated({ ...created, course_code: course.course_code, course_name: course.course_name, exam_date: date, start_time: time });
    } catch (err) {
      setError(err.message || 'Không thể lưu môn học / ca thi.');
    } finally {
      setSaving(false);
      onSaving(false);
    }
  };

  return (
    <fieldset disabled={saving} className="mt-2 rounded-lg border border-teal-200 bg-teal-50 p-3 space-y-3"
      onKeyDown={e => { if (e.key === 'Enter' && e.target.tagName === 'INPUT') { e.preventDefault(); save(); } }}>
      <strong className="text-sm text-teal-800">Thêm môn học và ca thi trực tiếp</strong>
      <label className="block text-xs font-medium text-slate-700">Môn học
        <select className={fieldClass} value={courseId} onChange={e => setCourseId(e.target.value)}>
          <option value="">Nhập môn học mới</option>
          {lookups.courses.map(c => <option key={c.id} value={c.id}>{c.course_code} - {c.course_name}</option>)}
        </select>
      </label>
      {!courseId && <>
        <label className="block text-xs font-medium text-slate-700">Mã môn học *
          <input className={fieldClass} maxLength={50} placeholder="VD: INT1501" value={courseCode} onChange={e => setCourseCode(e.target.value)} />
        </label>
        <label className="block text-xs font-medium text-slate-700">Tên môn học *
          <input className={fieldClass} maxLength={200} placeholder="VD: Lập trình Java nâng cao" value={courseName} onChange={e => setCourseName(e.target.value)} />
        </label>
        <label className="block text-xs font-medium text-slate-700">Số tín chỉ *
          <input className={fieldClass} type="number" min="1" max="30" step="1" value={credits} onChange={e => setCredits(e.target.value)} />
        </label>
      </>}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
        <label className="block text-xs font-medium text-slate-700">Loại kỳ thi *
          <select className={fieldClass} value={examType} onChange={e => setExamType(e.target.value)}>
            <option value="">-- Chọn loại kỳ thi --</option>
            {lookups.examTypes.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
          </select>
        </label>
        <label className="block text-xs font-medium text-slate-700">Học kỳ *
          <select className={fieldClass} value={semester} onChange={e => setSemester(e.target.value)}>
            <option value="">-- Chọn học kỳ --</option>
            {lookups.semesters.map(s => <option key={s.id} value={s.id}>{s.semester_name} ({s.academic_year})</option>)}
          </select>
        </label>
        <label className="block text-xs font-medium text-slate-700">Ngày thi *
          <input className={fieldClass} type="date" value={date} onChange={e => setDate(e.target.value)} />
        </label>
        <label className="block text-xs font-medium text-slate-700">Giờ bắt đầu *
          <input className={fieldClass} type="time" value={time} onChange={e => setTime(e.target.value)} />
        </label>
        <label className="block text-xs font-medium text-slate-700">Thời lượng (phút) *
          <input className={fieldClass} type="number" min="15" max="240" step="1" value={duration} onChange={e => setDuration(e.target.value)} />
        </label>
        <label className="block text-xs font-medium text-slate-700">Hình thức thi
          <select className={fieldClass} value={format} onChange={e => setFormat(e.target.value)}>
            {['thi máy', 'trắc nghiệm', 'tự luận', 'vấn đáp', 'thực hành'].map(f => <option key={f} value={f}>{f}</option>)}
          </select>
        </label>
      </div>
      <p className="text-xs text-slate-600">Ca thi mới mặc định cần 2 cán bộ coi thi, chưa phân công. Có thể chỉnh tại Quản lý lịch thi.</p>
      {notice && <p className="text-xs text-teal-800" role="status">{notice}</p>}
      {error && <p role="alert" className="text-xs text-red-700">{error}</p>}
      <div className="flex justify-end gap-2">
        <button type="button" onClick={onCancel} className="px-3 py-1.5 text-xs rounded-md bg-white border border-slate-300">Hủy</button>
        <button type="button" onClick={save} disabled={saving} className="px-3 py-1.5 text-xs font-semibold rounded-md bg-teal-600 text-white disabled:opacity-50">{saving ? 'Đang lưu...' : 'Lưu và chọn ca thi'}</button>
      </div>
    </fieldset>
  );
}
