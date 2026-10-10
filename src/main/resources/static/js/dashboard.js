/**
 * NEXUS - Dashboard & Profile Hub
 */
let userProfile = null;

document.addEventListener('DOMContentLoaded', async () => {
  const user = await API.getCurrentUser();
  if (!user) {
    window.location.href = '/login.html';
    return;
  }
  userProfile = user;
  await loadDashboardData();
});

async function loadDashboardData() {
  await fetchProfile();
  await loadActivitySummary();
  await loadActivityTrends('WEEKLY');
  await loadActivityBreakdown();
  await loadRecentActivities();
  await loadAcademicAchievements();
  await loadMyProjects();
  await loadMyCircles();
  await loadMyBounties();
  await loadIncomingRequests();
}

let currentTrendRange = 'WEEKLY';

function setTrendRange(range) {
  currentTrendRange = range;
  document.querySelectorAll('#trend-range-selector .range-pill').forEach(btn => {
    btn.classList.toggle('active', btn.textContent.toLowerCase().includes(range.toLowerCase().substring(0, 4)));
  });
  loadActivityTrends(range);
}

async function loadActivitySummary() {
  const container = document.getElementById('activity-summary-grid');
  if (!container) return;

  try {
    const summary = await API.get('/api/activities/summary');
    container.innerHTML = `
      <div class="activity-stat-card">
        <div class="activity-stat-icon" style="background: var(--indigo-light); color: var(--indigo);">⚡</div>
        <div class="activity-stat-info">
          <span class="activity-stat-value">${summary.totalActivities || 0}</span>
          <span class="activity-stat-label">Total Activities</span>
        </div>
      </div>
      <div class="activity-stat-card">
        <div class="activity-stat-icon" style="background: var(--blue-light); color: var(--blue);">🚀</div>
        <div class="activity-stat-info">
          <span class="activity-stat-value">${summary.projectsCreated || 0}</span>
          <span class="activity-stat-label">Projects Created</span>
        </div>
      </div>
      <div class="activity-stat-card">
        <div class="activity-stat-icon" style="background: var(--teal-light); color: var(--teal);">🤝</div>
        <div class="activity-stat-info">
          <span class="activity-stat-value">${summary.collaborationsJoined || 0}</span>
          <span class="activity-stat-label">Collabs Joined</span>
        </div>
      </div>
      <div class="activity-stat-card">
        <div class="activity-stat-icon" style="background: var(--amber-light); color: var(--amber);">🧬</div>
        <div class="activity-stat-info">
          <span class="activity-stat-value">${summary.skillsAdded || 0}</span>
          <span class="activity-stat-label">Skills Added</span>
        </div>
      </div>
      <div class="activity-stat-card">
        <div class="activity-stat-icon" style="background: #F3E8FF; color: #9333EA;">💬</div>
        <div class="activity-stat-info">
          <span class="activity-stat-value">${summary.commentsPosted || 0}</span>
          <span class="activity-stat-label">Comments Posted</span>
        </div>
      </div>
      <div class="activity-stat-card">
        <div class="activity-stat-icon" style="background: var(--rose-light); color: var(--rose);">🏆</div>
        <div class="activity-stat-info">
          <span class="activity-stat-value">${summary.academicAchievements || 0}</span>
          <span class="activity-stat-label">Achievements</span>
        </div>
      </div>
    `;
  } catch (err) {
    console.error('Failed to load activity summary:', err);
    container.innerHTML = `<p style="color: var(--rose); font-size: 0.9rem;">Failed to load activity metrics.</p>`;
  }
}

async function loadActivityTrends(range = 'WEEKLY') {
  const svg = document.getElementById('trend-svg');
  const tooltip = document.getElementById('chart-tooltip');
  if (!svg) return;

  try {
    const trend = await API.get(`/api/activities/trend?range=${range}`);
    const data = trend.dataPoints || [];
    renderLineChart(svg, tooltip, data);
  } catch (err) {
    console.error('Failed to load activity trends:', err);
  }
}

