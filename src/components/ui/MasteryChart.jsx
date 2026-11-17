// Horizontal bars, one per topic. data: [[label, percent], ...]
export default function MasteryChart({ data }) {
  return (
    <div className="mastery">
      {data.map(([label, value]) => (
        <div key={label} className="m-row">
          <span className="m-label" title={label}>{label}</span>
          <div className="m-track">
            <div className={"m-fill " + (value >= 75 ? "high" : value >= 50 ? "mid" : "")} style={{ width: `${value}%` }} />
          </div>
          <span className="m-val">{value}%</span>
        </div>
      ))}
    </div>
  );
}
