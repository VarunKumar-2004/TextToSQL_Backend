# 🤖 Text-to-SQL AI Database Assistant

> An AI-powered full-stack application that allows users to upload SQLite databases and interact with their data using natural language.

---

## 📌 Overview

**Text-to-SQL AI Database Assistant** converts natural-language questions into SQL queries and executes them against an uploaded SQLite database.

Instead of manually writing SQL, users can ask questions such as:

> **"Which customers have made the highest total payments?"**

The application understands the database schema, generates SQL using an LLM, validates and executes the query, automatically attempts correction when execution fails, and presents the results through tables and visualizations.

---

## ✨ Features

- 🗣️ Natural-language database querying
- 📂 SQLite database upload
- 🧠 Schema-aware SQL generation
- 🤖 LLM integration using Spring AI
- 🔐 JWT authentication
- 💬 Session-based conversations
- ✅ SQL validation before execution
- 🔄 Automatic SQL correction and retry
- 🗄️ Temporary runtime management of uploaded databases
- 📄 Backend pagination for query results
- 📊 Tabular data visualization
- 📈 Line, bar, and pie chart suggestions
- 🧩 ER diagram support
- 🔌 RESTful backend APIs
- 💾 PostgreSQL persistence for application data

---

# 🏗️ System Architecture

```mermaid
flowchart LR
    U[User] --> FE[React.js Frontend]
    FE --> AUTH[JWT Authentication]
    FE --> API[Spring Boot REST API]

    API --> SESSION[Chat Session Service]
    API --> MSG[Chat Message Service]
    API --> SCHEMA[Schema Service]
    API --> RESULT[Result Pagination API]

    SESSION --> PG[(PostgreSQL)]
    MSG --> PG

    SESSION --> DBM[Database Session Manager]
    DBM --> SQLITE[(SQLite Database)]

    MSG --> AI[AI Query Planner]
    AI --> LLM[LLM via Spring AI]

    AI --> VALID[SQL Validation]
    VALID --> EXEC[SQLite Query Executor]
    EXEC --> SQLITE

    EXEC --> CORRECT[SQL Correction Service]
    CORRECT --> LLM

    API --> FE
```

---

# 🧩 Backend Architecture

The backend follows a layered architecture where each layer has a specific responsibility.

```text
src/main/java/com/project/TextToSQL
│
├── Controller
│   ├── ChatSessionController
│   └── ChatMessageController
│
├── Service
│   ├── ChatMessageService
│   ├── AIQueryPlannerService
│   ├── SQLCorrectionService
│   ├── DatabaseSessionManager
│   └── SchemaMapper
│
├── Repository
│   ├── UserRepository
│   ├── ChatSessionRepository
│   └── ChatMessageRepository
│
├── Entity
│   ├── User
│   ├── ChatSession
│   └── ChatMessage
│
├── DTO
│   ├── ChatSessionsResponse
│   ├── SessionMessagesResponse
│   ├── QueryResultPage
│   └── VisualizationSuggestion
│
├── Security
│   ├── JwtAuthenticationFilter
│   └── SecurityConfig
│
└── Database
    └── SQLiteQueryExecutor
```

### Layer Responsibilities

| Layer | Responsibility |
|---|---|
| **Controller** | Handles HTTP requests and API responses |
| **Service** | Contains application business logic |
| **Repository** | Handles PostgreSQL persistence |
| **Entity** | Represents persistent application data |
| **DTO** | Defines API request and response structures |
| **Security** | JWT authentication and authorization |
| **AI Service** | Question classification and SQL generation |
| **SQLite Executor** | Executes SQL against uploaded databases |
| **Database Session Manager** | Manages runtime database files |
| **Schema Mapper** | Processes database schema information |

---

# 🔄 End-to-End Workflow