function renderLineChart(svg, tooltip, points) {
  if (!points || points.length === 0) {
    svg.innerHTML = `<text x="50%" y="50%" text-anchor="middle" fill="#718096" font-size="14">No activity points</text>`;
    return;
  }

  const width = svg.clientWidth || 550;
  const height = 220;
  const padding = { top: 25, right: 30, bottom: 40, left: 40 };

  const chartWidth = width - padding.left - padding.right;
  const chartHeight = height - padding.top - padding.bottom;

  const maxVal = Math.max(...points.map(p => p.count), 4);
  const minVal = 0;

  // Coordinate mappers
  const getX = (index) => padding.left + (index / (points.length - 1 || 1)) * chartWidth;
  const getY = (val) => padding.top + chartHeight - ((val - minVal) / (maxVal - minVal)) * chartHeight;

  // Build grid & labels
  let gridLines = '';
  const yTicks = 4;
  for (let i = 0; i <= yTicks; i++) {
    const yVal = Math.round((maxVal / yTicks) * i);
    const yPos = getY(yVal);
    gridLines += `
      <line x1="${padding.left}" y1="${yPos}" x2="${width - padding.right}" y2="${yPos}" stroke="#E2E8F0" stroke-dasharray="3 3" />
      <text x="${padding.left - 10}" y="${yPos + 4}" text-anchor="end" fill="#718096" font-size="11" font-family="sans-serif">${yVal}</text>
    `;
  }

  // Path coordinates
  const coords = points.map((p, i) => ({ x: getX(i), y: getY(p.count), data: p }));
  const pathD = coords.reduce((acc, pt, i) => (i === 0 ? `M ${pt.x} ${pt.y}` : `${acc} L ${pt.x} ${pt.y}`), '');
  const areaD = `${pathD} L ${coords[coords.length - 1].x} ${padding.top + chartHeight} L ${coords[0].x} ${padding.top + chartHeight} Z`;

  // X axis labels
  let xLabels = '';
  const labelStep = Math.ceil(points.length / 7);
  coords.forEach((pt, i) => {
    if (i % labelStep === 0 || i === coords.length - 1) {
      xLabels += `
        <text x="${pt.x}" y="${height - 12}" text-anchor="middle" fill="#718096" font-size="11" font-family="sans-serif">
          ${escapeHtml(pt.data.label)}
        </text>
      `;
    }
  });

  // Dots
  let dots = '';
  coords.forEach((pt, i) => {
    dots += `
      <circle class="chart-point" cx="${pt.x}" cy="${pt.y}" r="4" 
              data-label="${escapeHtml(pt.data.label)}" data-count="${pt.data.count}" data-date="${escapeHtml(pt.data.date)}"></circle>
    `;
  });

  svg.setAttribute('viewBox', `0 0 ${width} ${height}`);
  svg.innerHTML = `
    <defs>
      <linearGradient id="trendGradient" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#625BEE" stop-opacity="0.25"/>
        <stop offset="100%" stop-color="#625BEE" stop-opacity="0.0"/>
      </linearGradient>
    </defs>
    ${gridLines}
    <path d="${areaD}" fill="url(#trendGradient)" />
    <path d="${pathD}" fill="none" stroke="#625BEE" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
    ${xLabels}
    ${dots}
  `;

  // Attach hover handlers to dots
  svg.querySelectorAll('.chart-point').forEach(circle => {
    circle.addEventListener('mouseenter', (e) => {
      const label = circle.getAttribute('data-label');
      const count = circle.getAttribute('data-count');
      const cx = parseFloat(circle.getAttribute('cx'));
      const cy = parseFloat(circle.getAttribute('cy'));

      tooltip.innerHTML = `<strong>${label}</strong>: ${count} action${count == 1 ? '' : 's'}`;
      tooltip.style.left = `${cx}px`;
      tooltip.style.top = `${cy}px`;
      tooltip.style.display = 'block';
    });
    circle.addEventListener('mouseleave', () => {
      tooltip.style.display = 'none';
    });
  });
}

async function loadActivityBreakdown() {
  const container = document.getElementById('breakdown-container');
  if (!container) return;

  try {
    const breakdown = await API.get('/api/activities/breakdown');
    const categories = [
      { key: 'Projects', name: 'Projects', count: breakdown.projects || 0, color: '#625BEE' },
      { key: 'Collaborations', name: 'Collaborations', count: breakdown.collaborations || 0, color: '#0D9488' },
      { key: 'Skills', name: 'Skills', count: breakdown.skills || 0, color: '#D97706' },
      { key: 'Comments & Likes', name: 'Comments & Likes', count: (breakdown.comments || 0) + (breakdown.likes || 0), color: '#EC4899' },
      { key: 'Skill Bounties', name: 'Skill Bounties', count: breakdown.skillBounties || 0, color: '#8B5CF6' },
      { key: 'Academic Achievements', name: 'Academic Achievements', count: breakdown.academicAchievements || 0, color: '#E11D48' },
      { key: 'Profile & Community', name: 'Profile & Community', count: (breakdown.profileUpdates || 0) + (breakdown.logins || 0), color: '#3B82F6' }
    ];

    const total = categories.reduce((sum, c) => sum + c.count, 0);

    if (total === 0) {
      container.innerHTML = `
        <div style="text-align: center; padding: 2rem 1rem; color: var(--ink-secondary);">
          <p style="font-weight: 500;">No activity breakdown recorded yet.</p>
          <p style="font-size: 0.8rem; color: var(--ink-muted); margin-top: 0.25rem;">Start exploring projects or adding skills!</p>
        </div>
      `;
      return;
    }

    container.innerHTML = categories.map(cat => {
      const pct = total > 0 ? Math.round((cat.count / total) * 100) : 0;
      return `
        <div class="breakdown-row">
          <div class="breakdown-header">
            <span class="breakdown-cat">
              <span class="breakdown-cat-dot" style="background: ${cat.color};"></span>
              ${cat.name}
            </span>
            <span class="breakdown-count"><strong>${cat.count}</strong> (${pct}%)</span>
          </div>
          <div class="breakdown-track">
            <div class="breakdown-fill" style="width: ${pct}%; background: ${cat.color};"></div>
          </div>
        </div>
      `;
    }).join('');
  } catch (err) {
    console.error('Failed to load activity breakdown:', err);
    container.innerHTML = `<p style="color: var(--rose); font-size: 0.85rem;">Failed to load breakdown.</p>`;
  }
}

