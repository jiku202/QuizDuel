import Card from "../components/ui/Card";
import StateMessage from "../components/ui/StateMessage";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";

export default function Leaderboard() {
  const { data, loading, error } = useApi(() => api.getLeaderboard());

  return (
    <div className="page">
      <StateMessage loading={loading} error={error} />
      {data && (
        <Card title="Class Leaderboard">
          {data.length === 0 ? (
            <div className="muted">No students have joined yet.</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Rank</th>
                  <th>Student</th>
                  <th>Points</th>
                  <th>Badges</th>
                </tr>
              </thead>
              <tbody>
                {data.map((r) => (
                  <tr key={r.studentId}>
                    <td>{r.rank}</td>
                    <td>{r.name}</td>
                    <td>{r.points}</td>
                    <td>{r.badges}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      )}
    </div>
  );
}
