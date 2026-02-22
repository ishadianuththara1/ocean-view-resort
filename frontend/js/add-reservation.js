var ROOM_RATES = { Standard: 80, Deluxe: 150, Suite: 250, Family: 180 };

document.addEventListener('DOMContentLoaded', async function () {
    checkAuth();
    setNavbarUser();

    var params = new URLSearchParams(window.location.search);
    var preselectedId = params.get('guestId');

    await loadGuestsDropdown(preselectedId);

    var today = new Date().toISOString().split('T')[0];
    document.getElementById('checkInDate').setAttribute('min', today);
    document.getElementById('checkOutDate').setAttribute('min', today);

    document.getElementById('checkInDate').addEventListener('change', function () {
        if (this.value) {
            document.getElementById('checkOutDate').setAttribute('min', this.value);
            var checkOut = document.getElementById('checkOutDate').value;
            if (checkOut && checkOut <= this.value) {
                document.getElementById('checkOutDate').value = '';
            }
        }
        updateCostPreview();
    });

    document.getElementById('checkOutDate').addEventListener('change', updateCostPreview);
    document.getElementById('roomType').addEventListener('change', updateCostPreview);
    document.getElementById('reservationForm').addEventListener('submit', handleSubmit);
});

async function loadGuestsDropdown(preselectedId) {
    try {
        var guests = await apiGet('/guests');
        var guestList = Array.isArray(guests) ? guests : [];
        var select = document.getElementById('guestId');
        select.innerHTML = '<option value="">-- Select a registered guest --</option>';
        guestList.forEach(function (g) {
            var option = document.createElement('option');
            option.value = g.guestId;
            option.textContent = g.fullName + ' (' + g.contactNumber + ')';
            if (preselectedId && String(g.guestId) === String(preselectedId)) {
                option.selected = true;
            }
            select.appendChild(option);
        });
    } catch (err) {
        showAlert('Could not load guests. Is the server running?', 'danger', false);
    }
}

function updateCostPreview() {
    var roomType = document.getElementById('roomType').value;
    var checkIn = document.getElementById('checkInDate').value;
    var checkOut = document.getElementById('checkOutDate').value;
    var costDiv = document.getElementById('costPreview');

    if (roomType && checkIn && checkOut && checkOut > checkIn) {
        var msPerDay = 24 * 60 * 60 * 1000;
        var nights = Math.round((new Date(checkOut) - new Date(checkIn)) / msPerDay);
        var rate = ROOM_RATES[roomType] || 0;
        var total = nights * rate;
        document.getElementById('costNights').textContent = nights;
        document.getElementById('costRate').textContent = 'LKR ' + rate;
        document.getElementById('costAmount').textContent = 'LKR ' + total.toFixed(2);
        costDiv.style.display = 'block';
    } else {
        costDiv.style.display = 'none';
    }
}

function validateForm() {
    var valid = true;

    var guestId = document.getElementById('guestId').value;
    var guestErr = document.getElementById('guestError');
    if (!guestId) {
        guestErr.textContent = 'Please select a guest.';
        guestErr.style.display = 'block';
        valid = false;
    } else {
        guestErr.style.display = 'none';
    }

    var roomType = document.getElementById('roomType').value;
    var roomTypeErr = document.getElementById('roomTypeError');
    if (!roomType) {
        roomTypeErr.textContent = 'Please select a room type.';
        roomTypeErr.style.display = 'block';
        valid = false;
    } else {
        roomTypeErr.style.display = 'none';
    }

    var checkIn = document.getElementById('checkInDate').value;
    var checkInErr = document.getElementById('checkInError');
    var today = new Date().toISOString().split('T')[0];
    if (!checkIn || checkIn < today) {
        checkInErr.textContent = 'Check-in date must be today or a future date.';
        checkInErr.style.display = 'block';
        valid = false;
    } else {
        checkInErr.style.display = 'none';
    }

    var checkOut = document.getElementById('checkOutDate').value;
    var checkOutErr = document.getElementById('checkOutError');
    if (!checkOut) {
        checkOutErr.textContent = 'Please select a check-out date.';
        checkOutErr.style.display = 'block';
        valid = false;
    } else if (checkOut <= checkIn) {
        checkOutErr.textContent = 'Check-out date must be after check-in date.';
        checkOutErr.style.display = 'block';
        valid = false;
    } else {
        checkOutErr.style.display = 'none';
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
        guestId: parseInt(document.getElementById('guestId').value),
        roomType: document.getElementById('roomType').value,
        checkInDate: document.getElementById('checkInDate').value,
        checkOutDate: document.getElementById('checkOutDate').value
    };

    try {
        var data = await apiPost('/reservations', formData);

        if (data.success) {
            showAlert('Reservation created! Number: ' + data.reservationNumber, 'success', false);
            window.location.href = 'reservations.html';
        } else {
            showAlert(data.message || 'Failed to create reservation.', 'danger', false);
        }
    } catch (err) {
        showAlert('Cannot connect to server. Is the Java server running?', 'danger', false);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create Reservation';
    }
}
