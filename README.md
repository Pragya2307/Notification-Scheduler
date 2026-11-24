# 🔔 Notification Scheduler

A Java-based Notification Scheduler developed using Data Structures and Algorithms (DSA). The application allows users to schedule, manage, cancel, and process notifications based on their scheduled date and time.

## 📌 Project Overview

The Notification Scheduler is a console-based application designed to manage time-based reminders efficiently. It processes notifications according to their scheduled time and supports recurring reminders.

## ✨ Features

- Schedule notifications for a specific date and time.
- View pending notifications.
- Cancel scheduled notifications.
- Process notifications that are due.
- Support recurring reminders.
- Save and load pending notifications using file handling.

## 🛠️ Technologies Used

- **Language:** Java
- **Concepts:** Data Structures and Algorithms
- **Storage:** File Handling
- **Interface:** Console-based application

## 🧠 Data Structures Used

| Data Structure | Purpose |
|---|---|
| Priority Queue | Processes notifications according to scheduled time |
| Queue | Maintains FIFO processing of ready notifications |
| Circular Queue | Manages recurring notification entries |

## 📂 Project Structure

```text
Notification-Scheduler/
│
├── Main.java
├── README.md
├── .gitignore
└── Notification_Scheduler_Report.docx
```

## ▶️ How to Run

**Prerequisite:** Java JDK 8 or later.

1. Clone the repository:

   `git clone https://github.com/Pragya2307/Notification-Scheduler.git`

2. Open the project directory:

   `cd Notification-Scheduler`

3. Compile the program:

   `javac Main.java`

4. Run the application:

   `java Main`

## 💡 Working

1. The user enters notification details.
2. The application validates the scheduled date and time.
3. Notifications are stored in a Priority Queue.
4. Due notifications are moved to a ready Queue and processed.
5. Recurring notifications are rescheduled.
6. Pending notifications can be saved for future use.

## 🚀 Future Enhancements

- Graphical User Interface (GUI).
- Email and SMS notification integration.
- Database connectivity.
- Automatic background scheduling.
- User authentication and personalized reminders.

## 👩‍💻 Author

Developed as a Java and Data Structures and Algorithms project.

**GitHub Repository:** [Notification Scheduler](https://github.com/Pragya2307/Notification-Scheduler)