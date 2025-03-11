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
        ppt_link: string;
        audio_link: string;
        quiz_link?: string;
        // additional fields if needed
    };
};

export default function TeacherDashboard() {
    const [courses, setCourses] = useState<Course[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string>("");
    const [expandedCourseId, setExpandedCourseId] = useState<string | null>(null);
    const [modules, setModules] = useState<Module[]>([]);
    const navigate = useNavigate();

    // States for module update modals
    const [showPptModal, setShowPptModal] = useState<boolean>(false);
    const [showAudioModal, setShowAudioModal] = useState<boolean>(false);
    const [showQuizLinkModal, setShowQuizLinkModal] = useState<boolean>(false);
    const [pptLinkInput, setPptLinkInput] = useState<string>("");
    const [audioLinkInput, setAudioLinkInput] = useState<string>("");
    const [quizLinkInput, setQuizLinkInput] = useState<string>("");
    const [selectedModuleName, setSelectedModuleName] = useState<string | null>(null);

    // State for Quiz Modal (for generated quiz output)
    const [showQuizModal, setShowQuizModal] = useState<boolean>(false);
    const [quizOutput, setQuizOutput] = useState<string>("");

    useEffect(() => {
        const teacherId = localStorage.getItem("teacherId");
        if (!teacherId) {
            setError("Teacher ID not found. Please log in again.");
            setLoading(false);
            return;
        }
        const fetchCourses = async () => {
            try {
                const response = await axios.post("http://localhost:9091/api/getTeacherDetails", {
                    teacher_id: teacherId,
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

    // Handler to update PPT link for a module
    const handleSubmitPptLink = async () => {
        if (!pptLinkInput.trim() || !selectedModuleName) {
            alert("Please enter a valid PPT link.");
            return;
        }
        try {
            const response = await axios.post("http://localhost:9091/api/addppt", {
                module_name: selectedModuleName,
                ppt_link: pptLinkInput,
            });
            if (response.data && response.data.status === "S") {
                alert("PPT link updated successfully.");
                setModules(prevModules =>
                    prevModules.map(module =>
                        module.module_name === selectedModuleName
                            ? { ...module, content: { ...module.content, ppt_link: pptLinkInput } }
                            : module
                    )
                );
                setShowPptModal(false);
                setPptLinkInput("");
                setSelectedModuleName(null);
            } else {
                alert("Failed to update PPT link: " + (response.data?.message || ""));
            }
        } catch (err: unknown) {
            if (err instanceof Error) {
                alert("Error updating PPT link: " + err.message);
            } else {
                alert("Error updating PPT link.");
            }
        }
    };

    // Handler to update Audio link for a module
    const handleSubmitAudioLink = async () => {
        if (!audioLinkInput.trim() || !selectedModuleName) {
            alert("Please enter a valid Audio link.");
            return;
        }
        try {
            const response = await axios.post("http://localhost:9091/api/addaudio", {
                module_name: selectedModuleName,
                audio_link: audioLinkInput,
            });
            if (response.data && response.data.status === "S") {
                alert("Audio link updated successfully.");
                setModules(prevModules =>
                    prevModules.map(module =>
                        module.module_name === selectedModuleName
                            ? { ...module, content: { ...module.content, audio_link: audioLinkInput } }
                            : module
                    )
                );
                setShowAudioModal(false);
                setAudioLinkInput("");
                setSelectedModuleName(null);
            } else {
                alert("Failed to update Audio link: " + (response.data?.message || ""));
            }
        } catch (err: unknown) {
            if (err instanceof Error) {
                alert("Error updating Audio link: " + err.message);
            } else {
                alert("Error updating Audio link.");
            }
        }
    };

    // Handler to generate quiz for a module and display in a modal
    const handleGenerateQuiz = async (moduleName: string) => {
        try {
            const response = await axios.post("http://localhost:9091/api/generateQuizFromModule", {
                module_name: moduleName,
            });
            if (response.data && response.data.status === "S") {
                setQuizOutput(response.data.quiz);
                setShowQuizModal(true);
            } else {
                alert("Failed to generate quiz: " + (response.data?.message || ""));
            }
        } catch (err: unknown) {
            if (err instanceof Error) {
                alert("Error generating quiz: " + err.message);
            } else {
                alert("Error generating quiz.");
            }
        }
    };

    // Handler to copy quiz text to clipboard
    const handleCopyQuiz = async () => {
        try {
            await navigator.clipboard.writeText(quizOutput);
            alert("Quiz text copied to clipboard.");
        } catch (err: unknown) {
            alert("Failed to copy quiz text.");
        }
    };

    // Handler to update Quiz link for a module
    const handleSubmitQuizLink = async () => {
        if (!quizLinkInput.trim() || !selectedModuleName) {
            alert("Please enter a valid Quiz link.");
            return;
        }
        try {
            // Use the endpoint "/uploadquizlink" if that's the correct mapping in your backend
            const response = await axios.post("http://localhost:9091/api/uploadquizlink", {
                module_name: selectedModuleName,
                quiz_link: quizLinkInput,
            });
            if (response.data && response.data.status === "S") {
                alert("Quiz link updated successfully.");
                setModules(prevModules =>
                    prevModules.map(module =>
                        module.module_name === selectedModuleName
                            ? { ...module, content: { ...module.content, quiz_link: quizLinkInput } }
                            : module
                    )
                );
                setShowQuizLinkModal(false);
                setQuizLinkInput("");
                setSelectedModuleName(null);
            } else {
                alert("Failed to update Quiz link: " + (response.data?.message || ""));
            }
        } catch (err: unknown) {
            if (err instanceof Error) {
                alert("Error updating Quiz link: " + err.message);
            } else {
                alert("Error updating Quiz link.");
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
                <h1 className="text-2xl font-bold">Teacher Dashboard</h1>
                <button className="px-4 py-2 bg-gray-300 rounded">John Doe ▼</button>
            </header>

            <main className="mt-6 flex-grow">
                <h2 className="text-lg font-semibold mb-4">My Courses</h2>
                {courses.length === 0 ? (
                    <p>No courses found.</p>
                ) : (
                    courses.map((course, index) => (
                        <div key={index} className="bg-white p-4 rounded-lg mb-4 shadow flex flex-col">
                            <span className="text-lg font-semibold">Course ID: {course.course_id}</span>
                            <span className="text-md">Course Name: {course.course_name}</span>
                            <span className="text-sm text-gray-500">Teacher: {course.teacher_id}</span>
                            <div className="flex gap-2 mt-4">
                                <button
                                    className="px-4 py-2 bg-purple-400 text-white rounded"
                                    onClick={() => toggleResources(course.course_id)}
                                >
                                    {expandedCourseId === course.course_id ? "Hide Resources" : "View Resources"}
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
                                                <p className="text-xs text-gray-600">PPT Link: {module.content.ppt_link}</p>
                                                <p className="text-xs text-gray-600">Audio Link: {module.content.audio_link}</p>
                                                {module.content.quiz_link && (
                                                    <p className="text-xs text-gray-600">Quiz Link: {module.content.quiz_link}</p>
                                                )}
                                                <div className="flex gap-2 mt-2">
                                                    <button
                                                        className="px-4 py-2 bg-indigo-500 text-white rounded"
                                                        onClick={() => handleGenerateQuiz(module.module_name)}
                                                    >
                                                        Generate Quiz
                                                    </button>
                                                    <button
                                                        className="px-4 py-2 bg-green-500 text-white rounded"
                                                        onClick={() => {
                                                            setSelectedModuleName(module.module_name);
                                                            setShowPptModal(true);
                                                        }}
                                                    >
                                                        Add PPT
                                                    </button>
                                                    <button
                                                        className="px-4 py-2 bg-red-500 text-white rounded"
                                                        onClick={() => {
                                                            setSelectedModuleName(module.module_name);
                                                            setShowAudioModal(true);
                                                        }}
                                                    >
                                                        Add Audio
                                                    </button>
                                                    <button
                                                        className="px-4 py-2 bg-yellow-500 text-white rounded"
                                                        onClick={() => {
                                                            setSelectedModuleName(module.module_name);
                                                            setShowQuizLinkModal(true);
                                                        }}
                                                    >
                                                        Upload QuizLink
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

            <footer className="mt-6 flex justify-center">
                <button className="px-6 py-3 bg-purple-700 text-white rounded">ADD LECTURE</button>
            </footer>

            {/* PPT Modal */}
            {showPptModal && (
                <div className="fixed inset-0 flex items-center justify-center bg-gray-800 bg-opacity-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                        <h2 className="text-xl font-bold mb-4">Add PPT Link</h2>
                        <input
                            type="text"
                            value={pptLinkInput}
                            onChange={(e) => setPptLinkInput(e.target.value)}
                            className="w-full p-2 border rounded mb-4"
                            placeholder="Enter PPT link"
                        />
                        <div className="flex justify-end gap-2">
                            <button onClick={handleSubmitPptLink} className="px-4 py-2 bg-green-500 text-white rounded">
                                Submit
                            </button>
                            <button
                                onClick={() => {
                                    setShowPptModal(false);
                                    setPptLinkInput("");
                                    setSelectedModuleName(null);
                                }}
                                className="px-4 py-2 bg-gray-400 text-white rounded"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Audio Modal */}
            {showAudioModal && (
                <div className="fixed inset-0 flex items-center justify-center bg-gray-800 bg-opacity-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                        <h2 className="text-xl font-bold mb-4">Add Audio Link</h2>
                        <input
                            type="text"
                            value={audioLinkInput}
                            onChange={(e) => setAudioLinkInput(e.target.value)}
                            className="w-full p-2 border rounded mb-4"
                            placeholder="Enter Audio link"
                        />
                        <div className="flex justify-end gap-2">
                            <button onClick={handleSubmitAudioLink} className="px-4 py-2 bg-green-500 text-white rounded">
                                Submit
                            </button>
                            <button
                                onClick={() => {
                                    setShowAudioModal(false);
                                    setAudioLinkInput("");
                                    setSelectedModuleName(null);
                                }}
                                className="px-4 py-2 bg-gray-400 text-white rounded"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Quiz Link Modal */}
            {showQuizLinkModal && (
                <div className="fixed inset-0 flex items-center justify-center bg-gray-800 bg-opacity-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                        <h2 className="text-xl font-bold mb-4">Upload Quiz Link</h2>
                        <input
                            type="text"
                            value={quizLinkInput}
                            onChange={(e) => setQuizLinkInput(e.target.value)}
                            className="w-full p-2 border rounded mb-4"
                            placeholder="Enter Quiz link"
                        />
                        <div className="flex justify-end gap-2">
                            <button onClick={handleSubmitQuizLink} className="px-4 py-2 bg-green-500 text-white rounded">
                                Submit
                            </button>
                            <button
                                onClick={() => {
                                    setShowQuizLinkModal(false);
                                    setQuizLinkInput("");
                                    setSelectedModuleName(null);
                                }}
                                className="px-4 py-2 bg-gray-400 text-white rounded"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Quiz Modal for generated quiz output */}
            {showQuizModal && (
                <div className="fixed inset-0 flex items-center justify-center bg-gray-800 bg-opacity-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-2xl">
                        <h2 className="text-xl font-bold mb-4">Generated Quiz</h2>
                        <textarea
                            className="w-full p-2 border rounded mb-4"
                            rows={10}
                            value={quizOutput}
                            readOnly
                        />
                        <div className="flex justify-end gap-2">
                            <button
                                onClick={async () => {
                                    try {
                                        await navigator.clipboard.writeText(quizOutput);
                                        alert("Quiz text copied to clipboard.");
                                    } catch (err) {
                                        alert("Failed to copy quiz text.");
                                    }
                                }}
                                className="px-4 py-2 bg-blue-500 text-white rounded"
                            >
                                Copy Quiz
                            </button>
                            <button
                                onClick={() => setShowQuizModal(false)}
                                className="px-4 py-2 bg-gray-400 text-white rounded"
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
