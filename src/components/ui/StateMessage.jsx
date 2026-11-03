// Shows "Loading..." or an error for an API-backed section.
// Renders nothing when everything is fine.
export default function StateMessage({ loading, error }) {
  if (error) return <div className="notice error">{error}</div>;
  if (loading) return <div className="muted">Loading...</div>;
  return null;
}
