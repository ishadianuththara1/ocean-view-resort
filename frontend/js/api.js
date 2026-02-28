const API_BASE = 'http://localhost:8080/api';

function getUsername() {
    return localStorage.getItem('username');
}

function getFullName() {
    return localStorage.getItem('fullName');
}

function checkAuth() {
    if (!localStorage.getItem('username')) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

function setNavbarUser() {
    var el = document.getElementById('nav-username');
    if (el) {
        el.textContent = getFullName() || getUsername() || 'User';
    }
}

function logout() {
    fetch(API_BASE + '/logout', {
        method: 'POST',
        credentials: 'include'
    }).finally(function () {
        localStorage.removeItem('username');
        localStorage.removeItem('fullName');
        window.location.href = 'login.html';
    });
}

async function apiGet(endpoint) {
    var response = await fetch(API_BASE + endpoint, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'
    });
    return response.json();
}

async function apiPost(endpoint, data) {
    var response = await fetch(API_BASE + endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(data)
    });
    return response.json();
}

async function apiPut(endpoint, data) {
    var response = await fetch(API_BASE + endpoint, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(data)
    });
    return response.json();
}

async function apiDelete(endpoint) {
    var response = await fetch(API_BASE + endpoint, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'
    });
    return response.json();
}

function showAlert(message, type, autoHide) {
    var box = document.getElementById('alertBox');
    if (!box) return;
    box.className = 'alert alert-' + type;
    box.textContent = message;
    box.style.display = 'block';
    if (autoHide !== false) {
        setTimeout(function () { box.style.display = 'none'; }, 4000);
    }
}