async function loadRecentActivities() {
  const container = document.getElementById('recent-activity-container');
  if (!container) return;

  try {
    const res = await API.get('/api/activities/recent?page=0&size=10');
    const logs = res.content || res || [];

    if (logs.length === 0) {
      container.innerHTML = `
        <div style="background: var(--card-bg); border: 1px dashed var(--border); border-radius: var(--radius-md); padding: 2.5rem; text-align: center;">
          <div style="font-size: 2rem; margin-bottom: 0.5rem;">🌱</div>
          <h4 style="font-size: 1.1rem; margin-bottom: 0.25rem;">No recent activities yet</h4>
          <p style="color: var(--ink-muted); font-size: 0.875rem; max-width: 450px; margin: 0 auto;">
            Every milestone you reach on NEXUS is recorded here: creating projects, adding skills, commenting, and earning academic achievements.
          </p>
        </div>
      `;
      return;
    }

    const iconMap = {
      'USER_LOGIN': '🔑',
      'USER_REGISTER': '🎉',
      'PROFILE_UPDATE': '👤',
      'SKILL_ADD': '🧬',
      'SKILL_UPDATE': '🧬',
      'SKILL_REMOVE': '🧬',
      'PROJECT_CREATE': '🚀',
      'PROJECT_UPDATE': '🛠️',
      'PROJECT_COLLAB_REQUEST': '🤝',
      'PROJECT_COLLAB_JOINED': '🎉',
      'PROOF_OF_BUILD_UPDATE': '🏆',
      'PROJECT_COMMENT': '💬',
      'PROJECT_LIKE': '❤️',
      'CIRCLE_CREATE': '⭕',
      'CIRCLE_JOIN_REQUEST': '🙋',
      'CIRCLE_JOINED': '🌟',
      'BOUNTY_CREATE': '🎯',
      'BOUNTY_CLAIM': '💡',
      'BOUNTY_ACCEPTED': '🤝',
      'BOUNTY_COMPLETED': '✅',
      'BOUNTY_SOLVED': '⭐',
      'ACHIEVEMENT_ADD': '🏅',
      'ACHIEVEMENT_UPDATE': '📝',
      'ACHIEVEMENT_DELETE': '🗑️'
    };

    container.innerHTML = logs.map(item => {
      const icon = iconMap[item.activityType] || '📌';
      return `
        <div class="timeline-item">
          <div class="timeline-node">${icon}</div>
          <div class="timeline-header">
            <span class="timeline-desc">${escapeHtml(item.description)}</span>
            <span class="timeline-time">${formatDate(item.createdAt)}</span>
          </div>
          <div style="margin-top: 0.25rem;">
            <span class="badge" style="background: var(--indigo-light); color: var(--indigo); font-size: 0.7rem; font-weight: 700;">
              ${escapeHtml(item.category || item.activityType)}
            </span>
          </div>
        </div>
      `;
    }).join('');
  } catch (err) {
    console.error('Failed to load recent activities:', err);
    container.innerHTML = `<p style="color: var(--rose); font-size: 0.85rem;">Failed to load activity timeline.</p>`;
  }
}

let myAchievements = [];

async function loadAcademicAchievements() {
  const container = document.getElementById('achievements-container');
  if (!container) return;

  try {
    myAchievements = await API.get('/api/achievements');
    if (myAchievements.length === 0) {
      container.innerHTML = `
        <div style="grid-column: 1 / -1; background: var(--card-bg); border: 1px dashed var(--border); border-radius: var(--radius-lg); padding: 2.5rem; text-align: center;">
          <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🏆</div>
          <h4 style="font-size: 1.15rem; margin-bottom: 0.25rem;">Showcase Your Academic & Professional Achievements</h4>
          <p style="color: var(--ink-secondary); font-size: 0.9rem; max-width: 500px; margin: 0 auto 1.25rem auto;">
            Add your hackathon wins, research papers, certifications, and awards to stand out when collaborating on student projects.
          </p>
          <button onclick="openAddAchievementModal()" class="btn btn-primary btn-sm">+ Add Your First Achievement</button>
        </div>
      `;
      return;
    }

    container.innerHTML = myAchievements.map(item => `
      <div class="achievement-card">
        <div>
          <div class="achievement-top">
            <span class="badge" style="background: var(--indigo-light); color: var(--indigo); font-weight: 700; font-size: 0.72rem;">
              ${escapeHtml(item.category)}
            </span>
            <div style="display: flex; gap: 0.35rem; align-items: center;">
              <span class="achievement-badge-self">${escapeHtml(item.verificationStatus || 'SELF_REPORTED')}</span>
              <span class="achievement-badge-vis ${item.isPublic ? 'public' : 'private'}">
                ${item.isPublic ? 'Public' : 'Private'}
              </span>
            </div>
          </div>
          <h4 class="achievement-title">${escapeHtml(item.title)}</h4>
          <div class="achievement-org">${escapeHtml(item.issuingOrganization)}</div>
          ${item.description ? `<p class="achievement-desc">${escapeHtml(item.description)}</p>` : ''}
          ${item.credentialUrl ? `
            <div style="margin-bottom: 0.75rem;">
              <a href="${escapeHtml(item.credentialUrl)}" target="_blank" rel="noopener noreferrer" style="font-size: 0.8rem; font-weight: 600; display: inline-flex; align-items: center; gap: 0.25rem;">
                Verify Credential ↗
              </a>
            </div>
          ` : ''}
        </div>
        <div>
          <div class="achievement-meta">
            <span>Issued: ${escapeHtml(item.issueDate || 'N/A')}</span>
          </div>
          <div class="achievement-actions">
            <button onclick="toggleAchievementVisibility(${item.id})" class="btn btn-secondary btn-sm" style="flex: 1; font-size: 0.775rem; padding: 0.4rem;">
              ${item.isPublic ? '🔒 Make Private' : '🌐 Make Public'}
            </button>
            <button onclick="openEditAchievementModal(${item.id})" class="btn btn-secondary btn-sm" style="font-size: 0.775rem; padding: 0.4rem 0.65rem;">
              Edit
            </button>
            <button onclick="deleteAchievement(${item.id})" class="btn btn-danger btn-sm" style="font-size: 0.775rem; padding: 0.4rem 0.65rem; background: var(--rose-light); color: var(--rose); border: 1px solid #FECDD3;">
              Delete
            </button>
          </div>
        </div>
      </div>
    `).join('');
  } catch (err) {
    console.error('Failed to load achievements:', err);
    container.innerHTML = `<p style="color: var(--rose); font-size: 0.85rem;">Failed to load academic achievements.</p>`;
  }
}

