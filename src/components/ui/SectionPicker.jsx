// Dropdown of the teacher's sections. Pass allowAll to include an "All sections" option (value "").
export default function SectionPicker({ sections, value, onChange, allowAll = false }) {
  return (
    <select value={value} onChange={(e) => onChange(e.target.value)}>
      {allowAll && <option value="">All sections</option>}
      {sections.map((s) => (
        <option key={s.id} value={s.id}>
          {s.name}
        </option>
      ))}
    </select>
  );
}
