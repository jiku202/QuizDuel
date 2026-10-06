import { NavLink, useNavigate } from "react-router-dom";
import { NAV_ITEMS } from "../../config/navigation";
import { useAuth } from "../../context/useAuth";

export default function Sidebar() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <div className="sidebar">
      <div className="brand">QuizDuel</div>
      <div className="brandsub">Teacher Console · Grade 3</div>

      {NAV_ITEMS.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          className={({ isActive }) => "navitem" + (isActive ? " active" : "")}
        >
          {item.label}
        </NavLink>
      ))}

      <div className="logout" onClick={handleLogout}>
        Log out
      </div>
    </div>
  );
}
