import { useEffect, useRef, useState } from "react";

// 3-dot menu. items: [{ label, onClick, danger? }]
export default function KebabMenu({ items }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    if (!open) return;
    const close = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [open]);

  return (
    <div className="kebab inline" ref={ref} onClick={(e) => e.stopPropagation()}>
      <button
        type="button"
        className={"kebab-btn" + (open ? " open" : "")}
        aria-label="Options"
        onClick={() => setOpen(!open)}
      >
        ⋮
      </button>
      {open && (
        <div className="menu">
          {items.map((it) => (
            <button
              key={it.label}
              type="button"
              className={it.danger ? "danger" : ""}
              onClick={() => {
                setOpen(false);
                it.onClick();
              }}
            >
              {it.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
