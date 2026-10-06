export default function StatBox({ label, value }) {
  return (
    <div className="statbox">
      <div className="lbl">{label}</div>
      <div className="num">{value}</div>
    </div>
  );
}
