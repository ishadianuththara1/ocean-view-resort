document.addEventListener('DOMContentLoaded', async function() {
    checkAuth();
    setNavbarUser();

    var params = new URLSearchParams(window.location.search);
    var id = params.get('id');

    if (!id || isNaN(parseInt(id))) {
        alert('No guest ID specified.');
        window.location.href = 'guests.html';
        return;
    }

    await loadGuest(id);

    document.getElementById('editBtn').addEventListener('click', function() {
        document.getElementById('viewSection').style.display = 'none';
        document.getElementById('editSection').style.display = 'block';
    });

    document.getElementById('cancelEditBtn').addEventListener('click', function() {
        document.getElementById('viewSection').style.display = 'block';
        document.getElementById('editSection').style.display = 'none';
    });

    document.getElementById('editForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        await saveGuest(id);
    });

    document.getElementById('newReservationBtn').addEventListener('click', function() {
        window.location.href = 'add-reservation.html?guestId=' + id;
    });

    document.getElementById('deleteBtn').addEventListener('click', async function() {
        await deleteGuest(id);
    });
});

async function loadGuest(id) {
    try {
        var g = await apiGet('/guests/' + id);
        if (!g || !g.guestId) {
            showAlert('Guest not found.', 'danger', false);
            return;
        }
        displayGuest(g);
    } catch (err) {
        showAlert('Failed to load guest details.', 'danger', false);
    }
}

function displayGuest(g) {
    document.getElementById('viewGuestId').textContent   = g.guestId;
    document.getElementById('viewFirstName').textContent = g.firstName;
    document.getElementById('viewLastName').textContent  = g.lastName;
    document.getElementById('viewFullName').textContent  = g.fullName;
    document.getElementById('viewAddress').textContent   = g.address;
    document.getElementById('viewContact').textContent   = g.contactNumber;
    document.getElementById('viewEmail').textContent     = g.email || '—';

    document.getElementById('editFirstName').value = g.firstName;
    document.getElementById('editLastName').value  = g.lastName;
    document.getElementById('editAddress').value   = g.address;
    document.getElementById('editContact').value   = g.contactNumber;
    document.getElementById('editEmail').value     = g.email || '';
}

async function saveGuest(id) {
    var firstName = document.getElementById('editFirstName').value.trim();
    var lastName  = document.getElementById('editLastName').value.trim();
    var address   = document.getElementById('editAddress').value.trim();
    var contact   = document.getElementById('editContact').value.trim();
    var email     = document.getElementById('editEmail').value.trim();

    if (!firstName || firstName.length < 2 || !/^[a-zA-Z ]+$/.test(firstName)) {
        showAlert('First name must be 2-50 characters (letters only).', 'danger', false); return;
    }
    if (!lastName || lastName.length < 2 || !/^[a-zA-Z ]+$/.test(lastName)) {
        showAlert('Last name must be 2-50 characters (letters only).', 'danger', false); return;
    }
    if (!address || address.length < 5) {
        showAlert('Address must be at least 5 characters.', 'danger', false); return;
    }
    var digits = contact.replace(/[\s\-\+]/g, '');
    if (!digits || !/^\d{10,15}$/.test(digits)) {
        showAlert('Contact number must contain 10-15 digits.', 'danger', false); return;
    }
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showAlert('Please enter a valid email address.', 'danger', false); return;
    }

    var saveBtn = document.getElementById('saveBtn');
    saveBtn.disabled    = true;
    saveBtn.textContent = 'Saving...';

    try {
        var data = await apiPut('/guests/' + id, {
            firstName:     firstName,
            lastName:      lastName,
            address:       address,
            contactNumber: contact,
            email:         email
        });

        if (data.success) {
            showAlert('Guest updated successfully!', 'success');
            await loadGuest(id);
            document.getElementById('viewSection').style.display = 'block';
            document.getElementById('editSection').style.display = 'none';
        } else {
            showAlert(data.message || 'Failed to update guest.', 'danger', false);
        }
    } catch (err) {
        showAlert('Failed to connect to server.', 'danger', false);
    } finally {
        saveBtn.disabled    = false;
        saveBtn.textContent = 'Save Changes';
    }
}

async function deleteGuest(id) {
    if (!confirm('Delete this guest permanently?\n\nNote: Guests with existing reservations cannot be deleted.')) return;

    try {
        var data = await apiDelete('/guests/' + id);
        if (data.success) {
            alert('Guest deleted successfully.');
            window.location.href = 'guests.html';
        } else {
            showAlert(data.message || 'Failed to delete guest.', 'danger', false);
        }
    } catch (err) {
        showAlert('Failed to connect to server.', 'danger', false);
    }
}
