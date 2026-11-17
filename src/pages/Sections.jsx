import { useEffect, useRef, useState } from "react";
import Card from "../components/ui/Card";
import StateMessage from "../components/ui/StateMessage";
import Modal from "../components/ui/Modal";
import ConfirmDialog from "../components/ui/ConfirmDialog";
import Notice from "../components/ui/Notice";
import SortTh from "../components/ui/SortTh";
import SortControl, { BY_NEWEST, alpha } from "../components/ui/SortControl";
import EmptyState from "../components/ui/EmptyState";
import { Person } from "../components/ui/Avatar";
import { RankPill } from "../components/ui/Pill";
import { useSort } from "../hooks/useSort";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";

const CARD_SORTS = [
  ...BY_NEWEST,
  ...alpha("name"),
  { label: "Most students", key: "studentCount", dir: "desc" },
  { label: "Fewest students", key: "studentCount", dir: "asc" },
];

// "Grade 3" -> "G3"
const shortGrade = (grade = "") =>
  grade.split(/\s+/).filter(Boolean).map((w) => w[0]).join("").toUpperCase().slice(0, 3) || "📘";

const EMPTY_FORM = { name: "", gradeLevel: "Grade 3", subject: "Social Studies", members: "" };

// "a@x.com, b@x.com\nc@x.com" -> ["a@x.com", "b@x.com", "c@x.com"]
const parseEmails = (text) =>
  text
    .split(/[\s,;]+/)
    .map((e) => e.trim())
    .filter(Boolean);

// Summarises the result of an "add members" call.
function describeAdd(res) {
  const parts = [];
  if (res.added.length) parts.push(`Added ${res.added.length} student${res.added.length > 1 ? "s" : ""}.`);
  if (res.alreadyIn.length) parts.push(`Already in section: ${res.alreadyIn.join(", ")}.`);
  if (res.notFound.length) parts.push(`No student account for: ${res.notFound.join(", ")}.`);
  return parts.join(" ") || "Nothing to add.";
}

// The 3-dot menu on each section card (like MS Teams).
function SectionMenu({ onRename, onView, onDelete }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    if (!open) return;
    const close = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [open]);

  const pick = (fn) => (e) => {
    e.stopPropagation();
    setOpen(false);
    fn();
  };

  return (
    <div className="kebab" ref={ref} onClick={(e) => e.stopPropagation()}>
      <button
        type="button"
        className={"kebab-btn" + (open ? " open" : "")}
        aria-label="Section options"
        onClick={() => setOpen(!open)}
      >
        ⋮
      </button>
      {open && (
        <div className="menu">
          <button type="button" onClick={pick(onRename)}>Rename</button>
          <button type="button" onClick={pick(onView)}>View members / students</button>
          <button type="button" className="danger" onClick={pick(onDelete)}>Delete</button>
        </div>
      )}
    </div>
  );
}

