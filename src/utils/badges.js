// Look of ranks, battle statuses and difficulties (pill colours + icons).
const RANKS = {
  "Newcomer": { icon: "🌱", tone: "" },
  "Rookie Scholar": { icon: "📘", tone: "blue" },
  "Bayani Badge": { icon: "🎖️", tone: "teal" },
  "History Buff": { icon: "📜", tone: "purple" },
  "Kasaysayan Master": { icon: "👑", tone: "gold" },
};

export function rankMeta(name) {
  return RANKS[name] || { icon: "⭐", tone: "" };
}

export function statusTone(label = "") {
  const l = label.toLowerCase();
  if (l.includes("complete") || l.includes("done") || l.includes("finish")) return "green";
  if (l.includes("progress") || l.includes("active") || l.includes("playing")) return "coral";
  if (l.includes("pending") || l.includes("wait") || l.includes("schedule")) return "blue";
  return "";
}

export const DIFFICULTY_TONE = { Easy: "green", Medium: "gold", Hard: "red" };
