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
        // additional fields if needed (e.g. quiz_link)
    };
};

export default function StudentDashboard() {
    const [courses, setCourses] = useState<Course[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string>("");
    const [expandedCourseId, setExpandedCourseId] = useState<string | null>(null);
    const [modules, setModules] = useState<Module[]>([]);
    const navigate = useNavigate();

    // States for Summary Modals
    const [showTextSummaryModal, setShowTextSummaryModal] = useState<boolean>(false);
    const [textSummaryOutput, setTextSummaryOutput] = useState<string>("");
    const [showAudioSummaryModal, setShowAudioSummaryModal] = useState<boolean>(false);
    const [audioSummaryOutput, setAudioSummaryOutput] = useState<string>("");

    useEffect(() => {
        const studentId = localStorage.getItem("studentId");
        if (!studentId) {
            setError("Student ID not found. Please log in again.");
            setLoading(false);
            return;
        }

        const fetchCourses = async () => {
            try {
                const response = await axios.post("http://localhost:9091/api/getStudentDetails", {
                    student_id: studentId,
                });
                if (response.data && response.data.status === "S") {
                    setCourses(response.data.courses || []);
                } else {
                    setError(response.data?.message || "Failed to fetch courses.");
                }
            } catch (err: unknown) {
                setError(err instanceof Error ? err.message : String(err));
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
            setError(err instanceof Error ? err.message : String(err));
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

    // Handler to view the quiz link for a module and open it in a new tab
    const handleAttendQuiz = async (moduleName: string) => {
        try {
            const response = await axios.post("http://localhost:9091/api/getQuizLink", {
                module_name: moduleName,
            });
            if (response.data && response.data.status === "S" && response.data.quiz_link) {
                window.open(response.data.quiz_link, "_blank");
            } else {
                alert("Failed to retrieve quiz link: " + (response.data?.message || ""));
            }
        } catch (err: unknown) {
            alert("Error retrieving quiz link: " + (err instanceof Error ? err.message : ""));
        }
    };

    // Handler to generate text summary for a module and display it in a modal
    const handleSummarizeText = async (moduleName: string) => {
        try {
            const response = await axios.post("http://localhost:9091/api/setSummaryText", {
                module_name: moduleName,
            });
            if (response.data && response.data.status === "S") {
                setTextSummaryOutput(response.data.summary);
                setShowTextSummaryModal(true);
            } else {
                alert("Failed to summarize text: " + (response.data?.message || ""));
            }
        } catch (err: unknown) {
            alert("Error summarizing text: " + (err instanceof Error ? err.message : ""));
        }
    };

    // Handler to generate audio summary for a module and display it in a modal
    const handleSummarizeAudio = async (moduleName: string) => {
        try {
            const response = await axios.post("http://localhost:9091/api/setSummaryAudio", {
                module_name: moduleName,
            });
            if (response.data && response.data.status === "S") {
                setAudioSummaryOutput(response.data.audio_summary);
                setShowAudioSummaryModal(true);
            } else {
                alert("Failed to summarize audio: " + (response.data?.message || ""));
            }
        } catch (err: unknown) {
            alert("Error summarizing audio: " + (err instanceof Error ? err.message : ""));
        }
    };

    // Handler to copy text to clipboard
    const handleCopyText = async (text: string) => {
        try {
            await navigator.clipboard.writeText(text);
            alert("Text copied to clipboard.");
        } catch (err: unknown) {
            alert("Failed to copy text.");
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
            {/* Header */}
            <header className="flex justify-between items-center bg-white p-4 rounded-lg shadow">
                <h1 className="text-2xl font-bold">Student Dashboard</h1>
                <button className="px-4 py-2 bg-gray-300 rounded">John Doe ▼</button>
            </header>

            {/* Main Content */}
            <main className="mt-6 flex-grow">
                <h2 className="text-lg font-semibold mb-4">My Courses</h2>
                {courses.length === 0 ? (
                    <p>No courses enrolled.</p>
                ) : (
                    courses.map((course, index) => (
                        <div key={index} className="bg-white p-4 rounded-lg mb-4 shadow flex flex-col">
                            <span className="text-lg font-semibold">Course ID: {course.course_id}</span>
                            <span className="text-md">Course Name: {course.course_name}</span>
                            <span className="text-sm text-gray-500">Teacher: {course.teacher_id}</span>
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
                                            <div key={idx} className="bg-gray-50 p-2 rounded mb-2 flex flex-col gap-2">
                                                <p className="text-sm font-semibold">{module.module_name}</p>
                                                <div className="flex gap-2 mt-2">
                                                    <button
                                                        className="px-4 py-2 bg-green-500 text-white rounded"
                                                        onClick={() =>
                                                            window.open(module.content.google_drive_link, "_blank")
                                                        }
                                                    >
                                                        Open Drive
                                                    </button>
                                                    <button
                                                        className="px-4 py-2 bg-blue-500 text-white rounded"
                                                        onClick={() => handleAttendQuiz(module.module_name)}
                                                    >
                                                        Attend Quiz
                                                    </button>
                                                    <button
                                                        className="px-4 py-2 bg-indigo-500 text-white rounded"
                                                        onClick={() => handleSummarizeText(module.module_name)}
                                                    >
                                                        Summarize Text
                                                    </button>
                                                    <button
                                                        className="px-4 py-2 bg-red-500 text-white rounded"
                                                        onClick={() => handleSummarizeAudio(module.module_name)}
                                                    >
                                                        Summarize Audio
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

            {/* Footer */}
            <footer className="fixed bottom-4 right-4">
                <button className="px-4 py-2 bg-blue-500 text-white rounded flex items-center gap-2">
                    🤖 Doubt Solver
                </button>
            </footer>

            {/* Text Summary Modal */}
            {showTextSummaryModal && (
                <div className="fixed inset-0 flex items-center justify-center bg-gray-800 bg-opacity-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg max-w-2xl w-full">
                        <h2 className="text-xl font-bold mb-4">Text Summary</h2>
                        <textarea
                            className="w-full p-2 border rounded mb-4"
                            rows={10}
                            value={textSummaryOutput}
                            readOnly
                        />
                        <div className="flex justify-end gap-2">
                            <button
                                className="px-4 py-2 bg-blue-500 text-white rounded"
                                onClick={() => handleCopyText(textSummaryOutput)}
                            >
                                Copy
                            </button>
                            <button
                                className="px-4 py-2 bg-gray-400 text-white rounded"
                                onClick={() => setShowTextSummaryModal(false)}
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Audio Summary Modal */}
            {showAudioSummaryModal && (
                <div className="fixed inset-0 flex items-center justify-center bg-gray-800 bg-opacity-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg max-w-2xl w-full">
                        <h2 className="text-xl font-bold mb-4">Audio Summary</h2>
                        <textarea
                            className="w-full p-2 border rounded mb-4"
                            rows={10}
                            value={audioSummaryOutput}
                            readOnly
                        />
                        <div className="flex justify-end gap-2">
                            <button
                                className="px-4 py-2 bg-blue-500 text-white rounded"
                                onClick={() => handleCopyText(audioSummaryOutput)}
                            >
                                Copy
                            </button>
                            <button
                                className="px-4 py-2 bg-gray-400 text-white rounded"
                                onClick={() => setShowAudioSummaryModal(false)}
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

// Handler to view the quiz link for a module (must be defined outside the component if reused)
async function handleAttendQuiz(moduleName: string) {
    try {
        const response = await axios.post("http://localhost:9091/api/getQuizLink", {
            module_name: moduleName,
        });
        if (response.data && response.data.status === "S" && response.data.quiz_link) {
            window.open(response.data.quiz_link, "_blank");
        } else {
            alert("Failed to retrieve quiz link: " + (response.data?.message || ""));
        }
    } catch (err: unknown) {
        alert("Error retrieving quiz link: " + (err instanceof Error ? err.message : ""));
    }
}
