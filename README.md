# Smart Residential Energy Management System (SREMS) 🏠 

A **Java Spring Boot–based backend system** designed to manage energy usage, user access, and smart devices in a residential environment.
SREMS focuses on **secure authentication**, **role-based access**, and **controlled device operations**, forming a strong foundation for future energy optimization and automation.

---

## Project Overview

The **Smart Residential Energy Management System (SREMS)** enables residents and administrators to:

* Securely access the system using role-based authentication
* Manage smart electrical devices within a residence
* Control who can view or operate devices
* Establish a scalable backend for energy monitoring and automation

This project is structured in **incremental modules**, where each module builds upon the previous one.

---

## 🔐 Module 1 — Authentication & Access Control

**Branch:** `module1_authentication`

This module handles secure user access and authorization.

### Features

✔ User registration and login
✔ Secure authentication using Spring Security
✔ Role-based access control

### User Roles

* **Resident (Owner)**
* **Family Member**
* **Guest**

✔ Session management
✔ Protection against unauthorized access

---

## ⚡ Module 2 — Smart Device Management

**Branch:** `module2_devicemanagement`

Extends Module 1 by introducing smart device control within a residential setup.

### Features

✔ Register and manage residential electrical devices
✔ Turn devices ON / OFF
✔ View device status in real time
✔ Backend-enforced role permissions

### Role-Based Device Permissions

| Role             | View Devices | Control Devices | Add / Remove Devices |
| ---------------- | ------------ | --------------- | -------------------- |
| Resident (Owner) | ✅            | ✅               | ✅                    |
| Family Member    | ✅            | ✅               | ❌                    |
| Guest            | ✅            | ❌               | ❌                    |

✔ Centralized validation of all device operations

---

## 🧱 System Architecture

SREMS follows a **layered backend architecture**:

* **Controller Layer** – Handles REST/HTTP requests
* **Service Layer** – Business logic & validations
* **Repository Layer** – Database interaction
* **Entity/Model Layer** – Core domain objects

This structure improves **maintainability**, **scalability**, and **security**.

---

## 🛠 Technology Stack

| Category        | Technology      |
| --------------- | --------------- |
| Language        | Java            |
| Framework       | Spring Boot     |
| Security        | Spring Security |
| MVC             | Spring MVC      |
| ORM             | Hibernate / JPA |
| Database        | MySQL           |
| Template Engine | Thymeleaf       |
| Build Tool      | Maven           |
| Version Control | Git & GitHub    |

---

## 🌿 Branch Structure

```
main
├── module1_authentication
│   └── User authentication & role management
└── module2_devicemanagement
    └── Authentication + residential device management
```

---

## 🚀 Getting Started

### Prerequisites

* Java JDK 11 or higher
* Maven
* MySQL
* Git

### Installation Steps

1. Clone the repository:

   ```bash
   git clone https://github.com/Sanivada-Keerthana/Smart_Home_Energy_Management_System.git
   ```

2. Switch to the required module:

   ```bash
   git checkout module2_devicemanagement
   ```

3. Configure database credentials in `application.properties`.

4. Run the application:

   ```bash
   mvn spring-boot:run
   ```

---

## 🔮 Future Enhancements

Planned features for upcoming modules:

✨ Real-time energy consumption tracking
✨ Energy usage analytics & reports
✨ Peak-hour alerts and notifications
✨ Device scheduling and automation rules
✨ Admin dashboards for residential monitoring

---

## 📄 License

This project is licensed under the **MIT License**.

---

## 📘 About the Project

The **Smart Residential Energy Management System (SREMS)** is built to simulate a real-world residential energy platform, emphasizing **security**, **access control**, and **structured backend design**—making it suitable for academic projects and scalable enterprise systems.
