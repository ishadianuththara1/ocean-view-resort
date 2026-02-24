document.addEventListener('DOMContentLoaded', async function() {
    checkAuth();
    setNavbarUser();

    await loadReservationsDropdown();

    var params = new URLSearchParams(window.location.search);
    var preselectedId = params.get('id');
    if (preselectedId) {
        document.getElementById('reservationSelect').value = preselectedId;
        await loadBill(preselectedId);
    }

    document.getElementById('reservationSelect').addEventListener('change', async function() {
        var id = this.value;
        if (id) {
            await loadBill(id);
        } else {
            document.getElementById('billSection').style.display = 'none';
        }
    });

    document.getElementById('printBtn').addEventListener('click', function() {
        window.print();
    });
});

async function loadReservationsDropdown() {
    try {
        var reservations = await apiGet('/reservations');
        var select = document.getElementById('reservationSelect');
        select.innerHTML = '<option value="">-- Select a Reservation --</option>';

        if (Array.isArray(reservations)) {
            reservations.forEach(function(r) {
                var option = document.createElement('option');
                option.value       = r.reservationId;
                option.textContent = r.reservationNumber + ' - ' + r.guestName + ' (' + r.status + ')';
                select.appendChild(option);
            });
        }
    } catch (err) {
        showAlert('Failed to load reservations.', 'danger', false);
    }
}

async function loadBill(reservationId) {
    try {
        var bill = await apiGet('/bill/' + reservationId);

        if (bill && bill.reservationNumber) {
            displayBill(bill);
            document.getElementById('billSection').style.display = 'block';
        } else {
            showAlert(bill.message || 'Could not load bill details.', 'danger', false);
            document.getElementById('billSection').style.display = 'none';
        }
    } catch (err) {
        showAlert('Failed to load bill.', 'danger', false);
    }
}

function displayBill(bill) {
    document.getElementById('billResNumber').textContent  = bill.reservationNumber;
    document.getElementById('billGuestName').textContent  = bill.guestName;
    document.getElementById('billAddress').textContent    = bill.address;
    document.getElementById('billContact').textContent    = bill.contactNumber;
    document.getElementById('billRoomType').textContent   = bill.roomType;
    document.getElementById('billCheckIn').textContent    = bill.checkInDate;
    document.getElementById('billCheckOut').textContent   = bill.checkOutDate;
    document.getElementById('billNights').textContent     = bill.numberOfNights + ' night(s)';
    document.getElementById('billPriceNight').textContent = 'LKR ' + parseFloat(bill.pricePerNight).toFixed(2);
    document.getElementById('billTotal').textContent      = 'LKR ' + parseFloat(bill.totalAmount).toFixed(2);
    document.getElementById('billDate').textContent       = new Date().toLocaleDateString('en-GB', {
        day: '2-digit', month: 'long', year: 'numeric'
    });
}
