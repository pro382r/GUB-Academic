<?php

header("Content-Type: application/json");

require_once "config/db_connect.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {

    echo json_encode([
        "success" => false,
        "message" => "Invalid request method."
    ]);

    exit();
}

$id = trim($_POST["id"] ?? "");
$full_name = trim($_POST["full_name"] ?? "");
$age = trim($_POST["age"] ?? "");
$gender = trim($_POST["gender"] ?? "");
$blood_group = trim($_POST["blood_group"] ?? "");
$phone = trim($_POST["phone"] ?? "");
$email = trim($_POST["email"] ?? "");
$address = trim($_POST["address"] ?? "");
$last_donation = trim($_POST["last_donation"] ?? "");

if (
    empty($id) ||
    empty($full_name) ||
    empty($age) ||
    empty($gender) ||
    empty($blood_group) ||
    empty($phone) ||
    empty($email) ||
    empty($address) ||
    empty($last_donation)
) {

    echo json_encode([
        "success" => false,
        "message" => "All fields are required."
    ]);

    exit();
}

$sql = "UPDATE donors
        SET
            full_name = ?,
            age = ?,
            gender = ?,
            blood_group = ?,
            phone = ?,
            email = ?,
            address = ?,
            last_donation = ?
        WHERE id = ?";

$stmt = $conn->prepare($sql);

$stmt->bind_param(
    "sissssssi",
    $full_name,
    $age,
    $gender,
    $blood_group,
    $phone,
    $email,
    $address,
    $last_donation,
    $id
);

if ($stmt->execute()) {

    echo json_encode([
        "success" => true,
        "message" => "Donor information updated successfully."
    ]);

} else {

    echo json_encode([
        "success" => false,
        "message" => "Failed to update donor."
    ]);

}

$stmt->close();
$conn->close();

?>
