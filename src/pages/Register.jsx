import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", school: "", email: "", password: "" });
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const update = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await register(form);
      setDone(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="authwrap">
      <div className="authcard">
        <h1>Create an Account</h1>
        <div className="sub">For teachers using QuizDuel.</div>

        {done ? (
          <div>
            <div className="banner success">Account created. You can now log in.</div>
            <button
              className="btn solid"
              style={{ width: "100%" }}
              onClick={() => navigate("/login")}
            >
              Go to Login
            </button>
          </div>
        ) : (
          <>
            {error && <div className="banner">{error}</div>}
            <form onSubmit={handleSubmit}>
              <div className="field">
                <label>Full Name</label>
                <input type="text" placeholder="Mrs. Santos" value={form.name} onChange={update("name")} required />
              </div>
              <div className="field">
                <label>School</label>
                <input type="text" placeholder="Mabini Elementary School" value={form.school} onChange={update("school")} required />
              </div>
              <div className="field">
                <label>Email</label>
                <input type="email" placeholder="teacher@school.edu.ph" value={form.email} onChange={update("email")} required />
              </div>
              <div className="field">
                <label>Password</label>
                <input type="password" placeholder="At least 6 characters" value={form.password} onChange={update("password")} minLength={6} required />
              </div>
              <button className="btn solid" style={{ width: "100%" }} type="submit" disabled={submitting}>
                {submitting ? "Creating account..." : "Create Account"}
              </button>
            </form>
          </>
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
