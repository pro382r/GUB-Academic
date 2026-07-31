// ======================================
// LifeDrop - Remove Donor
// ======================================

const searchForm = document.getElementById("searchForm");

searchForm.addEventListener("submit", function(event){

    event.preventDefault();

    const search = document.getElementById("search").value.trim();

    if(search === ""){

        alert("Please enter a donor name or phone number.");

        return;

    }

    const formData = new FormData();

    formData.append("search", search);

    fetch("../backend/donor_search.php",{

        method:"POST",

        body:formData

    })

    .then(response => response.json())

    .then(result => {

        if(result.success){

            const donor = result.data;

            document.getElementById("donorInfo").style.display = "block";

            document.getElementById("showID").textContent = donor.id;

            document.getElementById("showName").textContent = donor.full_name;

            document.getElementById("showAge").textContent = donor.age;

            document.getElementById("showGender").textContent = donor.gender;

            document.getElementById("showBlood").textContent = donor.blood_group;

            document.getElementById("showPhone").textContent = donor.phone;

            document.getElementById("showEmail").textContent = donor.email;

            document.getElementById("showAddress").textContent = donor.address;

            document.getElementById("showDonation").textContent = donor.last_donation;

            document.getElementById("deleteID").value = donor.id;

        }

        else{

            document.getElementById("donorInfo").style.display = "none";

            alert(result.message);

        }

    })

    .catch(error => {

        console.error(error);

        alert("Something went wrong.");

    });

});


// ======================================
// Delete Confirmation
// ======================================

const deleteForm = document.querySelector(".donor-info form");

if(deleteForm){

    deleteForm.addEventListener("submit", function(event){

        const confirmDelete = confirm(

            "Are you sure you want to permanently delete this donor?"

        );

        if(!confirmDelete){

            event.preventDefault();

        }

    });

}