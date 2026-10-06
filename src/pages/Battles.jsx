import Card from "../components/ui/Card";
import { TODAY_BATTLES, BATTLE_LOG } from "../data/mockData";

export default function Battles() {
  return (
    <div className="page">
      <Card title="Today's Battles — Oct 10">
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

      <Card title="Battle Results Log">
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Player 1</th>
              <th>Player 2</th>
              <th>Winner</th>
            </tr>
          </thead>
          <tbody>
            {BATTLE_LOG.map((row, i) => (
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
