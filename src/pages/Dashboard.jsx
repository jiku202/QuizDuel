import Card from "../components/ui/Card";
import StatBox from "../components/ui/StatBox";
import MasteryChart from "../components/ui/MasteryChart";
import StateMessage from "../components/ui/StateMessage";
import EmptyState from "../components/ui/EmptyState";
import Pill from "../components/ui/Pill";
import { Person } from "../components/ui/Avatar";
import SortTh from "../components/ui/SortTh";
import SortControl from "../components/ui/SortControl";
import { useApi } from "../hooks/useApi";
import { useSort } from "../hooks/useSort";
import { useAuth } from "../context/useAuth";
import { api } from "../services/api";
import { formatTime } from "../utils/formatTime";
import { statusTone } from "../utils/badges";

const MASTERY_SORTS = [
  { label: "Alphabetical (A–Z)", key: "topic", dir: "asc" },
  { label: "Alphabetical (Z–A)", key: "topic", dir: "desc" },
  { label: "Highest mastery", key: "percent", dir: "desc" },
  { label: "Lowest mastery", key: "percent", dir: "asc" },
];

export default function Dashboard() {
  const { teacher } = useAuth();
  const { data, loading, error } = useApi(() => api.getDashboard());
  const battleSort = useSort(data?.todaysBattles || []);
  const masterySort = useSort(data?.topicMastery || [], "topic", "asc");

  if (!data) {
    return (
      <div className="page">
        <StateMessage loading={loading} error={error} />
      </div>
    );
  }

  const { stats, topicMastery, todaysBattles } = data;

  return (
    <div className="page">
      <div className="welcome">
        <div>
          <h2>Welcome back{teacher?.name ? `, ${teacher.name}` : ""}! 👋</h2>
          <p>Here's how your classes are doing today.</p>
        </div>
        <div className="w-art">🏆</div>
      </div>

      <div className="row">
        <StatBox icon="⚔️" tone="coral" label="Active Battles" value={stats.activeBattles} />
        <StatBox icon="🎓" label="Students" value={stats.students} />
        <StatBox icon="📈" tone="gold" label="Average Score" value={`${stats.averageScore}%`} />
        <StatBox icon="🏫" tone="purple" label="Sections" value={stats.sections} />
      </div>

      <Card title="Topic Mastery">
        {topicMastery.length === 0 ? (
          <EmptyState icon="🧠" title="No answers yet">
            Mastery shows up here once your students start playing battles.
          </EmptyState>
        ) : (
          <>
            <div style={{ marginBottom: 14 }}>
              <SortControl options={MASTERY_SORTS} sort={masterySort.sort} onChange={masterySort.setSort} />
            </div>
            <MasteryChart data={masterySort.sorted.map((t) => [t.topic, t.percent])} />
          </>
        )}
      </Card>

      <Card title="Today's Battles">
        {todaysBattles.length === 0 ? (
          <EmptyState icon="⚔️" title="No battles today">
            Generate them from the Battles page and your students will be paired up.
          </EmptyState>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <SortTh label="Time" k="battleTime" {...battleSort} />
                  <SortTh label="Player 1" k="player1" {...battleSort} />
                  <SortTh label="Player 2" k="player2" {...battleSort} />
                  <SortTh label="Topic" k="topic" {...battleSort} />
                  <SortTh label="Status" k="statusLabel" {...battleSort} />
                </tr>
              </thead>
              <tbody>
                {battleSort.sorted.map((b) => (
                  <tr key={b.id}>
                    <td>🕒 {formatTime(b.battleTime)}</td>
                    <td><Person name={b.player1} /></td>
                    <td><Person name={b.player2} /></td>
                    <td>{b.topic}</td>
                    <td><Pill tone={statusTone(b.statusLabel)}>{b.statusLabel}</Pill></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
