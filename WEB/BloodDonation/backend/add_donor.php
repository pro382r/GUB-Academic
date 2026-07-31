<?php

require_once "config/db_connect.php";

// Check Request Method

if ($_SERVER["REQUEST_METHOD"] != "POST") {
    die("Invalid Request!");
}

// Receive Form Data

$full_name = trim($_POST["full_name"]);
$age = intval($_POST["age"]);
$gender = trim($_POST["gender"]);
$blood_group = trim($_POST["blood_group"]);
$phone = trim($_POST["phone"]);
$email = trim($_POST["email"]);
$address = trim($_POST["address"]);
$last_donation = $_POST["last_donation"];

// Insert Query

$sql = "INSERT INTO donors
(full_name, age, gender, blood_group, phone, email, address, last_donation)
VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

$stmt = $conn->prepare($sql);

$stmt->bind_param(
    "sissssss",
    $full_name,
    $age,
    $gender,
    $blood_group,
    $phone,
    $email,
    $address,
    $last_donation
);

// Execute

if ($stmt->execute()) {

    echo "<script>
        alert('Donor Added Successfully!');
        window.location.href='../frontend/donor-add.html';
    </script>";

} else {

    echo "<script>
        alert('Error: Unable to Add Donor!');
        window.history.back();
    </script>";

}

$stmt->close();
$conn->close();

?>
