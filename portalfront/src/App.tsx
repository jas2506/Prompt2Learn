import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import StudentRegistration from "./components/StudentRegistration";
import TeacherRegistration from "./components/TeacherRegistration";
import StudentLogin from "./components/StudentLogin";
import TeacherLogin from "./components/TeacherLogin";
import OuterDashboard from "./components/OuterDashboard";
import StudentDashboard from "./components/StudentDashboard";
import TeacherDashboard from "./components/TeacherDashboard";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<StudentRegistration />} />
                <Route path="/teacher-registration" element={<TeacherRegistration />} />
                <Route path="/student-login" element={<StudentLogin />} />
                <Route path="/teacher-login" element={<TeacherLogin />} />
                <Route path="/outer-dashboard" element={<OuterDashboard />} />
                <Route path="/student-dashboard" element={<StudentDashboard />} />
                <Route path="/teacher-dashboard" element={<TeacherDashboard />} />
            </Routes>
        </Router>
    );
}

export default App;