function openAddAchievementModal() {
  document.getElementById('achievement-id').value = '';
  document.getElementById('form-achievement').reset();
  document.getElementById('achievement-is-public').checked = true;
  document.getElementById('achievement-modal-title').textContent = 'Add Academic Achievement';
  document.getElementById('btn-save-achievement').textContent = 'Add Achievement';
  document.getElementById('modal-achievement').classList.add('active');
}

function openEditAchievementModal(id) {
  const item = myAchievements.find(a => a.id === id);
  if (!item) return;

  document.getElementById('achievement-id').value = item.id;
  document.getElementById('achievement-title').value = item.title;
  document.getElementById('achievement-category').value = item.category;
  document.getElementById('achievement-date').value = item.issueDate;
  document.getElementById('achievement-org').value = item.issuingOrganization;
  document.getElementById('achievement-url').value = item.credentialUrl || '';
  document.getElementById('achievement-description').value = item.description || '';
  document.getElementById('achievement-is-public').checked = item.isPublic;

  document.getElementById('achievement-modal-title').textContent = 'Edit Academic Achievement';
  document.getElementById('btn-save-achievement').textContent = 'Save Changes';
  document.getElementById('modal-achievement').classList.add('active');
}

function closeAchievementModal() {
  document.getElementById('modal-achievement').classList.remove('active');
}

async function handleSaveAchievement(e) {
  e.preventDefault();

  const id = document.getElementById('achievement-id').value;
  const credentialUrl = document.getElementById('achievement-url').value.trim();

  // URL security check (reject non-http/https)
  if (credentialUrl && !/^https?:\/\//i.test(credentialUrl)) {
    showToast('Credential URL must begin with http:// or https://', 'error');
    return;
  }

  const payload = {
    title: document.getElementById('achievement-title').value.trim(),
    category: document.getElementById('achievement-category').value,
    issueDate: document.getElementById('achievement-date').value,
    issuingOrganization: document.getElementById('achievement-org').value.trim(),
    credentialUrl: credentialUrl,
    description: document.getElementById('achievement-description').value.trim(),
    isPublic: document.getElementById('achievement-is-public').checked
  };

  try {
    if (id) {
      await API.put(`/api/achievements/${id}`, payload);
      showToast('Achievement updated successfully!', 'success');
    } else {
      await API.post('/api/achievements', payload);
      showToast('Achievement added successfully!', 'success');
    }
    closeAchievementModal();
    await loadAcademicAchievements();
    await loadActivitySummary();
    await loadActivityBreakdown();
    await loadRecentActivities();
    await loadActivityTrends(currentTrendRange);
  } catch (err) {
    showToast(err.message || 'Failed to save achievement', 'error');
  }
}

async function toggleAchievementVisibility(id) {
  try {
    const updated = await API.put(`/api/achievements/${id}/visibility`);
    showToast(`Achievement is now ${updated.isPublic ? 'Public' : 'Private'}`, 'info');
    await loadAcademicAchievements();
  } catch (err) {
    showToast(err.message || 'Failed to toggle visibility', 'error');
  }
}

async function deleteAchievement(id) {
  if (!confirm('Are you sure you want to delete this achievement? This action cannot be undone.')) {
    return;
  }

  try {
    await API.delete(`/api/achievements/${id}`);
    showToast('Achievement deleted successfully', 'info');
    await loadAcademicAchievements();
    await loadActivitySummary();
    await loadActivityBreakdown();
    await loadRecentActivities();
    await loadActivityTrends(currentTrendRange);
  } catch (err) {
    showToast(err.message || 'Failed to delete achievement', 'error');
  }
}

async function fetchProfile() {
  try {
    userProfile = await API.get('/api/users/profile');
    renderProfileHeader();
    renderSkillDna();
  } catch (e) {
    showToast('Failed to load profile details', 'error');
  }
}

