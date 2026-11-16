// Previous / Next navigator. Renders nothing when everything fits on one page.
export default function Pager({ page, pages, onChange }) {
  if (pages <= 1) return null;
  return (
    <div className="pager">
      <button type="button" className="btn small" disabled={page <= 1} onClick={() => onChange(page - 1)}>
        ‹ Previous
      </button>
      <span>Page {page} of {pages}</span>
      <button type="button" className="btn small" disabled={page >= pages} onClick={() => onChange(page + 1)}>
        Next ›
      </button>
    </div>
  );
}
