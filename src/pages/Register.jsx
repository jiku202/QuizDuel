import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

export default function Register() {
  const [done, setDone] = useState(false);
  const navigate = useNavigate();

  return (
    <div className="authwrap">
      <div className="authcard">
        <h1>Create an Account</h1>
        <div className="sub">For teachers using QuizDuel.</div>

        {done ? (
          <div>
            <p style={{ fontSize: 13, marginBottom: 14 }}>
              Account created (demo). You can now log in.
            </p>
            <button
              className="btn solid"
              style={{ width: "100%" }}
              onClick={() => navigate("/login")}
            >
              Go to Login
            </button>
          </div>
        ) : (
          <form
            onSubmit={(e) => {
              e.preventDefault();
              setDone(true);
            }}
          >
            <div className="field">
              <label>Full Name</label>
              <input type="text" placeholder="Mrs. Santos" required />
            </div>
            <div className="field">
              <label>School</label>
              <input type="text" placeholder="Mabini Elementary School" required />
            </div>
            <div className="field">
              <label>Email</label>
              <input type="email" placeholder="teacher@school.edu.ph" required />
            </div>
            <div className="field">
              <label>Password</label>
              <input type="password" placeholder="••••••••" required />
            </div>
            <button className="btn solid" style={{ width: "100%" }} type="submit">
              Create Account
            </button>
          </form>
        )}

        {!done && (
          <div className="switchlink">
            Already have an account? <Link to="/login">Log in</Link>
          </div>
        )}
      </div>
    </div>
  );
}
