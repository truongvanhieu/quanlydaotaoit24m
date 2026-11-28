// Native browser print provides both physical printing and Save as PDF, including Vietnamese Unicode.
const esc = value => String(value ?? '').replace(/[&<>"']/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
const date = value => /^\d{4}-\d{2}-\d{2}/.test(value || '') ? value.slice(0,10).split('-').reverse().join('/') : (value || '');
const time = value => (value || '').slice(0,5);
const today = () => new Date().toLocaleDateString('vi-VN');
const style = `@page {size:A4 landscape;margin:12mm} *{box-sizing:border-box} body{font-family:'Times New Roman',serif;color:#111;font-size:12pt;margin:0} .tools{position:sticky;top:0;background:#e8f4f3;padding:12px;text-align:center;font:14px Arial} button{background:#0f766e;color:white;border:0;border-radius:6px;padding:10px 18px;cursor:pointer} .sheet{padding:12mm;max-width:1100px;margin:auto;background:white} .sheet+.sheet{break-before:page} .top{display:grid;grid-template-columns:1fr 2fr;gap:10px;text-align:center;margin-bottom:14px} h1{font-size:16pt;margin:0 0 5px} p{margin:4px 0} table{border-collapse:collapse;width:100%;table-layout:fixed;font-size:10.5pt} th,td{border:1px solid #333;padding:5px 4px;text-align:center;overflow-wrap:anywhere} th{background:#eee} td.name,td.cls{text-align:left} td{height:30px} thead{display:table-header-group} tr{break-inside:avoid} .summary{display:flex;justify-content:space-between;gap:10px;margin:8px 0;flex-wrap:wrap} .sign{display:grid;grid-template-columns:repeat(4,1fr);text-align:center;gap:12px;margin-top:12px;font-size:11pt;break-inside:avoid;min-height:85px} .foot{break-inside:avoid} .note{text-align:right} .slip{max-width:700px;margin:18px auto;border:1px solid #111;padding:24px} .slip h1{text-align:center} .slip p{margin:12px 0} @media print{.tools{display:none}.sheet{padding:0;max-width:none}.slip{margin:0 auto}.sheet+.sheet{break-before:page} body{background:white}}`;
function printDocument(title, html, portrait = false, toolbar = true) {
  return `<!doctype html><html lang="vi"><head><meta charset="UTF-8"><title>${esc(title)}</title><style>${style}${portrait ? '@page{size:A4 portrait}' : ''}</style></head><body>${toolbar ? '<div class="tools"><button type="button" onclick="window.print()">In / Lưu PDF</button> &nbsp; Chọn “Lưu dưới dạng PDF” trong hộp thoại in.</div>' : ''}${html}</body></html>`;
}
function render(win, title, html, portrait = false) {
  if (!win || win.closed) throw new Error('Cửa sổ in đã đóng.');
  win.document.open();
  win.document.write(printDocument(title, html, portrait));
  win.document.close(); win.focus();
}
export const rosterClass = row => row.student_class || '(Chưa có lớp)';
export const rosterGroupLabel = id => `LG-${String(id).slice(0,8).toUpperCase()}`;
export function selectRosterRows(rows, type, className = '', groupId = '') {
  return rows.filter(row => type === 'single' ? !row.merged_group_id && (!className || rosterClass(row) === className) : type === 'merged' ? !!row.merged_group_id && (!groupId || String(row.merged_group_id) === String(groupId)) : true);
}
export function rosterDocument(exam, rows) {
  const groups = new Map();
  for (const r of rows) {
    const key = JSON.stringify([r.exam_room_id, r.merged_group_id ? 'merged' : 'single', r.merged_group_id || rosterClass(r)]);
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push(r);
  }
  const sheets = [...groups.values()].map(list => {
    list.sort((a,b) => String(a.roll_number).localeCompare(String(b.roll_number), 'vi', {numeric:true}));
    const first = list[0];
    const className = first.merged_group_id ? `Lớp gộp: ${rosterGroupLabel(first.merged_group_id)}` : `Lớp riêng: ${rosterClass(first)}`;
    const originClasses = first.merged_group_id ? `<p>Lớp gốc trong danh sách: ${esc([...new Set(list.map(rosterClass))].sort((a,b) => a.localeCompare(b, 'vi')).join(', '))}</p>` : '';
    const absent = list.filter(r => r.result_status === 'Vắng').length;
    const rowHtml = list.map((r,i) => `<tr><td>${i+1}</td><td>${esc(r.student_code)}</td><td class="name">${esc(r.student_name)}</td><td>${esc(date(r.birth_date))}</td><td class="cls">${esc(r.student_class)}</td><td>${esc(r.roll_number)}</td><td></td><td></td><td>${esc(r.score)}</td><td></td><td></td></tr>`).join('');
    return `<section class="sheet"><div class="top"><div><strong>TRƯỜNG ĐẠI HỌC ĐÔNG Á</strong></div><div><h1>DANH SÁCH THI – KÝ TÊN</h1><p><strong>${esc(exam?.exam_type_name || 'Kỳ thi')} – ${esc(first.course_name)} (${esc(first.course_code)})</strong></p><p>Ngày thi: ${esc(date(first.exam_date))} &nbsp; Giờ thi: ${esc(time(first.start_time))}${exam?.end_time ? ' – '+esc(time(exam.end_time)) : ''}</p><p>Phòng thi: ${esc(first.room_number)} (${esc(first.building)}) &nbsp; Hình thức thi: ${esc(exam?.exam_format)}</p><p><strong>${esc(className)}</strong></p>${originClasses}</div></div><table><colgroup><col style="width:4%"><col style="width:9%"><col style="width:19%"><col style="width:9%"><col style="width:10%"><col style="width:12%"><col style="width:7%"><col style="width:10%"><col style="width:5%"><col style="width:7%"><col style="width:8%"></colgroup><thead><tr><th rowspan="2">STT</th><th rowspan="2">Mã SV</th><th rowspan="2">Họ tên sinh viên</th><th rowspan="2">Sinh ngày</th><th rowspan="2">Lớp SH</th><th rowspan="2">SBD</th><th rowspan="2">Mã đề<br>B. làm</th><th rowspan="2">Chữ ký<br>Sinh viên</th><th colspan="2">Điểm</th><th rowspan="2">Nợ HP<br>(Ng. đồng)</th></tr><tr><th>Số</th><th>Chữ</th></tr></thead><tbody>${rowHtml}</tbody></table><div class="foot"><div class="summary"><span>Danh sách có ${list.length} SV.</span><span>Vắng: ${absent || '.......'} &nbsp; – Có phép: ....... &nbsp; – Không phép: .......</span></div><p class="note">Lập ngày ${esc(today())}</p><div class="sign"><b>TRƯỞNG BỘ PHẬN</b><b>GIÁO VỤ</b><b>GIẢNG VIÊN CHẤM 1&amp;2</b><b>GIÁM THỊ 1&amp;2</b></div></div></section>`;
  }).join('');
  return printDocument('Danh sách ký tên - ' + (exam?.course_code || ''), sheets, false, false);
}
export function printExamSlip(row, exam) {
  const win = window.open('', '_blank');
  if (!win) throw new Error('Trình duyệt đã chặn cửa sổ in.');
  render(win, 'Phiếu dự thi - ' + row.roll_number, `<section class="sheet"><div class="slip"><p style="text-align:center"><b>TRƯỜNG ĐẠI HỌC ĐÔNG Á</b></p><h1>PHIẾU DỰ THI</h1><p><b>Kỳ thi:</b> ${esc(exam?.exam_type_name)}</p><p><b>Họ tên sinh viên:</b> ${esc(row.student_name)}</p><p><b>Mã sinh viên:</b> ${esc(row.student_code)} &nbsp; <b>Lớp SH:</b> ${esc(row.student_class)}</p><p><b>Ngày sinh:</b> ${esc(date(row.birth_date))}</p><p><b>Số báo danh:</b> ${esc(row.roll_number)}</p><p><b>Học phần:</b> ${esc(row.course_name)} (${esc(row.course_code)})</p><p><b>Ngày thi:</b> ${esc(date(row.exam_date))} &nbsp; <b>Giờ thi:</b> ${esc(time(row.start_time))}${exam?.end_time ? ' – '+esc(time(exam.end_time)) : ''}</p><p><b>Phòng thi:</b> ${esc(row.room_number)} – ${esc(row.building)}</p><p><b>Hình thức thi:</b> ${esc(exam?.exam_format)}</p><p style="text-align:right">Lập ngày ${esc(today())}</p><div style="text-align:right;min-height:85px"><b>GIÁO VỤ / PHÒNG KHẢO THÍ</b></div></div></section>`, true);
}
