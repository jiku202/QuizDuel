import Card from "../components/ui/Card";
import StatBox from "../components/ui/StatBox";
import MasteryChart from "../components/ui/MasteryChart";
import StateMessage from "../components/ui/StateMessage";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";

export default function Analytics() {
  const { data, loading, error } = useApi(() => api.getAnalytics());

  if (!data) {
    return (
      <div className="page">
        <StateMessage loading={loading} error={error} />
      </div>
    );
  }

  return (
    <div className="page">
      <div className="row">
        <StatBox label="Average Score" value={`${data.averageScore}%`} />
        <StatBox label="Battles Today" value={data.battlesToday} />
        <StatBox label="Active Students" value={data.activeStudents} />
      </div>

      <Card title="Topic Mastery">
        {data.topicMastery.length === 0 ? (
          <div className="muted">No battle answers yet.</div>
        ) : (
          <MasteryChart data={data.topicMastery.map((t) => [t.topic, t.percent])} />
        )}
      </Card>
    </div>
  );
}