```mermaid
sequenceDiagram
    actor User
    participant React
    participant Controller
    participant ChatService
    participant AI as AIQueryPlannerService
    participant LLM
    participant Executor as SQLiteQueryExecutor
    participant DB as SQLite
    participant Corrector as SQLCorrectionService
    participant PostgreSQL

    User->>React: Ask question
    React->>Controller: POST /messages
    Controller->>ChatService: processMessage()

    ChatService->>PostgreSQL: Load session
    ChatService->>AI: Question + Schema Context

    AI->>LLM: Generate Query Plan
    LLM-->>AI: Structured Response
    AI-->>ChatService: Generated SQL

    ChatService->>Executor: Validate & Execute SQL
    Executor->>DB: Execute Query

    alt Query succeeds
        DB-->>Executor: Query Results
        Executor-->>ChatService: Results
    else Query fails
        DB-->>Executor: SQL Error
        Executor-->>ChatService: Execution Error
        ChatService->>Corrector: Correct SQL
        Corrector->>LLM: SQL + Error + Schema
        LLM-->>Corrector: Corrected SQL
        Corrector-->>ChatService: Retry SQL
        ChatService->>Executor: Execute Corrected SQL
        Executor->>DB: Execute Query
        DB-->>Executor: Query Results
        Executor-->>ChatService: Results
    end

    ChatService->>PostgreSQL: Save Message
    ChatService-->>Controller: Response
    Controller-->>React: JSON Response
    React-->>User: Answer + Results + Visualization
```

---

# 🧠 AI Query Processing

The LLM is not directly connected to the database.

The backend controls the complete query lifecycle.

```mermaid
flowchart TD
    A[User Question] --> B[Load Chat Session]
    B --> C[Retrieve Schema Context]
    C --> D[AI Query Planner]

    D --> E{Question Type}

    E -->|DATA_QUERY| F[Generate SQL]
    E -->|METADATA_QUERY| G[Metadata Response]
    E -->|OFF_TOPIC| H[Out-of-Scope Response]

    F --> I[SQL Validation]
    I --> J{Valid SQL?}

    J -->|Yes| K[Execute SQL]
    J -->|No| L[SQL Correction]

    K --> M{Execution Successful?}

    M -->|Yes| N[Query Result]
    M -->|No| L

    L --> O[SQLCorrectionService]
    O --> P[LLM Correction]
    P --> Q{Retry Available?}

    Q -->|Yes| I
    Q -->|No| R[Controlled Error]

    N --> S[Generate Answer]
    N --> T[Visualization Suggestion]

    S --> U[Persist Message]
    T --> U
    U --> V[Return API Response]
```

---

# 🔍 Query Classification

The AI planner classifies questions into three categories.

## `DATA_QUERY`

Questions that require data from the uploaded database.

Example:

```text
Which customers have made the highest total payments?
```

The system generates SQL and executes it.

## `METADATA_QUERY`

Questions about the database structure.

Example:

```text
What tables are available in this database?
```

These questions can be answered using the stored schema context.

## `OFF_TOPIC`

Questions unrelated to the uploaded database.

Example:

```text
What is the weather today?
```

The system avoids unnecessary SQL generation for these questions.

---

# 🧬 Schema-Aware SQL Generation

When a database is uploaded, the backend extracts its schema.

```mermaid
flowchart LR
    A[SQLite Database] --> B[Schema Extraction]
    B --> C[Tables]
    C --> D[Columns]
    D --> E[Data Types]
    E --> F[Relationships]
    F --> G[Schema Context]
    G --> H[ChatSession.schemaContext]
    H --> I[AI Query Planner]
```

The LLM receives information about the actual database structure.

For example:

```text
Database Schema:

customers
    customer_id
    first_name
    last_name

payments
    payment_id
    customer_id
    amount
    payment_date

Relationship:

payments.customer_id -> customers.customer_id
```

This allows the model to generate SQL using the actual tables and columns instead of guessing the database structure.

---

# 🔄 SQL Validation & Self-Correction

