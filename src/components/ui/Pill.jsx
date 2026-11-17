import { rankMeta } from "../../utils/badges";

export default function Pill({ tone = "", children }) {
  return <span className={"pill " + tone}>{children}</span>;
}

export function RankPill({ name }) {
  const { icon, tone } = rankMeta(name);
  return <Pill tone={tone}>{icon} {name}</Pill>;
}
