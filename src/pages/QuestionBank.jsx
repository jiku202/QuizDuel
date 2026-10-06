import { useState } from "react";
import Card from "../components/ui/Card";
import Chip from "../components/ui/Chip";
import { TOPICS, QUESTIONS } from "../data/mockData";

export default function QuestionBank() {
  const [activeTopic, setActiveTopic] = useState("All");
  const [showAdd, setShowAdd] = useState(false);

  const filtered =
    activeTopic === "All" ? QUESTIONS : QUESTIONS.filter((q) => q.topic === activeTopic);

  return (
    <div className="page">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <h3>Question Bank</h3>
        <button className="btn solid" onClick={() => setShowAdd(!showAdd)}>
          + Add Question
        </button>
      </div>

      <div className="row" style={{ flexWrap: "wrap", gap: 8 }}>
        <Chip active={activeTopic === "All"} onClick={() => setActiveTopic("All")}>
          All
        </Chip>
        {TOPICS.map((t) => (
          <Chip key={t} active={activeTopic === t} onClick={() => setActiveTopic(t)}>
            {t}
          </Chip>
        ))}
      </div>

      {showAdd && (
        <Card title="New Question">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              setShowAdd(false);
            }}
          >
            <div className="field">
              <label>Topic</label>
              <select>
                {TOPICS.map((t) => (
                  <option key={t}>{t}</option>
                ))}
              </select>
            </div>
            <div className="field">
              <label>Question</label>
              <textarea rows="2" placeholder="Enter the question..."></textarea>
            </div>
            <div className="row">
              <div className="col field">
                <label>Answer A</label>
                <input type="text" />
              </div>
              <div className="col field">
                <label>Answer B</label>
                <input type="text" />
              </div>
            </div>
            <div className="row">
              <div className="col field">
                <label>Answer C</label>
                <input type="text" />
              </div>
              <div className="col field">
                <label>Answer D</label>
                <input type="text" />
              </div>
            </div>
            <button className="btn solid" type="submit">
              Save
            </button>
          </form>
        </Card>
      )}

      <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
        {filtered.map((item, i) => (
          <div key={i} className="qrow">
            <div>
              <div>{item.q}</div>
              <div className="meta">
                {item.topic} · {item.diff}
              </div>
            </div>
            <div style={{ display: "flex", gap: 6 }}>
              <button className="btn small">Edit</button>
              <button className="btn small">Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
