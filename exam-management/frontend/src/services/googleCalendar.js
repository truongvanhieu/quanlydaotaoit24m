const TIMEZONE = 'Asia/Ho_Chi_Minh';

function formatIcsDateTime(examDate, timeStr) {
  if (!examDate) return '';
  const datePart = String(examDate).slice(0, 10).replace(/-/g, '');
  const cleanTime = String(timeStr || '08:00').trim();
  const [h = '08', m = '00', s = '00'] = cleanTime.split(':');
  return `${datePart}T${h.padStart(2, '0')}${m.padStart(2, '0')}${s.padStart(2, '0')}`;
}

function buildTitle(item) {
  const code = item.course_code ? ` (${item.course_code})` : '';
  return `[Thi] ${item.course_name || 'Lịch thi'}${code}`;
}

function buildLocation(item) {
  if (!item.room_number) return '';
  return item.building ? `Phòng ${item.room_number} - ${item.building}` : `Phòng ${item.room_number}`;
}

function buildDetails(item) {
  const lines = [];
  if (item.exam_type_name) lines.push(`Kỳ thi: ${item.exam_type_name}`);
  if (item.semester_name) lines.push(`Học kỳ: ${item.semester_name}${item.academic_year ? ` (${item.academic_year})` : ''}`);
  if (item.exam_format) lines.push(`Hình thức: ${item.exam_format}`);
  if (item.duration_minutes) lines.push(`Thời lượng: ${item.duration_minutes} phút`);
  if (item.roll_number) lines.push(`Số báo danh: ${item.roll_number}`);
  if (Array.isArray(item.invigilators) && item.invigilators.length > 0) {
    lines.push(`Cán bộ coi thi: ${item.invigilators.map((i) => i.full_name).join(', ')}`);
  }
  return lines.join('\n');
}

function escapeIcsText(value) {
  return String(value || '')
    .replace(/\\/g, '\\\\')
    .replace(/\r?\n/g, '\\n')
    .replace(/,/g, '\\,')
    .replace(/;/g, '\\;');
}

export function buildGoogleCalendarUrl(item) {
  const start = formatIcsDateTime(item.exam_date, item.start_time);
  const end = formatIcsDateTime(item.exam_date, item.end_time || item.start_time);
  const params = new URLSearchParams({
    action: 'TEMPLATE',
    text: buildTitle(item),
    dates: `${start}/${end}`,
    ctz: TIMEZONE,
    details: buildDetails(item),
  });
  const location = buildLocation(item);
  if (location) params.set('location', location);
  return `https://calendar.google.com/calendar/render?${params.toString()}`;
}

export function downloadExamsIcs(items, filename = 'lich-thi.ics') {
  if (!Array.isArray(items) || items.length === 0) return;

  const nowStamp = new Date().toISOString().replace(/[-:]/g, '').replace(/\.\d{3}Z$/, 'Z');
  const events = items
    .filter((item) => item && item.exam_date)
    .map((item, idx) => {
      const start = formatIcsDateTime(item.exam_date, item.start_time);
      const end = formatIcsDateTime(item.exam_date, item.end_time || item.start_time);
      const uid = `${item.id || item.registration_id || idx}-${start}@exam-management`;
      const location = buildLocation(item);
      const lines = [
        'BEGIN:VEVENT',
        `UID:${uid}`,
        `DTSTAMP:${nowStamp}`,
        `DTSTART;TZID=${TIMEZONE}:${start}`,
        `DTEND;TZID=${TIMEZONE}:${end}`,
        `SUMMARY:${escapeIcsText(buildTitle(item))}`,
        `DESCRIPTION:${escapeIcsText(buildDetails(item))}`,
      ];
      if (location) lines.push(`LOCATION:${escapeIcsText(location)}`);
      lines.push('END:VEVENT');
      return lines.join('\r\n');
    });

  if (events.length === 0) return;

  const icsContent = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//Exam Management//Lich Thi//VI',
    'CALSCALE:GREGORIAN',
    'X-WR-CALNAME:Lịch thi',
    `X-WR-TIMEZONE:${TIMEZONE}`,
    'BEGIN:VTIMEZONE',
    `TZID:${TIMEZONE}`,
    'BEGIN:STANDARD',
    'DTSTART:19700101T000000',
    'TZOFFSETFROM:+0700',
    'TZOFFSETTO:+0700',
    'TZNAME:+07',
    'END:STANDARD',
    'END:VTIMEZONE',
    ...events,
    'END:VCALENDAR',
  ].join('\r\n');

  const blob = new Blob([icsContent], { type: 'text/calendar;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}