function renderProfileHeader() {
  const container = document.getElementById('profile-header-card');
  if (!container) return;

  const initials = getInitials(userProfile.fullName);
  container.innerHTML = `
    <div style="display: flex; gap: 1.5rem; align-items: flex-start; flex-wrap: wrap;">
      <div class="avatar avatar-lg" style="background-color: ${userProfile.avatarColor || '#625BEE'};">
        ${initials}
      </div>
      <div style="flex: 1; min-width: 250px;">
        <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
          <h2 style="font-size: 1.75rem;">${escapeHtml(userProfile.fullName)}</h2>
          <span class="badge" style="background: var(--indigo-light); color: var(--indigo); font-weight: 700;">
            ${escapeHtml(userProfile.studyYear || 'Student')}
          </span>
          ${userProfile.role === 'ROLE_ADMIN' ? '<span class="badge" style="background: #FEF3C7; color: #92400E;">Platform Admin</span>' : ''}
        </div>
        <p style="color: var(--ink-secondary); font-size: 0.95rem; margin-top: 0.25rem;">
          ${escapeHtml(userProfile.college || 'College')} ${userProfile.degree ? '• ' + escapeHtml(userProfile.degree) : ''}
        </p>
        <p style="margin-top: 0.75rem; font-size: 0.95rem; color: var(--ink); max-width: 650px;">
          ${escapeHtml(userProfile.bio || 'Add a bio to let other student builders know what you enjoy working on!')}
        </p>
        
        <div style="display: flex; gap: 1rem; margin-top: 1rem; font-size: 0.875rem;">
          ${userProfile.githubUrl ? `<a href="${escapeHtml(userProfile.githubUrl)}" target="_blank" rel="noopener">GitHub ↗</a>` : ''}
          ${userProfile.linkedinUrl ? `<a href="${escapeHtml(userProfile.linkedinUrl)}" target="_blank" rel="noopener">LinkedIn ↗</a>` : ''}
        </div>
      </div>
      <div>
        <button onclick="openEditProfileModal()" class="btn btn-secondary btn-sm">Edit Profile</button>
      </div>
    </div>

    <!-- Quick stats strip -->
    <div style="display: flex; gap: 2rem; border-top: 1px solid var(--border); margin-top: 1.5rem; padding-top: 1.25rem; font-size: 0.9rem;">
      <div><strong>${userProfile.projectCount}</strong> <span style="color: var(--ink-muted);">Projects</span></div>
      <div><strong>${userProfile.circleCount}</strong> <span style="color: var(--ink-muted);">Build Circles</span></div>
      <div><strong>${userProfile.connectionCount}</strong> <span style="color: var(--ink-muted);">Connections</span></div>
    </div>
  `;
}

