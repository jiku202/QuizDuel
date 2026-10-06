// Demo/mock data only — no backend is wired up yet.
// Swap these for real API calls once a backend exists.

export const TOPICS = [
  "Filipino Heroes",
  "Spanish Colonization",
  "National Symbols",
  "EDSA Revolution",
];

export const QUESTIONS = [
  { q: "Who is the national hero of the Philippines?", topic: "Filipino Heroes", diff: "Easy" },
  { q: "What year did Ferdinand Magellan arrive in the Philippines?", topic: "Spanish Colonization", diff: "Medium" },
  { q: "What color does the Philippine flag show when the country is at war?", topic: "National Symbols", diff: "Medium" },
  { q: "Who became the first female president of the Philippines?", topic: "EDSA Revolution", diff: "Easy" },
  { q: "In which province was Andres Bonifacio born?", topic: "Filipino Heroes", diff: "Hard" },
];

export const TODAY_BATTLES = [
  ["Ana Cruz", "Miguel Reyes", "Filipino Heroes", "In Progress"],
  ["Liza Santos", "Paolo Diaz", "National Symbols", "Completed"],
  ["Carlo Ramos", "Nina Bautista", "Spanish Colonization", "Completed"],
  ["Ella Torres", "Practice Bot", "Filipino Heroes", "In Progress"],
];

export const BATTLE_LOG = [
  ["Oct 10", "Ana Cruz", "Miguel Reyes", "Ana Cruz"],
  ["Oct 10", "Liza Santos", "Paolo Diaz", "Paolo Diaz"],
  ["Oct 9", "Carlo Ramos", "Nina Bautista", "Nina Bautista"],
  ["Oct 9", "Ella Torres", "Practice Bot", "Ella Torres"],
];

export const LEADERBOARD = [
  ["1", "Ana Cruz", "480", "5"],
  ["2", "Miguel Reyes", "420", "4"],
  ["3", "Ella Torres", "320", "3"],
  ["4", "Liza Santos", "300", "3"],
  ["5", "Paolo Diaz", "260", "2"],
];

export const ROSTER = [
  ["Ana Cruz", "480"],
  ["Miguel Reyes", "420"],
  ["Liza Santos", "300"],
];

export const MASTERY = [
  ["Heroes", 82],
  ["Colonization", 54],
  ["Symbols", 90],
  ["EDSA", 61],
];
