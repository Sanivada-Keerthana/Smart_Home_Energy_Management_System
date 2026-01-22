🏠 Smart Home Energy Management System (SHEMS)

📌 Project Overview

The Smart Home Energy Management System (SHEMS) is a Java-based backend application built using Spring Boot.
The project focuses on secure user authentication and role-based smart device management as the foundation of an intelligent smart home system.

Currently, two core modules have been implemented and pushed to GitHub using separate branches for clear development tracking.


🧩 Implemented Modules

🔐 Module 1: Authentication & User Access

This module provides secure access control to the system using role-based authentication.

Features:

* User registration and login
* Role-based access control (Owner, Family Member, Guest)
* Secure session handling
* Prevention of unauthorized access

📌 Branch: `module1_authentication`
This branch contains only Module 1 implementation with authentication and authorization logic.

⚡ Module 2: Smart Device Management

This module manages smart home devices and enforces permissions based on user roles.

Features:

* Device registration
* Device ON/OFF control
* Device status monitoring
* Role-based permissions:
  * Owner: Add, Update, Remove, Toggle devices
  * Family Member: View and Toggle devices
  * Guest: View devices only
* Backend validation for all device actions

📌 Branch: `module2_devicemanagement`
This branch contains Module 1 + Module 2, including updated backend logic and dashboard integration.


🏗 Architecture

The project follows a layered architecture:

* Controller Layer
* Service Layer
* Repository Layer
* Model / Entity Layer

This ensures clean separation of concerns and scalability.


🛠 Technology Stack

* Java
* Spring Boot
* Spring MVC
* Spring Data JPA (Hibernate)
* MySQL
* Thymeleaf
* Git & GitHub


🗂 Branch Structure

main
├── module1_authentication
│   └── Authentication & Role Management
│
└── module2_devicemanagement
    └── Authentication + Smart Device Management


🔮 Future Work

* Energy consumption analytics
* Automation rules
* Alerts and notifications
* Advanced logging and monitoring
* Mobile app integration
