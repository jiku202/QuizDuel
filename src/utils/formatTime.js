// "15:00" -> "3:00 PM". Anything that isn't HH:mm is shown as-is ("—" when empty).
export function formatTime(value) {
  if (!value) return "—";
  const m = /^(\d{1,2}):(\d{2})$/.exec(value);
  if (!m) return value;
  const h = Number(m[1]);
  return `${h % 12 === 0 ? 12 : h % 12}:${m[2]} ${h < 12 ? "AM" : "PM"}`;
}
