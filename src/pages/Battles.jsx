import { useState } from "react";
import Card from "../components/ui/Card";
import StateMessage from "../components/ui/StateMessage";
import SectionPicker from "../components/ui/SectionPicker";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";

export default function Battles() {
  const sections = useApi(() => api.getSections());
  const [sectionId, setSectionId] = useState(""); // "" = all sections
  const [message, setMessage] = useState({ type: "", text: "" });

  const today = useApi(() => api.getTodaysBattles({ sectionId }), [sectionId]);
  const log = useApi(() => api.getBattleLog({ sectionId }), [sectionId]);

  async function handleGenerate() {
    setMessage({ type: "", text: "" });
    try {
      const res = await api.generateBattles(sectionId);
      setMessage(
        res.alreadyExisted
          ? { type: "", text: "Today's battles were already generated for this section." }
          : { type: "success", text: `Created ${res.created} battle(s). Topic of the day: ${res.topic}.` }
      );
      today.reload();
    } catch (err) {
      setMessage({ type: "error", text: err.message });
    }
  }

  return (
    <div className="page">
      <div className="toolbar">
        <SectionPicker allowAll sections={sections.data || []} value={sectionId} onChange={setSectionId} />
        <button className="btn solid" onClick={handleGenerate} disabled={!sectionId}>
          Generate Today's Battles
        </button>
        {!sectionId && <span className="muted">Pick a section to generate its battles.</span>}
      </div>
      {message.text && <div className={`notice ${message.type}`}>{message.text}</div>}

      <Card title="Today's Battles">
        <StateMessage loading={today.loading} error={today.error} />
        {today.data && today.data.length === 0 && <div className="muted">No battles today yet.</div>}
        {today.data && today.data.length > 0 && (
          <table>
            <thead>
              <tr><th>Player 1</th><th>Player 2</th><th>Topic</th><th>Status</th></tr>
            </thead>
            <tbody>
              {today.data.map((b) => (
                <tr key={b.id}>
                  <td>{b.player1}</td>
                  <td>{b.player2}</td>
                  <td>{b.topic}</td>
                  <td>{b.statusLabel}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <Card title="Battle Results Log">
        <StateMessage loading={log.loading} error={log.error} />
        {log.data && log.data.length === 0 && <div className="muted">No completed battles yet.</div>}
        {log.data && log.data.length > 0 && (
          <table>
            <thead>
              <tr><th>Date</th><th>Player 1</th><th>Player 2</th><th>Score</th><th>Winner</th></tr>
            </thead>
            <tbody>
              {log.data.map((b) => (
                <tr key={b.id}>
                  <td>{b.date}</td>
                  <td>{b.player1}</td>
                  <td>{b.player2}</td>
                  <td>{b.player1Score} – {b.player2Score}</td>
                  <td>{b.winner}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  );
}
