document.addEventListener('DOMContentLoaded', async function() {
    checkAuth();
    setNavbarUser();

    var today = new Date();
    document.getElementById('reportDate').textContent =
        'Data as of ' + today.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });

    try {
        var daily   = await apiGet('/reports?type=daily');
        renderDaily(daily);
    } catch (err) {
        showAlert('Failed to load daily report.', 'danger', false);
    }

    try {
        var revenue = await apiGet('/reports?type=revenue');
        renderRevenue(revenue);
    } catch (err) {
        showAlert('Failed to load revenue report.', 'danger', false);
    }

    try {
        var guests  = await apiGet('/reports?type=guests');
        renderGuests(guests);
    } catch (err) {
        showAlert('Failed to load guest list.', 'danger', false);
    }
});

function renderDaily(data) {
    document.getElementById('checkInCount').textContent  = data.checkInsCount  || 0;
    document.getElementById('checkOutCount').textContent = data.checkOutsCount || 0;

    renderDailyTable('checkInsBody',  data.checkIns,  'checkOutDate', 5);
    renderDailyTable('checkOutsBody', data.checkOuts, 'checkInDate',  5);
}

function renderDailyTable(tbodyId, rows, dateField, colspan) {
    var tbody = document.getElementById(tbodyId);
    if (!rows || rows.length === 0) {
        tbody.innerHTML = '<tr><td colspan="' + colspan + '" class="text-center" style="padding:16px;color:#888;">None today.</td></tr>';
        return;
    }
    tbody.innerHTML = '';
    rows.forEach(function(r) {
        var tr = document.createElement('tr');
        tr.innerHTML =
            '<td>' + escHtml(r.reservationNumber) + '</td>' +
            '<td>' + escHtml(r.guestName)          + '</td>' +
            '<td>' + escHtml(r.roomType)            + '</td>' +
            '<td>' + escHtml(r[dateField])          + '</td>' +
            '<td>' + escHtml(r.status)              + '</td>';
        tbody.appendChild(tr);
    });
}

function renderRevenue(data) {
    var tbody = document.getElementById('revenueBody');
    if (!data.rows || data.rows.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center" style="padding:16px;color:#888;">No data.</td></tr>';
        return;
    }
    tbody.innerHTML = '';
    data.rows.forEach(function(row) {
        var tr = document.createElement('tr');
        tr.innerHTML =
            '<td>' + escHtml(row.roomType)               + '</td>' +
            '<td>' + escHtml(String(row.count))          + '</td>' +
            '<td>LKR ' + escHtml(String(row.revenue))       + '</td>';
        tbody.appendChild(tr);
    });
    var tfoot = document.createElement('tr');
    tfoot.style.fontWeight = 'bold';
    tfoot.style.borderTop  = '2px solid #1a3c5e';
    tfoot.innerHTML =
        '<td>Total</td>' +
        '<td>' + escHtml(String(data.grandCount)) + '</td>' +
        '<td>LKR ' + escHtml(String(data.grandTotal)) + '</td>';
    tbody.appendChild(tfoot);
}

function renderGuests(data) {
    var tbody = document.getElementById('guestListBody');
    if (!Array.isArray(data) || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center" style="padding:16px;color:#888;">No guests found.</td></tr>';
        return;
    }
    tbody.innerHTML = '';
    data.forEach(function(g) {
        var tr = document.createElement('tr');
        tr.innerHTML =
            '<td>' + escHtml(g.firstName + ' ' + g.lastName) + '</td>' +
            '<td>' + escHtml(g.contactNumber)                 + '</td>' +
            '<td>' + escHtml(g.email || '—')                  + '</td>' +
            '<td>' + escHtml(String(g.reservationCount))      + '</td>' +
            '<td>LKR ' + escHtml(String(g.totalSpent))           + '</td>' +
            '<td><a href="view-guest.html?id=' + g.guestId + '" class="btn btn-primary btn-sm">View</a></td>';
        tbody.appendChild(tr);
    });
}

function escHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}
