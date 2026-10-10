/**
 * NEXUS - Authentication Handler
 */
document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('form-login');
  const registerForm = document.getElementById('form-register');

  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearErrors();
      const submitBtn = loginForm.querySelector('button[type="submit"]');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Authenticating...';

      const email = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value;

      try {
        await API.post('/api/auth/login', { email, password });
        showToast('Welcome back! Redirecting to dashboard...', 'success');
        setTimeout(() => { window.location.href = '/dashboard.html'; }, 800);
      } catch (err) {
        showError(err.message || 'Invalid email or password');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Sign in';
      }
    });
  }

  if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearErrors();
      const submitBtn = registerForm.querySelector('button[type="submit"]');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Creating builder profile...';

      const password = document.getElementById('password').value;
      const confirmPassword = document.getElementById('confirmPassword') ? document.getElementById('confirmPassword').value : password;

      if (password !== confirmPassword) {
        showError('Passwords do not match');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create Profile';
        return;
      }

      if (password.length < 6) {
        showError('Password must be at least 6 characters long');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create Profile';
        return;
      }

      const payload = {
        fullName: document.getElementById('fullName').value.trim(),
        email: document.getElementById('email').value.trim(),
        password: password,
        confirmPassword: confirmPassword,
        college: document.getElementById('college').value.trim(),
        degree: document.getElementById('degree').value.trim(),
        studyYear: document.getElementById('studyYear').value,
        skills: document.getElementById('skills') ? document.getElementById('skills').value.trim() : '',
        bio: document.getElementById('bio').value.trim()
      };

      try {
        await API.post('/api/auth/register', payload);
        showToast('Account created! Welcome to NEXUS.', 'success');
        setTimeout(() => { window.location.href = '/dashboard.html'; }, 800);
      } catch (err) {
        if (err.data && err.data.errors) {
          const firstKey = Object.keys(err.data.errors)[0];
          showError(err.data.errors[firstKey]);
        } else {
          showError(err.message || 'Registration failed');
        }
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create Profile';
      }
    });
  }
});

function showError(msg) {
  const errBox = document.getElementById('auth-error');
  if (errBox) {
    errBox.textContent = msg;
    errBox.style.display = 'block';
  } else {
    showToast(msg, 'error');
  }
}

function clearErrors() {
  const errBox = document.getElementById('auth-error');
  if (errBox) {
    errBox.style.display = 'none';
  }
}
