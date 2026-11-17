import { useLocation } from "react-router-dom";
import { NAV_ITEMS } from "../../config/navigation";
import { useAuth } from "../../context/useAuth";
import { Avatar } from "../ui/Avatar";

export default function Topbar() {
  const location = useLocation();
  const { teacher } = useAuth();
  const current = NAV_ITEMS.find((item) => item.to === location.pathname);

  return (
    <header className="topbar">
      <h2>{current ? `${current.icon} ${current.label}` : ""}</h2>
      {teacher?.name && (
        <div className="who">
          <div>
            <strong>{teacher.name}</strong>
            {teacher.school}
          </div>
          <Avatar name={teacher.name} large />
        </div>
      )}
    </header>
  );
}
