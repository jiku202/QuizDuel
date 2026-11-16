// Clickable table header that sorts by column `k`. Use with useSort: <SortTh label="Name" k="name" {...sorter} />
export default function SortTh({ label, k, sort, toggle }) {
  const active = sort.key === k;
  return (
    <th
      className="sortable"
      onClick={() => toggle(k)}
      aria-sort={active ? (sort.dir === "asc" ? "ascending" : "descending") : "none"}
      title="Click to sort"
    >
      {label}
      <span className={"sort-arrow" + (active ? " on" : "")}>{active ? (sort.dir === "asc" ? "▲" : "▼") : "↕"}</span>
    </th>
  );
}
