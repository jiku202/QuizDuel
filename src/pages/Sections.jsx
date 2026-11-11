import { useState } from "react";
import Card from "../components/ui/Card";
import StateMessage from "../components/ui/StateMessage";
import SectionPicker from "../components/ui/SectionPicker";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";

export default function Sections() {
  const sections = useApi(() => api.getSections());
  const badges = useApi(() => api.getBadges());

  const [form, setForm] = useState({ name: "", gradeLevel: "Grade 3", subject: "Social Studies" });
  const [created, setCreated] = useState(null);
  const [message, setMessage] = useState({ type: "", text: "" });
  const [picked, setPicked] = useState("");
  const [badgeId, setBadgeId] = useState("");

  const list = sections.data || [];
  const selectedId = picked || (list[0] ? String(list[0].id) : "");
  const selected = list.find((s) => String(s.id) === selectedId);

  const roster = useApi(
    () => (selectedId ? api.getRoster(selectedId) : Promise.resolve([])),
    [selectedId]
  );

  const update = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  async function handleCreate(e) {
    e.preventDefault();
    setMessage({ type: "", text: "" });
    try {
      const section = await api.createSection(form);
      setCreated(section);
      setForm({ ...form, name: "" });
      setPicked(String(section.id));
      sections.reload();
    } catch (err) {
      setMessage({ type: "error", text: err.message });
    }
  }

  async function handleAward(student) {
    const badge = badgeId || (badges.data?.[0] && String(badges.data[0].id));
    if (!badge) return;
    try {
      const res = await api.awardBadge(selectedId, student.id, badge);
      setMessage({
        type: "success",
        text: res.awarded
          ? `Awarded "${res.badge.name}" to ${student.name}.`
          : `${student.name} already has "${res.badge.name}".`,
      });
      roster.reload();
    } catch (err) {
      setMessage({ type: "error", text: err.message });
    }
  }

  return (
    <div className="page">
      {message.text && <div className={`notice ${message.type}`}>{message.text}</div>}

      <Card title="Create New Section">
        <form onSubmit={handleCreate}>
          <div className="row">
            <div className="col field">
              <label>Section Name</label>
              <input type="text" placeholder="Grade 3 - Mabini" value={form.name} onChange={update("name")} required />
            </div>
            <div className="col field">
              <label>Grade Level</label>
              <input type="text" placeholder="Grade 3" value={form.gradeLevel} onChange={update("gradeLevel")} />
            </div>
          </div>
          <div className="field">
            <label>Subject</label>
            <input type="text" placeholder="Social Studies" value={form.subject} onChange={update("subject")} />
          </div>
          <button className="btn solid" type="submit">
            Generate Join Code
          </button>
        </form>
        {created && (
          <div className="notice success" style={{ marginTop: 12 }}>
            <strong>{created.name}</strong> created. Join code: <strong>{created.joinCode}</strong> (share this with your students)
          </div>
        )}
      </Card>

      <Card title={selected ? `Class Roster — ${selected.name}` : "Class Roster"}>
        <StateMessage loading={sections.loading} error={sections.error} />
        {list.length === 0 && !sections.loading && <div className="muted">Create a section to get started.</div>}

        {list.length > 0 && (
          <>
            <div className="toolbar" style={{ marginBottom: 12 }}>
              <SectionPicker sections={list} value={selectedId} onChange={setPicked} />
              {selected && <span className="muted">Join code: <strong>{selected.joinCode}</strong></span>}
              <select value={badgeId} onChange={(e) => setBadgeId(e.target.value)} title="Badge to award">
                {(badges.data || []).map((b) => (
                  <option key={b.id} value={b.id}>{b.name}</option>
                ))}
              </select>
            </div>

            <StateMessage loading={roster.loading} error={roster.error} />
            {roster.data && roster.data.length === 0 && (
              <div className="muted">No students yet. Share the join code with your class.</div>
            )}
            {roster.data && roster.data.length > 0 && (
              <table>
                <thead>
                  <tr>
                    <th>Student</th>
                    <th>Points</th>
                    <th>Badges</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {roster.data.map((s) => (
                    <tr key={s.id}>
                      <td>{s.name}</td>
                      <td>{s.points}</td>
                      <td>{s.badges}</td>
                      <td>
                        <button className="btn small" onClick={() => handleAward(s)}>
                          Award Badge
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </>
        )}
      </Card>
    </div>
  );
}
