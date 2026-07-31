const searchBtn = document.getElementById("searchBtn");
const updateForm = document.getElementById("updateForm");

const searchResult = document.getElementById("searchResult");
const resultBody = document.getElementById("resultBody");

searchBtn.addEventListener("click", searchDonor);

function searchDonor(){

    const search = document.getElementById("searchId").value.trim();

    if(search===""){
        alert("Please enter Donor ID, Name or Phone Number.");
        return;
    }

    const formData = new FormData();
    formData.append("search", search);

    fetch("../backend/donor_search.php",{
        method:"POST",
        body:formData
    })
    .then(res=>res.json())
    .then(result=>{

        if(!result.success){
            alert(result.message);
            searchResult.style.display="none";
            return;
        }

        resultBody.innerHTML="";

        if(result.count===1){

            searchResult.style.display="none";
            fillForm(result.data[0]);

        }else{

            searchResult.style.display="block";

            result.data.forEach(donor=>{

                resultBody.innerHTML+=`
                <tr>
                    <td>${donor.id}</td>
                    <td>${donor.full_name}</td>
                    <td>${donor.phone}</td>
                    <td>${donor.blood_group}</td>
                    <td>
                        <button onclick="selectDonor(${donor.id})">
                            Select
                        </button>
                    </td>
                </tr>
                `;

            });

            window.donorList=result.data;

        }

    });

}

function selectDonor(id){

    const donor=window.donorList.find(d=>d.id==id);

    if(donor){

        fillForm(donor);
        searchResult.style.display="none";

    }

}

function fillForm(donor){

    document.getElementById("donorId").value=donor.id;
    document.getElementById("fullName").value=donor.full_name;
    document.getElementById("age").value=donor.age;
    document.getElementById("gender").value=donor.gender;
    document.getElementById("bloodGroup").value=donor.blood_group;
    document.getElementById("phone").value=donor.phone;
    document.getElementById("email").value=donor.email;
    document.getElementById("address").value=donor.address;
    document.getElementById("lastDonation").value=donor.last_donation;

}

updateForm.addEventListener("submit",function(e){

    e.preventDefault();

    const formData=new FormData();

    formData.append("id",document.getElementById("donorId").value);
    formData.append("full_name",document.getElementById("fullName").value);
    formData.append("age",document.getElementById("age").value);
    formData.append("gender",document.getElementById("gender").value);
    formData.append("blood_group",document.getElementById("bloodGroup").value);
    formData.append("phone",document.getElementById("phone").value);
    formData.append("email",document.getElementById("email").value);
    formData.append("address",document.getElementById("address").value);
    formData.append("last_donation",document.getElementById("lastDonation").value);

    fetch("../backend/update_donor.php",{
        method:"POST",
        body:formData
    })
    .then(res=>res.json())
    .then(result=>{

        alert(result.message);

        if(result.success){

            updateForm.reset();
            searchResult.style.display="none";
            resultBody.innerHTML="";

        }

    });

});
