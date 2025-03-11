"use client";

import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import axios from "axios";

export default function StudentRegistration() {
  const [studentId, setStudentId] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    // Basic validation
    if (!studentId || !email || !password || !confirmPassword) {
      setError("All fields are required");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setError(""); // Clear error before submission

    try {
      // Use the correct URL: include the '/api' prefix
      const response = await axios.post("http://localhost:9091/api/RegisterStudent", {
        student_id: studentId,
        email: email,
        password: password,
      });

      if (response.data && response.data.status === "S") {
        alert("Registration successful!");
        // Reset form fields after successful registration
        setStudentId("");
        setEmail("");
        setPassword("");
        setConfirmPassword("");
      } else {
        setError(response.data?.message || "Registration failed");
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
            <Link
                to="/teacher-registration"
                className="px-4 py-2 text-black hover:underline"
            >
              Teacher Registration
            </Link>
            <Link
                to="/student-login"
                className="px-4 py-2 bg-purple-700 text-white rounded"
            >
              Student Login
            </Link>
          </div>
        </header>

        <main className="flex-1 flex flex-col items-center justify-center p-4">
          <h2 className="text-3xl font-bold mb-8">Register as a Student</h2>

          <form onSubmit={handleSubmit} className="w-full max-w-md border rounded-lg p-8">
            {error && <div className="mb-4 text-red-500 text-sm">{error}</div>}

            <div className="mb-6">
              <label htmlFor="studentId" className="block mb-2">
                Enter Student ID:
              </label>
              <input
                  id="studentId"
                  type="text"
                  value={studentId}
                  onChange={(e) => setStudentId(e.target.value)}
                  className="w-full p-2 bg-gray-200 rounded"
              />
            </div>

            <div className="mb-6">
              <label htmlFor="email" className="block mb-2">
                Enter email:
              </label>
              <input
                  id="email"
                  type="email"
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

            <div className="mb-8">
              <label htmlFor="confirmPassword" className="block mb-2">
                Confirm password:
              </label>
              <input
                  id="confirmPassword"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full p-2 bg-gray-200 rounded"
              />
            </div>

            <div className="flex justify-center">
              <button
                  type="submit"
                  className="px-6 py-2 bg-purple-700 text-white uppercase font-medium rounded"
              >
                Register
              </button>
            </div>
          </form>
        </main>
      </div>
  );
}
