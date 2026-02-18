document.addEventListener('DOMContentLoaded', async function() {
    checkAuth();
    setNavbarUser();
    await loadGuests();

    document.getElementById('searchInput').addEventListener('input', async function() {
        var query = this.value.trim();
        if (query.length === 0) {
            await loadGuests();
        } else {
            await searchGuests(query);
        }
    });
});

async function loadGuests() {
    try {
        var data = await apiGet('/guests');
        renderTable(Array.isArray(data) ? data : []);
    } catch (err) {
        showAlert('Failed to load guests. Is the server running?', 'danger', false);
    }
}

async function searchGuests(query) {
    try {
        var data = await apiGet('/guests?search=' + encodeURIComponent(query));
        renderTable(Array.isArray(data) ? data : []);
    } catch (err) {
        showAlert('Search failed.', 'danger');
    }
}

function renderTable(guests) {
    var tbody = document.getElementById('guestsBody');

    if (guests.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center" style="padding:30px; color:#888;">No guests found.</td></tr>';
        return;
    }

    tbody.innerHTML = '';
    guests.forEach(function(g) {
        var row = document.createElement('tr');
        row.innerHTML =
            '<td>' + escHtml(g.guestId)       + '</td>' +
            '<td>' + escHtml(g.fullName)       + '</td>' +
            '<td>' + escHtml(g.address)        + '</td>' +
            '<td>' + escHtml(g.contactNumber)  + '</td>' +
            '<td>' + escHtml(g.email || '—')   + '</td>' +
            '<td>' +
                '<a href="view-guest.html?id=' + g.guestId + '" class="btn btn-primary btn-sm">View</a> ' +
                '<button onclick="deleteGuest(' + g.guestId + ', \'' + escHtml(g.fullName) + '\')" class="btn btn-danger btn-sm">Delete</button>' +
            '</td>';
        tbody.appendChild(row);
    });
}

async function deleteGuest(id, name) {
    if (!confirm('Delete guest "' + name + '"?\n\nNote: Cannot delete if they have existing reservations.')) return;

    try {
        var data = await apiDelete('/guests/' + id);
        if (data.success) {
            showAlert('Guest deleted successfully.', 'success');
            await loadGuests();
        } else {
            showAlert(data.message || 'Failed to delete guest.', 'danger', false);
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
