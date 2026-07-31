// ===============================
// LifeDrop - Add Donor Validation
// ===============================

const donorForm = document.getElementById("donorForm");

donorForm.addEventListener("submit", function (event) {

    const fullName = document.getElementById("full_name").value.trim();
    const age = parseInt(document.getElementById("age").value);
    const bloodGroup = document.getElementById("blood_group").value;
    const phone = document.getElementById("phone").value.trim();
    const email = document.getElementById("email").value.trim();
    const address = document.getElementById("address").value.trim();
    const donationDate = document.getElementById("last_donation").value;

    // ===============================
    // Full Name

    if (fullName.length < 3) {
        alert("Full Name must contain at least 3 characters.");
        event.preventDefault();
        return;
    }

    // ===============================
    // Age

    if (isNaN(age) || age < 18 || age > 65) {
        alert("Age must be between 18 and 65.");
        event.preventDefault();
        return;
    }

    // ===============================
    // Blood Group
    // ===============================

    if (bloodGroup === "") {
        alert("Please select a blood group.");
        event.preventDefault();
        return;
    }

    // ===============================
    // Phone Number
    // Bangladesh Format

    const phonePattern = /^01[3-9]\d{8}$/;

    if (!phonePattern.test(phone)) {
        alert("Enter a valid Bangladeshi phone number.");
        event.preventDefault();
        return;
    }

    // ===============================
    // Email

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailPattern.test(email)) {
        alert("Enter a valid email address.");
        event.preventDefault();
        return;
    }

    // ===============================
    // Address

    if (address.length < 5) {
        alert("Please enter a valid address.");
        event.preventDefault();
        return;
    }

    // ===============================
    // Last Donation Date

    const today = new Date();
    const selectedDate = new Date(donationDate);

    today.setHours(0, 0, 0, 0);

    if (selectedDate > today) {
        alert("Last donation date cannot be in the future.");
        event.preventDefault();
        return;
    }

    alert("Donor information is valid.\nReady to submit!");

});