export default function Sections() {
  const sections = useApi(() => api.getSections());

  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [message, setMessage] = useState({ type: "", text: "" });
  const [selectedId, setSelectedId] = useState(null); // roster only shows once a card is picked
  const [showCode, setShowCode] = useState(false); // join code stays hidden until asked for
  const [newMembers, setNewMembers] = useState("");
  const [renaming, setRenaming] = useState(null); // { id, name }
  const [deleting, setDeleting] = useState(null); // section being deleted
  const [removing, setRemoving] = useState(null); // student about to be removed

  const list = sections.data || [];
  const selected = list.find((s) => s.id === selectedId);

  const roster = useApi(
    () => (selectedId ? api.getRoster(selectedId) : Promise.resolve([])),
    [selectedId]
  );

  const cardSort = useSort(list, "id", "desc", { studentCount: (c) => c.studentCount ?? 0 });
  const rosterSort = useSort(roster.data || [], null, "asc", { rank: (r) => r.points });

  const update = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  function selectSection(id) {
    setShowCode(false);
    setSelectedId(id);
  }
  const notify = (type, text) => setMessage({ type, text });
  const clearMessage = () => setMessage({ type: "", text: "" });

  // ---------- CREATE ----------
  async function handleCreate(e) {
    e.preventDefault();
    notify("", "");
    try {
      const { members, ...rest } = form;
      const memberEmails = parseEmails(members);
      const section = await api.createSection({ ...rest, memberEmails });
      setForm(EMPTY_FORM);
      setShowCreate(false);
      selectSection(section.id);
      sections.reload();
      notify("success", `"${section.name}" created. Click "Show code" in its roster to get the join code for your students.`);
    } catch (err) {
      notify("error", err.message);
    }
  }

  // ---------- UPDATE (rename) ----------
  async function handleRename(e) {
    e.preventDefault();
    const name = renaming.name.trim();
    if (!name) return;
    try {
      await api.updateSection(renaming.id, { name });
      setRenaming(null);
      notify("success", "Section renamed.");
      sections.reload();
    } catch (err) {
      notify("error", err.message);
    }
  }

  // ---------- DELETE ----------
  async function handleDelete() {
    try {
      await api.deleteSection(deleting.id);
      if (selectedId === deleting.id) selectSection(null);
      notify("success", `"${deleting.name}" was deleted.`);
      setDeleting(null);
      sections.reload();
    } catch (err) {
      setDeleting(null);
      notify("error", err.message);
    }
  }

  // ---------- members: add / remove / award ----------
  async function handleAddMembers(e) {
    e.preventDefault();
    const emails = parseEmails(newMembers);
    if (!emails.length) return;
    try {
      const res = await api.addMembers(selectedId, emails);
      notify(res.added.length ? "success" : "error", describeAdd(res));
      if (res.added.length) setNewMembers("");
      roster.reload();
      sections.reload();
    } catch (err) {
      notify("error", err.message);
    }
  }

  async function handleRemove() {
    const student = removing;
    setRemoving(null);
    try {
      await api.removeMember(selectedId, student.id);
      notify("success", `${student.name} was removed from ${selected.name}.`);
      roster.reload();
      sections.reload();
    } catch (err) {
      notify("error", err.message);
    }
  }

  return (
    <div className="page">
      <Notice message={message} onClose={clearMessage} />

      <div className="section-head">
        <h3>Your Sections</h3>
        <button
          className={"btn " + (showCreate ? "" : "solid")}
          onClick={() => {
            setShowCreate(!showCreate);
            notify("", "");
          }}
        >
          {showCreate ? "Cancel" : "＋ Create Section"}
        </button>
      </div>

      {/* Create New Section: only visible after clicking "Create Section" */}
      {showCreate && (
        <Card title="Create New Section">
          <form onSubmit={handleCreate}>
            <div className="row">
              <div className="col field">
                <label>Section Name</label>
                <input type="text" placeholder="Grade 3 - Mabini" value={form.name} onChange={update("name")} required autoFocus />
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
            <div className="field">
              <label>Members (optional)</label>
              <textarea
                placeholder="student1@school.edu.ph, student2@school.edu.ph"
                value={form.members}
                onChange={update("members")}
              />
              <div className="hint">Student emails, separated by commas or new lines. You can also add members later.</div>
            </div>
            <button className="btn solid" type="submit">Create &amp; Generate Join Code</button>
          </form>
        </Card>
      )}

      {/* Section cards */}
      <StateMessage loading={sections.loading && !sections.data} error={sections.error} />
      {!sections.loading && !sections.error && list.length === 0 && (
        <EmptyState icon="🏫" title="No sections yet">Click "+ Create Section" to set up your first class. You can add students right away or share the join code.</EmptyState>
      )}
      {list.length > 1 && (
        <SortControl
          options={CARD_SORTS}
          sort={cardSort.sort}
          onChange={cardSort.setSort}
        />
      )}
      {list.length > 0 && (
        <div className="section-grid">
          {cardSort.sorted.map((s) => (
            <div
              key={s.id}
              className={"section-card tone-" + (s.id % 6) + (s.id === selectedId ? " selected" : "")}
              onClick={() => selectSection(s.id === selectedId ? null : s.id)}
            >
              <SectionMenu
                onRename={() => setRenaming({ id: s.id, name: s.name })}
                onView={() => selectSection(s.id)}
                onDelete={() => setDeleting(s)}
              />
              <div className="sc-icon">{shortGrade(s.gradeLevel)}</div>
              <div className="sc-name">{s.name}</div>
              <div className="sc-sub">{s.gradeLevel} · {s.subject}</div>
              <div className="sc-foot">
                <span className="pill teal">👥 {s.studentCount ?? 0} student{s.studentCount === 1 ? "" : "s"}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Class Roster: only visible after clicking a section card */}
      {selected && (
        <Card title={`Class Roster — ${selected.name}`}>
          <div className="toolbar" style={{ marginBottom: 12 }}>
            <span className="code-box">
              🔑 Join code: <strong>{showCode ? selected.joinCode : "••••••"}</strong>
            </span>
            <button className="btn small" onClick={() => setShowCode(!showCode)}>
              {showCode ? "Hide code" : "Show code"}
            </button>
            <button className="btn small" onClick={() => selectSection(null)} style={{ marginLeft: "auto" }}>
              Close
            </button>
          </div>
          <div className="hint" style={{ marginBottom: 12 }}>
            Points and ranks are for this section only. Ranks go up and down automatically with the points.
          </div>

          <form className="add-members" onSubmit={handleAddMembers}>
            <textarea
              placeholder="Add members by email: student1@school.edu.ph, student2@school.edu.ph"
              value={newMembers}
              onChange={(e) => setNewMembers(e.target.value)}
            />
            <button className="btn solid" type="submit">Add Members</button>
          </form>

          <StateMessage loading={roster.loading && !roster.data} error={roster.error} />
          {roster.data && roster.data.length === 0 && (
            <EmptyState icon="🎓" title="No students yet">Add them by email above, or show the join code to your class so they can join themselves.</EmptyState>
          )}
          {roster.data && roster.data.length > 0 && (
            <div className="table-wrap"><table>
              <thead>
                <tr>
                  <SortTh label="Student" k="name" {...rosterSort} />
                  <SortTh label="Email" k="email" {...rosterSort} />
                  <SortTh label="Points" k="points" {...rosterSort} />
                  <SortTh label="Rank" k="rank" {...rosterSort} />
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {rosterSort.sorted.map((s) => (
                  <tr key={s.id}>
                    <td><Person name={s.name} /></td>
                    <td>{s.email}</td>
                    <td><strong>{s.points}</strong> pts</td>
                    <td><RankPill name={s.rank} /></td>
                    <td style={{ whiteSpace: "nowrap", textAlign: "right" }}>
                      <button className="btn small" onClick={() => setRemoving(s)}>Remove</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table></div>
          )}
        </Card>
      )}

      {/* Rename dialog */}
      {renaming && (
        <Modal title="Rename section" onClose={() => setRenaming(null)}>
          <form onSubmit={handleRename}>
            <div className="field">
              <label>Section Name</label>
              <input
                type="text"
                value={renaming.name}
                onChange={(e) => setRenaming({ ...renaming, name: e.target.value })}
                required
                autoFocus
              />
            </div>
            <div className="actions">
              <button type="button" className="btn" onClick={() => setRenaming(null)}>Cancel</button>
              <button type="submit" className="btn solid">Save</button>
            </div>
          </form>
        </Modal>
      )}

      {/* Delete confirmations */}
      {deleting && (
        <ConfirmDialog title="Delete section?" onConfirm={handleDelete} onCancel={() => setDeleting(null)}>
          <strong>{deleting.name}</strong> will be permanently deleted, including its student roster,
          topics, questions and battles. This can't be undone.
        </ConfirmDialog>
      )}
      {removing && (
        <ConfirmDialog title="Remove student?" confirmLabel="Remove" onConfirm={handleRemove} onCancel={() => setRemoving(null)}>
          Remove <strong>{removing.name}</strong> from <strong>{selected?.name}</strong>? Their points in this section are lost.
        </ConfirmDialog>
      )}
    </div>
  );
}
