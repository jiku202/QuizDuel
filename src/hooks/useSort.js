import { useMemo, useState } from "react";

// Client-side sorting for a list of rows.
//   useSort(rows, "name", "asc", { rank: (r) => r.points })
// `getters` lets a column sort by something other than its own value. Empty values always go last.
export function useSort(rows, initialKey = null, initialDir = "asc", getters = {}) {
  const [sort, setSort] = useState({ key: initialKey, dir: initialDir });

  const sorted = useMemo(() => {
    if (!sort.key) return rows;
    const get = getters[sort.key] || ((r) => r[sort.key]);
    const factor = sort.dir === "asc" ? 1 : -1;
    return [...rows].sort((a, b) => {
      const x = get(a);
      const y = get(b);
      const xEmpty = x === null || x === undefined || x === "";
      const yEmpty = y === null || y === undefined || y === "";
      if (xEmpty && yEmpty) return 0;
      if (xEmpty) return 1;
      if (yEmpty) return -1;
      if (typeof x === "number" && typeof y === "number") return (x - y) * factor;
      return String(x).localeCompare(String(y), undefined, { numeric: true, sensitivity: "base" }) * factor;
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rows, sort]);

  // clicking a column header: same column flips the order, a new column starts ascending
  function toggle(key) {
    setSort((s) => (s.key === key ? { key, dir: s.dir === "asc" ? "desc" : "asc" } : { key, dir: "asc" }));
  }

  return { sorted, sort, setSort, toggle };
}
