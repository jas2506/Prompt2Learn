"use client";

import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

type Course = {
    course_id: string;
    course_name: string;
    teacher_id: string;
};

type Module = {
    module_name: string;
    content: {
        google_drive_link: string;
        // additional fields if needed
    };
};

export default function OuterDashboard() {
    const [courses, setCourses] = useState<Course[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string>("");
    const [expandedCourseId, setExpandedCourseId] = useState<string | null>(null);
    const [modules, setModules] = useState<Module[]>([]);
    const [showDoubtModal, setShowDoubtModal] = useState<boolean>(false);
    const [fileLinksText, setFileLinksText] = useState<string>(""); // multiline text input for file links
    const [prompt, setPrompt] = useState<string>("");

    const navigate = useNavigate();

    useEffect(() => {
        // Retrieve studentId from localStorage (set during login)
        const studentId = localStorage.getItem("studentId");
        if (!studentId) {
            setError("Student ID not found. Please log in again.");
            setLoading(false);
            return;
        }

        const fetchCourses = async () => {
            try {
                // POST request with JSON payload
                const response = await axios.post("http://localhost:9091/api/getStudentDetails", {
                    student_id: studentId,
                });
                if (response.data && response.data.status === "S") {
                    setCourses(response.data.courses || []);
                } else {
                    setError(response.data?.message || "Failed to fetch courses.");
                }
            } catch (err: unknown) {
                if (err instanceof Error) {
                    setError(err.message);
                } else {
                    setError("Error fetching courses.");
                }
            } finally {
                setLoading(false);
            }
        };

        fetchCourses();
    }, []);

    const fetchModules = async (courseId: string) => {
        try {
            const response = await axios.post("http://localhost:9091/api/getAllModules", {
                course_id: courseId,
            });
            if (response.data && response.data.status === "S") {
                setModules(response.data.modules || []);
            } else {
                setError(response.data?.message || "Failed to fetch modules.");
            }
        } catch (err: unknown) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError("Error fetching modules.");
            }
        }
    };

    const toggleResources = (courseId: string) => {
        if (expandedCourseId === courseId) {
            setExpandedCourseId(null);
            setModules([]);
        } else {
            setExpandedCourseId(courseId);
            fetchModules(courseId);
        }
    };

    // Handler to summarize text for a module
    const handleModuleSummarizeText = async (moduleName: string) => {
        localStorage.setItem("selectedModuleName", moduleName);
        try {
            const response = await axios.post("http://localhost:9091/api/setSummaryText", {
                module_name: moduleName,
            });
            if (response.data && response.data.status === "S") {
                alert("Text summary: " + response.data.summary);
            } else {
                alert("Text summarization failed: " + (response.data?.message || ""));
            }
        } catch (err: unknown) {
            if (err instanceof Error) {
                alert("Error summarizing text: " + err.message);
            } else {
                alert("Error summarizing text.");
            }
        }
    };

    // Handler to summarize audio for a module
    const handleModuleSummarizeAudio = async (moduleName: string) => {
        localStorage.setItem("selectedModuleName", moduleName);
        try {
            const response = await axios.post("http://localhost:9091/api/setSummaryAudio", {
                module_name: moduleName,
            });
            if (response.data && response.data.status === "S") {
                alert("Audio summary: " + response.data.audio_summary);
            } else {
                alert("Audio summarization failed: " + (response.data?.message || ""));
            }
        } catch (err: unknown) {
            if (err instanceof Error) {
                alert("Error summarizing audio: " + err.message);
            } else {
                alert("Error summarizing audio.");
            }
        }
    };

    // Handler for file upload: extract file links (one per line) and call backend API.
    const handleFileUpload = async () => {
        // Split the text area value by newlines to create an array of file links
        const fileLinks = fileLinksText
            .split("\n")
            .map(link => link.trim())
            .filter(link => link !== "");

        if (fileLinks.length === 0) {
            alert("Please enter at least one file link.");
            return;
        }

        try {
            const response = await axios.post("http://localhost:9091/api/uploadFilesByLink", {
                file_links: fileLinks,
            });
            if (response.data && response.data.status === "S") {
                alert("Files uploaded successfully to Flask.");
            } else {
                alert("File upload failed: " + (response.data?.message || ""));
            }
        } catch (err: unknown) {
            if (err instanceof Error) {
                alert("Error uploading files: " + err.message);
            } else {
                alert("Error uploading files.");
            }
        }
    };

    // Handler for chatbot query
    const handleChatbotQuery = async () => {
        if (!prompt) {
            alert("Please enter a query prompt.");
            return;
        }
        try {
            const response = await axios.post("http://localhost:9091/api/chatbotquery", {
                prompt: prompt,
            });
            if (response.data && response.data.status === "S") {
                alert("Chatbot response: " + response.data.response);
            } else {
                alert("Chatbot query failed: " + (response.data?.message || ""));
            }
        } catch (err: unknown) {
            if (err instanceof Error) {
                alert("Error querying chatbot: " + err.message);
            } else {
                alert("Error querying chatbot.");
            }
        }
    };

    if (loading) {
        return <div className="p-4">Loading courses...</div>;
    }

    if (error) {
        return <div className="p-4 text-red-500">Error: {error}</div>;
    }

    return (
        <div className="min-h-screen p-4 bg-gray-100 flex flex-col justify-between relative">
            <header className="flex justify-between items-center bg-white p-4 rounded-lg shadow">
                <h1 className="text-2xl font-bold">Student Dashboard</h1>
                <button className="px-4 py-2 bg-gray-300 rounded">John Doe ▼</button>
            </header>

            <main className="mt-6 flex-grow">
                <h2 className="text-lg font-semibold mb-4">My Courses</h2>
                {courses.length === 0 ? (
                    <p>No courses enrolled.</p>
                ) : (
                    courses.map((course, index) => (
                        <div
                            key={index}
                            className="bg-white p-4 rounded-lg mb-4 shadow flex flex-col"
                        >
              <span className="text-lg font-semibold">
                Course ID: {course.course_id}
              </span>
                            <span className="text-md">Course Name: {course.course_name}</span>
                            <span className="text-sm text-gray-500">
                Teacher: {course.teacher_id}
              </span>
                            <div className="flex gap-2 mt-4">
                                <button className="px-4 py-2 bg-purple-200 text-purple-700 rounded">
                                    Summarize
                                </button>
                                <button
                                    className="px-4 py-2 bg-purple-400 text-white rounded"
                                    onClick={() => toggleResources(course.course_id)}
                                >
                                    {expandedCourseId === course.course_id ? "Hide Resources" : "View Resources"}
                                </button>
                                <button className="px-4 py-2 bg-purple-700 text-white rounded">
                                    Take Quiz
                                </button>
                            </div>
                            {expandedCourseId === course.course_id && (
                                <div className="mt-4">
                                    <h3 className="text-md font-semibold mb-2">Modules:</h3>
                                    {modules.length === 0 ? (
                                        <p>No modules available for this course.</p>
                                    ) : (
                                        modules.map((module, idx) => (
                                            <div key={idx} className="bg-gray-50 p-2 rounded mb-2">
                                                <p className="text-sm font-semibold">{module.module_name}</p>
                                                <div className="flex gap-2 mt-2">
                                                    <button
                                                        className="px-4 py-2 bg-yellow-500 text-white rounded"
                                                        onClick={() => handleModuleSummarizeText(module.module_name)}
                                                    >
                                                        Summarize Text
                                                    </button>
                                                    <button
                                                        className="px-4 py-2 bg-red-500 text-white rounded"
                                                        onClick={() => handleModuleSummarizeAudio(module.module_name)}
                                                    >
                                                        Summarize Audio
                                                    </button>
                                                    <button
                                                        className="px-4 py-2 bg-green-500 text-white rounded"
                                                        onClick={() =>
                                                            window.open(module.content.google_drive_link, "_blank")
                                                        }
                                                    >
                                                        Open Drive
                                                    </button>
                                                </div>
                                            </div>
                                        ))
                                    )}
                                </div>
                            )}
                        </div>
                    ))
                )}
            </main>

            <footer className="fixed bottom-4 right-4">
                <button
                    className="px-4 py-2 bg-blue-500 text-white rounded flex items-center gap-2"
                    onClick={() => setShowDoubtModal(true)}
                >
                    🤖 Doubt Solver
                </button>
            </footer>

            {showDoubtModal && (
                <div className="fixed inset-0 flex justify-center items-center bg-black bg-opacity-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                        <h2 className="text-xl font-bold mb-4">Doubt Solver</h2>
                        <div className="mb-4">
                            <label className="block mb-2">File Links (one per line):</label>
                            <textarea
                                className="w-full p-2 border rounded"
                                rows={4}
                                value={fileLinksText}
                                onChange={(e) => setFileLinksText(e.target.value)}
                                placeholder="Enter full paths of files, one per line"
                            ></textarea>
                        </div>
                        <button
                            className="px-4 py-2 bg-green-500 text-white rounded mb-4"
                            onClick={handleFileUpload}
                        >
                            Upload Files
                        </button>
                        <div className="mb-4">
                            <label className="block mb-2">Chatbot Query Prompt:</label>
                            <input
                                type="text"
                                className="w-full p-2 border rounded"
                                value={prompt}
                                onChange={(e) => setPrompt(e.target.value)}
                                placeholder="Enter your query"
                            />
                        </div>
                        <button
                            className="px-4 py-2 bg-purple-600 text-white rounded"
                            onClick={handleChatbotQuery}
                        >
                            Query Chatbot
                        </button>
                        <button
                            className="mt-4 px-4 py-2 bg-gray-300 rounded"
                            onClick={() => setShowDoubtModal(false)}
                        >
                            Close
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
