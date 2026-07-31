<?php

// =======================================
// LifeDrop Database Connection
// File: backend/config/db_connect.php
// =======================================

// Database Information

$host = "localhost";
$username = "root";
$password = "";
$database = "blood_donation";

// Create Connection

$conn = new mysqli(
    $host,
    $username,
    $password,
    $database
);

// Check Connection

if ($conn->connect_error) {

    die("Database Connection Failed: " . $conn->connect_error);

}

// Set Character Encoding

$conn->set_charset("utf8");

?>