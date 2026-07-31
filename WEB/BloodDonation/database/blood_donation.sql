-- ===========================================
-- LifeDrop Blood Donation Database
-- ===========================================

CREATE DATABASE IF NOT EXISTS blood_donation;

USE blood_donation;

-- ===========================================
-- Create Donors Table
-- ===========================================

CREATE TABLE IF NOT EXISTS donors (

    id INT AUTO_INCREMENT PRIMARY KEY,

    full_name VARCHAR(100) NOT NULL,

    age INT NOT NULL,

    gender ENUM('Male','Female','Other') NOT NULL,

    blood_group ENUM(
        'A+',
        'A-',
        'B+',
        'B-',
        'AB+',
        'AB-',
        'O+',
        'O-'
    ) NOT NULL,

    phone VARCHAR(20) NOT NULL UNIQUE,

    email VARCHAR(100) NOT NULL UNIQUE,

    address VARCHAR(255) NOT NULL,

    last_donation DATE NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);

-- ===========================================
-- Default Donors
-- ===========================================

INSERT INTO donors
(
    full_name,
    age,
    gender,
    blood_group,
    phone,
    email,
    address,
    last_donation
)

VALUES

(
    'Rahim Ahmed',
    24,
    'Male',
    'A+',
    '01711111111',
    'rahim@gmail.com',
    'Dhaka',
    '2026-03-10'
),

(
    'Nusrat Jahan',
    22,
    'Female',
    'O-',
    '01822222222',
    'nusrat@gmail.com',
    'Chattogram',
    '2026-01-18'
),

(
    'Karim Hasan',
    30,
    'Male',
    'B+',
    '01933333333',
    'karim@gmail.com',
    'Khulna',
    '2026-04-22'
);
