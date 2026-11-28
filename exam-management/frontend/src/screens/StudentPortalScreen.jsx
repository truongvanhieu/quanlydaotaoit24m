import React, { useState, useEffect } from 'react';
import { GraduationCap, Search, Calendar, CalendarPlus, Download, DoorOpen, Award, Clock, FileText, CheckCircle2, AlertCircle } from 'lucide-react';
import { studentPortalApi, lookupsApi } from '../services/api';
import { buildGoogleCalendarUrl, downloadExamsIcs } from '../services/googleCalendar';
import Alert from '../components/Alert';

export default function StudentPortalScreen() {
  const [studentCodeInput, setStudentCodeInput] = useState('B21DCCN001');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState(null);
  const [studentsList, setStudentsList] = useState([]);

  useEffect(() => {
    // Load student list for quick selection
    lookupsApi
      .getAll()
      .then((res) => {
        setStudentsList(res.data.data.students || []);
      })
      .catch((err) => console.error(err));

    // Initial search
    handleSearch('B21DCCN001');
  }, []);

  const handleSearch = async (codeToSearch) => {
    const code = (codeToSearch || studentCodeInput || '').trim();
    if (!code) {
      setAlert({ type: 'warning', message: 'Vui lòng nhập mã sinh viên!' });
      return;
    }

    setLoading(true);
    setAlert(null);
    try {
      const res = await studentPortalApi.getByStudentCode(code);
      setData(res.data.data);
    } catch (err) {
      setData(null);
      setAlert({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleSelectStudent = (code) => {
    setStudentCodeInput(code);
    handleSearch(code);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-gradient-to-r from-teal-700 to-teal-900 text-white p-6 rounded-2xl shadow-md">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-teal-200 text-sm font-semibold tracking-wider uppercase mb-1">
              <GraduationCap className="w-5 h-5 text-teal-300" />
              <span>Cổng Thông Tin Sinh Viên</span>
            </div>
            <h1 className="text-2xl font-bold">Tra cứu Lịch thi, Phòng thi & Điểm thi</h1>
            <p className="text-teal-100 text-sm mt-1 max-w-xl">
              Sinh viên có thể tra cứu lịch thi, phòng thi và kết quả điểm thi chính thức của mình
            </p>
          </div>

          {/* Quick Select Student */}
          <div className="bg-white/10 backdrop-blur-sm p-3 rounded-xl border border-white/20">
            <label className="block text-xs font-semibold text-teal-200 mb-1">Chọn sinh viên mẫu để test:</label>
            <select
              value={studentCodeInput}
              onChange={(e) => handleSelectStudent(e.target.value)}
              className="px-3 py-1.5 text-sm rounded-lg bg-teal-800 text-white border border-teal-600 focus:outline-none focus:ring-2 focus:ring-white"
            >
              {studentsList.map((st) => (
                <option key={st.id} value={st.student_code}>
                  {st.student_code} - {st.full_name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Search Bar */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSearch();
          }}
          className="mt-6 flex flex-col sm:flex-row gap-2 max-w-lg"
        >
          <div className="relative flex-1">
            <input
              type="text"
              value={studentCodeInput}
              onChange={(e) => setStudentCodeInput(e.target.value)}
              placeholder="Nhập mã sinh viên (VD: B21DCCN001)..."
              className="w-full pl-10 pr-4 py-2.5 rounded-xl text-slate-800 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-teal-300 shadow-sm"
            />
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3.5" />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="px-6 py-2.5 bg-teal-500 hover:bg-teal-400 text-teal-950 font-bold text-sm rounded-xl shadow transition"
          >
            {loading ? 'Đang tra cứu...' : 'Tra cứu'}
          </button>
        </form>
      </div>

      {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

      {/* Student Profile Card */}
      {data?.student && (
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <h2 className="text-sm font-bold uppercase tracking-wider text-teal-700 mb-3 flex items-center">
            <span className="w-2 h-2 rounded-full bg-teal-500 mr-2"></span>
            Thông tin sinh viên
          </h2>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-4 text-sm">
            <div>
              <span className="text-xs text-slate-400 block">Họ và tên</span>
              <strong className="text-slate-900 font-semibold">{data.student.full_name}</strong>
            </div>
            <div>
              <span className="text-xs text-slate-400 block">Mã sinh viên</span>
              <strong className="text-teal-700 font-mono font-bold">{data.student.student_code}</strong>
            </div>
            <div>
              <span className="text-xs text-slate-400 block">Lớp quản lý</span>
              <span className="text-slate-700 font-medium">{data.student.class_name || '—'}</span>
            </div>
            <div>
              <span className="text-xs text-slate-400 block">Email sinh viên</span>
              <span className="text-slate-700 text-xs font-mono">{data.student.email || '—'}</span>
            </div>
            <div>
              <span className="text-xs text-slate-400 block">Số điện thoại</span>
              <span className="text-slate-700 text-xs">{data.student.phone || '—'}</span>
            </div>
          </div>
        </div>
      )}

      {/* Exam Schedule & Results Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-200 bg-slate-50 flex flex-wrap items-center justify-between gap-3">
          <div className="font-bold text-slate-800 text-base">
            Lịch thi & Kết quả điểm thi ({data?.exams?.length || 0} môn thi)
          </div>
          {data?.exams?.length > 0 && (
            <button
              type="button"
              onClick={() => downloadExamsIcs(data.exams, `lich-thi-${data.student?.student_code || 'sv'}.ics`)}
              title="Tải file .ics để nhập toàn bộ lịch thi vào Google Calendar"
              className="inline-flex items-center px-3 py-1.5 bg-white hover:bg-slate-100 text-slate-700 border border-slate-300 font-medium text-xs rounded-lg shadow-sm transition"
            >
              <Download className="w-3.5 h-3.5 mr-1.5 text-blue-600" />
              Xuất Google Calendar (.ics)
            </button>
          )}
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600 border-collapse">
            <thead className="bg-slate-100/70 text-slate-700 font-semibold uppercase text-xs border-b border-slate-200 tracking-wider">
              <tr>
                <th className="px-4 py-3.5">Học phần / Môn thi</th>
                <th className="px-4 py-3.5">Kỳ thi / Học kỳ</th>
                <th className="px-4 py-3.5">Thời gian thi</th>
                <th className="px-4 py-3.5">Phòng thi</th>
                <th className="px-4 py-3.5 text-center">Số báo danh</th>
                <th className="px-4 py-3.5 text-center">Đề thi</th>
                <th className="px-4 py-3.5 text-center">Điểm thi</th>
                <th className="px-4 py-3.5 text-center">Trạng thái điểm</th>
                <th className="px-4 py-3.5">Phúc khảo</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="9" className="text-center py-8 text-slate-500">
                    <div className="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-teal-600 mb-2"></div>
                    <div>Đang tải dữ liệu...</div>
                  </td>
                </tr>
              ) : !data || data.exams.length === 0 ? (
                <tr>
                  <td colSpan="9" className="text-center py-8 text-slate-400 font-medium">
                    Sinh viên này hiện chưa được xếp lịch thi nào
                  </td>
                </tr>
              ) : (
                data.exams.map((item) => (
                  <tr key={item.registration_id} className="hover:bg-slate-50/80 transition">
                    <td className="px-4 py-3.5">
                      <div className="font-semibold text-slate-900">{item.course_name}</div>
                      <div className="text-xs text-slate-500 font-mono">
                        {item.course_code} ({item.credits} tín chỉ)
                      </div>
                    </td>

                    <td className="px-4 py-3.5 whitespace-nowrap text-xs">
                      <span className="font-medium text-teal-800 bg-teal-50 px-2 py-0.5 rounded border border-teal-200">
                        {item.exam_type_name}
                      </span>
                      <div className="text-slate-500 mt-1">{item.semester_name}</div>
                    </td>

                    <td className="px-4 py-3.5 whitespace-nowrap text-xs">
                      <div className="flex items-center gap-1.5">
                        <span className="font-bold text-slate-800">{item.exam_date}</span>
                        <a
                          href={buildGoogleCalendarUrl(item)}
                          target="_blank"
                          rel="noreferrer"
                          title="Thêm môn thi này vào Google Calendar"
                          className="inline-flex items-center p-1 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded transition"
                        >
                          <CalendarPlus className="w-3.5 h-3.5" />
                        </a>
                      </div>
                      <div className="text-slate-500 flex items-center mt-0.5">
                        <Clock className="w-3 h-3 mr-1 text-slate-400" />
                        {item.start_time} - {item.end_time} ({item.duration_minutes}p)
                      </div>
                    </td>

                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <span className="font-bold text-slate-800 bg-slate-100 px-2 py-0.5 rounded text-xs border border-slate-200">
                        {item.room_number}
                      </span>
                      <div className="text-[11px] text-slate-400 mt-0.5">{item.building}</div>
                    </td>

                    <td className="px-4 py-3.5 text-center whitespace-nowrap">
                      <span className="font-mono font-bold text-teal-800 bg-teal-50 px-2.5 py-1 rounded border border-teal-200 text-xs">
                        {item.roll_number}
                      </span>
                    </td>

                    <td className="px-4 py-3.5 text-center whitespace-nowrap text-xs">
                      {item.paper_code ? (
                        <a
                          href={item.paper_file_url}
                          target="_blank"
                          rel="noreferrer"
                          className="font-mono font-semibold text-teal-600 hover:text-teal-800 underline"
                          title="Tải hoặc xem file đề thi"
                        >
                          {item.paper_code}
                        </a>
                      ) : (
                        <span className="text-slate-400 italic">Chưa phát</span>
                      )}
                    </td>

                    <td className="px-4 py-3.5 text-center whitespace-nowrap">
                      {item.score !== null ? (
                        <div
                          className={`font-mono text-base font-extrabold ${
                            item.score >= 4.0 ? 'text-emerald-600' : 'text-rose-600'
                          }`}
                        >
                          {Number(item.score).toFixed(2)}
                        </div>
                      ) : (
                        <span className="text-xs text-slate-400 italic">Chưa có điểm</span>
                      )}
                    </td>

                    <td className="px-4 py-3.5 text-center whitespace-nowrap">
                      {item.is_locked ? (
                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
                          <CheckCircle2 className="w-3 h-3 mr-1" />
                          Đã chốt chính thức
                        </span>
                      ) : item.result_id ? (
                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800">
                          Điểm tạm tính
                        </span>
                      ) : (
                        <span className="text-xs text-slate-400">—</span>
                      )}
                    </td>

                    <td className="px-4 py-3.5 whitespace-nowrap text-xs">
                      {item.appeal_status === 'Chờ xử lý' ? (
                        <span className="font-bold text-amber-700 bg-amber-50 px-2 py-0.5 rounded border border-amber-200">
                          Đang phúc khảo
                        </span>
                      ) : item.appeal_status === 'Đã duyệt' ? (
                        <span className="font-bold text-blue-700 bg-blue-50 px-2 py-0.5 rounded border border-blue-200">
                          Đã duyệt phúc khảo
                        </span>
                      ) : (
                        <span className="text-slate-400">Không</span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
