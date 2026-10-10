/**
 * NEXUS - Project Showcase & Ghost Project Rescue
 */
let allProjects = [];
let currentFilterRescue = null;

document.addEventListener('DOMContentLoaded', async () => {
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get('rescue') === 'true') {
    currentFilterRescue = true;
    const rescueTab = document.getElementById('tab-rescue');
    if (rescueTab) rescueTab.classList.add('active');
    const allTab = document.getElementById('tab-all');
    if (allTab) allTab.classList.remove('active');
  }

  if (urlParams.get('action') === 'new') {
    openCreateProjectModal();
  }

  if (urlParams.get('id')) {
    openProjectDetailsModal(urlParams.get('id'));
  }

  await loadProjects();

  const searchInput = document.getElementById('search-query');
  const catSelect = document.getElementById('filter-category');
  const statusSelect = document.getElementById('filter-status');

  if (searchInput) searchInput.addEventListener('input', debounce(loadProjects, 300));
  if (catSelect) catSelect.addEventListener('change', loadProjects);
  if (statusSelect) statusSelect.addEventListener('change', loadProjects);
});

function setTab(isRescue) {
  currentFilterRescue = isRescue;
  document.getElementById('tab-all')?.classList.toggle('btn-primary', !isRescue);
  document.getElementById('tab-all')?.classList.toggle('btn-secondary', isRescue);
  document.getElementById('tab-rescue')?.classList.toggle('btn-primary', isRescue);
  document.getElementById('tab-rescue')?.classList.toggle('btn-secondary', !isRescue);
  loadProjects();
}

