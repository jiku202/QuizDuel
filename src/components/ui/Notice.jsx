import { useEffect, useRef } from "react";

// Message banner ({ type: "" | "success" | "error", text }). Success/info messages hide themselves.
export default function Notice({ message, onClose }) {
  const { type, text } = message;
  const closeRef = useRef(onClose);
  closeRef.current = onClose;

  useEffect(() => {
    if (!text || type === "error") return;
    const timer = setTimeout(() => closeRef.current(), 6000);
    return () => clearTimeout(timer);
  }, [text, type]);

  if (!text) return null;
  return (
    <div className={`notice ${type}`} role={type === "error" ? "alert" : "status"} style={{ display: "flex", gap: 10 }}>
      <span style={{ flex: 1 }}>{text}</span>
      <button type="button" className="notice-x" aria-label="Dismiss" onClick={onClose}>×</button>
    </div>
  );
}
