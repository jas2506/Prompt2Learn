"use client";

import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";

export default function TeacherLogin() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    // Basic validation
    if (!email || !password) {
      setError("All fields are required");
      return;
    }

    // Clear previous errors
    setError("");

    try {
      // Make the API call to the backend endpoint
      const response = await axios.post("http://localhost:9091/api/setCredentialsTeacher", {
        email: email,
        password: password,
      });

      if (response.data && response.data.status === "S") {
        // Store the teacher ID in localStorage
        localStorage.setItem("teacherId", response.data.teacher_id);

        // Clear form fields and navigate to teacher dashboard
        setEmail("");
        setPassword("");
        navigate("/teacher-dashboard");
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
            <p className="text-lg">Teacher</p>
          </div>
          <div className="flex gap-4">
            <Link to="/student-login" className="px-4 py-2 text-black hover:underline">
              Student Login
            </Link>
            <Link to="/teacher-registration" className="px-4 py-2 bg-purple-700 text-white rounded">
              Teacher Registration
            </Link>
          </div>
        </header>

        <main className="flex-1 flex flex-col items-center justify-center p-4">
          <h2 className="text-3xl font-bold mb-8">Faculty Login</h2>
          <form onSubmit={handleSubmit} className="w-full max-w-md border rounded-lg p-8">
            {error && <div className="mb-4 text-red-500 text-sm">{error}</div>}
            <div className="mb-6">
              <label htmlFor="email" className="block mb-2">
                Enter email:
              </label>
              <input
                  id="email"
                  type="text"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
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
