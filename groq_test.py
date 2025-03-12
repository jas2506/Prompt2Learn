from flask import Flask, request, jsonify
from langchain_groq import ChatGroq

app = Flask(__name__)

# Setup API Key
groq_api_key = ""

# Initialize Groq LLM
llm_groq = ChatGroq(
    groq_api_key=groq_api_key,
    model_name="llama3-70b-8192",
    temperature=0.2
)

@app.route('/chat', methods=['POST'])
def chat():
    data = request.get_json()
    user_input = data.get("message", "")

    if not user_input:
        return jsonify({"error": "Message cannot be empty"}), 400

    response = llm_groq.invoke(user_input)
    return jsonify({"response": response.content})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
