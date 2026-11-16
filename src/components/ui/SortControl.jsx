// "Sort by [field] [↑/↓]" for card and row lists. options: [{ key, label }]; sort/onChange come from useSort.
export default function SortControl({ options, sort, onChange, label = "Sort by" }) {
  return (
    <div className="sort-control">
      <label>{label}</label>
      <select value={sort.key || ""} onChange={(e) => onChange({ key: e.target.value || null, dir: sort.dir })}>
        <option value="">Default</option>
        {options.map((o) => (
          <option key={o.key} value={o.key}>{o.label}</option>
        ))}
      </select>
      <button
        type="button"
        className="btn small"
        title="Switch ascending / descending"
        disabled={!sort.key}
        onClick={() => onChange({ key: sort.key, dir: sort.dir === "asc" ? "desc" : "asc" })}
      >
        {sort.dir === "asc" ? "↑ Asc" : "↓ Desc"}
      </button>
    </div>
  );
}
