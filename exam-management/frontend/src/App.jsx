import React, { useState } from 'react';
import {
  Layers,
  Calendar,
  DoorOpen,
  UserCheck,
  Award,
  FileCode,
  GraduationCap,
  Menu,
  X,
  FileSpreadsheet,
} from 'lucide-react';

// Screens
import ExamTypesScreen from './screens/ExamTypesScreen';
import ExamsScreen from './screens/ExamsScreen';
import ExamRoomsScreen from './screens/ExamRoomsScreen';
import ExamRegistrationsScreen from './screens/ExamRegistrationsScreen';
import ExamResultsScreen from './screens/ExamResultsScreen';
import ExamPapersScreen from './screens/ExamPapersScreen';
import StudentPortalScreen from './screens/StudentPortalScreen';

export default function App() {
  const [activeTab, setActiveTab] = useState('exam_types');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navItems = [
    {
      id: 'exam_types',
      label: '1. Loại kỳ thi',
      icon: <Layers className="w-5 h-5 flex-shrink-0" />,
    },
    {
      id: 'exams',
      label: '2. Lịch thi',
      icon: <Calendar className="w-5 h-5 flex-shrink-0" />,
    },
    {
      id: 'exam_rooms',
      label: '3. Phòng thi',
      icon: <DoorOpen className="w-5 h-5 flex-shrink-0" />,
    },
    {
      id: 'exam_registrations',
      label: '4. Danh sách thí sinh',
      icon: <UserCheck className="w-5 h-5 flex-shrink-0" />,
    },
    {
      id: 'exam_results',
      label: '5. Kết quả thi',
      icon: <Award className="w-5 h-5 flex-shrink-0" />,
    },
    {
      id: 'exam_papers',
      label: '6. Đề thi',
      icon: <FileCode className="w-5 h-5 flex-shrink-0" />,
    },
    {
      id: 'student_portal',
      label: '7. Cổng sinh viên',
      icon: <GraduationCap className="w-5 h-5 flex-shrink-0" />,
      highlight: true,
    },
  ];

  return (
    <div className="min-h-screen flex flex-col bg-slate-100 text-slate-800 w-full">
      {/* Header Full Width */}
      <header className="bg-teal-700 text-white shadow-md sticky top-0 z-40 w-full">
        <div className="w-full px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="lg:hidden p-2 rounded-lg text-teal-100 hover:text-white hover:bg-teal-800 focus:outline-none transition"
              title="Mở menu"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>

            <div className="w-10 h-10 rounded-lg bg-teal-800 flex items-center justify-center text-white shadow font-bold text-lg">
              KT
            </div>
            <div>
              <h1 className="font-bold text-lg sm:text-xl tracking-tight leading-tight">
                HỆ THỐNG QUẢN LÝ THI – KHẢO THÍ
              </h1>
              <p className="text-xs text-teal-100 hidden sm:block">
                Quản lý kỳ thi, tổ chức phòng thi, cấp số báo danh và công bố kết quả
              </p>
            </div>
          </div>
        </div>
      </header>

      {/* Main Layout: 2 Columns Full Width */}
      <div className="flex-1 flex w-full">
        {/* Sidebar Navigation */}
        <aside
          className={`${
            mobileMenuOpen ? 'fixed inset-y-0 left-0 z-50 bg-white w-72 shadow-2xl p-4' : 'hidden'
          } lg:block lg:w-64 xl:w-72 bg-white border-r border-slate-200 flex-shrink-0 min-h-[calc(100vh-4rem)] p-4`}
        >
          {mobileMenuOpen && (
            <div className="flex items-center justify-between pb-3 mb-3 border-b border-slate-200 lg:hidden">
              <span className="font-bold text-slate-700 text-sm">Danh mục chức năng</span>
              <button
                onClick={() => setMobileMenuOpen(false)}
                className="p-1.5 rounded-lg text-slate-400 hover:bg-slate-100"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
          )}

          <div className="text-xs font-bold text-slate-400 uppercase tracking-wider px-3 mb-2">
            Chức năng quản lý
          </div>

          <nav className="space-y-1">
            {navItems.map((item) => {
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => {
                    setActiveTab(item.id);
                    setMobileMenuOpen(false);
                  }}
                  className={`w-full text-left px-3.5 py-3 rounded-xl flex items-center space-x-3 transition-colors ${
                    isActive
                      ? 'bg-teal-700 text-white font-semibold shadow-sm'
                      : item.highlight
                      ? 'bg-amber-50 text-amber-900 hover:bg-amber-100 font-semibold border border-amber-200 mt-3'
                      : 'text-slate-700 hover:bg-slate-100 font-medium'
                  }`}
                >
                  <div className={isActive ? 'text-white' : item.highlight ? 'text-amber-700' : 'text-slate-500'}>
                    {item.icon}
                  </div>
                  <span className="text-sm font-medium">{item.label}</span>
                </button>
              );
            })}
          </nav>
        </aside>

        {/* Content Area - Uses 100% remaining space */}
        <main className="flex-1 w-full min-w-0 p-4 sm:p-6 lg:p-8 overflow-x-auto bg-slate-50">
          <div className="w-full">
            {activeTab === 'exam_types' && <ExamTypesScreen />}
            {activeTab === 'exams' && <ExamsScreen />}
            {activeTab === 'exam_rooms' && <ExamRoomsScreen />}
            {activeTab === 'exam_registrations' && <ExamRegistrationsScreen />}
            {activeTab === 'exam_results' && <ExamResultsScreen />}
            {activeTab === 'exam_papers' && <ExamPapersScreen />}
            {activeTab === 'student_portal' && <StudentPortalScreen />}
          </div>
        </main>
      </div>

      {/* Footer Minimal Full Width */}
      <footer className="bg-white border-t border-slate-200 py-3 w-full">
        <div className="w-full px-4 sm:px-6 lg:px-8 text-center text-xs text-slate-500">
          Dự án Quản lý Thi – Khảo thí &copy; 2026
        </div>
      </footer>
    </div>
  );
}
