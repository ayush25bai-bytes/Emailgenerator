# AI Email Architect (Java Edition) ✉️

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Groq API](https://img.shields.io/badge/Groq-f55036?style=for-the-badge&logo=groq&logoColor=white)

A high-performance, dependency-free Java CLI tool that leverages Groq's `openai/gpt-oss-120b` LLM to automatically draft highly professional, perfectly structured emails based on minimal user input.

## 🌟 Key Features
- **Pure Object-Oriented Java:** Built entirely with standard Java libraries. No Maven, no external dependencies. Compiles instantly.
- **Interactive Drafting Wizard:** A clean, ANSI-colorized terminal interface that sequentially collects your requirements (Tone, Purpose, CC, BCC).
- **Beautiful Terminal Preview:** Previews the final drafted email natively in the terminal with colored metadata headers.
- **Human-Like Synthesis:** Strictly prompted to avoid robotic "AI-isms" and deliver concise, direct, human-sounding text.
- **Intelligent API Management:** Automatically prompts for, validates, and locally caches your Groq API key in a `.env` file.

## 🛠️ Setup & Execution

Since the project is completely dependency-free, it can be compiled and run instantly on any machine with the JDK installed.

1. **Clone the Repository**
2. **Compile the source code:**
   ```bash
   javac main.java
   ```
3. **Run the generator:**
   ```bash
   java main
   ```
4. *On first run, the CLI will seamlessly prompt you to paste your free Groq API key.*

## 📂 Output Structure
All drafted emails are automatically saved in the `email_drafts/` directory as `.txt` files containing the full layout (FROM, TO, SUBJECT, BODY, SIGNATURE).

## 🧠 Architecture
- **Model:** Uses `openai/gpt-oss-120b` via Groq for ultra-fast generation.
- **Tone-Driven Generation:** Analyzes the requested tone (e.g., Urgent, Casual, Professional) to modify vocabulary and sentence structure.
- **CMD Sanitization:** Actively intercepts and replaces complex Unicode dashes and quotes with safe ASCII characters to ensure flawless rendering on legacy terminals.
