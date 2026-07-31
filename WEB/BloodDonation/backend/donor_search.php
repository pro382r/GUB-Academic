<?php

header("Content-Type: application/json");

include "config/db_connect.php";

if (!isset($_POST["search"]) || empty(trim($_POST["search"]))) {
    echo json_encode([
        "success" => false,
        "message" => "Search value is required."
    ]);
    exit;
}

$search = trim($_POST["search"]);

if (is_numeric($search)) {

    $sql = "SELECT * FROM donors
            WHERE id = ?
            OR phone = ?
            ORDER BY id ASC";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("is", $search, $search);

} else {

    $sql = "SELECT * FROM donors
            WHERE full_name LIKE ?
            ORDER BY full_name ASC";

    $stmt = $conn->prepare($sql);

    $name = "%".$search."%";
    $stmt->bind_param("s", $name);
}

$stmt->execute();

$result = $stmt->get_result();

$donors = [];

while($row = $result->fetch_assoc()){
    $donors[] = $row;
}

if(count($donors) > 0){

    echo json_encode([
        "success" => true,
        "count" => count($donors),
        "data" => $donors
    ]);

}else{

    echo json_encode([
        "success" => false,
        "message" => "No donor found."
    ]);
}

$stmt->close();
$conn->close();

?>
