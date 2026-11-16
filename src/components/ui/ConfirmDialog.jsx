import Modal from "./Modal";

// "Are you sure?" dialog used instead of the browser's confirm().
export default function ConfirmDialog({ title, children, confirmLabel = "Delete", danger = true, onConfirm, onCancel }) {
  return (
    <Modal title={title} onClose={onCancel}>
      <p>{children}</p>
      <div className="actions">
        <button className="btn" onClick={onCancel}>Cancel</button>
        <button className={"btn " + (danger ? "danger" : "solid")} onClick={onConfirm} autoFocus>
          {confirmLabel}
        </button>
      </div>
    </Modal>
  );
}