AI-generated SQL can fail because of:

- Incorrect column names
- Incorrect table names
- SQL syntax errors
- Incorrect joins
- Database-specific SQL differences
- Incorrect assumptions about the schema

Instead of immediately returning an error, the backend attempts to correct the query.

```text
User Question
      │
      ▼
Generate SQL
      │
      ▼
Validate SQL
      │
      ▼
Execute SQL
      │
      ├─────────────── Success ───────────────► Result
      │
      ▼
    Error
      │
      ▼
SQL Correction Service
      │
      ▼
     LLM
      │
      ▼
Corrected SQL
      │
      ▼
    Retry
```

The correction process is bounded to prevent an uncontrolled retry loop.

---

# 🗄️ Database Architecture

The application uses **PostgreSQL** for application persistence and **SQLite** for the uploaded databases.

```mermaid
flowchart TB
    APP[Spring Boot Application]

    APP --> PG[(PostgreSQL)]
    APP --> SQLITE[(Uploaded SQLite Database)]

    PG --> USERS[Users]
    PG --> SESSIONS[Chat Sessions]
    PG --> MESSAGES[Chat Messages]
    PG --> SCHEMA[Schema Context]

    SQLITE --> TABLES[User Database Tables]
```

### PostgreSQL

Used for application-level data:

- Users
- Chat sessions
- Chat messages
- Schema context
- Query metadata

### SQLite

Used as the database uploaded by the user and queried through natural language.

---

# 📂 Database Session Management

Uploaded SQLite databases are associated with individual chat sessions.

The backend uses a `DatabaseSessionManager` to maintain the runtime relationship between:

```text
Chat Session ID
       │
       ▼
SQLite Database Path
       │
       ▼
SQLiteQueryExecutor
```

```mermaid
flowchart LR
    A[SQLite Upload] --> B[Temporary Database File]
    B --> C[DatabaseSessionManager]

    C --> D[Session ID]
    C --> E[Database Path]
    C --> F[Last Access Time]

    D --> G[Chat Session]
    E --> H[SQLiteQueryExecutor]
    H --> I[Execute SQL]
```

This keeps the application's persistent data separate from the uploaded database being queried.

---

# 🔐 Authentication & Security

The application uses **Spring Security with JWT authentication**.

```mermaid
sequenceDiagram
    participant User
    participant React
    participant SpringSecurity
    participant API

    User->>React: Login
    React->>SpringSecurity: Credentials
    SpringSecurity-->>React: JWT Token

    React->>API: Request + Bearer Token
    API->>SpringSecurity: Validate JWT
    SpringSecurity-->>API: Authenticated User
    API-->>React: Protected Response
```

Protected requests use:

```http
Authorization: Bearer <JWT>
```

The authenticated user is also used when retrieving chat sessions, preventing users from accessing another user's sessions.

---

# 🔌 REST API

## Chat Sessions

### Get User Sessions

```http
GET /api/chat/sessions
```

Returns the authenticated user's chat sessions.

### Upload Database

```http
POST /api/chat/sessions/upload
```

Uploads a SQLite database and creates a chat session.

### Get Session

```http
GET /api/chat/sessions/{sessionId}
```

Retrieves the selected session and its messages.

### Get Schema

```http
GET /api/chat/sessions/{sessionId}/schema
```

Retrieves the database schema associated with the session.

## Chat

### Ask Question

```http
POST /api/chat/sessions/{sessionId}/messages
```

Processes a natural-language question and returns the AI-generated response.

## Query Results

### Get Paginated Results

```http
GET /api/chat/sessions/{sessionId}/messages/{messageId}/results?page=0&pageSize=50
```

Retrieves query results page by page.

---

# 📄 Backend Pagination

The backend does not send an unlimited number of database rows to the frontend.

Instead, query results are retrieved in pages.

