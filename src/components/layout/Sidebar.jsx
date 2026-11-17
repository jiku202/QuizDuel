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
    <aside className="sidebar">
      <div className="brand"><span className="logo">⚔️</span>QuizDuel</div>
      <div className="brandsub">Teacher Console</div>

      <nav>
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => "navitem" + (isActive ? " active" : "")}
          >
            <span className="ni">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <button type="button" className="logout" onClick={handleLogout}>
        <span className="ni">🚪</span>
        Log out
      </button>
    </aside>
  );
}
