# Smart Product Analyzer

![React](https://img.shields.io/badge/React-18-blue.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen) ![Java](https://img.shields.io/badge/Java-21-orange.svg) ![Docker](https://img.shields.io/badge/Docker-Compose-blue)

The **Smart Product Analyzer** is a full-stack web application that leverages AI to transform natural language queries into detailed e-commerce product analyses. It can analyze products directly from an Amazon URL or intelligently search for a product based on a description, scrape the results, and deliver a comprehensive summary including pros, cons, a final verdict, and a rating.

This project demonstrates a powerful integration of a modern frontend, a robust backend, and AI-driven web scraping to create a smart, automated product intelligence tool.


https://github.com/user-attachments/assets/5da38ea3-7b38-4acc-827a-b8143778c155



---

## 🚀 Core Features

*   **Modern & Responsive UI**: A clean user interface built with React, TypeScript, and Shadcn UI.
*   **Secure Authentication**: Supports both email/password registration and OAuth2 login (Google & GitHub).
*   **Dual-Mode Analysis**: Analyze a product either by providing a direct **URL** or a natural language **text description**.
*   **AI-Powered Search**: Uses an AI model (Ollama `llama3`) to parse text descriptions into structured search criteria.
*   **Automated Web Scraping**: Employs Selenium WebDriver in a Docker container to scrape Amazon for product details and reviews.
*   **Intelligent Review Summarization**: Gathers reviews and uses an AI prompt to generate a concise list of **Pros**, **Cons**, a **Verdict**, and a **Rating**.
*   **Containerized**: The entire application stack is containerized with Docker Compose for easy setup and consistent deployment.

---

## 🛠️ Tech Stack

| Component | Technology / Library |
| --- | --- |
| **Frontend** | React, Vite, TypeScript, Tailwind CSS, Shadcn UI |
| **Backend** | Spring Boot, Java 21, Spring Security, Spring Data JPA, Spring AI |
| **Database** | MySQL 8.0 |
| **AI & Scraping** | Ollama (`llama3`), Selenium WebDriver |
| **Authentication** | JWT, OAuth2 (Google & GitHub) |
| **Containerization** | Docker, Docker Compose |
| **Build Tool** | Maven |

---

## 🚀 Getting Started

This project is fully containerized, so you only need Git and Docker to get it running. No local Java, Node, or MySQL installation is required.

### Prerequisites

*   **Git**
*   **Docker Desktop**

### Installation & Setup

1.  **Clone the Repository**

    ```bash
    git clone https://github.com/Saucyyy8/Smart-Product-Analyzer.git
    cd Smart-Product-Analyzer
    ```

2.  **Create the Environment File**

    Create a `.env` file by copying the example file. This file will store all your secrets.

    ```bash
    cp .env.example .env
    ```

3.  **Configure Your Secrets**

    Open the newly created `.env` file and fill in your actual secrets for the database password, JWT, and OAuth2 credentials. See the **Environment Variables** section below for details.

4.  **Build and Run with Docker Compose**

    This single command will build the custom images for the frontend, backend, and Ollama, and start all the services.

    ```bash
    docker-compose up --build -d
    ```

    > **Note:** The first build will take several minutes as it downloads the `llama3` model (a few gigabytes) into the Ollama service image.

5.  **Access the Application**

    Once all containers are running, the application will be available at:

    *   **Frontend:** [http://localhost:3000](http://localhost:3000)
    *   **Backend API:** [http://localhost:8080](http://localhost:8080)

---

## 🔑 Environment Variables

Your `.env` file is crucial for running the application. It must contain the following variables:

| Variable | Description |
| --- | --- |
| `SPRING_DATASOURCE_USERNAME` | The username for the MySQL database. Defaults to `root`. |
| `SPRING_DATASOURCE_PASSWORD` | The password for the MySQL database. **Must match `MYSQL_ROOT_PASSWORD`**. |
| `JWT_SECRET` | A long, random, Base64-encoded string used for signing JWT tokens. |
| `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID` | Your Google OAuth2 Client ID. |
| `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET` | Your Google OAuth2 Client Secret. |
| `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENT_ID` | Your GitHub OAuth2 Client ID. |
| `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENT_SECRET` | Your GitHub OAuth2 Client Secret. |
| `MYSQL_ROOT_PASSWORD` | The root password for the MySQL container. **Must match `SPRING_DATASOURCE_PASSWORD`**. |

---

## 🐳 Docker Services

The `docker-compose.yml` file orchestrates the following 5 services:

1.  `frontend`: Serves the React application using Nginx.
2.  `backend`: Runs the Spring Boot application.
3.  `db`: A MySQL database instance for data persistence.
4.  `browser`: A standalone Selenium Chrome container for web scraping, accessible by the backend.
5.  `ollama`: A custom Ollama service that comes with the `llama3` model pre-installed.

---

## 🔒 Security: JWT & OAuth2

Authentication is handled by Spring Security.

*   **Local Authentication**: Users can register with an email and password. Upon successful login, the backend issues a JWT.
*   **OAuth2 Authentication**: Users can log in via Google or GitHub. After a successful OAuth2 flow, the backend creates a user record and issues a JWT.
*   **JWT Usage**: The frontend stores the JWT and sends it in the `Authorization` header for all subsequent requests to secure API endpoints.

---

## 🛑 Stopping the Application

To stop all running containers and remove the network, run:

```bash
docker-compose down
```

To also delete the database and Ollama model data (for a complete reset), add the `--volumes` flag:

```bash
docker-compose down --volumes
```

---

## 📝 License

This project is licensed under the MIT License.
