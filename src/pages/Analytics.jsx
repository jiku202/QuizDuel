import Card from "../components/ui/Card";
import StatBox from "../components/ui/StatBox";
import MasteryChart from "../components/ui/MasteryChart";
import { MASTERY } from "../data/mockData";

export default function Analytics() {
  return (
    <div className="page">
      <div className="row">
        <StatBox label="Average Score" value="76%" />
        <StatBox label="Battles Today" value="14" />
        <StatBox label="Active Students" value="28" />
      </div>

      <Card title="Topic Mastery">
        <MasteryChart data={MASTERY} />
      </Card>
    </div>
  );
}
