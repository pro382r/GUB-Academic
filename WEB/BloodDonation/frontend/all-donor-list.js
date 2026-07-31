// ======================================
// LifeDrop - All Donor List
// ======================================

const donorTable = document.getElementById("donorTable");

// ======================================
// Load All Donors
// ======================================

function loadDonors() {

    fetch("../backend/donor_info.php")
        .then(response => response.json())
        .then(result => {

            donorTable.innerHTML = "";

            if (result.success && result.data.length > 0) {

                result.data.forEach(donor => {

                    // Calculate Donor Status

                    const today = new Date();
                    const donationDate = new Date(donor.last_donation);

                    const diffTime = today - donationDate;
                    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

                    const eligible = diffDays >= 120;

                    const status = eligible
                        ? '<span class="status green">Available </span>'
                        : '<span class="status red">Not Available</span>';

                    donorTable.innerHTML += `
                        <tr>
                            <td>${donor.id}</td>
                            <td>${donor.full_name}</td>
                            <td>${donor.age}</td>
                            <td>${donor.gender}</td>
                            <td>${donor.blood_group}</td>
                            <td>${donor.phone}</td>
                            <td>${donor.email}</td>
                            <td>${donor.address}</td>
                            <td>${donor.last_donation}</td>
                            <td>${status}</td>
                        </tr>
                    `;

                });

            } else {

                donorTable.innerHTML = `
                    <tr>
                        <td colspan="10">
                            No donors found.
                        </td>
                    </tr>
                `;

            }

        })
        .catch(error => {

            console.error(error);

            donorTable.innerHTML = `
                <tr>
                    <td colspan="10">
                        Failed to load donor data.
                    </td>
                </tr>
            `;

        });

}

// ======================================
// Initial Load
// ======================================

loadDonors();