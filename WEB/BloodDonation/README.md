# ❤️ LifeDrop - Blood Donation Management System

A simple Blood Donation Management System built using **HTML, CSS, JavaScript, PHP, MySQL, and XAMPP**. This project allows users to add, update, remove, and manage blood donor information.

---

# 📦 Project Contents

After extracting the project, you should have the following files:

```
BloodDonation/
├── frontend/
├── backend/
├── database/
│   └── blood_donation.sql
├── assets/
├── index.html
└── README.md
```

---

# 💻 System Requirements

Before running the project, make sure the following software is installed:

- XAMPP (Apache & MySQL)
- A modern web browser (Chrome, Edge, Firefox, etc.)

---

# 🚀 Installation Guide

## Step 1: Install XAMPP

Download and install XAMPP if it is not already installed.

After installation, open the **XAMPP Control Panel**.

---

## Step 2: Start Apache and MySQL

Open the XAMPP Control Panel and start the following services:

- Apache
- MySQL

Both services should display a green status.

---

## Step 3: Copy the Project

Copy the **BloodDonation** folder into the XAMPP `htdocs` directory.

Example:

```
C:\xampp\htdocs\BloodDonation
```

---

## Step 4: Create the Database

Open your browser and visit:

```
http://localhost/phpmyadmin
```

Click **New** and create a database named:

```
blood_donation
```

The database name must be exactly:

```
blood_donation
```

---

## Step 5: Import the Database

1. Select the **blood_donation** database.
2. Click the **Import** tab.
3. Click **Choose File**.
4. Select:

```
database/blood_donation.sql
```

or the provided **blood_donation.sql** file.

5. Click **Go**.

Wait until phpMyAdmin displays:

```
Import has been successfully finished.
```

The database will now contain all donor records included with the project.

---

## Step 6: Verify Database Connection

Open:

```
backend/config/db_connect.php
```

Ensure the configuration is:

```php
$host = "localhost";
$username = "root";
$password = "";
$database = "blood_donation";
```

If your MySQL username or password is different, update the file accordingly.

---

## Step 7: Run the Project

Open your browser and visit:

```
http://localhost/BloodDonation
```

The project should now be running successfully.

---

# ✨ Features

- Add New Donor
- Update Donor Information
- Remove Donor
- View All Donors
- Search by ID
- Search by Phone Number
- Search by Name
- Multiple Matching Results for Same Name
- Donor Eligibility Status
- Responsive User Interface

---

# 📁 Technologies Used

- HTML5
- CSS3
- JavaScript
- PHP
- MySQL
- XAMPP

---

# 📌 Notes

- Apache and MySQL must be running before opening the project.
- Always import the provided **blood_donation.sql** file before using the system.
- If the database is not imported, the application will not display donor data.
- Any new donors added after importing will be stored in your local MySQL database.

---

# 👨‍💻 Developed By

**Md. Reahoon Zannah**

Developed for educational purposes.