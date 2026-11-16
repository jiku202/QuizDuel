import { useState } from "react";
import Card from "../components/ui/Card";
import StateMessage from "../components/ui/StateMessage";
import SectionPicker from "../components/ui/SectionPicker";
import KebabMenu from "../components/ui/KebabMenu";
import Pager from "../components/ui/Pager";
import ConfirmDialog from "../components/ui/ConfirmDialog";
import Notice from "../components/ui/Notice";
import SortControl from "../components/ui/SortControl";
import { useSort } from "../hooks/useSort";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";

const PAGE_SIZE = 10;

const EMPTY_Q = { topic: "", text: "", A: "", B: "", C: "", D: "", correctAnswer: "A", difficulty: "Easy" };
const DIFFICULTY_ORDER = { Easy: 1, Medium: 2, Hard: 3 };
const EMPTY_TOPIC = { mode: "new", name: "", existingId: "", copyQuestions: true };

// Returns the rows for the current page plus the (clamped) page number.
function paginate(rows, page) {
  const pages = Math.max(1, Math.ceil(rows.length / PAGE_SIZE));
  const current = Math.min(Math.max(page, 1), pages);
  return { rows: rows.slice((current - 1) * PAGE_SIZE, current * PAGE_SIZE), page: current, pages };
}

export default function QuestionBank() {
  const sections = useApi(() => api.getSections());

  const [sectionId, setSectionId] = useState(""); // "" = all sections
  const [message, setMessage] = useState({ type: "", text: "" });
  const [confirm, setConfirm] = useState(null); // { title, body, label, run }

  // topics
  const [topicPage, setTopicPage] = useState(1);
  const [showTopicForm, setShowTopicForm] = useState(false);
  const [topicForm, setTopicForm] = useState(EMPTY_TOPIC);
  const [addSection, setAddSection] = useState(""); // target section when viewing "All"
  const [renamingId, setRenamingId] = useState(null);
  const [renameValue, setRenameValue] = useState("");
  const [openTopicId, setOpenTopicId] = useState(null); // questions only show for the opened topic

  // questions
  const [questionPage, setQuestionPage] = useState(1);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_Q);
  const [formError, setFormError] = useState("");

  const sectionList = sections.data || [];

  // every topic of every section; the picked section only filters what is shown
  const topics = useApi(() => api.listTopics(""));
  const allTopics = !topics.loading && topics.data ? topics.data : [];
  const topicList = sectionId ? allTopics.filter((t) => String(t.sectionId) === sectionId) : allTopics;
  const openTopic = allTopics.find((t) => t.id === openTopicId);
  const topicSort = useSort(topicList, "sectionName", "asc");

  const questions = useApi(
    () => (openTopic ? api.getQuestions({ sectionId: openTopic.sectionId, topic: openTopic.name }) : Promise.resolve([])),
    [openTopic?.sectionId, openTopic?.name]
  );

  const questionList = !questions.loading && questions.data ? questions.data : [];
  const questionSort = useSort(questionList, null, "asc", { difficulty: (q) => DIFFICULTY_ORDER[q.difficulty] ?? 9 });

  const topicsView = paginate(topicSort.sorted, topicPage);
  const questionsView = paginate(questionSort.sorted, questionPage);

  // where a new topic goes, and which existing topics could be added there
  const targetSection = sectionId || addSection || (sectionList[0] ? String(sectionList[0].id) : "");
  const existingChoices = allTopics.filter(
    (t) =>
      String(t.sectionId) !== targetSection &&
      !allTopics.some((x) => String(x.sectionId) === targetSection && x.name.toLowerCase() === t.name.toLowerCase())
  );

  const notify = (type, text) => setMessage({ type, text });
  const clearMessage = () => setMessage({ type: "", text: "" });
  const update = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  function closeQuestionForm() {
    setShowForm(false);
    setEditingId(null);
    setFormError("");
  }

  function pickSection(id) {
    setSectionId(id);
    setOpenTopicId(null);
    setTopicPage(1);
    setShowTopicForm(false);
    setRenamingId(null);
    closeQuestionForm();
    clearMessage();
  }

  function toggleTopic(t) {
    closeQuestionForm();
    setQuestionPage(1);
    setOpenTopicId(t.id === openTopicId ? null : t.id);
  }

  // ---------- topics: create / add existing / rename / delete ----------
  function toggleTopicForm() {
    setShowTopicForm(!showTopicForm);
    setTopicForm(EMPTY_TOPIC);
    clearMessage();
  }

  async function handleAddTopic(e) {
    e.preventDefault();
    try {
      let t;
      if (topicForm.mode === "existing") {
        t = await api.copyTopic(topicForm.existingId, {
          sectionId: Number(targetSection),
          copyQuestions: topicForm.copyQuestions,
        });
      } else {
        t = await api.createTopic({ sectionId: Number(targetSection), name: topicForm.name });
      }
      setShowTopicForm(false);
      setTopicForm(EMPTY_TOPIC);
      notify("success", `Topic "${t.name}" added to ${t.sectionName}.`);
      topics.reload();
      setTopicPage(1);
    } catch (err) {
      notify("error", err.message);
    }
  }

  async function handleRename(e, t) {
    e.preventDefault();
    try {
      await api.renameTopic(t.id, renameValue);
      setRenamingId(null);
      notify("success", "Topic renamed.");
      topics.reload();
    } catch (err) {
      notify("error", err.message);
    }
  }

  function askDeleteTopic(t) {
    setConfirm({
      title: "Delete topic?",
      label: "Delete",
      body: t.questionCount
        ? `"${t.name}" in ${t.sectionName} and its ${t.questionCount} question${t.questionCount > 1 ? "s" : ""} will be permanently deleted.`
        : `"${t.name}" in ${t.sectionName} will be permanently deleted.`,
      run: async () => {
        await api.deleteTopic(t.id);
        if (openTopicId === t.id) setOpenTopicId(null);
        notify("success", `Topic "${t.name}" deleted.`);
        topics.reload();
      },
    });
  }

  // ---------- questions: create / edit (move) / delete ----------
  function openNew() {
    setEditingId(null);
    setForm({ ...EMPTY_Q, topic: openTopic.name });
    setFormError("");
    setShowForm(true);
  }

  function openEdit(q) {
    setEditingId(q.id);
    setForm({
      topic: q.topic,
      text: q.text,
      ...q.choices,
      correctAnswer: q.correctAnswer,
      difficulty: q.difficulty,
    });
    setFormError("");
    setShowForm(true);
  }

  async function handleSaveQuestion(e) {
    e.preventDefault();
    setFormError("");
    const body = {
      topic: form.topic, // new questions always go into the opened topic; edits may move it
      text: form.text,
      choices: { A: form.A, B: form.B, C: form.C, D: form.D },
      correctAnswer: form.correctAnswer,
      difficulty: form.difficulty,
    };
    try {
      if (editingId) {
        await api.updateQuestion(editingId, body);
        if (form.topic !== openTopic.name) notify("success", `Question moved to "${form.topic}".`);
      } else {
        await api.createQuestion({ ...body, sectionId: openTopic.sectionId });
      }
      closeQuestionForm();
      questions.reload();
      topics.reload();
    } catch (err) {
      setFormError(err.message);
    }
  }

  function askDeleteQuestion(q) {
    setConfirm({
      title: "Delete question?",
      label: "Delete",
      body: `"${q.text}" will be permanently deleted.`,
      run: async () => {
        await api.deleteQuestion(q.id);
        questions.reload();
        topics.reload();
      },
    });
  }

  async function runConfirm() {
    const { run } = confirm;
    setConfirm(null);
    try {
      await run();
    } catch (err) {
      notify("error", err.message);
    }
  }

  const sameSectionTopics = openTopic ? allTopics.filter((x) => x.sectionId === openTopic.sectionId) : [];

  return (
    <div className="page">
      <div className="section-head">
        <h3>Question Bank</h3>
        {sectionList.length > 0 && (
          <button className={"btn " + (showTopicForm ? "" : "solid")} onClick={toggleTopicForm}>
            {showTopicForm ? "Cancel" : "+ Add Topic"}
          </button>
        )}
      </div>

      <StateMessage loading={sections.loading && !sections.data} error={sections.error} />
      {sections.data && sectionList.length === 0 && (
        <div className="notice">Create a section first (Sections page), then add topics and questions to it.</div>
      )}
      <Notice message={message} onClose={clearMessage} />

      {sectionList.length > 0 && (
        <div className="toolbar">
          <label style={{ margin: 0 }}>Section</label>
          <SectionPicker allowAll sections={sectionList} value={sectionId} onChange={pickSection} />
          {topicList.length > 1 && (
            <SortControl
              label="Sort topics"
              options={[
                { key: "sectionName", label: "Section" },
                { key: "name", label: "Name" },
                { key: "questionCount", label: "Number of questions" },
              ]}
              sort={topicSort.sort}
              onChange={(s) => { topicSort.setSort(s); setTopicPage(1); }}
            />
          )}
        </div>
      )}

      {showTopicForm && (
        <Card title="Add Topic">
          <form onSubmit={handleAddTopic}>
            <div className="radio-row">
              <label className="check">
                <input type="radio" name="topic-mode" checked={topicForm.mode === "new"}
                  onChange={() => setTopicForm({ ...topicForm, mode: "new" })} />
                <span>New topic</span>
              </label>
              <label className="check">
                <input type="radio" name="topic-mode" checked={topicForm.mode === "existing"}
                  onChange={() => setTopicForm({ ...topicForm, mode: "existing" })} />
                <span>Existing topic</span>
              </label>
            </div>

            {!sectionId && (
              <div className="field">
                <label>Add to section</label>
                <SectionPicker sections={sectionList} value={targetSection} onChange={setAddSection} />
              </div>
            )}

            {topicForm.mode === "new" ? (
              <div className="field">
                <label>Topic name</label>
                <input type="text" placeholder="e.g. Filipino Heroes" value={topicForm.name}
                  onChange={(e) => setTopicForm({ ...topicForm, name: e.target.value })} required autoFocus />
              </div>
            ) : existingChoices.length === 0 ? (
              <div className="muted" style={{ marginBottom: 12 }}>
                No other topics to add. Topics from your other sections show up here.
              </div>
            ) : (
              <>
                <div className="field">
                  <label>Topic from another section</label>
                  <select value={topicForm.existingId}
                    onChange={(e) => setTopicForm({ ...topicForm, existingId: e.target.value })} required>
                    <option value="">Select a topic...</option>
                    {existingChoices.map((t) => (
                      <option key={t.id} value={t.id}>{t.name} — {t.sectionName} ({t.questionCount})</option>
                    ))}
                  </select>
                </div>
                <label className="check" style={{ marginBottom: 12 }}>
                  <input type="checkbox" checked={topicForm.copyQuestions}
                    onChange={(e) => setTopicForm({ ...topicForm, copyQuestions: e.target.checked })} />
                  <span>Also copy its questions</span>
                </label>
              </>
            )}

            <button className="btn solid" type="submit" disabled={topicForm.mode === "existing" && existingChoices.length === 0}>
              Add Topic
            </button>
          </form>
        </Card>
      )}

      <StateMessage loading={topics.loading && !topics.data} error={topics.error} />
      {!topics.loading && topics.data && topicList.length === 0 && (
        <div className="muted">
          {sectionId ? "No topics in this section yet." : "No topics yet."} Click "+ Add Topic" to create one.
        </div>
      )}

      <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
        {topicsView.rows.map((t) => {
          const isOpen = t.id === openTopicId;
          return (
            <div key={t.id} className={"topic-item" + (isOpen ? " open" : "")}>
              {renamingId === t.id ? (
                <form className="inline-form" style={{ padding: 10 }} onSubmit={(e) => handleRename(e, t)}>
                  <input type="text" value={renameValue} onChange={(e) => setRenameValue(e.target.value)} required autoFocus />
                  <button className="btn solid small" type="submit">Save</button>
                  <button className="btn small" type="button" onClick={() => setRenamingId(null)}>Cancel</button>
                </form>
              ) : (
                <div className="topic-row" onClick={() => toggleTopic(t)}>
                  <span className="t-arrow">{isOpen ? "▾" : "▸"}</span>
                  <span className="t-name">{t.name}</span>
                  {!sectionId && <span className="t-sec">{t.sectionName}</span>}
                  <span className="t-count">{t.questionCount} question{t.questionCount === 1 ? "" : "s"}</span>
                  <KebabMenu
                    items={[
                      { label: "Rename", onClick: () => { setRenamingId(t.id); setRenameValue(t.name); } },
                      { label: "Delete", danger: true, onClick: () => askDeleteTopic(t) },
                    ]}
                  />
                </div>
              )}

              {/* Questions: only visible once the topic row is clicked */}
              {isOpen && renamingId !== t.id && (
                <div className="topic-body">
                  <div className="body-head">
                    <strong style={{ fontSize: 13 }}>Questions in {t.name} · {t.sectionName}</strong>
                    {!showForm && (
                      <button className="btn solid small" onClick={openNew}>+ Add Question</button>
                    )}
                  </div>
                  {questionList.length > 1 && (
                    <SortControl
                      label="Sort questions"
                      options={[
                        { key: "text", label: "Question" },
                        { key: "difficulty", label: "Difficulty" },
                        { key: "correctAnswer", label: "Correct answer" },
                      ]}
                      sort={questionSort.sort}
                      onChange={(s) => { questionSort.setSort(s); setQuestionPage(1); }}
                    />
                  )}

                  {showForm && (
                    <Card title={editingId ? "Edit Question" : `New Question — ${t.name}`}>
                      {formError && <div className="notice error" style={{ marginBottom: 10 }}>{formError}</div>}
                      <form onSubmit={handleSaveQuestion}>
                        {editingId && (
                          <div className="field">
                            <label>Topic (change to move this question)</label>
                            <select value={form.topic} onChange={update("topic")}>
                              {sameSectionTopics.map((x) => <option key={x.id} value={x.name}>{x.name}</option>)}
                            </select>
                          </div>
                        )}
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
                          <button className="btn" type="button" onClick={closeQuestionForm}>Cancel</button>
                        </div>
                      </form>
                    </Card>
                  )}

                  <StateMessage loading={questions.loading} error={questions.error} />
                  {!questions.loading && questions.data && questionList.length === 0 && (
                    <div className="muted">No questions in this topic yet.</div>
                  )}

                  {questionsView.rows.map((q) => (
                    <div key={q.id} className="qrow">
                      <div>
                        <div>{q.text}</div>
                        <div className="meta">{q.difficulty} · Correct: {q.correctAnswer}</div>
                      </div>
                      <div style={{ display: "flex", gap: 6 }}>
                        <button className="btn small" onClick={() => openEdit(q)}>Edit</button>
                        <button className="btn small" onClick={() => askDeleteQuestion(q)}>Delete</button>
                      </div>
                    </div>
                  ))}
                  <Pager page={questionsView.page} pages={questionsView.pages} onChange={setQuestionPage} />
                </div>
              )}
            </div>
          );
        })}
      </div>

      <Pager page={topicsView.page} pages={topicsView.pages} onChange={setTopicPage} />

      {confirm && (
        <ConfirmDialog title={confirm.title} confirmLabel={confirm.label} onConfirm={runConfirm} onCancel={() => setConfirm(null)}>
          {confirm.body}
        </ConfirmDialog>
      )}
    </div>
  );
}