function renderSkillDna() {
  const container = document.getElementById('skill-dna-section');
  if (!container) return;

  const skills = userProfile.skills || [];
  const dna = userProfile.skillDna || {};

  const proficient = skills.filter(s => s.skillType === 'PROFICIENT');
  const teach = skills.filter(s => s.skillType === 'CAN_TEACH');
  const learn = skills.filter(s => s.skillType === 'WANTS_TO_LEARN');

  // DNA category distribution
  const categories = ['Frontend', 'Backend', 'Data and AI', 'Design', 'DevOps', 'Mobile'];
  const totalProficient = proficient.length || 1;

  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
      <div>
        <span class="eyebrow" style="margin-bottom: 0.25rem;">SIGNATURE ARCHITECTURE</span>
        <h3 style="font-size: 1.25rem;">Your Skill DNA</h3>
      </div>
      <button onclick="openAddSkillModal()" class="btn btn-primary btn-sm">+ Add Skill</button>
    </div>

    <!-- Category balance visualizer -->
    <div style="background: var(--bg); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 1rem; margin-bottom: 1.5rem;">
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 1rem;">
        ${categories.map(cat => {
          const count = dna[cat] || 0;
          const pct = Math.round((count / totalProficient) * 100);
          return `
            <div>
              <div class="dna-label">
                <span>${cat}</span>
                <span>${count}</span>
              </div>
              <div class="dna-progress">
                <div class="dna-fill" style="width: ${pct}%;"></div>
              </div>
            </div>
          `;
        }).join('')}
      </div>
    </div>

    <!-- Skill Chips Groups -->
    <div style="margin-bottom: 1.25rem;">
      <h4 style="font-size: 0.875rem; text-transform: uppercase; color: var(--ink-muted); margin-bottom: 0.5rem; letter-spacing: 0.05em;">Proficient In</h4>
      <div style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
        ${proficient.length > 0 ? proficient.map(s => renderSkillTag(s)).join('') : '<span style="color: var(--ink-muted); font-size: 0.85rem;">No proficient skills added yet.</span>'}
      </div>
    </div>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
      <div>
        <h4 style="font-size: 0.875rem; text-transform: uppercase; color: var(--ink-muted); margin-bottom: 0.5rem; letter-spacing: 0.05em;">Can Teach & Mentor</h4>
        <div style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
          ${teach.length > 0 ? teach.map(s => renderSkillTag(s)).join('') : '<span style="color: var(--ink-muted); font-size: 0.85rem;">None added yet.</span>'}
        </div>
      </div>
      <div>
        <h4 style="font-size: 0.875rem; text-transform: uppercase; color: var(--ink-muted); margin-bottom: 0.5rem; letter-spacing: 0.05em;">Wants to Learn</h4>
        <div style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
          ${learn.length > 0 ? learn.map(s => renderSkillTag(s)).join('') : '<span style="color: var(--ink-muted); font-size: 0.85rem;">None added yet.</span>'}
        </div>
      </div>
    </div>
  `;
}

function renderSkillTag(skill) {
  const catClass = skill.category ? skill.category.toLowerCase().replace(/[^a-z0-9]/g, '-') : 'general';
  return `
    <span class="skill-chip ${catClass}">
      ${escapeHtml(skill.skillName)}
      <span style="font-size: 0.65rem; opacity: 0.7;">(${escapeHtml(skill.proficiencyLevel.substring(0, 3))})</span>
      <button onclick="removeSkill(${skill.id})" style="border: none; background: transparent; cursor: pointer; color: inherit; padding-left: 2px; font-weight: bold;" title="Remove skill">×</button>
    </span>
  `;
}

async function removeSkill(id) {
  try {
    await API.delete(`/api/skills/${id}`);
    showToast('Skill removed from profile', 'info');
    await fetchProfile();
  } catch (e) {
    showToast('Failed to remove skill', 'error');
  }
}

// Edit Profile Modal
function openEditProfileModal() {
  document.getElementById('edit-fullName').value = userProfile.fullName || '';
  document.getElementById('edit-college').value = userProfile.college || '';
  document.getElementById('edit-degree').value = userProfile.degree || '';
  document.getElementById('edit-studyYear').value = userProfile.studyYear || 'Year 1';
  document.getElementById('edit-bio').value = userProfile.bio || '';
  document.getElementById('edit-githubUrl').value = userProfile.githubUrl || '';
  document.getElementById('edit-linkedinUrl').value = userProfile.linkedinUrl || '';
  document.getElementById('modal-edit-profile').classList.add('active');
}

function closeEditProfileModal() {
  document.getElementById('modal-edit-profile').classList.remove('active');
}

async function handleProfileUpdate(e) {
  e.preventDefault();
  const payload = {
    fullName: document.getElementById('edit-fullName').value.trim(),
    college: document.getElementById('edit-college').value.trim(),
    degree: document.getElementById('edit-degree').value.trim(),
    studyYear: document.getElementById('edit-studyYear').value,
    bio: document.getElementById('edit-bio').value.trim(),
    githubUrl: document.getElementById('edit-githubUrl').value.trim(),
    linkedinUrl: document.getElementById('edit-linkedinUrl').value.trim()
  };

  try {
    await API.put('/api/users/profile', payload);
    showToast('Profile updated successfully!', 'success');
    closeEditProfileModal();
    await fetchProfile();
  } catch (err) {
    showToast(err.message || 'Failed to update profile', 'error');
  }
}

// Add Skill Modal
function openAddSkillModal() {
  document.getElementById('modal-add-skill').classList.add('active');
}

function closeAddSkillModal() {
  document.getElementById('modal-add-skill').classList.remove('active');
}

async function handleAddSkill(e) {
  e.preventDefault();
  const payload = {
    skillName: document.getElementById('new-skill-name').value.trim(),
    category: document.getElementById('new-skill-category').value,
    skillType: document.getElementById('new-skill-type').value,
    proficiencyLevel: document.getElementById('new-skill-level').value
  };

  try {
    await API.post('/api/skills', payload);
    showToast('Skill added!', 'success');
    closeAddSkillModal();
    document.getElementById('form-add-skill').reset();
    await fetchProfile();
  } catch (err) {
    showToast(err.message || 'Failed to add skill', 'error');
  }
}

// My Projects
async function loadMyProjects() {
  const container = document.getElementById('my-projects-container');
  if (!container) return;

  try {
    const projects = await API.get('/api/projects/my');
    if (projects.length === 0) {
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">🚀</div>
          <h3>No projects yet</h3>
          <p>Showcase an ongoing side project or rescue a stalled repository.</p>
          <a href="/projects.html?action=new" class="btn btn-primary btn-sm">+ Create Project</a>
        </div>
      `;
      return;
    }

    container.innerHTML = `
      <div class="grid-2">
        ${projects.map(p => `
          <div class="card">
            <div class="card-body">
              <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
                <span class="badge ${p.rescue ? 'badge-rescue' : 'badge-in-progress'}">${p.rescue ? 'Needs Rescue' : p.status}</span>
                <span style="font-size: 0.75rem; color: var(--ink-muted);">${p.memberCount} member(s)</span>
              </div>
              <h3 style="font-size: 1.15rem; margin-bottom: 0.5rem;">
                <a href="/projects.html?id=${p.id}">${escapeHtml(p.title)}</a>
              </h3>
              <p style="font-size: 0.875rem; color: var(--ink-secondary); margin-bottom: 1rem;">
                ${escapeHtml(p.shortDescription || 'No description.')}
              </p>
              <div style="display: flex; gap: 0.5rem; justify-content: space-between; align-items: center; border-top: 1px solid var(--border); padding-top: 0.75rem;">
                <button onclick="openProofUpdateModal(${p.id}, '${escapeHtml(p.title)}')" class="btn btn-secondary btn-sm" style="font-size: 0.775rem;">+ Proof of Build</button>
                <a href="/projects.html?id=${p.id}" class="btn btn-sm" style="font-size: 0.775rem;">View Details →</a>
              </div>
            </div>
          </div>
        `).join('')}
      </div>
    `;
  } catch (e) {
    container.innerHTML = `<p style="color: var(--rose);">Could not load projects.</p>`;
  }
}

