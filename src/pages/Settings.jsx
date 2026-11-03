import { useState } from "react";
import Card from "../components/ui/Card";
import StateMessage from "../components/ui/StateMessage";
import SectionPicker from "../components/ui/SectionPicker";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";

export default function Settings() {
  const sections = useApi(() => api.getSections());
  const [picked, setPicked] = useState("");
  const [edits, setEdits] = useState({}); // unsaved changes per field
  const [message, setMessage] = useState({ type: "", text: "" });

  const list = sections.data || [];
  const selectedId = picked || (list[0] ? String(list[0].id) : "");
  const section = list.find((s) => String(s.id) === selectedId);

  const battleTime = edits.battleTime ?? section?.battleTime ?? "15:00";
  const weekendBattles = edits.weekendBattles ?? section?.weekendBattles ?? false;

  function pickSection(id) {
    setPicked(id);
    setEdits({});
    setMessage({ type: "", text: "" });
  }

  async function handleSave(e) {
    e.preventDefault();
    setMessage({ type: "", text: "" });
    try {
      await api.updateSection(selectedId, { battleTime, weekendBattles });
      setEdits({});
      setMessage({ type: "success", text: "Settings saved." });
      sections.reload();
    } catch (err) {
      setMessage({ type: "error", text: err.message });
    }
  }

  return (
    <div className="page">
      <Card title="Battle Settings">
        <StateMessage loading={sections.loading} error={sections.error} />
        {sections.data && list.length === 0 && <div className="muted">Create a section first.</div>}
        {section && (
          <form onSubmit={handleSave} style={{ maxWidth: 360 }}>
            <div className="field">
              <label>Section</label>
              <SectionPicker sections={list} value={selectedId} onChange={pickSection} />
            </div>
            <div className="field">
              <label>Daily Battle Time</label>
              <input type="time" value={battleTime} onChange={(e) => setEdits({ ...edits, battleTime: e.target.value })} required />
            </div>
            <div className="field" style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <input
                type="checkbox"
                style={{ width: "auto" }}
                id="wk"
                checked={weekendBattles}
                onChange={(e) => setEdits({ ...edits, weekendBattles: e.target.checked })}
              />
              <label htmlFor="wk" style={{ margin: 0, textTransform: "none", fontSize: 13 }}>
                Allow weekend battles
              </label>
            </div>
            {message.text && <div className={`notice ${message.type}`} style={{ marginBottom: 12 }}>{message.text}</div>}
            <button className="btn solid" type="submit">Save Settings</button>
          </form>
        )}
      </Card>
    </div>
  );
}
