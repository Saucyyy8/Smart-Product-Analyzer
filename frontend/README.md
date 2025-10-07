# Smart Product Analyzer

This project is a web application that helps users analyze products from e-commerce websites. It scrapes product information and provides insights to the user.

## Project Structure

The project is divided into two main parts:

*   `frontend`: A React application built with Vite and TypeScript.
*   `backend`: A Java application built with Spring Boot.

## Getting Started

### Prerequisites

*   Node.js and npm (for the frontend)
*   Java 17 or later and Maven (for the backend)

### Running the Frontend

```sh
# Step 1: Navigate to the frontend directory.
cd frontend

# Step 2: Install the necessary dependencies.
npm i

# Step 3: Start the development server.
npm run dev
```

The frontend will be available at `http://localhost:3000`.

### Running the Backend

```sh
# Step 1: Navigate to the backend directory.
cd backend

# Step 2: Build the project using Maven.
./mvnw clean install

# Step 3: Run the application.
./mvnw spring-boot:run
```

The backend will be available at `http://localhost:8080`.

## Authentication

This application uses JWT (JSON Web Tokens) for authentication. You can sign up for a new account or use the following social logins:

*   Google
*   GitHub

When you sign in, a JWT is generated and stored in your browser. This token is used to authenticate subsequent requests to the backend.

## Technologies Used

### Frontend

*   Vite
*   TypeScript
*   React
*   shadcn-ui
*   Tailwind CSS

### Backend

*   Java
*   Spring Boot
*   Spring Security (with JWT and OAuth2)
*   Maven
