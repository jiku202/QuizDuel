// "Sort by [Newest | Oldest | A–Z | Z–A ...]". options: [{ label, key, dir }]; sort/onChange come from useSort.
export default function SortControl({ options, sort, onChange, label = "Sort by" }) {
  const current = options.findIndex((o) => o.key === sort.key && o.dir === sort.dir);
  return (
    <div className="sort-control">
      <label>{label}</label>
      <select
        value={current}
        onChange={(e) => {
          const o = options[Number(e.target.value)];
          if (o) onChange({ key: o.key, dir: o.dir });
        }}
      >
        {current < 0 && <option value={-1}>Default</option>}
        {options.map((o, i) => (
          <option key={o.label} value={i}>{o.label}</option>
        ))}
      </select>
    </div>
  );
}

// Presets reused around the app
export const BY_NEWEST = [
  { label: "Newest first", key: "id", dir: "desc" },
  { label: "Oldest first", key: "id", dir: "asc" },
];
export const alpha = (key) => [
  { label: "Alphabetical (A–Z)", key, dir: "asc" },
  { label: "Alphabetical (Z–A)", key, dir: "desc" },
];
