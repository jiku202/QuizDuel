export default function Chip({ active, onClick, children }) {
  return (
    <span className={"chip" + (active ? " active" : "")} onClick={onClick}>
      {children}
    </span>
  );
}
