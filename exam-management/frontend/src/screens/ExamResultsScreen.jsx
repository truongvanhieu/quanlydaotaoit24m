import React, { useState, useEffect } from 'react';
import { Award, Plus, Search, Edit2, Trash2, Lock, Unlock, FileText, RefreshCw, CheckCircle2, Clock } from 'lucide-react';
import { examResultsApi, examRegistrationsApi, lookupsApi } from '../services/api';
import Modal from '../components/Modal';
import Pagination from '../components/Pagination';
import Alert from '../components/Alert';

const scoreStatus = (score) => Number(score) >= 4 ? 'Đạt' : 'Không đạt';
const isSpecialStatus = (status) => status === 'Vắng' || status === 'Vi phạm';
const formStatus = (score, status) => isSpecialStatus(status) || score === '' || score == null
  ? status : scoreStatus(score);

export default function ExamResultsScreen() {
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, limit: 10, total: 0, totalPages: 1 });
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState(null);

  // Filters
  const [search, setSearch] = useState('');
  const [examFilter, setExamFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [lockFilter, setLockFilter] = useState('');

  // Lookups data
  const [lookups, setLookups] = useState({ exams: [] });
  const [unassignedRegistrations, setUnassignedRegistrations] = useState([]);
  const [registrationSearch, setRegistrationSearch] = useState('');
  const [registeredCount, setRegisteredCount] = useState(0);

  // Modals
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isAppealModalOpen, setIsAppealModalOpen] = useState(false);

  const [selectedItem, setSelectedItem] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // Form states
  const [createForm, setCreateForm] = useState({
    registration_id: '',
    score: '',
    status: 'Đạt',
    appeal_status: 'Không',
  });

  const [editForm, setEditForm] = useState({
    score: '',
    status: 'Đạt',
    appeal_status: 'Không',
  });

  const [appealForm, setAppealForm] = useState({
    appeal_status: 'Chờ xử lý',
    new_score: '',
  });

  const loadLookups = async () => {
    try {
      const res = await lookupsApi.getAll();
      setLookups({ exams: res.data.data.exams || [] });
    } catch (err) {
      console.error('Error loading lookups:', err);
    }
  };

  const fetchData = async (page = 1) => {
    setLoading(true);
    try {
      const res = await examResultsApi.getAll({
        page,
        limit: 10,
        search,
        exam_id: examFilter,
        status: statusFilter,
        is_locked: lockFilter,
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
  }, [examFilter, statusFilter, lockFilter]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchData(1);
  };

  // Open Create Modal and fetch registrations without score
  const openCreateModal = async () => {
    try {
      // Load all registered students, not just the first 100. The database is the source of truth.
      let all = [];
      let page = 1;
      let totalPages = 1;
      do {
        const res = await examRegistrationsApi.getAll({ page, limit: 100 });
        all = all.concat(res.data.data || []);
        totalPages = res.data.pagination?.totalPages || 1;
        page++;
      } while (page <= totalPages);
      const unassigned = all.filter((r) => !r.result_id);
      setRegisteredCount(all.length);
      setRegistrationSearch('');
      setUnassignedRegistrations(unassigned);
      setCreateForm({
        registration_id: unassigned[0]?.id || '',
        score: '8.00',
        status: 'Đạt',
        appeal_status: 'Không',
      });
      setIsCreateModalOpen(true);
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    }
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    if (!createForm.registration_id) {
      setAlert({ type: 'warning', message: 'Vui lòng chọn thí sinh để nhập điểm!' });
      return;
    }

    setSubmitting(true);
    try {
      await examResultsApi.create({ ...createForm, status: formStatus(createForm.score, createForm.status) });
      setAlert({ type: 'success', message: 'Nhập điểm thành công!' });
      setIsCreateModalOpen(false);
      fetchData(pagination.page);
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    } finally {
      setSubmitting(false);
    }
  };

  // Open Edit Modal (Check is_locked)
  const openEditModal = (item) => {
    if (item.is_locked) {
      setAlert({
        type: 'error',
        message: 'Điểm thi này ĐÃ BỊ KHÓA (CHỐT ĐIỂM)! Không thể chỉnh sửa điểm.',
      });
      return;
    }

    setSelectedItem(item);
    setEditForm({
      score: item.score !== null ? item.score : '',
      status: formStatus(item.score, item.status || 'Đạt'),
      appeal_status: item.appeal_status || 'Không',
    });
    setIsEditModalOpen(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await examResultsApi.update(selectedItem.id, { ...editForm, status: formStatus(editForm.score, editForm.status) });
      setAlert({ type: 'success', message: 'Cập nhật điểm thành công!' });
      setIsEditModalOpen(false);
      fetchData(pagination.page);
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    } finally {
      setSubmitting(false);
    }
  };

  // Toggle Lock
  const handleToggleLock = async (item) => {
    const actionText = item.is_locked ? 'mở khóa' : 'CHỐT ĐIỂM';
    if (!window.confirm(`Bạn có chắc chắn muốn ${actionText} điểm thi cho thí sinh "${item.student_name}"?`)) return;

    try {
      const res = await examResultsApi.toggleLock(item.id, !item.is_locked);
      setAlert({ type: 'success', message: res.data.message });
      fetchData(pagination.page);
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    }
  };

  // Appeal Modal
  const openAppealModal = (item) => {
    setSelectedItem(item);
    setAppealForm({
      appeal_status: item.appeal_status || 'Chờ xử lý',
      new_score: item.score !== null ? item.score : '',
    });
    setIsAppealModalOpen(true);
  };

  const handleAppealSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await examResultsApi.updateAppeal(selectedItem.id, appealForm);
      setAlert({ type: 'success', message: 'Cập nhật tình trạng phúc khảo thành công!' });
      setIsAppealModalOpen(false);
      fetchData(pagination.page);
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    } finally {
      setSubmitting(false);
    }
  };

  // Delete
  const handleDelete = async (item) => {
    if (item.is_locked) {
      setAlert({ type: 'error', message: 'Không thể xóa điểm thi đã bị chốt (is_locked = 1)!' });
      return;
    }
    if (!window.confirm(`Bạn có chắc muốn xóa mềm kết quả của "${item.student_name}"?`)) return;

    try {
      await examResultsApi.delete(item.id);
      setAlert({ type: 'success', message: 'Đã xóa mềm kết quả thi thành công!' });
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
            <Award className="w-6 h-6 text-teal-700" />
            <span>Quản lý Kết quả thi & Chốt điểm</span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Nhập điểm thi, xử lý đơn phúc khảo và chốt điểm chính thức
          </p>
        </div>

        <button
          onClick={openCreateModal}
          className="inline-flex items-center px-4 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-medium text-sm rounded-lg shadow-sm hover:shadow transition"
        >
          <Plus className="w-4 h-4 mr-2" />
          Nhập điểm thi
        </button>
      </div>

      {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row gap-3 items-stretch md:items-center justify-between">
        <form onSubmit={handleSearchSubmit} className="relative flex-1 max-w-sm">
          <input
            type="text"
            placeholder="Tìm theo SBD, mã SV, họ tên..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
        </form>

        <div className="flex flex-wrap items-center gap-3">
          {/* Lọc ca thi */}
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

          {/* Lọc trạng thái chốt điểm */}
          <select
            value={lockFilter}
            onChange={(e) => setLockFilter(e.target.value)}
            className="px-3 py-2 text-sm border border-slate-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-teal-500"
          >
            <option value="">Tất cả khóa điểm</option>
            <option value="1">Đã chốt điểm (Khóa)</option>
            <option value="0">Chưa chốt (Có thể sửa)</option>
          </select>

          {/* Lọc kết quả */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 text-sm border border-slate-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-teal-500"
          >
            <option value="">Tất cả kết quả</option>
            <option value="Đạt">Đạt</option>
            <option value="Không đạt">Không đạt</option>
            <option value="Vắng">Vắng</option>
            <option value="Vi phạm">Vi phạm</option>
          </select>

          <button
            onClick={() => {
              setSearch('');
              setExamFilter('');
              setLockFilter('');
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
                <th className="px-4 py-3.5">Thí sinh / SBD</th>
                <th className="px-4 py-3.5">Môn thi / Phòng</th>
                <th className="px-4 py-3.5 text-center">Điểm số</th>
                <th className="px-4 py-3.5">Trạng thái</th>
                <th className="px-4 py-3.5">Chốt điểm</th>
                <th className="px-4 py-3.5">Phúc khảo</th>
                <th className="px-4 py-3.5">Thời gian chấm</th>
                <th className="px-4 py-3.5 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="8" className="text-center py-8 text-slate-500">
                    <div className="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-teal-600 mb-2"></div>
                    <div>Đang tải kết quả thi...</div>
                  </td>
                </tr>
              ) : data.length === 0 ? (
                <tr>
                  <td colSpan="8" className="text-center py-8 text-slate-400 font-medium">
                    Không tìm thấy kết quả thi nào
                  </td>
                </tr>
              ) : (
                data.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50/80 transition">
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <div className="font-semibold text-slate-900">{item.student_name}</div>
                      <div className="flex items-center space-x-2 text-xs mt-0.5">
                        <span className="font-mono text-slate-500">{item.student_code}</span>
                        <span className="font-mono text-teal-800 bg-teal-50 px-1.5 py-0.2 rounded border border-teal-200">
                          {item.roll_number}
                        </span>
                      </div>
                    </td>
                    <td className="px-4 py-3.5">
                      <div className="font-medium text-slate-900">{item.course_name}</div>
                      <div className="text-xs text-slate-500">
                        {item.course_code} - Phòng {item.room_number}
                      </div>
                    </td>
                    <td className="px-4 py-3.5 text-center whitespace-nowrap">
                      {item.score !== null ? (
                        <span
                          className={`font-mono text-base font-bold ${
                            item.score >= 4.0 ? 'text-emerald-600' : 'text-rose-600'
                          }`}
                        >
                          {Number(item.score).toFixed(2)}
                        </span>
                      ) : (
                        <span className="text-xs text-slate-400 italic">Chưa chấm</span>
                      )}
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <span
                        className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                          item.status === 'Đạt'
                            ? 'bg-emerald-100 text-emerald-800'
                            : item.status === 'Không đạt'
                            ? 'bg-rose-100 text-rose-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}
                      >
                        {item.status}
                      </span>
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      {item.is_locked ? (
                        <span className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-rose-50 text-rose-700 border border-rose-200">
                          <Lock className="w-3 h-3 mr-1 text-rose-600" />
                          Đã chốt điểm
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
                          <Unlock className="w-3 h-3 mr-1 text-emerald-600" />
                          Có thể sửa
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <span
                        className={`inline-block px-2 py-0.5 rounded text-xs font-medium ${
                          item.appeal_status === 'Chờ xử lý'
                            ? 'bg-amber-100 text-amber-800 font-bold'
                            : item.appeal_status === 'Đã duyệt'
                            ? 'bg-blue-100 text-blue-800 font-bold'
                            : 'text-slate-500'
                        }`}
                      >
                        {item.appeal_status || 'Không'}
                      </span>
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap text-xs text-slate-500">
                      {item.graded_at || '—'}
                    </td>
                    <td className="px-4 py-3.5 text-right space-x-1.5 whitespace-nowrap">
                      {/* Nút Toggle Chốt điểm */}
                      <button
                        onClick={() => handleToggleLock(item)}
                        title={item.is_locked ? 'Mở khóa điểm' : 'Chốt điểm thi'}
                        className={`inline-flex items-center p-1.5 rounded-md transition ${
                          item.is_locked
                            ? 'text-rose-600 hover:bg-rose-50'
                            : 'text-emerald-600 hover:bg-emerald-50'
                        }`}
                      >
                        {item.is_locked ? <Unlock className="w-4 h-4" /> : <Lock className="w-4 h-4" />}
                      </button>

                      {/* Nút Sửa Điểm (Disable nếu is_locked = 1) */}
                      <button
                        onClick={() => openEditModal(item)}
                        disabled={item.is_locked}
                        title={item.is_locked ? 'Điểm đã bị chốt (Khóa)' : 'Sửa điểm'}
                        className={`inline-flex items-center p-1.5 rounded-md transition ${
                          item.is_locked
                            ? 'text-slate-300 cursor-not-allowed'
                            : 'text-teal-600 hover:text-teal-800 hover:bg-teal-50'
                        }`}
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>

                      {/* Nút Phúc khảo */}
                      <button
                        onClick={() => openAppealModal(item)}
                        title="Quản lý phúc khảo"
                        className="inline-flex items-center p-1.5 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-md transition"
                      >
                        <FileText className="w-4 h-4" />
                      </button>

                      {/* Nút Xóa (Disable nếu is_locked) */}
                      <button
                        onClick={() => handleDelete(item)}
                        disabled={item.is_locked}
                        title={item.is_locked ? 'Không thể xóa điểm đã chốt' : 'Xóa mềm'}
                        className={`inline-flex items-center p-1.5 rounded-md transition ${
                          item.is_locked
                            ? 'text-slate-300 cursor-not-allowed'
                            : 'text-rose-600 hover:text-rose-800 hover:bg-rose-50'
                        }`}
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

      {/* Modal 1: Nhập Điểm Mới */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Nhập điểm thi cho thí sinh"
      >
        <form onSubmit={handleCreateSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Thí sinh dự thi <span className="text-rose-500">*</span>
            </label>
            <p className="text-xs text-slate-600 mb-2">
              Có <strong>{registeredCount}</strong> lượt đăng ký thi; <strong>{unassignedRegistrations.length}</strong> lượt chưa có kết quả và có thể nhập điểm.
              Những sinh viên đã có kết quả không hiển thị ở đây để tránh nhập điểm trùng.
            </p>
            <input
              type="search"
              placeholder="Tìm theo tên, mã SV, SBD hoặc mã môn..."
              value={registrationSearch}
              onChange={e => {
                const q = e.target.value;
                setRegistrationSearch(q);
                const match = unassignedRegistrations.find(reg =>
                  [reg.student_name, reg.student_code, reg.roll_number, reg.course_code]
                    .some(value => String(value || '').toLowerCase().includes(q.toLowerCase()))
                );
                setCreateForm(prev => ({ ...prev, registration_id: match?.id || '' }));
              }}
              className="w-full px-3 py-2 mb-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
            />
            <select
              required
              value={createForm.registration_id}
              onChange={(e) => setCreateForm({ ...createForm, registration_id: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
            >
              {unassignedRegistrations.length === 0 ? (
                <option value="">-- Chưa có thí sinh nào chờ nhập điểm --</option>
              ) : (
                unassignedRegistrations
                  .filter(reg => [reg.student_name, reg.student_code, reg.roll_number, reg.course_code]
                    .some(value => String(value || '').toLowerCase().includes(registrationSearch.toLowerCase())))
                  .map(reg => (
                    <option key={reg.id} value={reg.id}>
                      {reg.roll_number} - {reg.student_name} ({reg.student_code}) - Môn: {reg.course_code}
                    </option>
                  ))
              )}
            </select>
            {unassignedRegistrations.length === 0 && <p className="mt-2 text-xs text-amber-700">Muốn nhập điểm cho sinh viên mới, hãy đăng ký sinh viên vào ca thi tại mục 4. Danh sách thí sinh trước.</p>}
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Điểm số (Thang điểm 10) <span className="text-rose-500">*</span>
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              max="10"
              required
              value={createForm.score}
              onChange={(e) => setCreateForm(prev => ({ ...prev, score: e.target.value,
                status: e.target.value === '' ? prev.status : scoreStatus(e.target.value) }))}
              placeholder="VD: 8.50"
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none font-bold"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Trạng thái thi
            </label>
            <select
              value={formStatus(createForm.score, createForm.status)}
              onChange={(e) => setCreateForm({ ...createForm, status: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
            >
              <option value="Đạt" disabled={createForm.score !== '' && scoreStatus(createForm.score) !== 'Đạt'} >Đạt</option>
              <option value="Không đạt" disabled={createForm.score !== '' && scoreStatus(createForm.score) !== 'Không đạt'} >Không đạt</option>
              <option value="Vắng">Vắng thi</option>
              <option value="Vi phạm">Vi phạm quy chế</option>
            </select>
            <p className="mt-1 text-xs text-slate-500">Tự động theo điểm: từ 4.00 là Đạt; dưới 4.00 là Không đạt. Vắng / Vi phạm chọn riêng.</p>
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={() => setIsCreateModalOpen(false)}
              className="px-4 py-2 text-sm font-medium text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              disabled={submitting || unassignedRegistrations.length === 0}
              className="px-5 py-2 text-sm font-medium text-white bg-teal-600 hover:bg-teal-700 rounded-lg shadow-sm disabled:opacity-50 transition"
            >
              {submitting ? 'Đang lưu...' : 'Lưu kết quả'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Modal 2: Chỉnh Sửa Điểm */}
      <Modal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        title="Chỉnh sửa điểm thi"
      >
        <form onSubmit={handleEditSubmit} className="space-y-4">
          <div className="bg-slate-50 p-3 rounded-lg border border-slate-200 text-xs text-slate-600">
            <div>
              Thí sinh: <strong className="text-slate-900">{selectedItem?.student_name}</strong> ({selectedItem?.student_code})
            </div>
            <div>
              Môn thi: <strong className="text-slate-900">{selectedItem?.course_name}</strong> | SBD: {selectedItem?.roll_number}
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Điểm số mới <span className="text-rose-500">*</span>
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              max="10"
              required
              value={editForm.score}
              onChange={(e) => setEditForm(prev => ({ ...prev, score: e.target.value,
                status: e.target.value === '' ? prev.status : scoreStatus(e.target.value) }))}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none font-bold"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Trạng thái
            </label>
            <select
              value={formStatus(editForm.score, editForm.status)}
              onChange={(e) => setEditForm({ ...editForm, status: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
            >
              <option value="Đạt" disabled={editForm.score !== '' && scoreStatus(editForm.score) !== 'Đạt'} >Đạt</option>
              <option value="Không đạt" disabled={editForm.score !== '' && scoreStatus(editForm.score) !== 'Không đạt'} >Không đạt</option>
              <option value="Vắng">Vắng thi</option>
              <option value="Vi phạm">Vi phạm quy chế</option>
            </select>
            <p className="mt-1 text-xs text-slate-500">Tự động theo điểm: từ 4.00 là Đạt; dưới 4.00 là Không đạt. Vắng / Vi phạm chọn riêng.</p>
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={() => setIsEditModalOpen(false)}
              className="px-4 py-2 text-sm font-medium text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2 text-sm font-medium text-white bg-teal-600 hover:bg-teal-700 rounded-lg shadow-sm disabled:opacity-50 transition"
            >
              {submitting ? 'Đang lưu...' : 'Cập nhật điểm'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Modal 3: Quản Lý Phúc Khảo */}
      <Modal
        isOpen={isAppealModalOpen}
        onClose={() => setIsAppealModalOpen(false)}
        title="Xử lý đơn phúc khảo điểm"
      >
        <form onSubmit={handleAppealSubmit} className="space-y-4">
          <div className="bg-slate-50 p-3 rounded-lg border border-slate-200 text-xs text-slate-600">
            <div>
              Thí sinh: <strong className="text-slate-900">{selectedItem?.student_name}</strong>
            </div>
            <div>
              Điểm hiện tại: <strong className="text-teal-700">{selectedItem?.score}</strong>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Trạng thái phúc khảo <span className="text-rose-500">*</span>
            </label>
            <select
              value={appealForm.appeal_status}
              onChange={(e) => setAppealForm({ ...appealForm, appeal_status: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
            >
              <option value="Không">Không phúc khảo</option>
              <option value="Chờ xử lý">Chờ xử lý (Đã nhận đơn)</option>
              <option value="Đã duyệt">Đã duyệt (Cập nhật điểm mới)</option>
              <option value="Từ chối">Từ chối phúc khảo (Giữ nguyên điểm)</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Điểm số sau phúc khảo
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              max="10"
              value={appealForm.new_score}
              onChange={(e) => setAppealForm({ ...appealForm, new_score: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none font-bold"
            />
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={() => setIsAppealModalOpen(false)}
              className="px-4 py-2 text-sm font-medium text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm disabled:opacity-50 transition"
            >
              {submitting ? 'Đang lưu...' : 'Lưu kết quả phúc khảo'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
