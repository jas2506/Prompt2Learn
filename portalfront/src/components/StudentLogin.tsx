"use client";

import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import useApiWithAuth from "../hooks/useApiWithAuth"; // Adjust the path as needed

export default function StudentLogin() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const { apiCall } = useApiWithAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    // Basic validation
    if (!username || !password) {
      setError("All fields are required");
      return;
    }

    // Reset error
    setError("");

    try {
      // Call the /api/setCredentialsStudent endpoint with JSON payload.
      const response = await apiCall("POST", "/api/setCredentialsStudent", {
        email: username,
        password: password,
      });

      if (response.data && response.data.status === "S") {
        // On successful login, store the student ID in localStorage.
        localStorage.setItem("studentId", response.data.student_id);

        // Reset form fields and navigate to Outer Dashboard.
        setUsername("");
        setPassword("");
        navigate("/outer-dashboard");
      } else {
        setError(response.data?.message || "Login failed");
      }
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError(String(err));
      }
    }
  };

  return (
      <div className="min-h-screen flex flex-col">
        <header className="p-4 flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold">TriCoders</h1>
            <p className="text-lg">Student</p>
          </div>
          <div className="flex gap-4">
            <Link to="/teacher-login" className="px-4 py-2 text-black hover:underline">
              Teacher Login
            </Link>
            <Link to="/" className="px-4 py-2 bg-purple-700 text-white rounded">
              Student Registration
            </Link>
          </div>
        </header>

        <main className="flex-1 flex flex-col items-center justify-center p-4">
          <h2 className="text-3xl font-bold mb-8">Student Login</h2>
          <form onSubmit={handleSubmit} className="w-full max-w-md border rounded-lg p-8">
            {error && <div className="mb-4 text-red-500 text-sm">{error}</div>}
            <div className="mb-6">
              <label htmlFor="username" className="block mb-2">
                Enter username (email):
              </label>
              <input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="w-full p-2 bg-gray-200 rounded"
              />
            </div>
            <div className="mb-6">
              <label htmlFor="password" className="block mb-2">
                Enter password:
              </label>
              <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full p-2 bg-gray-200 rounded"
              />
            </div>
            <div className="flex justify-center">
              <button type="submit" className="px-6 py-2 bg-purple-700 text-white uppercase font-medium rounded">
                Login
              </button>
            </div>
          </form>
        </main>
      </div>
  );
}
