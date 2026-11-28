import React, { useState } from 'react';

// Lựa chọn cũ vẫn giữ nguyên; thêm cách gõ một giá trị khác cho trường văn bản.
export default function EditableChoice({ value, onChange, options, maxLength = 20, placeholder = 'Nhập nội dung mới' }) {
  const isCustom = !!value && !options.some(option => option.value === value);
  const [manual, setManual] = useState(false);
  const showInput = manual || isCustom;
  return (
    <div className="space-y-2">
      <select value={showInput ? '__manual__' : value} onChange={(event) => {
        if (event.target.value === '__manual__') { setManual(true); onChange(''); }
        else { setManual(false); onChange(event.target.value); }
      }} className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none">
        {options.map(option => <option value={option.value} key={option.value}>{option.label}</option>)}
        <option value="__manual__">+ Tự nhập giá trị khác...</option>
      </select>
      {showInput && (
        <input type="text" value={value || ''} maxLength={maxLength} required
          placeholder={placeholder} onChange={e => onChange(e.target.value)}
          className="w-full px-3 py-2 text-sm border border-teal-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:outline-none" />
      )}
    </div>
  );
}