```mermaid
sequenceDiagram
    participant React
    participant API
    participant SQLite

    React->>API: Execute Question
    API->>SQLite: Execute SQL
    SQLite-->>API: Query Results
    API-->>React: Initial Response

    React->>API: page=1, pageSize=50
    API->>SQLite: Fetch Page
    SQLite-->>API: 50 Records
    API-->>React: Page 1

    React->>API: page=2, pageSize=50
    API->>SQLite: Fetch Page
    SQLite-->>API: 50 Records
    API-->>React: Page 2
```

This reduces:

- API response size
- Browser memory usage
- Rendering overhead
- Initial response payload

---

# 📊 Visualization Workflow

The backend can return visualization suggestions along with query results.

Example:

```json
{
  "type": "LINE_CHART",
  "xAxis": "payment_date",
  "yAxis": "total_amount",
  "title": "Payments Over Time"
}
```

The React frontend uses the suggestion to render the appropriate visualization.

```mermaid
flowchart LR
    A[SQL Result] --> B[Visualization Suggestion]
    B --> C{Visualization Type}

    C --> D[Table]
    C --> E[Line Chart]
    C --> F[Bar Chart]
    C --> G[Pie Chart]
    C --> H[ER Diagram]
```

---

# ⚛️ Frontend Architecture

```text
React Application
│
├── Authentication
│   └── JWT Token Handling
│
├── Chat Context
│   ├── Sessions
│   ├── Selected Session
│   ├── Loading State
│   └── Messages
│
├── Session UI
│   ├── New Session
│   ├── Session History
│   └── Active Session
│
├── Chat UI
│   ├── MessageList
│   ├── UserMessage
│   └── AIMessage
│
├── Results
│   └── ResultTable
│
└── Visualizations
    ├── Charts
    └── ER Diagram
```

The frontend uses **React Context API** to manage session and chat state without excessive prop drilling.

---

# 🔄 Frontend Session Flow

```mermaid
flowchart TD
    A[Application Load] --> B[Load User Sessions]
    B --> C{Sessions Exist?}

    C -->|No| D[Show Get Started]
    C -->|Yes| E[Select Session]

    D --> F[Upload SQLite Database]
    F --> G[Create Chat Session]

    E --> H[Fetch Session Messages]

    G --> I[Open Chat]
    H --> I

    I --> J[Ask Question]
    J --> K[Backend API]
    K --> L[AI Response]

    L --> M[Render Answer]
    M --> N[Render Table]
    M --> O[Render Visualization]
```

---

# 🗃️ Data Model

```mermaid
erDiagram
    USER ||--o{ CHAT_SESSION : owns
    CHAT_SESSION ||--o{ CHAT_MESSAGE : contains

    USER {
        UUID id PK
        string username
        string email
        string password
    }

    CHAT_SESSION {
        UUID id PK
        UUID user_id FK
        string session_name
        text schema_context
        datetime created_at
    }

    CHAT_MESSAGE {
        UUID id PK
        UUID session_id FK
        text user_prompt
        text generated_sql
        text answer
        text query_result
        text visualization_suggestions
        datetime created_at
    }
```

---

# 🧠 Engineering Decisions

## 1. Schema-Aware AI

The LLM receives the actual database schema rather than generating SQL blindly.

```text
Question
    +
Schema Context
    ↓
   LLM
    ↓
Generated SQL
```

This provides the model with the information required to reason about tables, columns, data types, and relationships.

## 2. AI Does Not Directly Execute SQL

The LLM generates SQL, but the backend controls execution.

```text
LLM
 ↓
Generated SQL
 ↓
Validation
 ↓
Execution
 ↓
Error Handling
 ↓
Correction / Retry
 ↓
Result
```

## 3. Bounded SQL Correction

A failed query can trigger an automated correction process.

```text
Generated SQL
      ↓
Execution
      ↓
    Error
      ↓
Correction
      ↓
Retry
```

The retry count is bounded to avoid uncontrolled loops.

## 4. Session-Based Architecture

