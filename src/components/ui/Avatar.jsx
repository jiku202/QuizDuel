const COLORS = ["#1A6B66", "#E76F51", "#8A6FDF", "#4C9BE8", "#D9A21B", "#D96A9C", "#2A9D8F"];

function initials(name = "") {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "?";
  return (parts[0][0] + (parts.length > 1 ? parts[parts.length - 1][0] : "")).toUpperCase();
}

function colorFor(name = "") {
  let h = 0;
  for (const ch of name) h = (h * 31 + ch.charCodeAt(0)) >>> 0;
  return COLORS[h % COLORS.length];
}

export function Avatar({ name, large = false }) {
  return (
    <span className={"avatar" + (large ? " lg" : "")} style={{ background: colorFor(name) }} aria-hidden="true">
      {initials(name)}
    </span>
  );
}

// Avatar + name, used wherever a student or teacher is listed.
export function Person({ name }) {
  if (!name) return <span className="muted">—</span>;
  return (
    <span className="person">
      <Avatar name={name} />
      <span>{name}</span>
    </span>
  );
}
