# MotorPH Payroll System 💼

A Java Swing desktop payroll and HR management system with role-based access for MotorPH operations.

## 📌 About the Application

The MotorPH Payroll System is a desktop application for managing employee records, attendance, leave requests, payroll processing, and system users.

- **Purpose:** Centralize HR and payroll workflows in one system.
- **Problem it solves:** Reduces manual tracking of attendance, leave, and salary computation.
- **Intended users:** HR staff, Finance staff, IT admins, and employees.

## 🚀 Features

- **Employee Management**
	- Add, edit, view, and remove employee records with validation.
- **Daily Time Record (DTR)**
	- View employee attendance logs, time-in/time-out, and summary metrics.
- **Payroll Management**
	- Approve attendance cycles and generate payroll records per pay period.
- **Leave Management**
	- Submit, approve, or decline leave requests with status tracking.
- **User Management**
	- Manage login accounts and role assignments for system access.
- **Payslip Viewing**
	- Employees view personal payslips; Finance can view employee payslips.

## 🔐 Test Login Accounts

The system is seeded with these usernames in `resources/users.csv`:

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `password` |
| HR | `hr` | `password` |
| Finance | `finance` | `password` |
| IT | `it` | `password` |
| Employee | `employee` | `password` |

> **Note:** Credentials are stored using PBKDF2 hashes for security. These accounts are for testing/demo use only.

## 🧑‍💼 System Roles

- **Admin**
	- Full access to all modules (DTR, Leave, Payroll, Employees, Users, Payslips).
- **HR**
	- Access to employee management, leave management, plus personal modules.
- **Finance**
	- Access to payroll processing, employee payslips, plus personal modules.
- **IT**
	- Access to user management, plus personal modules.
- **Employee**
	- Access to personal DTR, leave application, and own payslip.


## ⚙️ How to Run the Application

1. **Clone or download** this repository.
2. Open the project in your IDE (e.g., IntelliJ or VS Code with Java support).
3. Ensure **JDK 21** is installed and configured.
4. Build/compile the project (Maven or IDE build).
5. Run the main class: `com.group.motorph.Main`.
6. Sign in using one of the test accounts above (e.g., username `admin`, password `password`).

## 📋 Application Module Overview

- **Login Screen**
	- Authenticates users and routes them to role-based navigation.
- **Dashboard / Main Navigation**
	- Shows modules based on user role permissions.
- **Employee Management (HR/Admin)**
	- Manage employee profiles, IDs, and compensation details.
- **Leave Application / Leave Management**
	- Employees submit requests; HR/Admin reviews and updates status.
- **Payroll / Payslips (Finance/Admin)**
	- Approve attendance, process payroll, and review generated payslips.
- **User Management (IT/Admin)**
	- Create, edit, and delete user accounts with role assignment.
- **Daily Time Record**
	- View attendance entries and computed summaries (late, overtime, worked time).

## 💻 Technologies Used

- **Java 21**
- **Java Swing** (desktop GUI)
- **Maven** (project build configuration)
- **CSV/TSV file-based storage** (`resources/*.csv`, `resources/*.tsv`)
- **PBKDF2 password hashing** for account security

## 📖 Project Purpose

This project is primarily intended for:

- **Academic coursework / OOP practice**
- **Learning desktop application architecture**
- **Payroll and HR system prototyping**