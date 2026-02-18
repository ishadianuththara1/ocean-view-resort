document.addEventListener('DOMContentLoaded', function () {
    checkAuth();
    setNavbarUser();
    document.getElementById('guestForm').addEventListener('submit', handleSubmit);
});

function validateForm() {
    var valid = true;

    var firstName = document.getElementById('firstName').value.trim();
    var firstNameErr = document.getElementById('firstNameError');
    if (!firstName || firstName.length < 2 || !/^[a-zA-Z ]+$/.test(firstName)) {
        firstNameErr.textContent = 'First name must be 2-50 characters (letters only).';
        firstNameErr.style.display = 'block';
        valid = false;
    } else {
        firstNameErr.style.display = 'none';
    }

    var lastName = document.getElementById('lastName').value.trim();
    var lastNameErr = document.getElementById('lastNameError');
    if (!lastName || lastName.length < 2 || !/^[a-zA-Z ]+$/.test(lastName)) {
        lastNameErr.textContent = 'Last name must be 2-50 characters (letters only).';
        lastNameErr.style.display = 'block';
        valid = false;
    } else {
        lastNameErr.style.display = 'none';
    }

    var address = document.getElementById('address').value.trim();
    var addressErr = document.getElementById('addressError');
    if (!address || address.length < 5) {
        addressErr.textContent = 'Address must be at least 5 characters.';
        addressErr.style.display = 'block';
        valid = false;
    } else {
        addressErr.style.display = 'none';
    }

    var contact = document.getElementById('contactNumber').value.trim();
    var contactErr = document.getElementById('contactError');
    var digits = contact.replace(/[\s\-\+]/g, '');
    if (!digits || !/^\d{10,15}$/.test(digits)) {
        contactErr.textContent = 'Contact number must contain 10-15 digits.';
        contactErr.style.display = 'block';
        valid = false;
    } else {
        contactErr.style.display = 'none';
    }

    var email = document.getElementById('email').value.trim();
    var emailErr = document.getElementById('emailError');
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        emailErr.textContent = 'Please enter a valid email address.';
        emailErr.style.display = 'block';
        valid = false;
    } else {
        emailErr.style.display = 'none';
    }

    return valid;
}

async function handleSubmit(e) {
    e.preventDefault();

    if (!validateForm()) return;

    var submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';

    var formData = {
        firstName: document.getElementById('firstName').value.trim(),
        lastName: document.getElementById('lastName').value.trim(),
        address: document.getElementById('address').value.trim(),
        contactNumber: document.getElementById('contactNumber').value.trim(),
        email: document.getElementById('email').value.trim()
    };

    try {
        var data = await apiPost('/guests', formData);

        if (data.success) {
            showAlert('Guest registered successfully!', 'success', false);
            document.getElementById('guestForm').reset();
            window.location.href = 'guests.html';
        } else {
            showAlert(data.message || 'Failed to register guest.', 'danger', false);
        }
    } catch (err) {
        showAlert('Cannot connect to server. Is the Java server running?', 'danger', false);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Register Guest';
    }
}
