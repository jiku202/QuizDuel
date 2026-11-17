// tone: "" (teal) | "coral" | "gold" | "purple" | "blue"
export default function StatBox({ label, value, icon, tone = "" }) {
  return (
    <div className={"statbox " + tone}>
      {icon && <div className="ico">{icon}</div>}
      <div>
        <div className="num">{value}</div>
        <div className="lbl">{label}</div>
      </div>
    </div>
  );
}