async function loadProjects() {
  const container = document.getElementById('projects-grid');
  if (!container) return;

  const query = document.getElementById('search-query')?.value || '';
  const category = document.getElementById('filter-category')?.value || '';
  const status = document.getElementById('filter-status')?.value || '';

  container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 3rem;"><span style="color: var(--indigo); font-weight: 600;">Loading project portfolio...</span></div>`;

  try {
    const params = new URLSearchParams();
    if (query.trim()) params.append('query', query.trim());
    if (category.trim()) params.append('category', category.trim());
    if (status.trim()) params.append('status', status.trim());
    if (currentFilterRescue !== null) params.append('isRescue', currentFilterRescue);

    const projects = await API.get(`/api/projects?${params.toString()}`);
    allProjects = projects;

    if (!projects || projects.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="grid-column: 1/-1;">
          <div class="empty-state-icon">📂</div>
          <h3>${currentFilterRescue ? 'No projects currently awaiting rescue' : 'No projects found'}</h3>
          <p>${currentFilterRescue ? 'Have an unfinished side project gathering dust? Submit it for community rescue.' : 'Try adjusting your search criteria or create the first project.'}</p>
          <button onclick="openCreateProjectModal(${currentFilterRescue ? 'true' : 'false'})" class="btn btn-primary btn-sm">
            ${currentFilterRescue ? 'Rescue a Project' : '+ Create Project'}
          </button>
        </div>
      `;
      return;
    }

    container.innerHTML = projects.map(p => renderProjectCard(p)).join('');
  } catch (err) {
    container.innerHTML = `<div class="empty-state" style="grid-column: 1/-1;"><p style="color: var(--rose);">Could not load projects from backend.</p></div>`;
  }
}

function renderProjectCard(p) {
  const techs = p.technologies ? p.technologies.split(',').map(t => t.trim()).filter(Boolean) : [];

  return `
    <div class="card" onclick="openProjectDetailsModal(${p.id})" style="cursor: pointer;">
      <div class="card-header-art ${p.rescue ? 'rescue' : ''}" style="display: flex; align-items: center; justify-content: space-between; padding: 1rem 1.25rem;">
        <span class="badge ${p.rescue ? 'badge-rescue' : 'badge-in-progress'}">
          ${p.rescue ? '🚨 NEEDS A SECOND LIFE' : p.status}
        </span>
        <span style="font-size: 0.75rem; font-weight: 600; color: var(--ink-secondary); background: rgba(255,255,255,0.7); padding: 2px 8px; border-radius: 12px;">
          ${escapeHtml(p.category || 'Web')}
        </span>
      </div>

      <div class="card-body">
        <h3 style="font-size: 1.2rem; margin-bottom: 0.5rem; line-height: 1.3;">
          ${escapeHtml(p.title)}
        </h3>

        <p style="font-size: 0.875rem; color: var(--ink-secondary); line-height: 1.5; margin-bottom: 1rem; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
          ${escapeHtml(p.shortDescription || p.detailedDescription || 'No description provided.')}
        </p>

        ${p.rescue && p.rescueBlockers ? `
          <div style="background: #FFFBEB; border: 1px solid #FDE68A; border-radius: var(--radius-sm); padding: 0.65rem; margin-bottom: 1rem; font-size: 0.8rem; color: #92400E;">
            <strong>Blocker:</strong> ${escapeHtml(p.rescueBlockers)}
          </div>
        ` : ''}

        <!-- Tech Chips -->
        <div style="display: flex; flex-wrap: wrap; gap: 0.35rem; margin-bottom: 1rem;">
          ${techs.slice(0, 4).map(t => `<span class="skill-chip" style="font-size: 0.7rem;">${escapeHtml(t)}</span>`).join('')}
          ${techs.length > 4 ? `<span style="font-size: 0.7rem; color: var(--ink-muted);">+${techs.length - 4}</span>` : ''}
        </div>

        <!-- Footer -->
        <div style="display: flex; align-items: center; justify-content: space-between; border-top: 1px solid var(--border); padding-top: 0.85rem; font-size: 0.8rem; color: var(--ink-secondary);">
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <div class="avatar avatar-sm" style="background: ${p.creatorAvatarColor || '#625BEE'};">${getInitials(p.creatorName)}</div>
            <span>${escapeHtml(p.creatorName)}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 0.85rem;">
            <span title="Likes">❤️ ${p.likeCount || 0}</span>
            <span title="Comments">💬 ${p.commentCount || 0}</span>
            <span>${p.memberCount} builder(s)</span>
          </div>
        </div>
      </div>
    </div>
  `;
}

// Project Details Modal
async function openProjectDetailsModal(id) {
  const modal = document.getElementById('modal-project-details');
  const content = document.getElementById('project-details-content');
  if (!modal || !content) return;

  modal.classList.add('active');
  content.innerHTML = `<div style="text-align: center; padding: 2rem;"><span style="color: var(--indigo); font-weight: 600;">Loading project details...</span></div>`;

  try {
    const p = await API.get(`/api/projects/${id}`);
    const techs = p.technologies ? p.technologies.split(',').map(t => t.trim()).filter(Boolean) : [];

    let likeInfo = { liked: false, likeCount: p.likeCount || 0 };
    try {
      likeInfo = await API.get(`/api/projects/${id}/likes`);
    } catch (e) {}

    let comments = [];
    try {
      comments = await API.get(`/api/projects/${id}/comments`);
    } catch (e) {}

    content.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem; flex-wrap: wrap; gap: 0.75rem;">
        <div>
          <span class="badge ${p.rescue ? 'badge-rescue' : 'badge-in-progress'}" style="margin-bottom: 0.5rem;">
            ${p.rescue ? '🚨 NEEDS A SECOND LIFE' : p.status}
          </span>
          <h2 style="font-size: 1.75rem;">${escapeHtml(p.title)}</h2>
          <p style="font-size: 0.875rem; color: var(--ink-secondary); margin-top: 2px;">
            Created by <strong>${escapeHtml(p.creatorName)}</strong> (${escapeHtml(p.creatorCollege || 'University')})
          </p>
        </div>
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <button id="btn-project-like-${p.id}" onclick="toggleProjectLike(${p.id})" class="like-btn ${likeInfo.liked ? 'liked' : ''}">
            ❤️ <span id="like-count-display">${likeInfo.likeCount}</span>
          </button>
          ${p.githubUrl ? `<a href="${escapeHtml(p.githubUrl)}" target="_blank" rel="noopener" class="btn btn-secondary btn-sm">GitHub ↗</a>` : ''}
          ${p.demoUrl ? `<a href="${escapeHtml(p.demoUrl)}" target="_blank" rel="noopener" class="btn btn-primary btn-sm">Demo ↗</a>` : ''}
        </div>
      </div>

      <!-- Description -->
      <div style="margin-bottom: 1.5rem; line-height: 1.6; font-size: 0.95rem;">
        ${escapeHtml(p.detailedDescription || p.shortDescription || '')}
      </div>

      <!-- Ghost Project Rescue Diagnostic -->
      ${p.rescue ? `
        <div style="background: #FFFBEB; border: 1px solid #FDE68A; border-radius: var(--radius-md); padding: 1.25rem; margin-bottom: 1.5rem;">
          <h4 style="color: #92400E; font-size: 0.9rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">
            Ghost Project Rescue Request
          </h4>
          <p style="font-size: 0.9rem; color: #78350F; margin-bottom: 0.5rem;">
            <strong>What's blocking this idea:</strong> ${escapeHtml(p.rescueBlockers || 'Not specified')}
          </p>
          <p style="font-size: 0.9rem; color: #78350F;">
            <strong>Skills urgently needed:</strong> ${escapeHtml(p.rescueSkillsNeeded || 'Open collaboration')}
          </p>
        </div>
      ` : ''}

      <!-- Tech Stack -->
      <div style="margin-bottom: 1.5rem;">
        <h4 style="font-size: 0.8rem; text-transform: uppercase; color: var(--ink-muted); margin-bottom: 0.4rem;">Tech Stack</h4>
        <div style="display: flex; flex-wrap: wrap; gap: 0.4rem;">
          ${techs.map(t => `<span class="skill-chip">${escapeHtml(t)}</span>`).join('')}
        </div>
      </div>

      <!-- Proof of Build (Updates) -->
      <div style="margin-bottom: 1.5rem; border-top: 1px solid var(--border); padding-top: 1.25rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
          <h4 style="font-size: 0.9rem; text-transform: uppercase; color: var(--indigo); letter-spacing: 0.05em;">Proof of Build Updates</h4>
        </div>
        ${p.updates && p.updates.length > 0 ? `
          <div style="display: flex; flex-direction: column; gap: 0.85rem;">
            ${p.updates.map(u => `
              <div style="background: var(--bg); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 1rem;">
                <div style="display: flex; justify-content: space-between; font-weight: 600; font-size: 0.9rem; margin-bottom: 0.25rem;">
                  <span>${escapeHtml(u.title)}</span>
                  <span style="font-size: 0.75rem; color: var(--ink-muted);">${new Date(u.createdAt).toLocaleDateString()}</span>
                </div>
                <p style="font-size: 0.85rem; color: var(--ink-secondary); margin-bottom: 0.5rem;">${escapeHtml(u.description || '')}</p>
                ${u.challenges ? `<p style="font-size: 0.8rem; color: #BE123C; margin-bottom: 0.25rem;"><strong>Obstacle:</strong> ${escapeHtml(u.challenges)}</p>` : ''}
                ${u.lessonsLearned ? `<p style="font-size: 0.8rem; color: #047857;"><strong>Takeaway:</strong> ${escapeHtml(u.lessonsLearned)}</p>` : ''}
              </div>
            `).join('')}
          </div>
        ` : '<p style="color: var(--ink-muted); font-size: 0.85rem;">No updates logged yet.</p>'}
      </div>

      <!-- Team Members -->
      <div style="margin-bottom: 1.5rem; border-top: 1px solid var(--border); padding-top: 1.25rem;">
        <h4 style="font-size: 0.8rem; text-transform: uppercase; color: var(--ink-muted); margin-bottom: 0.65rem;">Project Builders (${p.memberCount})</h4>
        <div style="display: flex; flex-wrap: wrap; gap: 0.75rem;">
          ${p.members ? p.members.map(m => `
            <div style="display: flex; align-items: center; gap: 0.5rem; background: var(--bg); border: 1px solid var(--border); border-radius: var(--radius-full); padding: 0.35rem 0.85rem; font-size: 0.825rem;">
              <span style="font-weight: 600;">${escapeHtml(m.userName)}</span>
              <span style="color: var(--ink-muted); font-size: 0.75rem;">(${escapeHtml(m.role)})</span>
            </div>
          `).join('') : ''}
        </div>
      </div>

      <!-- Comments & Feedback Section -->
      <div class="comments-container">
        <h4 style="font-size: 0.9rem; text-transform: uppercase; color: var(--indigo); letter-spacing: 0.05em; margin-bottom: 0.75rem;">
          Discussion & Technical Feedback (${comments.length})
        </h4>

        <div id="project-comments-list" class="comment-list">
          ${comments.length > 0 ? comments.map(c => `
            <div class="comment-item">
              <div class="comment-avatar" style="background: ${c.userAvatarColor || '#625BEE'};">
                ${getInitials(c.userName)}
              </div>
              <div class="comment-content">
                <div class="comment-meta">
                  <span class="comment-author">${escapeHtml(c.userName)}</span>
                  ${c.userCollege ? `<span class="comment-college">• ${escapeHtml(c.userCollege)}</span>` : ''}
                  <span class="comment-time">${formatDate(c.createdAt)}</span>
                </div>
                <div class="comment-text">${escapeHtml(c.content)}</div>
              </div>
            </div>
          `).join('') : '<p style="color: var(--ink-muted); font-size: 0.85rem; padding: 0.5rem 0;">No comments yet. Share technical thoughts, architecture suggestions, or kudos!</p>'}
        </div>

        <!-- Add comment input -->
        <div style="display: flex; gap: 0.5rem; margin-top: 1rem;">
          <input type="text" id="input-new-comment" class="form-control" placeholder="Share feedback or ask a technical question..." style="font-size: 0.875rem;" onkeydown="if(event.key==='Enter') submitProjectComment(${p.id})" />
          <button onclick="submitProjectComment(${p.id})" class="btn btn-primary btn-sm" style="white-space: nowrap;">Post</button>
        </div>
      </div>

      <!-- Collaborate Action -->
      <div style="border-top: 1px solid var(--border); padding-top: 1.25rem; margin-top: 1.5rem; display: flex; flex-direction: column; gap: 0.75rem;">
        <div id="collab-form-box" style="display: none; background: var(--bg); padding: 1rem; border-radius: var(--radius-md); border: 1px solid var(--border);">
          <label class="form-label">Note for project creator:</label>
          <textarea id="collab-message" class="form-control" rows="2" placeholder="Describe what skills you can contribute..."></textarea>
          <div style="display: flex; gap: 0.5rem; margin-top: 0.5rem; justify-content: flex-end;">
            <button onclick="document.getElementById('collab-form-box').style.display = 'none'" class="btn btn-secondary btn-sm">Cancel</button>
            <button onclick="submitCollabRequest(${p.id})" class="btn btn-primary btn-sm">Send Request</button>
          </div>
        </div>

        <div style="display: flex; justify-content: space-between; align-items: center;">
          <button onclick="closeProjectDetailsModal()" class="btn btn-secondary btn-sm">Close</button>
          <button id="btn-request-collab" onclick="document.getElementById('collab-form-box').style.display = 'block'" class="btn btn-primary btn-sm">
            ${p.rescue ? '🚨 Request to Rescue / Contribute' : '🤝 Request to Collaborate'}
          </button>
        </div>
      </div>
    `;
  } catch (err) {
    content.innerHTML = `<p style="color: var(--rose); text-align: center;">Could not load project details.</p>`;
  }
}

async function toggleProjectLike(projectId) {
  try {
    const res = await API.post(`/api/projects/${projectId}/like`);
    const btn = document.getElementById(`btn-project-like-${projectId}`);
    const display = document.getElementById('like-count-display');
    if (btn) btn.classList.toggle('liked', res.liked);
    if (display) display.textContent = res.likeCount;
    showToast(res.liked ? 'Added to liked projects!' : 'Project unliked', 'info');
  } catch (err) {
    if (err.status === 401) {
      window.location.href = '/login.html';
    } else {
      showToast(err.message || 'Could not update like', 'error');
    }
  }
}

async function submitProjectComment(projectId) {
  const input = document.getElementById('input-new-comment');
  if (!input) return;
  const content = input.value.trim();
  if (!content) return;

  try {
    const saved = await API.post(`/api/projects/${projectId}/comments`, { content });
    input.value = '';
    showToast('Comment posted!', 'success');
    
    // Refresh modal comments
    const list = document.getElementById('project-comments-list');
    if (list) {
      const commentEl = document.createElement('div');
      commentEl.className = 'comment-item';
      commentEl.innerHTML = `
        <div class="comment-avatar" style="background: ${saved.userAvatarColor || '#625BEE'};">
          ${getInitials(saved.userName)}
        </div>
        <div class="comment-content">
          <div class="comment-meta">
            <span class="comment-author">${escapeHtml(saved.userName)}</span>
            ${saved.userCollege ? `<span class="comment-college">• ${escapeHtml(saved.userCollege)}</span>` : ''}
            <span class="comment-time">${formatDate(saved.createdAt)}</span>
          </div>
          <div class="comment-text">${escapeHtml(saved.content)}</div>
        </div>
      `;
      // If previous was placeholder
      if (list.querySelector('p')) list.innerHTML = '';
      list.appendChild(commentEl);
      list.scrollTop = list.scrollHeight;
    }
  } catch (err) {
    if (err.status === 401) {
      window.location.href = '/login.html';
    } else {
      showToast(err.message || 'Failed to post comment', 'error');
    }
  }
}

function closeProjectDetailsModal() {
  document.getElementById('modal-project-details')?.classList.remove('active');
}

async function submitCollabRequest(projectId) {
  const message = document.getElementById('collab-message')?.value.trim();
  try {
    await API.post(`/api/projects/${projectId}/collaborate`, { message });
    showToast('Collaboration request sent to project creator!', 'success');
    document.getElementById('collab-form-box').style.display = 'none';
    const btn = document.getElementById('btn-request-collab');
    if (btn) {
      btn.textContent = 'Request Sent';
      btn.disabled = true;
    }
  } catch (err) {
    if (err.status === 401) {
      window.location.href = '/login.html';
    } else {
      showToast(err.message || 'Failed to submit request', 'error');
    }
  }
}

// Create Project Modal
function openCreateProjectModal(rescueMode = false) {
  const modal = document.getElementById('modal-create-project');
  if (!modal) return;
  modal.classList.add('active');
  const rescueCheckbox = document.getElementById('project-isRescue');
  if (rescueCheckbox) {
    rescueCheckbox.checked = rescueMode;
    toggleRescueFields();
  }
}

function closeCreateProjectModal() {
  document.getElementById('modal-create-project')?.classList.remove('active');
}

function toggleRescueFields() {
  const isChecked = document.getElementById('project-isRescue')?.checked;
  const fields = document.getElementById('rescue-specific-fields');
  if (fields) fields.style.display = isChecked ? 'block' : 'none';
}

async function handleCreateProjectSubmit(e) {
  e.preventDefault();
  const payload = {
    title: document.getElementById('project-title').value.trim(),
    shortDescription: document.getElementById('project-shortDesc').value.trim(),
    detailedDescription: document.getElementById('project-detailedDesc').value.trim(),
    technologies: document.getElementById('project-technologies').value.trim(),
    category: document.getElementById('project-category').value,
    status: document.getElementById('project-status').value,
    githubUrl: document.getElementById('project-githubUrl').value.trim(),
    demoUrl: document.getElementById('project-demoUrl').value.trim(),
    rescue: document.getElementById('project-isRescue').checked,
    rescueBlockers: document.getElementById('project-rescueBlockers')?.value.trim(),
    rescueSkillsNeeded: document.getElementById('project-rescueSkillsNeeded')?.value.trim()
  };

  try {
    await API.post('/api/projects', payload);
    showToast('Project launched on NEXUS!', 'success');
    closeCreateProjectModal();
    document.getElementById('form-create-project').reset();
    await loadProjects();
  } catch (err) {
    if (err.status === 401) {
      window.location.href = '/login.html';
    } else {
      showToast(err.message || 'Failed to create project', 'error');
    }
  }
}

function debounce(func, delay) {
  let timeout;
  return function(...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), delay);
  };
}
