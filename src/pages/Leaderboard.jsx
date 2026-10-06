import Card from "../components/ui/Card";
import { LEADERBOARD } from "../data/mockData";

export default function Leaderboard() {
  return (
    <div className="page">
      <Card title="Class Leaderboard — Section 3-Mabini">
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
            {LEADERBOARD.map((row, i) => (
              <tr key={i}>
                {row.map((cell, j) => (
                  <td key={j}>{cell}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
