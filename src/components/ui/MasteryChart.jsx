export default function MasteryChart({ data }) {
  return (
    <div className="chartph">
      {data.map(([label, value]) => (
        <div key={label} className="barchart" style={{ height: value + "%" }}>
          <span className="lbl">{label}</span>
        </div>
      ))}
    </div>
  );
}
