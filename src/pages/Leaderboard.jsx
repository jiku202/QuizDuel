import { useState } from "react";
import Card from "../components/ui/Card";
import StateMessage from "../components/ui/StateMessage";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";
import SortTh from "../components/ui/SortTh";
import EmptyState from "../components/ui/EmptyState";
import Pill, { RankPill } from "../components/ui/Pill";
import { Person } from "../components/ui/Avatar";
import { useSort } from "../hooks/useSort";

export default function Leaderboard() {
  const sections = useApi(() => api.getSections());
  const sectionList = sections.data || [];

  // ids of the ticked sections; null means "never touched" = All
  const [picked, setPicked] = useState(null);
  const selected = picked ?? sectionList.map((s) => s.id);
  const allChecked = sectionList.length > 0 && sectionList.every((s) => selected.includes(s.id));

  function toggleAll() {
    setPicked(allChecked ? [] : sectionList.map((s) => s.id));
  }

  function toggleSection(id) {
    setPicked(selected.includes(id) ? selected.filter((x) => x !== id) : [...selected, id]);
  }

  // "All" sends no filter, otherwise the ticked ids
  const sectionIds = allChecked ? "" : selected.join(",");
  const board = useApi(
    () => (sectionList.length === 0 || selected.length === 0 ? Promise.resolve([]) : api.getLeaderboard({ sectionIds })),
    [sectionList.length, sectionIds, selected.length]
  );
  const rows = !board.loading && board.data ? board.data : [];
  const sorter = useSort(rows, "position", "asc", { rank: (r) => r.points });

  return (
    <div className="page">
      <StateMessage loading={sections.loading && !sections.data} error={sections.error} />

      {sectionList.length > 0 && (
        <Card title="Sections">
          <div className="checks">
            <label className="check">
              <input type="checkbox" checked={allChecked} onChange={toggleAll} />
              <span>All</span>
            </label>
            {sectionList.map((s) => (
              <label key={s.id} className="check">
                <input type="checkbox" checked={selected.includes(s.id)} onChange={() => toggleSection(s.id)} />
                <span>{s.name}</span>
              </label>
            ))}
          </div>
          <div className="hint">
            Points are kept separately for each section, so a student in several sections gets one row per section.
          </div>
        </Card>
      )}

      {sections.data && sectionList.length === 0 && (
        <EmptyState icon="🏫" title="No sections yet">Create a section first (Sections page) to see its leaderboard.</EmptyState>
      )}

      {sectionList.length > 0 && (
        <Card title="Class Leaderboard">
          <StateMessage loading={board.loading} error={board.error} />
          {selected.length === 0 ? (
            <EmptyState icon="☑️" title="Pick a section">Tick at least one section above to see the leaderboard.</EmptyState>
          ) : (
            !board.loading &&
            board.data &&
            (rows.length === 0 ? (
              <EmptyState icon="🎓" title="No students yet">Students show up here once they join the selected section(s).</EmptyState>
            ) : (
              <div className="table-wrap"><table>
                <thead>
                  <tr>
                    <SortTh label="#" k="position" {...sorter} />
                    <SortTh label="Student" k="name" {...sorter} />
                    <SortTh label="Section" k="sectionName" {...sorter} />
                    <SortTh label="Points" k="points" {...sorter} />
                    <SortTh label="Rank" k="rank" {...sorter} />
                  </tr>
                </thead>
                <tbody>
                  {sorter.sorted.map((r) => (
                    <tr key={`${r.sectionId}-${r.studentId}`}>
                      <td>{["🥇", "🥈", "🥉"][r.position - 1] ? <span className="medal">{["🥇", "🥈", "🥉"][r.position - 1]}</span> : r.position}</td>
                      <td><Person name={r.name} /></td>
                      <td><Pill tone="purple">{r.sectionName}</Pill></td>
                      <td><strong>{r.points}</strong> pts</td>
                      <td><RankPill name={r.rank} /></td>
                    </tr>
                  ))}
                </tbody>
              </table></div>
            ))
          )}
        </Card>
      )}
    </div>
  );
}
