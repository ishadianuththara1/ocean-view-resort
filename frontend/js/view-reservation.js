document.addEventListener('DOMContentLoaded', async function() {
    checkAuth();
    setNavbarUser();

    var params = new URLSearchParams(window.location.search);
    var id = params.get('id');

    if (!id || isNaN(parseInt(id))) {
        alert('No reservation ID specified.');
        window.location.href = 'reservations.html';
        return;
    }

    await loadReservation(id);

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
        await saveReservation(id);
    });

    document.getElementById('billBtn').addEventListener('click', function() {
        window.location.href = 'bill.html?id=' + id;
    });

    document.getElementById('deleteBtn').addEventListener('click', async function() {
        await deleteReservation(id);
    });
});

async function loadReservation(id) {
    try {
        var r = await apiGet('/reservations/' + id);

        if (!r || !r.reservationId) {
            showAlert('Reservation not found.', 'danger', false);
            return;
        }

        document.getElementById('viewGuestName').textContent = r.guestName || '—';
        document.getElementById('viewAddress').textContent   = r.address   || '—';
        document.getElementById('viewContact').textContent   = r.contactNumber || '—';
        document.getElementById('viewEmail').textContent     = r.email     || '—';
        document.getElementById('viewGuestLink').href        = 'view-guest.html?id=' + r.guestId;

        document.getElementById('viewResNumber').textContent = r.reservationNumber;
        document.getElementById('viewRoomType').textContent  = r.roomType;
        document.getElementById('viewCheckIn').textContent   = r.checkInDate;
        document.getElementById('viewCheckOut').textContent  = r.checkOutDate;
        document.getElementById('viewStatus').textContent    = r.status;
        document.getElementById('viewTotal').textContent     = 'LKR ' + parseFloat(r.totalAmount).toFixed(2);

        document.getElementById('editRoomType').value = r.roomType;
        document.getElementById('editCheckIn').value  = r.checkInDate;
        document.getElementById('editCheckOut').value = r.checkOutDate;
        document.getElementById('editStatus').value   = r.status;

    } catch (err) {
        showAlert('Failed to load reservation.', 'danger', false);
    }
}

async function saveReservation(id) {
    var roomType  = document.getElementById('editRoomType').value;
    var checkIn   = document.getElementById('editCheckIn').value;
    var checkOut  = document.getElementById('editCheckOut').value;
    var status    = document.getElementById('editStatus').value;

    if (!roomType) {
        showAlert('Please select a room type.', 'danger', false); return;
    }
    if (!checkIn || !checkOut || checkOut <= checkIn) {
        showAlert('Check-out date must be after check-in date.', 'danger', false); return;
    }

    var saveBtn = document.getElementById('saveBtn');
    saveBtn.disabled    = true;
    saveBtn.textContent = 'Saving...';

    try {
        var data = await apiPut('/reservations/' + id, {
            roomType:     roomType,
            checkInDate:  checkIn,
            checkOutDate: checkOut,
            status:       status
        });

        if (data.success) {
            showAlert('Reservation updated successfully!', 'success');
            await loadReservation(id);
            document.getElementById('viewSection').style.display = 'block';
            document.getElementById('editSection').style.display = 'none';
        } else {
            showAlert(data.message || 'Failed to update reservation.', 'danger', false);
        }
    } catch (err) {
        showAlert('Failed to connect to server.', 'danger', false);
    } finally {
        saveBtn.disabled    = false;
        saveBtn.textContent = 'Save Changes';
    }
}

async function deleteReservation(id) {
    if (!confirm('Delete this reservation permanently? This cannot be undone.')) return;

    try {
        var data = await apiDelete('/reservations/' + id);
        if (data.success) {
            alert('Reservation deleted successfully.');
            window.location.href = 'reservations.html';
        } else {
            showAlert(data.message || 'Failed to delete reservation.', 'danger', false);
        }
    } catch (err) {
        showAlert('Failed to connect to server.', 'danger', false);
    }
}
