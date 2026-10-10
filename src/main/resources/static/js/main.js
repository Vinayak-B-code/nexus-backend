/**
 * NEXUS - Global Shell & Navigation
 */
let currentUser = null;

document.addEventListener('DOMContentLoaded', async () => {
  await initNavAndAuth();
});

async function initNavAndAuth() {
  currentUser = await API.getCurrentUser();
  renderNavAuth();
  highlightActiveLink();

  if (currentUser) {
    loadNotifications();
    // Poll unread notifications periodically
    setInterval(updateUnreadCount, 30000);
  }
}

function renderNavAuth() {
  const authContainer = document.getElementById('nav-auth');
  if (!authContainer) return;

  if (currentUser) {
    const initials = getInitials(currentUser.fullName);
    authContainer.innerHTML = `
      <!-- Notification Dropdown -->
      <div class="dropdown" style="position: relative;">
        <button id="btn-notifications" class="btn btn-secondary btn-sm" style="padding: 0.4rem 0.65rem; border-radius: 50%;" title="Notifications">
          <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/></svg>
          <span id="notif-badge" style="display: none; position: absolute; top: -3px; right: -3px; background: #E11D48; color: white; border-radius: 50%; font-size: 0.65rem; width: 16px; height: 16px; align-items: center; justify-content: center; font-weight: 700;">0</span>
        </button>
        <div id="notif-dropdown" class="dropdown-menu" style="width: 320px; right: 0;">
          <div style="padding: 0.75rem 1rem; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: 700; font-size: 0.85rem;">Notifications</span>
            <button onclick="markAllNotificationsRead()" class="btn btn-sm" style="font-size: 0.75rem; padding: 0.15rem 0.5rem;">Mark all read</button>
          </div>
          <div id="notif-list" style="max-height: 260px; overflow-y: auto;">
            <p style="padding: 1rem; text-align: center; color: var(--ink-muted); font-size: 0.85rem;">No new notifications</p>
          </div>
        </div>
      </div>

      <!-- User Menu -->
      <a href="/dashboard.html" class="avatar" style="background-color: ${currentUser.avatarColor || '#625BEE'}; text-decoration: none;" title="${escapeHtml(currentUser.fullName)}">
        ${initials}
      </a>
      <button onclick="API.logout()" class="btn btn-secondary btn-sm">Sign out</button>
    `;

    // Dropdown toggle
    const notifBtn = document.getElementById('btn-notifications');
    const notifMenu = document.getElementById('notif-dropdown');
    if (notifBtn && notifMenu) {
      notifBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        notifMenu.classList.toggle('show');
      });
      document.addEventListener('click', () => notifMenu.classList.remove('show'));
      notifMenu.addEventListener('click', (e) => e.stopPropagation());
    }
  } else {
    authContainer.innerHTML = `
      <a href="/login.html" class="btn btn-secondary btn-sm">Sign in</a>
      <a href="/register.html" class="btn btn-primary btn-sm">Join NEXUS</a>
    `;
  }
}

function highlightActiveLink() {
  const currentPath = window.location.pathname;
  const links = document.querySelectorAll('.nav-link');
  links.forEach(link => {
    const href = link.getAttribute('href');
    if (href === currentPath || (currentPath === '/' && href === '/index.html') || (href.includes(currentPath) && currentPath !== '/')) {
      link.classList.add('active');
    }
  });
}

function getInitials(name) {
  if (!name) return 'NX';
  const parts = name.trim().split(' ');
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase();
  }
  return name.substring(0, 2).toUpperCase();
}

async function loadNotifications() {
  try {
    const notifications = await API.get('/api/notifications');
    const listEl = document.getElementById('notif-list');
    if (!listEl) return;

    if (!notifications || notifications.length === 0) {
      listEl.innerHTML = `<p style="padding: 1.5rem; text-align: center; color: var(--ink-muted); font-size: 0.85rem;">You're all caught up!</p>`;
      return;
    }

    listEl.innerHTML = notifications.map(n => `
      <div class="dropdown-item" style="cursor: pointer; background: ${n.read ? 'transparent' : 'var(--indigo-subtle)'};" onclick="handleNotificationClick(${n.id}, '${n.link || ''}')">
        <div style="font-weight: 600; font-size: 0.825rem; color: var(--ink);">${escapeHtml(n.title)}</div>
        <div style="font-size: 0.775rem; color: var(--ink-secondary); margin-top: 2px;">${escapeHtml(n.message)}</div>
      </div>
    `).join('');

    updateUnreadCount();
  } catch (err) {
    console.error('Failed to load notifications', err);
  }
}

async function updateUnreadCount() {
  try {
    const res = await API.get('/api/notifications/unread-count');
    const badge = document.getElementById('notif-badge');
    if (badge && res && res.unreadCount > 0) {
      badge.textContent = res.unreadCount;
      badge.style.display = 'inline-flex';
    } else if (badge) {
      badge.style.display = 'none';
    }
  } catch (err) {
    // Silent fail on background poll
  }
}

async function handleNotificationClick(id, link) {
  try {
    await API.put(`/api/notifications/${id}/read`);
  } catch (e) {}
  if (link && link.trim() !== '') {
    window.location.href = link;
  } else {
    loadNotifications();
  }
}

async function markAllNotificationsRead() {
  try {
    await API.put('/api/notifications/read-all');
    showToast('All notifications marked as read', 'success');
    loadNotifications();
  } catch (e) {
    showToast('Failed to mark notifications', 'error');
  }
}
