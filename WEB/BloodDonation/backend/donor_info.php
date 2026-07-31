<?php
header("Content-Type: application/json");
require_once "config/db_connect.php";
// ===============================
// Get All Donors
// ===============================

$sql = "SELECT
            id,
            full_name,
            age,
            gender,
            blood_group,
            phone,
            email,
            address,
            last_donation
        FROM donors
        ORDER BY id ASC";

$result = $conn->query($sql);

// ===============================
// Store Data
// ===============================

$donors = [];
if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $donors[] = $row;
    }
}

// ===============================
// Return JSON
// ===============================

echo json_encode([

    "success" => true,
    "count" => count($donors),
    "data" => $donors

]);

$conn->close();
?>
