import Card from "../components/ui/Card";
import StatBox from "../components/ui/StatBox";
import MasteryChart from "../components/ui/MasteryChart";
import StateMessage from "../components/ui/StateMessage";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";

export default function Dashboard() {
  const { data, loading, error } = useApi(() => api.getDashboard());

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
          <MasteryChart data={topicMastery.map((t) => [t.topic, t.percent])} />
        )}
      </Card>

      <Card title="Today's Battles">
        {todaysBattles.length === 0 ? (
          <div className="muted">No battles today. Generate them from the Battles page.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Player 1</th>
                <th>Player 2</th>
                <th>Topic</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {todaysBattles.map((b) => (
                <tr key={b.id}>
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
