<?php
require_once "config/db_connect.php";

// ===============================
// Check Request Method
// ===============================

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    die("Invalid Request!");

}

// ===============================
// Get Donor ID
// ===============================

$donor_id = intval($_POST["donor_id"]);
// ===============================
// Validate
// ===============================

if ($donor_id <= 0) {
    echo "<script>
        alert('Invalid Donor ID.');
        window.location.href='../frontend/donor-remove.html';
    </script>";
    exit();
}

// ===============================
// Check Donor Exists
// ===============================

$check = $conn->prepare(
    "SELECT id FROM donors WHERE id = ?"

);

$check->bind_param("i", $donor_id);
$check->execute();
$result = $check->get_result();

if($result->num_rows == 0){
    echo "<script>
        alert('Donor not found.');
        window.location.href='../frontend/donor-remove.html';
    </script>";
    exit();
}

$check->close();

// ===============================
// Delete Query
// ===============================

$sql = "DELETE FROM donors WHERE id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $donor_id);

// ===============================
// Execute
// ===============================

if($stmt->execute()){
    echo "<script>
        alert('Donor deleted successfully!');
        window.location.href='../frontend/donor-remove.html';
    </script>";
}

else{
    echo "<script>
        alert('Failed to delete donor.');
        window.location.href='../frontend/donor-remove.html';
    </script>";

}

$stmt->close();
$conn->close();
?>