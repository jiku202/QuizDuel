import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";
import Topbar from "./Topbar";

export default function AppShell() {
  return (
    <div className="shell">
      <Sidebar />
      <main>
        <Topbar />
        <Outlet />
      </main>
    </div>
  );
}
