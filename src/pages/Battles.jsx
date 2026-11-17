import { useState } from "react";
import Card from "../components/ui/Card";
import StateMessage from "../components/ui/StateMessage";
import SectionPicker from "../components/ui/SectionPicker";
import Modal from "../components/ui/Modal";
import Notice from "../components/ui/Notice";
import SortTh from "../components/ui/SortTh";
import EmptyState from "../components/ui/EmptyState";
import Pill from "../components/ui/Pill";
import { Person } from "../components/ui/Avatar";
import { statusTone } from "../utils/badges";
import { useSort } from "../hooks/useSort";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";
import { formatTime } from "../utils/formatTime";

export default function Battles() {
  const sections = useApi(() => api.getSections());
  const [sectionId, setSectionId] = useState(""); // "" = all sections
  const [message, setMessage] = useState({ type: "", text: "" });
  const [asking, setAsking] = useState(false); // time dialog open
  const [time, setTime] = useState("15:00");
  const [weekend, setWeekend] = useState(false);
  const [busy, setBusy] = useState(false);

  const today = useApi(() => api.getTodaysBattles({ sectionId }), [sectionId]);
  const log = useApi(() => api.getBattleLog({ sectionId }), [sectionId]);

  const section = (sections.data || []).find((s) => String(s.id) === sectionId);

  const todayRows = today.data || [];
  const logRows = log.data || [];
  const todaySort = useSort(todayRows);
  const logSort = useSort(logRows, null, "asc", { score: (b) => b.player1Score + b.player2Score });

  // Step 1: ask what time the battles are for (pre-filled with the section's usual time)
  function askTime() {
    setTime(section?.battleTime || "15:00");
    setWeekend(!!section?.weekendBattles);
    setMessage({ type: "", text: "" });
    setAsking(true);
  }

  // Step 2: generate with the chosen time
  async function handleGenerate(e) {
    e.preventDefault();
    setBusy(true);
    try {
      const res = await api.generateBattles(sectionId, time, weekend);
      setAsking(false);
      setMessage(
        res.alreadyExisted
          ? { type: "", text: "Today's battles were already generated for this section." }
          : { type: "success", text: `Created ${res.created} battle(s) for ${formatTime(time)}. Topic of the day: ${res.topic}.` }
      );
      today.reload();
      sections.reload();
    } catch (err) {
      setAsking(false);
      setMessage({ type: "error", text: err.message });
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="page">
      <div className="toolbar">
        <SectionPicker allowAll sections={sections.data || []} value={sectionId} onChange={setSectionId} />
        <button className="btn coral" onClick={askTime} disabled={!sectionId}>
          ⚔️ Generate Today's Battles
        </button>
        {!sectionId && <span className="muted">Pick a section to generate its battles.</span>}
      </div>
      <Notice message={message} onClose={() => setMessage({ type: "", text: "" })} />

      <Card title="Today's Battles">
        <StateMessage loading={today.loading} error={today.error} />
        {today.data && today.data.length === 0 && <EmptyState icon="⚔️" title="No battles today">Pick a section and click "Generate Today's Battles" to pair your students.</EmptyState>}
        {today.data && today.data.length > 0 && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <SortTh label="Time" k="battleTime" {...todaySort} />
                  <SortTh label="Player 1" k="player1" {...todaySort} />
                  <SortTh label="Player 2" k="player2" {...todaySort} />
                  <SortTh label="Topic" k="topic" {...todaySort} />
                  <SortTh label="Status" k="statusLabel" {...todaySort} />
                </tr>
              </thead>
              <tbody>
                {todaySort.sorted.map((b) => (
                  <tr key={b.id}>
                    <td>🕒 {formatTime(b.battleTime)}</td>
                    <td><Person name={b.player1} /></td>
                    <td><Person name={b.player2} /></td>
                    <td>{b.topic}</td>
                    <td><Pill tone={statusTone(b.statusLabel)}>{b.statusLabel}</Pill></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <Card title="Battle Results Log">
        <StateMessage loading={log.loading} error={log.error} />
        {log.data && log.data.length === 0 && <EmptyState icon="🏁" title="No finished battles yet">Results appear here once students complete their duels.</EmptyState>}
        {log.data && log.data.length > 0 && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <SortTh label="Date" k="date" {...logSort} />
                  <SortTh label="Player 1" k="player1" {...logSort} />
                  <SortTh label="Player 2" k="player2" {...logSort} />
                  <SortTh label="Score" k="score" {...logSort} />
                  <SortTh label="Winner" k="winner" {...logSort} />
                </tr>
              </thead>
              <tbody>
                {logSort.sorted.map((b) => (
                  <tr key={b.id}>
                    <td>{b.date}</td>
                    <td><Person name={b.player1} /></td>
                    <td><Person name={b.player2} /></td>
                    <td><strong>{b.player1Score} – {b.player2Score}</strong></td>
                    <td>{b.winner && b.winner !== "Draw" ? <Pill tone="gold">🏆 {b.winner}</Pill> : <Pill>{b.winner || "—"}</Pill>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {asking && (
        <Modal title="What time is the battle?" onClose={() => setAsking(false)}>
          <form onSubmit={handleGenerate}>
            <div className="field">
              <label>Battle time{section ? ` — ${section.name}` : ""}</label>
              <input type="time" value={time} onChange={(e) => setTime(e.target.value)} required autoFocus />
              <div className="hint">This also becomes the section's daily battle time.</div>
            </div>
            <label className="check" style={{ marginBottom: 4 }}>
              <input type="checkbox" checked={weekend} onChange={(e) => setWeekend(e.target.checked)} />
              <span>Allow weekend battles</span>
            </label>
            <div className="hint">Needed to generate battles on a Saturday or Sunday. Saved to the section's settings.</div>
            <div className="actions">
              <button type="button" className="btn" onClick={() => setAsking(false)}>Cancel</button>
              <button type="submit" className="btn solid" disabled={busy || !time}>
                {busy ? "Generating..." : "Generate Battles"}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