// My Build Circles
async function loadMyCircles() {
  const container = document.getElementById('my-circles-container');
  if (!container) return;

  try {
    const circles = await API.get('/api/circles/my');
    if (circles.length === 0) {
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">⭕</div>
          <h3>Not in any Build Circles</h3>
          <p>Join a small squad of student builders or form your own circle.</p>
          <a href="/circles.html?action=new" class="btn btn-primary btn-sm">+ Create Build Circle</a>
        </div>
      `;
      return;
    }

    container.innerHTML = `
      <div class="grid-2">
        ${circles.map(c => `
          <div class="card">
            <div class="card-body">
              <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
                <span class="badge badge-in-progress">${escapeHtml(c.progressStatus)}</span>
                <span style="font-size: 0.75rem; color: var(--ink-muted);">${c.currentMemberCount}/${c.maxMembers} Members</span>
              </div>
              <h3 style="font-size: 1.15rem; margin-bottom: 0.5rem;">
                <a href="/circles.html?id=${c.id}">${escapeHtml(c.name)}</a>
              </h3>
              <p style="font-size: 0.875rem; color: var(--ink-secondary); margin-bottom: 0.75rem;">
                ${escapeHtml(c.projectGoal || c.description || '')}
              </p>
              <div class="dna-label" style="font-size: 0.75rem; margin-bottom: 4px;">
                <span>Milestones Progress</span>
                <span>${c.progressPercentage}%</span>
              </div>
              <div class="dna-progress" style="margin-bottom: 1rem;">
                <div class="dna-fill" style="width: ${c.progressPercentage}%;"></div>
              </div>
              <a href="/circles.html?id=${c.id}" class="btn btn-secondary btn-sm" style="width: 100%;">Open Circle Board →</a>
            </div>
          </div>
        `).join('')}
      </div>
    `;
  } catch (e) {
    container.innerHTML = `<p style="color: var(--rose);">Could not load circles.</p>`;
  }
}

// My Bounties
async function loadMyBounties() {
  const container = document.getElementById('my-bounties-container');
  if (!container) return;

  try {
    const bounties = await API.get('/api/bounties/my');
    if (bounties.length === 0) {
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">🎯</div>
          <h3>No open bounties</h3>
          <p>Stuck on a tricky bug or need pair programming help? Post a skill challenge.</p>
          <a href="/bounties.html?action=new" class="btn btn-primary btn-sm">+ Post Bounty</a>
        </div>
      `;
      return;
    }

    container.innerHTML = `
      <div class="grid-2">
        ${bounties.map(b => `
          <div class="card">
            <div class="card-body">
              <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
                <span class="badge ${b.status === 'COMPLETED' ? 'badge-completed' : 'badge-in-progress'}">${b.status}</span>
                <span class="badge" style="background: var(--bg); color: var(--ink);">${escapeHtml(b.bountyType)}</span>
              </div>
              <h3 style="font-size: 1.1rem; margin-bottom: 0.5rem;">
                <a href="/bounties.html?id=${b.id}">${escapeHtml(b.title)}</a>
              </h3>
              <p style="font-size: 0.85rem; color: var(--ink-secondary); margin-bottom: 0.75rem;">
                ${escapeHtml(b.description)}
              </p>
              <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border); padding-top: 0.75rem;">
                <span style="font-size: 0.8rem; color: var(--indigo); font-weight: 600;">${b.responseCount} helper offer(s)</span>
                <a href="/bounties.html?id=${b.id}" class="btn btn-secondary btn-sm" style="font-size: 0.775rem;">View Responses</a>
              </div>
            </div>
          </div>
        `).join('')}
      </div>
    `;
  } catch (e) {
    container.innerHTML = `<p style="color: var(--rose);">Could not load bounties.</p>`;
  }
}

