import Card from "../components/ui/Card";

export default function Settings() {
  return (
    <div className="page">
      <Card title="Battle Settings">
        <form onSubmit={(e) => e.preventDefault()} style={{ maxWidth: 360 }}>
          <div className="field">
            <label>Daily Battle Time</label>
            <input type="text" placeholder="3:00 PM" />
          </div>
          <div className="field" style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <input type="checkbox" style={{ width: "auto" }} id="wk" />
            <label htmlFor="wk" style={{ margin: 0, textTransform: "none", fontSize: 13 }}>
              Allow weekend battles
            </label>
          </div>
          <button className="btn solid" type="submit">
            Save Settings
          </button>
        </form>
      </Card>
    </div>
  );
}
