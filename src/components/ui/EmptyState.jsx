// Friendly placeholder for lists with nothing in them yet.
export default function EmptyState({ icon = "📭", title, children }) {
  return (
    <div className="empty">
      <div className="e-icon">{icon}</div>
      {title && <div className="e-title">{title}</div>}
      {children && <div className="e-text">{children}</div>}
    </div>
  );
}
