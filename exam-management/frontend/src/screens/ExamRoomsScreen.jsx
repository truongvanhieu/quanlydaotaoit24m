import React, { useState, useEffect } from 'react';
import { DoorOpen, Plus, Search, Edit2, Trash2, RefreshCw, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { examRoomsApi, lookupsApi } from '../services/api';
import Modal from '../components/Modal';
import Pagination from '../components/Pagination';
import Alert from '../components/Alert';
import QuickAddLookup from '../components/QuickAddLookup';

export default function ExamRoomsScreen() {
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, limit: 10, total: 0, totalPages: 1 });
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState(null);

  // Filters
  const [search, setSearch] = useState('');
  const [examFilter, setExamFilter] = useState('');

  // Lookups data
  const [lookups, setLookups] = useState({
    rooms: [],
    exams: [],
  });

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [formData, setFormData] = useState({
    exam_id: '',
    room_id: '',
    capacity: 35,
    room_number: '',
    building: '',
    room_max_capacity: 35,
  });
  const [selectedPhysicalRoom, setSelectedPhysicalRoom] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const loadLookups = async () => {
    try {
      const res = await lookupsApi.getAll();
      setLookups({
        rooms: res.data.data.rooms || [],
        exams: res.data.data.exams || [],
      });
    } catch (err) {
      console.error('Error loading lookups:', err);
    }
  };

  const fetchData = async (page = 1) => {
    setLoading(true);
    try {
      const res = await examRoomsApi.getAll({
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

  const handleRoomSelect = (roomId) => {
    const room = lookups.rooms.find((r) => r.id === roomId);
    setSelectedPhysicalRoom(room || null);
    setFormData((prev) => ({
      ...prev,
      room_id: roomId,
      capacity: room ? room.capacity : prev.capacity,
      room_number: room?.room_number || '',
      building: room?.building || '',
      room_max_capacity: room?.capacity || prev.room_max_capacity,
    }));
  };

  const openCreateModal = () => {
    setEditingItem(null);
    const initialExam = lookups.exams[0]?.id || '';
    const initialRoom = lookups.rooms[0] || null;
    setSelectedPhysicalRoom(initialRoom);
    setFormData({
      exam_id: initialExam,
      room_id: initialRoom ? initialRoom.id : '',
      capacity: initialRoom ? initialRoom.capacity : 35,
      room_number: initialRoom?.room_number || '',
      building: initialRoom?.building || '',
      room_max_capacity: initialRoom?.capacity || 35,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (item) => {
    setEditingItem(item);
    setFormData({
      exam_id: item.exam_id,
      room_id: item.room_id,
      capacity: item.capacity,
      room_number: item.room_number || '',
      building: item.building || '',
      room_max_capacity: item.room_max_capacity || item.capacity,
    });
    const room = lookups.rooms.find((r) => r.id === item.room_id);
    setSelectedPhysicalRoom(room || null);
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      if (editingItem) {
        await examRoomsApi.update(editingItem.id, {
          exam_id: formData.exam_id,
          room_id: formData.room_id,
          capacity: Number(formData.capacity),
          room_number: formData.room_number.trim(),
          building: formData.building.trim(),
          room_max_capacity: Number(formData.room_max_capacity),
        });
        await loadLookups();
        setAlert({ type: 'success', message: 'Cập nhật đầy đủ thông tin phòng thi thành công!' });
      } else {
        await examRoomsApi.create(formData);
        setAlert({ type: 'success', message: 'Gán phòng thi thành công!' });
      }
      setIsModalOpen(false);
      fetchData(pagination.page);
    } catch (err) {
      setAlert({ type: 'error', message: err.message });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id, roomName) => {
    if (!window.confirm(`Bạn có chắc chắn muốn hủy gán phòng "${roomName}"?`)) return;
    try {
      await examRoomsApi.delete(id);
      setAlert({ type: 'success', message: 'Hủy gán phòng thành công!' });
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
            <DoorOpen className="w-6 h-6 text-teal-700" />
            <span>Phân bổ Phòng thi</span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Gán phòng thi cho từng ca thi và kiểm soát sức chứa tối đa
          </p>
        </div>

        <button
          onClick={openCreateModal}
          className="inline-flex items-center px-4 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-medium text-sm rounded-lg shadow-sm hover:shadow transition"
        >
          <Plus className="w-4 h-4 mr-2" />
          Gán phòng mới
        </button>
      </div>

      {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row gap-3 items-stretch md:items-center justify-between">
        <form onSubmit={handleSearchSubmit} className="relative flex-1 max-w-sm">
          <input
            type="text"
            placeholder="Tìm theo phòng, tòa nhà, môn học..."
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
                {ex.course_code} - {ex.course_name} ({ex.exam_date} {ex.start_time})
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
                <th className="px-5 py-3.5">Phòng thi</th>
                <th className="px-5 py-3.5">Tòa nhà / Địa điểm</th>
                <th className="px-5 py-3.5">Ca thi / Học phần</th>
                <th className="px-5 py-3.5">Thời gian thi</th>
                <th className="px-5 py-3.5 text-center">Sức chứa cấu hình</th>
                <th className="px-5 py-3.5">Tình trạng đăng ký</th>
                <th className="px-5 py-3.5 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="7" className="text-center py-8 text-slate-500">
                    <div className="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-teal-600 mb-2"></div>
                    <div>Đang tải danh sách phòng thi...</div>
                  </td>
                </tr>
              ) : data.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center py-8 text-slate-400 font-medium">
                    Chưa có phòng thi nào được gán
                  </td>
                </tr>
              ) : (
                data.map((item) => {
                  const percent = Math.min(100, Math.round((item.current_registrations / item.capacity) * 100));
                  const isFull = item.current_registrations >= item.capacity;

                  return (
                    <tr key={item.id} className="hover:bg-slate-50/80 transition">
                      <td className="px-5 py-3.5 whitespace-nowrap">
                        <span className="font-bold text-slate-900 bg-slate-100 px-2.5 py-1 rounded-md font-mono text-sm border border-slate-200">
                          {item.room_number}
                        </span>
                      </td>
                      <td className="px-5 py-3.5 text-slate-600 whitespace-nowrap">{item.building}</td>
                      <td className="px-5 py-3.5">
                        <div className="font-medium text-slate-900">{item.course_name}</div>
                        <div className="text-xs text-slate-500 font-mono">{item.course_code}</div>
                      </td>
                      <td className="px-5 py-3.5 whitespace-nowrap text-xs">
                        <div className="font-semibold text-slate-800">{item.exam_date}</div>
                        <div className="text-slate-500">
                          {item.start_time} - {item.end_time}
                        </div>
                      </td>
                      <td className="px-5 py-3.5 text-center whitespace-nowrap">
                        <span className="inline-block px-3 py-1 font-bold text-teal-800 bg-teal-50 border border-teal-200 rounded-md">
                          {item.capacity} chỗ
                        </span>
                        <div className="text-[11px] text-slate-400 mt-0.5">
                          Thiết kế: {item.room_max_capacity}
                        </div>
                      </td>
                      <td className="px-5 py-3.5 whitespace-nowrap min-w-[180px]">
                        <div className="flex items-center justify-between text-xs mb-1">
                          <span className={isFull ? 'font-bold text-rose-600' : 'font-medium text-slate-700'}>
                            {item.current_registrations} / {item.capacity} thí sinh
                          </span>
                          <span className="text-slate-400 font-mono">{percent}%</span>
                        </div>
                        <div className="w-full bg-slate-200 rounded-full h-2 overflow-hidden">
                          <div
                            className={`h-2 rounded-full transition-all duration-300 ${
                              isFull ? 'bg-rose-500' : percent > 80 ? 'bg-amber-500' : 'bg-teal-500'
                            }`}
                            style={{ width: `${percent}%` }}
                          ></div>
                        </div>
                      </td>
                      <td className="px-5 py-3.5 text-right space-x-2 whitespace-nowrap">
                        <button
                          onClick={() => openEditModal(item)}
                          title="Sửa thông tin phòng thi"
                          className="inline-flex items-center p-1.5 text-teal-600 hover:text-teal-800 hover:bg-teal-50 rounded-md transition"
                        >
                          <Edit2 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(item.id, item.room_number)}
                          title="Hủy gán phòng"
                          className="inline-flex items-center p-1.5 text-rose-600 hover:text-rose-800 hover:bg-rose-50 rounded-md transition"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        <Pagination pagination={pagination} onPageChange={(p) => fetchData(p)} />
      </div>

      {/* Modal Gán Phòng Thi */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingItem ? 'Chỉnh sửa thông tin phòng thi' : 'Gán phòng cho ca thi'}
        maxWidth={editingItem ? 'max-w-2xl' : 'max-w-lg'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {editingItem ? (
            <>
              <p className="text-sm text-slate-500">
                Bạn có thể sửa ca thi, phòng được gán, mã phòng, địa điểm và sức chứa.
              </p>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Ca thi / Lịch thi *</label>
                  <select required value={formData.exam_id}
                    onChange={(e) => setFormData(prev => ({ ...prev, exam_id: e.target.value }))}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm">
                    {lookups.exams.map(ex => <option key={ex.id} value={ex.id}>
                      {ex.course_code} – {ex.course_name} ({ex.exam_date})
                    </option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Phòng vật lý *</label>
                  <select required value={formData.room_id} onChange={(e) => handleRoomSelect(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm">
                    {lookups.rooms.map(r => <option key={r.id} value={r.id}>
                      {r.room_number} – {r.building}
                    </option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Mã phòng *</label>
                  <input required maxLength={50} value={formData.room_number}
                    onChange={(e) => setFormData(prev => ({ ...prev, room_number: e.target.value }))}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"/>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Tòa nhà / Địa điểm *</label>
                  <input required maxLength={100} value={formData.building}
                    onChange={(e) => setFormData(prev => ({ ...prev, building: e.target.value }))}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"/>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Sức chứa thiết kế *</label>
                  <input type="number" required min="1" max="10000" value={formData.room_max_capacity}
                    onChange={(e) => setFormData(prev => ({ ...prev, room_max_capacity: e.target.value }))}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"/>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Sức chứa ca thi *</label>
                  <input type="number" required min="1" max={Number(formData.room_max_capacity) || 10000}
                    value={formData.capacity}
                    onChange={(e) => setFormData(prev => ({ ...prev, capacity: e.target.value }))}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"/>
                </div>
              </div>
              <p className="text-xs text-amber-700 bg-amber-50 border border-amber-200 p-3 rounded-lg">
                Lưu ý: Đổi mã phòng, địa điểm hoặc sức chứa thiết kế sẽ cập nhật phòng vật lý cho mọi ca thi đang dùng phòng đó.
                Nếu đã có thí sinh đăng ký, không thể chuyển phòng hoặc đổi ca thi; sức chứa cũng không được nhỏ hơn số đã đăng ký.
              </p>
            </>
          ) : (
            <>
              {/* Chọn ca thi */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                  Ca thi / Lịch thi <span className="text-rose-500">*</span>
                </label>
                <select
                  required
                  value={formData.exam_id}
                  onChange={(e) => setFormData({ ...formData, exam_id: e.target.value })}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
                >
                  <option value="">-- Chọn ca thi --</option>
                  {lookups.exams.map((ex) => (
                    <option key={ex.id} value={ex.id}>
                      {ex.course_code} - {ex.course_name} ({ex.exam_date} lúc {ex.start_time})
                    </option>
                  ))}
                </select>
              </div>

              {/* Chọn phòng vật lý */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                  Phòng thi vật lý <span className="text-rose-500">*</span>
                </label>
                <select
                  required
                  value={formData.room_id}
                  onChange={(e) => handleRoomSelect(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none"
                >
                  <option value="">-- Chọn phòng --</option>
                  {lookups.rooms.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.room_number} - {r.building} (Tối đa: {r.capacity} chỗ)
                    </option>
                  ))}
                </select>
                <QuickAddLookup kind="rooms" onCreated={async (id) => {
                  const response = await lookupsApi.getAll();
                  const newRooms = response.data.data.rooms || [];
                  setLookups(prev => ({ ...prev, rooms: newRooms, exams: response.data.data.exams || [] }));
                  const room = newRooms.find(r => r.id === id);
                  setSelectedPhysicalRoom(room || null);
                  setFormData(prev => ({ ...prev, room_id: id, capacity: room?.capacity || prev.capacity, room_number: room?.room_number || '', building: room?.building || '', room_max_capacity: room?.capacity || prev.room_max_capacity }));
                }} />
              </div>
            </>
          )}
          {!editingItem && (
            <>
          {/* Sức chứa cấu hình */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider">
                Sức chứa phục vụ ca thi này <span className="text-rose-500">*</span>
              </label>
              {selectedPhysicalRoom && (
                <span className="text-xs text-slate-500">
                  Sức chứa thiết kế: <strong className="text-slate-800">{selectedPhysicalRoom.capacity} chỗ</strong>
                </span>
              )}
            </div>
            <input
              type="number"
              min="1"
              max={selectedPhysicalRoom ? selectedPhysicalRoom.capacity : 100}
              required
              value={formData.capacity}
              onChange={(e) => setFormData({ ...formData, capacity: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none font-bold"
            />
            <p className="text-xs text-slate-500 mt-1">
              Hệ thống sẽ chặn đăng ký nếu số lượng thí sinh vượt quá con số này.
            </p>
          </div>
            </>
          )}
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
              {submitting ? 'Đang lưu...' : editingItem ? 'Lưu thay đổi' : 'Gán phòng thi'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
