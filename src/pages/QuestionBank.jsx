import { useState } from "react";
import Card from "../components/ui/Card";
import Chip from "../components/ui/Chip";
import StateMessage from "../components/ui/StateMessage";
import SectionPicker from "../components/ui/SectionPicker";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";

const EMPTY = {
  sectionId: "",
  topic: "",
  text: "",
  A: "",
  B: "",
  C: "",
  D: "",
  correctAnswer: "A",
  difficulty: "Easy",
};

export default function QuestionBank() {
  const sections = useApi(() => api.getSections());
  const questions = useApi(() => api.getQuestions());

  const [activeTopic, setActiveTopic] = useState("All");
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState("");

  const sectionList = sections.data || [];
  const all = questions.data || [];
  const topics = [...new Set(all.map((q) => q.topic))].sort();
  const shown = activeTopic === "All" ? all : all.filter((q) => q.topic === activeTopic);
  const update = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  function openNew() {
    setEditingId(null);
    setForm({ ...EMPTY, sectionId: sectionList[0] ? String(sectionList[0].id) : "" });
    setError("");
    setShowForm(true);
  }

  function openEdit(q) {
    setEditingId(q.id);
    setForm({
      sectionId: String(q.sectionId),
      topic: q.topic,
      text: q.text,
      ...q.choices,
      correctAnswer: q.correctAnswer,
      difficulty: q.difficulty,
    });
    setError("");
    setShowForm(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    setError("");
    const body = {
      topic: form.topic,
      text: form.text,
      choices: { A: form.A, B: form.B, C: form.C, D: form.D },
      correctAnswer: form.correctAnswer,
      difficulty: form.difficulty,
    };
    try {
      if (editingId) await api.updateQuestion(editingId, body);
      else await api.createQuestion({ ...body, sectionId: Number(form.sectionId) });
      setShowForm(false);
      questions.reload();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete(q) {
    if (!window.confirm("Delete this question?")) return;
    try {
      await api.deleteQuestion(q.id);
      questions.reload();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="page">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <h3>Question Bank</h3>
        <button className="btn solid" onClick={openNew} disabled={sectionList.length === 0}>
          + Add Question
        </button>
      </div>

      {sections.data && sectionList.length === 0 && (
        <div className="notice">Create a section first (Sections page), then add questions to it.</div>
      )}
      {error && !showForm && <div className="notice error">{error}</div>}

      <div className="row" style={{ flexWrap: "wrap", gap: 8 }}>
        <Chip active={activeTopic === "All"} onClick={() => setActiveTopic("All")}>All</Chip>
        {topics.map((t) => (
          <Chip key={t} active={activeTopic === t} onClick={() => setActiveTopic(t)}>{t}</Chip>
        ))}
      </div>

      {showForm && (
        <Card title={editingId ? "Edit Question" : "New Question"}>
          {error && <div className="notice error" style={{ marginBottom: 10 }}>{error}</div>}
          <form onSubmit={handleSave}>
            <div className="row">
              {!editingId && (
                <div className="col field">
                  <label>Section</label>
                  <SectionPicker sections={sectionList} value={form.sectionId} onChange={(v) => setForm({ ...form, sectionId: v })} />
                </div>
              )}
              <div className="col field">
                <label>Topic</label>
                <input type="text" list="topic-options" placeholder="e.g. Filipino Heroes" value={form.topic} onChange={update("topic")} required />
                <datalist id="topic-options">
                  {topics.map((t) => <option key={t} value={t} />)}
                </datalist>
              </div>
            </div>
            <div className="field">
              <label>Question</label>
              <textarea rows="2" placeholder="Enter the question..." value={form.text} onChange={update("text")} required></textarea>
            </div>
            <div className="row">
              <div className="col field"><label>Answer A</label><input type="text" value={form.A} onChange={update("A")} required /></div>
              <div className="col field"><label>Answer B</label><input type="text" value={form.B} onChange={update("B")} required /></div>
            </div>
            <div className="row">
              <div className="col field"><label>Answer C</label><input type="text" value={form.C} onChange={update("C")} required /></div>
              <div className="col field"><label>Answer D</label><input type="text" value={form.D} onChange={update("D")} required /></div>
            </div>
            <div className="row">
              <div className="col field">
                <label>Correct Answer</label>
                <select value={form.correctAnswer} onChange={update("correctAnswer")}>
                  {["A", "B", "C", "D"].map((l) => <option key={l}>{l}</option>)}
                </select>
              </div>
              <div className="col field">
                <label>Difficulty</label>
                <select value={form.difficulty} onChange={update("difficulty")}>
                  {["Easy", "Medium", "Hard"].map((d) => <option key={d}>{d}</option>)}
                </select>
              </div>
            </div>
            <div className="toolbar">
              <button className="btn solid" type="submit">Save</button>
              <button className="btn" type="button" onClick={() => setShowForm(false)}>Cancel</button>
            </div>
          </form>
        </Card>
      )}

      <StateMessage loading={questions.loading} error={questions.error} />
      {questions.data && shown.length === 0 && <div className="muted">No questions yet.</div>}

      <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
        {shown.map((q) => (
          <div key={q.id} className="qrow">
            <div>
              <div>{q.text}</div>
              <div className="meta">{q.topic} · {q.difficulty}</div>
            </div>
            <div style={{ display: "flex", gap: 6 }}>
              <button className="btn small" onClick={() => openEdit(q)}>Edit</button>
              <button className="btn small" onClick={() => handleDelete(q)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
