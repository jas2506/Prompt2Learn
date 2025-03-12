# Prompt2Learn

A web-based application designed to enhance the teaching and learning experience using AI-driven features such as transcription, summarization, automated quiz generation, performance analytics, and a context-aware RAG (Retrieval-Augmented Generation) chatbot.

## Table of Contents

1. [Overview](#overview) 
2. [Features](#features) 
3. [Project Structure](#project-structure) 
4. [Tech Stack](#tech-stack) 
5. [Getting Started](#getting-started) 
6. [Usage](#usage) 

---

## Overview

**Prompt2Learn** aims to provide:

- **Teachers** an easy way to upload lecture materials (audio, PPTs), generate summaries, create automated quizzes, and review student performance analytics. 
- **Students** quick access to lecture summaries, quizzes, personalized feedback, and a contextual chatbot to resolve doubts using past lecture content.

This repository contains code for both the **backend** (Spring Boot) and **frontend** (React), along with a **Python-based Flask server** dedicated to handling RAG chatbot interactions and content summarisation.

---

## Features

- **Lecture Upload**: Upload PPTs or audio files for AI-driven transcription and summarization. 
- **Automated Quiz Generation**: Create quizzes based on uploaded materials for instant student assessments. 
- **Performance Analytics**: Gain insights into student performance, identifying strengths and weaknesses. 
- **RAG Chatbot**: A context-aware chatbot that references previously uploaded content to answer student queries accurately. 
- **Teacher Insights**: Data-driven reports that highlight areas where students need additional support. 
- **Flask Server for AI Services**: A dedicated Python-based Flask server running the RAG chatbot and summarisation services.


## Project Structure
```
├── Portal
│   └── Portal # Spring Boot backend source code
├── portalfront
│   └── src # React frontend source code
├── flask_server # Flask server for RAG chatbot and summarisation
├── prompt2LearnIdea.pdf # Concept/idea document (not required for build)
└── README.md # Project README (this file)
```


---
## Tech Stack

- **Frontend**: React 
- **Backend**: Spring Boot (Java) 
- **Database**: MongoDB (or any other database you configure) 
- **AI/ML Components & Server**: 
  - Python-based Flask server that runs the RAG chatbot and content summarisation services. 
  - AI/ML libraries and APIs for transcription, summarization, and quiz generation.

---

## Getting Started

### Prerequisites

1. **Java 20+** installed (for Spring Boot) 
2. **Node.js (v14+ recommended)** and npm or yarn (for React) 
3. **Python 3.9+** (for the Flask server) 
4. **MongoDB** or another database (if not using an in-memory DB)

### Installation

1. **Clone the repository**
    ```bash
    git clone [https://github.com/jas2506/Prompt2Learn]
    cd Prompt2Learn
    ```

2. **Backend Setup (Spring Boot)**

    - Navigate to the backend folder (e.g., `Portal/Portal`).
    - Ensure you have the required dependencies (Maven or Gradle).
    - Configure your database settings in the application properties or YAML file.
    - Build the project:

    ```bash
    mvn clean install
    ```

    - Run the application:

    ```bash
    mvn spring-boot:run
    ```

    - The backend should be accessible on a configured port.

3. **Frontend Setup (React)**

    - Navigate to the `portalfront` folder.
    - Install dependencies:

    ```bash
    npm install
    ```

    - Start the development server:

    ```bash
    npm run dev
    ```


4. **Flask Server Setup (Python)**

    - Navigate to the project root where flask server is located.
    - Set up a virtual environment and install required packages:


    - Run the Flask server:

    ```bash
    python groq_test.py
    python rag_api.py
    ```

    - The Flask server should be running on its default port handling RAG chatbot and summarisation requests.

## Usage

1. **Upload Lecture Materials**: Teachers log in to upload PPTs or audio files via the backend interface, triggering AI-based transcription and summarisation.
2. **Generate Summaries & Quizzes**: Once materials are processed, the system auto-generates summaries and quiz questions.
3. **Student Dashboard**: Students access summarized content, take quizzes, and monitor their performance through the React-based frontend.
4. **RAG Chatbot & Summarisation**: The Python-based Flask server handles requests for the RAG chatbot, providing context-aware answers and additional content summarisation.

