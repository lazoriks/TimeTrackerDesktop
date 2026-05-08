# Time Tracker Desktop

A JavaFX desktop application for tracking employee work hours. Supports two roles: **regular users** who check in/out and view their session history, and **super users** who manage all accounts and generate reports.

---

## Features

### Regular User
- Register a new account (username, email, password)
- Log in and log out securely
- **Check In** — records the start of a work session
- **Check Out** — records the end of a session and calculates duration
- Dashboard shows all personal sessions in reverse chronological order with Check In time, Check Out time, and duration
- Active (unclosed) sessions are highlighted and can be checked out directly from the table

### Super User
- Full **user management** — create, edit, and delete accounts
- **Report page** — filter work sessions by date range and/or user
- Export reports to **CSV**, **Excel (.xlsx)**, and **PDF**

---

## Technology Stack

| Layer | Technology |
|---|---|
| UI | JavaFX 21 |
| Database | SQLite (embedded, no server required) |
| Excel export | Apache POI 5.2.5 |
| PDF export | OpenPDF 1.3.30 (LGPL) |
| Build | Maven 3.x |
| Java | Java 17+ |

---

## Requirements

- **Java 17** or newer ([Download](https://adoptium.net/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))

No database installation is needed — SQLite creates a local file automatically on first launch.

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/TimeTrackerDesktop.git
cd TimeTrackerDesktop
```

### 2. Run the application

```bash
mvn javafx:run
```

Maven will download all dependencies on the first run. The database file `timetracker.db` is created in the project root directory.

### 3. Default super user credentials

| Field | Value |
|---|---|
| Username | `admin` |
| Password | `admin` |

> Change the admin password after first login via **User Management → Edit**.

---

## Project Structure

```
TimeTrackerDesktop/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/timetracker/
                ├── Main.java                    # Application entry point
                ├── App.java                     # JavaFX Application, screen navigation
                ├── model/
                │   ├── User.java                # User entity (JavaFX properties)
                │   └── HoursRecord.java         # Work session entity
                ├── db/
                │   └── DatabaseManager.java     # All SQLite queries
                ├── ui/
                │   ├── LoginScreen.java          # Login page
                │   ├── RegistrationScreen.java   # Self-registration page
                │   ├── UserManagementScreen.java # Super user — user CRUD
                │   ├── ReportScreen.java         # Super user — reports & export
                │   └── TimeTrackingScreen.java   # Regular user — dashboard
                └── export/
                    ├── CsvExporter.java
                    ├── ExcelExporter.java
                    └── PdfExporter.java
```

---

## Database Schema

### `Users`

| Column | Type | Constraints |
|---|---|---|
| User | VARCHAR(150) | PRIMARY KEY |
| Password | VARCHAR(50) | NOT NULL |
| SuperUser | BOOLEAN | NOT NULL, DEFAULT 0 |
| Email | VARCHAR(150) | |
| Mobile | VARCHAR(15) | |

### `Hours`

| Column | Type | Constraints |
|---|---|---|
| Id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| User | VARCHAR(150) | FK → Users(User) ON UPDATE CASCADE ON DELETE CASCADE |
| ComeIn | DATETIME | |
| ComeOut | DATETIME | nullable while session is active |
| Hour | DECIMAL(10,2) | calculated on check-out |

---

## Usage

### Regular user flow

1. Open the app and click **"Don't have an account? Register here"** to create an account, or log in if you already have one.
2. On the dashboard, press **Check In** to start a session — the button turns grey while a session is active.
3. Press **Check Out** in the session row to close the session; duration is calculated automatically.
4. All sessions are listed in reverse chronological order below the action bar.

### Super user flow

1. Log in with a super user account (`admin` / `admin` by default).
2. The **User Management** screen lists all accounts — use **Add**, **Edit**, and **Delete** to manage them.
3. Click **Report** to open the report page:
   - Choose a start and end date (filtered by Check In date).
   - Optionally select a specific user, or leave blank for all users.
   - Click **Create Report** to load the data.
   - Export with **Upload to CSV**, **Upload to Excel**, or **Upload to PDF**.

---

## Build for Distribution

To produce a standalone fat JAR (dependencies bundled):

```bash
mvn package -Pshade
```

> **Note:** JavaFX native libraries are platform-specific and are not included in a plain fat JAR. For a fully self-contained installer use [jpackage](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jpackage.html) (bundled with JDK 17+):
>
> ```bash
> mvn javafx:jlink   # creates a custom runtime image in target/
> ```

---

## License

MIT License — see [LICENSE](LICENSE) for details.