// Incoming Requests (Collaboration, Circle, Connections)
async function loadIncomingRequests() {
  const container = document.getElementById('incoming-requests-container');
  if (!container) return;

  try {
    const [collabReqs, circleReqs, connReqs] = await Promise.all([
      API.get('/api/projects/collaboration-requests/pending'),
      API.get('/api/circles/join-requests/pending'),
      API.get('/api/connections/pending')
    ]);

    const total = collabReqs.length + circleReqs.length + connReqs.length;
    if (total === 0) {
      container.innerHTML = `
        <div style="padding: 1.5rem; text-align: center; color: var(--ink-muted); font-size: 0.9rem; border: 1px dashed var(--border); border-radius: var(--radius-md);">
          No pending collaboration or connection requests right now.
        </div>
      `;
      return;
    }

    let html = '<div style="display: flex; flex-direction: column; gap: 0.85rem;">';

    // Connection Requests
    connReqs.forEach(c => {
      html += `
        <div style="display: flex; align-items: center; justify-content: space-between; background: #FFFFFF; border: 1px solid var(--border); border-radius: var(--radius-md); padding: 0.85rem 1.25rem;">
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <div class="avatar avatar-sm" style="background: ${c.otherUserAvatarColor || '#625BEE'};">${getInitials(c.otherUserName)}</div>
            <div>
              <strong>${escapeHtml(c.otherUserName)}</strong> wants to connect.
              <div style="font-size: 0.75rem; color: var(--ink-secondary);">${escapeHtml(c.otherUserCollege || '')}</div>
            </div>
          </div>
          <div style="display: flex; gap: 0.5rem;">
            <button onclick="handleConnReq(${c.id}, true)" class="btn btn-primary btn-sm" style="font-size: 0.75rem;">Accept</button>
            <button onclick="handleConnReq(${c.id}, false)" class="btn btn-secondary btn-sm" style="font-size: 0.75rem;">Decline</button>
          </div>
        </div>
      `;
    });

    // Project Collab Requests
    collabReqs.forEach(cr => {
      html += `
        <div style="display: flex; align-items: center; justify-content: space-between; background: #FFFFFF; border: 1px solid var(--border); border-radius: var(--radius-md); padding: 0.85rem 1.25rem;">
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <div class="avatar avatar-sm" style="background: ${cr.requesterAvatarColor || '#625BEE'};">${getInitials(cr.requesterName)}</div>
            <div>
              <strong>${escapeHtml(cr.requesterName)}</strong> requested to join project <strong>${escapeHtml(cr.projectTitle)}</strong>
              <div style="font-size: 0.8rem; color: var(--ink-secondary); font-style: italic;">"${escapeHtml(cr.message)}"</div>
            </div>
          </div>
          <div style="display: flex; gap: 0.5rem;">
            <button onclick="handleCollabReq(${cr.id}, true)" class="btn btn-primary btn-sm" style="font-size: 0.75rem;">Accept</button>
            <button onclick="handleCollabReq(${cr.id}, false)" class="btn btn-secondary btn-sm" style="font-size: 0.75rem;">Decline</button>
          </div>
        </div>
      `;
    });

    // Circle Join Requests
    circleReqs.forEach(jr => {
      html += `
        <div style="display: flex; align-items: center; justify-content: space-between; background: #FFFFFF; border: 1px solid var(--border); border-radius: var(--radius-md); padding: 0.85rem 1.25rem;">
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <div class="avatar avatar-sm" style="background: ${jr.requesterAvatarColor || '#625BEE'};">${getInitials(jr.requesterName)}</div>
            <div>
              <strong>${escapeHtml(jr.requesterName)}</strong> wants to join Circle <strong>${escapeHtml(jr.circleName)}</strong>
              <div style="font-size: 0.8rem; color: var(--ink-secondary); font-style: italic;">"${escapeHtml(jr.message)}"</div>
            </div>
          </div>
          <div style="display: flex; gap: 0.5rem;">
            <button onclick="handleCircleJoinReq(${jr.id}, true)" class="btn btn-primary btn-sm" style="font-size: 0.75rem;">Accept</button>
            <button onclick="handleCircleJoinReq(${jr.id}, false)" class="btn btn-secondary btn-sm" style="font-size: 0.75rem;">Decline</button>
          </div>
        </div>
      `;
    });

    html += '</div>';
    container.innerHTML = html;
  } catch (e) {
    container.innerHTML = `<p style="color: var(--rose);">Could not load incoming requests.</p>`;
  }
}

async function handleConnReq(id, accept) {
  try {
    await API.post(`/api/connections/${id}/respond?accept=${accept}`);
    showToast(accept ? 'Connection accepted!' : 'Request declined', 'info');
    loadIncomingRequests();
    fetchProfile();
  } catch (e) {
    showToast(e.message || 'Action failed', 'error');
  }
}

async function handleCollabReq(id, accept) {
  try {
    await API.post(`/api/projects/collaboration-requests/${id}/respond?accept=${accept}`);
    showToast(accept ? 'Collaborator added to project!' : 'Request declined', 'info');
    loadIncomingRequests();
    loadMyProjects();
  } catch (e) {
    showToast(e.message || 'Action failed', 'error');
  }
}

async function handleCircleJoinReq(id, accept) {
  try {
    await API.post(`/api/circles/join-requests/${id}/respond?accept=${accept}`);
    showToast(accept ? 'New member added to Circle!' : 'Request declined', 'info');
    loadIncomingRequests();
    loadMyCircles();
  } catch (e) {
    showToast(e.message || 'Action failed', 'error');
  }
}

// Proof of Build Modal
let currentProofProjectId = null;
function openProofUpdateModal(projectId, title) {
  currentProofProjectId = projectId;
  document.getElementById('proof-modal-project-title').textContent = title;
  document.getElementById('form-proof-update').reset();
  document.getElementById('modal-proof-update').classList.add('active');
}

function closeProofUpdateModal() {
  document.getElementById('modal-proof-update').classList.remove('active');
}

async function handleProofSubmit(e) {
  e.preventDefault();
  if (!currentProofProjectId) return;

  const payload = {
    title: document.getElementById('proof-title').value.trim(),
    description: document.getElementById('proof-description').value.trim(),
    milestones: document.getElementById('proof-milestones').value.trim(),
    challenges: document.getElementById('proof-challenges').value.trim(),
    lessonsLearned: document.getElementById('proof-lessons').value.trim()
  };

  try {
    await API.post(`/api/projects/${currentProofProjectId}/updates`, payload);
    showToast('Proof of Build milestone logged successfully!', 'success');
    closeProofUpdateModal();
  } catch (err) {
    showToast(err.message || 'Failed to post update', 'error');
  }
}
