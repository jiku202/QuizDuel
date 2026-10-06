import Card from "../components/ui/Card";
import StatBox from "../components/ui/StatBox";
import MasteryChart from "../components/ui/MasteryChart";
import { TODAY_BATTLES, MASTERY } from "../data/mockData";

export default function Dashboard() {
  return (
    <div className="page">
      <div className="row">
        <StatBox label="Active Battles" value="14" />
        <StatBox label="Students" value="156" />
        <StatBox label="Average Score" value="76%" />
        <StatBox label="Sections" value="5" />
      </div>

      <Card title="Topic Mastery">
        <MasteryChart data={MASTERY} />
      </Card>

      <Card title="Today's Battles">
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
            {TODAY_BATTLES.map((row, i) => (
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
