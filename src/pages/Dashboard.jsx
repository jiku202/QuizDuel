import Card from "../components/ui/Card";
import StatBox from "../components/ui/StatBox";
import MasteryChart from "../components/ui/MasteryChart";
import StateMessage from "../components/ui/StateMessage";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";
import { formatTime } from "../utils/formatTime";
import SortTh from "../components/ui/SortTh";
import SortControl from "../components/ui/SortControl";
import { useSort } from "../hooks/useSort";

export default function Dashboard() {
  const { data, loading, error } = useApi(() => api.getDashboard());
  const battleSort = useSort(data?.todaysBattles || []);
  const masterySort = useSort(data?.topicMastery || []);

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
      <div className="row">
        <StatBox label="Active Battles" value={stats.activeBattles} />
        <StatBox label="Students" value={stats.students} />
        <StatBox label="Average Score" value={`${stats.averageScore}%`} />
        <StatBox label="Sections" value={stats.sections} />
      </div>

      <Card title="Topic Mastery">
        {topicMastery.length === 0 ? (
          <div className="muted">No battle answers yet. Mastery appears after students play.</div>
        ) : (
          <>
            <div style={{ marginBottom: 12 }}>
              <SortControl
                options={[{ key: "topic", label: "Topic" }, { key: "percent", label: "Mastery %" }]}
                sort={masterySort.sort}
                onChange={masterySort.setSort}
              />
            </div>
            <MasteryChart data={masterySort.sorted.map((t) => [t.topic, t.percent])} />
          </>
        )}
      </Card>

      <Card title="Today's Battles">
        {todaysBattles.length === 0 ? (
          <div className="muted">No battles today. Generate them from the Battles page.</div>
        ) : (
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
                  <td>{formatTime(b.battleTime)}</td>
                  <td>{b.player1}</td>
                  <td>{b.player2}</td>
                  <td>{b.topic}</td>
                  <td>{b.statusLabel}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  );
}
