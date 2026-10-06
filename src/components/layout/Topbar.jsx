import { useLocation } from "react-router-dom";
import { NAV_ITEMS } from "../../config/navigation";
import { useAuth } from "../../context/useAuth";

export default function Topbar() {
  const location = useLocation();
  const { teacher } = useAuth();
  const current = NAV_ITEMS.find((item) => item.to === location.pathname);

  return (
    <div className="topbar">
      <h2>{current ? current.label : ""}</h2>
      <div className="who">{teacher?.name || "Mrs. Santos"} — Grade 3</div>
    </div>
  );
}
