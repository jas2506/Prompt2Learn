from flask import Flask, request, jsonify
import PyPDF2
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.vectorstores import FAISS
from langchain.chains import ConversationalRetrievalChain
from langchain.memory import ChatMessageHistory, ConversationBufferMemory
from langchain.embeddings import HuggingFaceEmbeddings
from langchain_groq import ChatGroq
from docx import Document
import pandas as pd
import io
import logging
import os

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Use updated HuggingFaceEmbeddings (ensure you've installed via: pip install -U langchain-huggingface)
embeddings = HuggingFaceEmbeddings(model_name='all-MiniLM-L6-v2')

global_vector_store = None

# Updated Groq API key
groq_api_key = ''
llm_groq = ChatGroq(
    groq_api_key=groq_api_key,
    model_name="llama3-70b-8192",
    temperature=0.2
)

def process_pdf(file_path):
    with open(file_path, 'rb') as file:
        pdf_reader = PyPDF2.PdfReader(file)
        text = "".join([page.extract_text() for page in pdf_reader.pages])
    return text

def process_docx(file_path):
    doc = Document(file_path)
    return "\n".join([para.text for para in doc.paragraphs])

def process_csv(file_path):
    df = pd.read_csv(file_path)
    buffer = io.StringIO()
    df.info(buf=buffer)
    return buffer.getvalue() + "\n\nData Summary:\n" + df.describe().to_string()

def process_file(file_path):
    try:
        if file_path.lower().endswith('.pdf'):
            return process_pdf(file_path)
        elif file_path.lower().endswith('.docx'):
            return process_docx(file_path)
        elif file_path.lower().endswith('.csv'):
            return process_csv(file_path)
        else:
            raise ValueError(f"Unsupported file type: {file_path}")
    except Exception as e:
        logger.error(f"Error processing file {file_path}: {str(e)}")
        raise

@app.route('/upload', methods=['POST'])
def upload_file():
    global global_vector_store
    files = request.files.getlist('files')
    
    texts = []
    metadatas = []
    
    for file in files:
        file_path = os.path.join("uploads", file.filename)
        file.save(file_path)
        text = process_file(file_path)
        text_splitter = RecursiveCharacterTextSplitter(chunk_size=1200, chunk_overlap=50)
        file_texts = text_splitter.split_text(text)
        texts.extend(file_texts)
        metadatas.extend([{"source": f"{i}-{file.filename}"} for i in range(len(file_texts))])
    
    if texts:
        global_vector_store = FAISS.from_texts(texts, embeddings, metadatas=metadatas)
        return jsonify({"message": "Files processed successfully!", "chunks_added": len(texts)})
    return jsonify({"error": "No valid texts extracted!"}), 400

@app.route('/query', methods=['POST'])
def query():
    global global_vector_store
    if global_vector_store is None:
        return "No files uploaded yet!", 400

    data = request.get_json()
    query_text = data.get("query")
    
    if not query_text:
        return "Query cannot be empty!", 400

    # Configure the chain to return only the answer (avoiding extra keys)
    qa_chain = ConversationalRetrievalChain.from_llm(
        llm=llm_groq,
        chain_type="stuff",
        retriever=global_vector_store.as_retriever(),
        memory=ConversationBufferMemory(memory_key="chat_history", return_messages=True),
        return_source_documents=False
    )
    
    res = qa_chain({"question": query_text})
    answer = res.get("answer", "No answer generated")
    logger.info("Chain result: %s", res)
    
    # Return answer as plain text
    return answer, 200, {'Content-Type': 'text/plain'}

@app.route('/reset', methods=['POST'])
def reset():
    global global_vector_store
    global_vector_store = None
    return "System reset successfully!", 200, {'Content-Type': 'text/plain'}

if __name__ == '__main__':
    if not os.path.exists("uploads"):
        os.makedirs("uploads")
    app.run(host="0.0.0.0", port=5000)
