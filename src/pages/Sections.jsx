import { useState } from "react";
import Card from "../components/ui/Card";
import { ROSTER } from "../data/mockData";

export default function Sections() {
  const [roster] = useState(ROSTER);

  function awardBadge(name) {
    alert("Badge awarded to " + name + " (demo)");
  }

  return (
    <div className="page">
      <Card title="Create New Section">
        <form onSubmit={(e) => e.preventDefault()}>
          <div className="row">
            <div className="col field">
              <label>Section Name</label>
              <input type="text" placeholder="Grade 3 - Mabini" />
            </div>
            <div className="col field">
              <label>Grade Level</label>
              <input type="text" placeholder="Grade 3" />
            </div>
          </div>
          <div className="field">
            <label>Subject</label>
            <input type="text" placeholder="Social Studies" />
          </div>
          <button className="btn solid" type="submit">
            Generate Join Code
          </button>
        </form>
      </Card>

      <Card title="Class Roster — Section 3-Mabini">
        <table>
          <thead>
            <tr>
              <th>Student</th>
              <th>Points</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {roster.map(([name, pts], i) => (
              <tr key={i}>
                <td>{name}</td>
                <td>{pts}</td>
                <td>
                  <button className="btn small" onClick={() => awardBadge(name)}>
                    Award Badge
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
