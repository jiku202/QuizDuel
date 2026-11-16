import { useEffect } from "react";

// Centered dialog. Closes on Esc or when the backdrop is clicked.
export default function Modal({ title, children, onClose, width = 400 }) {
  useEffect(() => {
    const onKey = (e) => e.key === "Escape" && onClose();
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [onClose]);

  return (
    <div className="backdrop" onMouseDown={onClose}>
      <div className="modal" style={{ maxWidth: width }} role="dialog" aria-modal="true" onMouseDown={(e) => e.stopPropagation()}>
        <h3>{title}</h3>
        {children}
      </div>
    </div>
  );
}
