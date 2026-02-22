document.addEventListener('DOMContentLoaded', async function() {
    checkAuth();
    setNavbarUser();
    await loadReservations();

    document.getElementById('searchInput').addEventListener('input', async function() {
        var query = this.value.trim();
        if (query.length === 0) {
            await loadReservations();
        } else {
            await searchReservations(query);
        }
    });
});

async function loadReservations() {
    try {
        var data = await apiGet('/reservations');
        renderTable(Array.isArray(data) ? data : []);
    } catch (err) {
        showAlert('Failed to load reservations. Is the server running?', 'danger', false);
    }
}

async function searchReservations(query) {
    try {
        var data = await apiGet('/reservations?search=' + encodeURIComponent(query));
        renderTable(Array.isArray(data) ? data : []);
    } catch (err) {
        showAlert('Search failed.', 'danger');
    }
}

function renderTable(reservations) {
    var tbody = document.getElementById('reservationsBody');

    if (reservations.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center" style="padding:30px; color:#888;">No reservations found.</td></tr>';
        return;
    }

    tbody.innerHTML = '';
    reservations.forEach(function(r) {
        var statusClass = r.status === 'Confirmed' ? 'confirmed'
                        : r.status === 'Cancelled' ? 'cancelled'
                        : 'checked-out';

        var row = document.createElement('tr');
        row.innerHTML =
            '<td>' + escHtml(r.reservationNumber) + '</td>' +
            '<td>' + escHtml(r.guestName)         + '</td>' +
            '<td>' + escHtml(r.contactNumber)     + '</td>' +
            '<td>' + escHtml(r.roomType)          + '</td>' +
            '<td>' + escHtml(r.checkInDate)       + '</td>' +
            '<td>' + escHtml(r.checkOutDate)      + '</td>' +
            '<td><span class="badge-' + statusClass + '">' + escHtml(r.status) + '</span></td>' +
            '<td>' +
                '<a href="view-reservation.html?id=' + r.reservationId + '" class="btn btn-primary btn-sm">View</a> ' +
                '<button onclick="deleteReservation(' + r.reservationId + ')" class="btn btn-danger btn-sm">Delete</button>' +
            '</td>';
        tbody.appendChild(row);
    });
}

async function deleteReservation(id) {
    if (!confirm('Are you sure you want to delete this reservation? This cannot be undone.')) return;

    try {
        var data = await apiDelete('/reservations/' + id);
        if (data.success) {
            showAlert('Reservation deleted successfully.', 'success');
            await loadReservations();
        } else {
            showAlert(data.message || 'Failed to delete reservation.', 'danger');
        }
    } catch (err) {
        showAlert('Failed to connect to server.', 'danger');
    }
}

function escHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}