Each uploaded database belongs to a chat session.

A session maintains:

```text
Session
├── Database Reference
├── Schema Context
├── Chat Messages
├── Generated SQL
├── Query Results
└── Visualization Metadata
```

This allows users to ask follow-up questions while maintaining the same database context.

## 5. Backend Pagination

Instead of sending potentially thousands of rows to the frontend:

```text
Database
   ↓
Backend
   ↓
Paginated API
   ↓
React
```

The frontend requests additional pages when required.

---

# 🛠️ Technology Stack

### Frontend

- React.js
- React Router
- Context API
- Axios
- Tailwind CSS
- Data visualization components

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring AI
- REST APIs
- JWT
- Maven

### Databases

- PostgreSQL / Supabase
- SQLite

### AI

- Spring AI
- LLM-based SQL generation
- Structured AI responses
- SQL correction workflow

---

# 📁 Project Structure

```text
TextToSQL/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/project/TextToSQL/
│   │   │       ├── Controller/
│   │   │       ├── Service/
│   │   │       ├── Repository/
│   │   │       ├── Entity/
│   │   │       ├── DTO/
│   │   │       ├── Security/
│   │   │       └── ...
│   │   │
│   │   └── resources/
│   │       └── application.yml
│   │
│   └── test/
│
├── pom.xml
├── Dockerfile
└── README.md
```

---

# 💬 Example Questions

The application can answer questions based on the schema of the uploaded database.

### Customer Analysis

```text
Which customers have made the highest total payments?
```

### Revenue Analysis

```text
Show monthly revenue for the last two years.
```

### Product Analysis

```text
Which product category generated the most revenue?
```

### Customer Distribution

```text
Show the distribution of customers by country.
```

### Employee Analysis

```text
Which employees handled the most orders?
```

The available questions depend on the schema of the uploaded database.

---

# 🔒 Security Considerations

The current architecture includes:

- JWT-based authentication
- Spring Security
- Authenticated REST endpoints
- Session ownership checks
- Backend-controlled SQL execution
- SQL validation before execution
- Bounded SQL correction attempts
- Separation between application PostgreSQL data and uploaded SQLite databases

---

# 📈 What This Project Demonstrates

This project combines backend engineering, database systems, frontend development, and AI integration.

### Backend Engineering

- REST API design
- Spring Boot architecture
- JWT authentication
- Authorization
- Service-layer design
- Repository pattern
- Session management
- File handling
- Runtime resource management

### Database Engineering

- PostgreSQL persistence
- SQLite database execution
- Schema extraction
- Dynamic SQL execution
- Query pagination

### AI Engineering

- LLM integration with Spring AI
- Schema-aware prompting
- Structured AI responses
- SQL generation
- SQL validation
- Automatic SQL correction
- Controlled retry workflow

### Frontend Engineering

- React.js
- Context API
- Axios
- Session management
- Chat interface
- Paginated result tables
- Data visualization

---

# 🎯 Core Engineering Principle

> **The LLM generates the query, but the backend owns the execution.**

```mermaid
flowchart LR
    USER[User]
    --> QUESTION[Natural Language Question]

    QUESTION --> LLM[LLM]

    LLM --> SQL[Generated SQL]

    SQL --> BACKEND[Spring Boot Backend]

    BACKEND --> VALIDATE[Validate]

    VALIDATE --> EXECUTE[Execute]

    EXECUTE --> DB[(SQLite)]

    DB --> RESULT[Query Result]

    RESULT --> REACT[React Frontend]

    REACT --> TABLE[Table / Chart]
```

This separation allows the AI layer to focus on **understanding and generating queries**, while the backend remains responsible for **validation, execution, error handling, persistence, security, and result delivery**.

---

# 👨‍💻 Author

**Varun Kumar**

B.Tech — Information Technology

Focused on **Java Backend Development, Spring Boot, Full-Stack Development, and AI-powered applications**.
