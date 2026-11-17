import Card from "../components/ui/Card";
import StatBox from "../components/ui/StatBox";
import MasteryChart from "../components/ui/MasteryChart";
import StateMessage from "../components/ui/StateMessage";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";
import SortControl from "../components/ui/SortControl";
import EmptyState from "../components/ui/EmptyState";
import { useSort } from "../hooks/useSort";

export default function Analytics() {
  const { data, loading, error } = useApi(() => api.getAnalytics());
  const masterySort = useSort(data?.topicMastery || [], "topic", "asc");

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
        <StatBox icon="📈" tone="gold" label="Average Score" value={`${data.averageScore}%`} />
        <StatBox icon="⚔️" tone="coral" label="Battles Today" value={data.battlesToday} />
        <StatBox icon="🔥" label="Active Students" value={data.activeStudents} />
      </div>

      <Card title="Topic Mastery">
        {data.topicMastery.length === 0 ? (
          <EmptyState icon="🧠" title="No answers yet">Mastery shows up once your students start playing.</EmptyState>
        ) : (
          <>
            <div style={{ marginBottom: 14 }}>
              <SortControl
                options={[
                  { label: "Alphabetical (A–Z)", key: "topic", dir: "asc" },
                  { label: "Alphabetical (Z–A)", key: "topic", dir: "desc" },
                  { label: "Highest mastery", key: "percent", dir: "desc" },
                  { label: "Lowest mastery", key: "percent", dir: "asc" },
                ]}
                sort={masterySort.sort}
                onChange={masterySort.setSort}
              />
            </div>
            <MasteryChart data={masterySort.sorted.map((t) => [t.topic, t.percent])} />
          </>
        )}
      </Card>
    </div>
  );
}
