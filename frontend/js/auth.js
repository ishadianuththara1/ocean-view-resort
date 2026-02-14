document.addEventListener('DOMContentLoaded', function() {

    if (localStorage.getItem('username')) {
        window.location.href = 'index.html';
        return;
    }

    var form     = document.getElementById('loginForm');
    var errorDiv = document.getElementById('loginError');

    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        var username = document.getElementById('username').value.trim();
        var password = document.getElementById('password').value;

        errorDiv.style.display = 'none';

        if (!username) {
            showError('Please enter your username.');
            return;
        }
        if (!password) {
            showError('Please enter your password.');
            return;
        }

        var submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Logging in...';

        try {
            var response = await fetch('http://localhost:8080/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ username: username, password: password })
            });

            var data = await response.json();

            if (data.success) {
                localStorage.setItem('username',  data.username);
                localStorage.setItem('fullName',  data.fullName);
                window.location.href = 'index.html';
            } else {
                showError(data.message || 'Login failed. Please check your credentials.');
            }

        } catch (err) {
            showError('Cannot connect to the server. Please make sure the Java server is running on port 8080.');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Login';
        }
    });

    function showError(msg) {
        errorDiv.textContent    = msg;
        errorDiv.style.display  = 'block';
    }
});
