/**
 * NEXUS - Build Circles & Milestone Tracking
 */
let allCircles = [];

document.addEventListener('DOMContentLoaded', async () => {
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get('action') === 'new') {
    openCreateCircleModal();
  }
  if (urlParams.get('id')) {
    openCircleDetailsModal(urlParams.get('id'));
  }

  await loadCircles();

  const searchInput = document.getElementById('search-query');
  const statusSelect = document.getElementById('filter-status');

  if (searchInput) searchInput.addEventListener('input', debounce(loadCircles, 300));
  if (statusSelect) statusSelect.addEventListener('change', loadCircles);
});

async function loadCircles() {
  const container = document.getElementById('circles-grid');
  if (!container) return;

  const query = document.getElementById('search-query')?.value || '';
  const status = document.getElementById('filter-status')?.value || '';

  container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 3rem;"><span style="color: var(--indigo); font-weight: 600;">Loading Build Circles...</span></div>`;

  try {
    const params = new URLSearchParams();
    if (query.trim()) params.append('query', query.trim());
    if (status.trim()) params.append('status', status.trim());

    const circles = await API.get(`/api/circles?${params.toString()}`);
    allCircles = circles;

    if (!circles || circles.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="grid-column: 1/-1;">
          <div class="empty-state-icon">⭕</div>
          <h3>No Build Circles found</h3>
          <p>Assemble a targeted squad with clear milestones or adjust your filters.</p>
          <button onclick="openCreateCircleModal()" class="btn btn-primary btn-sm">+ Create Build Circle</button>
        </div>
      `;
      return;
    }

    container.innerHTML = circles.map(c => renderCircleCard(c)).join('');
  } catch (err) {
    container.innerHTML = `<div class="empty-state" style="grid-column: 1/-1;"><p style="color: var(--rose);">Could not load build circles.</p></div>`;
  }
}

function renderCircleCard(c) {
  const techs = c.technologies ? c.technologies.split(',').map(t => t.trim()).filter(Boolean) : [];

  return `
    <div class="card" onclick="openCircleDetailsModal(${c.id})" style="cursor: pointer;">
      <div class="card-header-art circle" style="display: flex; align-items: center; justify-content: space-between; padding: 1rem 1.25rem;">
        <span class="badge badge-in-progress">${escapeHtml(c.progressStatus)}</span>
        <span style="font-size: 0.8rem; font-weight: 700; color: var(--indigo); background: rgba(255,255,255,0.85); padding: 2px 10px; border-radius: 12px;">
          ${c.currentMemberCount}/${c.maxMembers} Members
        </span>
      </div>

      <div class="card-body">
        <h3 style="font-size: 1.25rem; margin-bottom: 0.5rem; line-height: 1.3;">
          ${escapeHtml(c.name)}
        </h3>

        <p style="font-size: 0.875rem; color: var(--ink-secondary); line-height: 1.5; margin-bottom: 1rem; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
          ${escapeHtml(c.projectGoal || c.description || '')}
        </p>

        ${c.openPositions ? `
          <div style="background: var(--bg); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 0.5rem 0.75rem; margin-bottom: 1rem; font-size: 0.775rem;">
            <strong style="color: var(--indigo);">Open Roles:</strong> ${escapeHtml(c.openPositions)}
          </div>
        ` : ''}

        <!-- Progress bar -->
        <div style="margin-bottom: 1rem;">
          <div class="dna-label" style="font-size: 0.75rem;">
            <span>Milestone Progress (${c.completedMilestonesCount}/${c.totalMilestonesCount})</span>
            <span>${c.progressPercentage}%</span>
          </div>
          <div class="dna-progress">
            <div class="dna-fill" style="width: ${c.progressPercentage}%;"></div>
          </div>
        </div>

        <!-- Tech Chips -->
        <div style="display: flex; flex-wrap: wrap; gap: 0.35rem; margin-bottom: 1rem;">
          ${techs.map(t => `<span class="skill-chip" style="font-size: 0.7rem;">${escapeHtml(t)}</span>`).join('')}
        </div>

        <!-- Footer -->
        <div style="display: flex; align-items: center; justify-content: space-between; border-top: 1px solid var(--border); padding-top: 0.85rem; font-size: 0.8rem; color: var(--ink-secondary);">
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <div class="avatar avatar-sm" style="background: ${c.creatorAvatarColor || '#625BEE'};">${getInitials(c.creatorName)}</div>
            <span>${escapeHtml(c.creatorName)}</span>
          </div>
          <span style="color: var(--indigo); font-weight: 600;">View Milestones →</span>
        </div>
      </div>
    </div>
  `;
}

// Circle Details Modal
async function openCircleDetailsModal(id) {
  const modal = document.getElementById('modal-circle-details');
  const content = document.getElementById('circle-details-content');
  if (!modal || !content) return;

  modal.classList.add('active');
  content.innerHTML = `<div style="text-align: center; padding: 2rem;"><span style="color: var(--indigo); font-weight: 600;">Loading Circle Board...</span></div>`;

  try {
    const c = await API.get(`/api/circles/${id}`);
    const isFull = c.currentMemberCount >= c.maxMembers;

    content.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.25rem;">
        <div>
          <span class="badge badge-in-progress" style="margin-bottom: 0.5rem;">${escapeHtml(c.progressStatus)}</span>
          <h2 style="font-size: 1.75rem;">${escapeHtml(c.name)}</h2>
          <p style="font-size: 0.875rem; color: var(--ink-secondary); margin-top: 2px;">
            Created by <strong>${escapeHtml(c.creatorName)}</strong> (${escapeHtml(c.creatorCollege || 'University')})
          </p>
        </div>
        <div class="compat-badge" style="font-size: 0.9rem;">
          ${c.currentMemberCount} / ${c.maxMembers} Members
        </div>
      </div>

      <div style="background: var(--bg); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 1rem; margin-bottom: 1.25rem;">
        <h4 style="font-size: 0.8rem; text-transform: uppercase; color: var(--indigo); margin-bottom: 0.35rem;">Target Objective</h4>
        <p style="font-size: 0.95rem; color: var(--ink);">${escapeHtml(c.projectGoal || c.description || 'Deliver project MVP')}</p>
      </div>

      <!-- Milestone Checklist -->
      <div style="margin-bottom: 1.5rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
          <h4 style="font-size: 0.875rem; text-transform: uppercase; color: var(--ink-muted); letter-spacing: 0.05em;">Milestone Roadmap (${c.progressPercentage}% Completed)</h4>
          <button onclick="toggleAddMilestoneForm()" class="btn btn-secondary btn-sm" style="font-size: 0.75rem;">+ Add Milestone</button>
        </div>

        <div id="add-milestone-box" style="display: none; background: var(--bg); padding: 1rem; border-radius: var(--radius-md); border: 1px solid var(--border); margin-bottom: 1rem;">
          <input type="text" id="new-milestone-title" class="form-control" placeholder="Milestone title..." style="margin-bottom: 0.5rem;" />
          <textarea id="new-milestone-desc" class="form-control" rows="2" placeholder="Details/deliverables..." style="margin-bottom: 0.5rem;"></textarea>
          <div style="display: flex; gap: 0.5rem; justify-content: flex-end;">
            <button onclick="toggleAddMilestoneForm()" class="btn btn-secondary btn-sm">Cancel</button>
            <button onclick="submitNewMilestone(${c.id})" class="btn btn-primary btn-sm">Save Milestone</button>
          </div>
        </div>

        <div class="dna-progress" style="margin-bottom: 1rem;">
          <div class="dna-fill" style="width: ${c.progressPercentage}%;"></div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 0.5rem;">
          ${c.milestones && c.milestones.length > 0 ? c.milestones.map(m => `
            <div style="display: flex; align-items: center; justify-content: space-between; background: #FFFFFF; border: 1px solid var(--border); border-radius: var(--radius-md); padding: 0.75rem 1rem;">
              <div style="display: flex; align-items: center; gap: 0.75rem;">
                <input type="checkbox" ${m.completed ? 'checked' : ''} onchange="toggleMilestone(${c.id}, ${m.id})" style="width: 18px; height: 18px; cursor: pointer;" />
                <div>
                  <div style="font-weight: 600; font-size: 0.9rem; text-decoration: ${m.completed ? 'line-through' : 'none'}; color: ${m.completed ? 'var(--ink-muted)' : 'var(--ink)'};">
                    ${escapeHtml(m.title)}
                  </div>
                  ${m.description ? `<div style="font-size: 0.775rem; color: var(--ink-secondary);">${escapeHtml(m.description)}</div>` : ''}
                </div>
              </div>
              ${m.completed ? '<span class="badge badge-completed">Done</span>' : '<span class="badge badge-in-progress">Pending</span>'}
            </div>
          `).join('') : '<p style="color: var(--ink-muted); font-size: 0.85rem;">No milestones defined yet.</p>'}
        </div>
      </div>

      <!-- Circle Members -->
      <div style="margin-bottom: 1.5rem;">
        <h4 style="font-size: 0.8rem; text-transform: uppercase; color: var(--ink-muted); margin-bottom: 0.65rem;">Active Roster</h4>
        <div style="display: flex; flex-wrap: wrap; gap: 0.75rem;">
          ${c.members ? c.members.map(m => `
            <div style="display: flex; align-items: center; gap: 0.5rem; background: var(--bg); border: 1px solid var(--border); border-radius: var(--radius-full); padding: 0.35rem 0.85rem; font-size: 0.825rem;">
              <span style="font-weight: 600;">${escapeHtml(m.userName)}</span>
              <span style="color: var(--indigo); font-size: 0.75rem; font-weight: 700;">${escapeHtml(m.role)}</span>
            </div>
          `).join('') : ''}
        </div>
      </div>

      <!-- Join Form / Action -->
      <div style="border-top: 1px solid var(--border); padding-top: 1.25rem;">
        <div id="join-circle-form-box" style="display: none; background: var(--bg); padding: 1rem; border-radius: var(--radius-md); border: 1px solid var(--border); margin-bottom: 1rem;">
          <label class="form-label">Introduce yourself to circle leads:</label>
          <textarea id="join-circle-message" class="form-control" rows="2" placeholder="Tell the circle why you're interested and what role you'd take..."></textarea>
          <div style="display: flex; gap: 0.5rem; margin-top: 0.5rem; justify-content: flex-end;">
            <button onclick="document.getElementById('join-circle-form-box').style.display = 'none'" class="btn btn-secondary btn-sm">Cancel</button>
            <button onclick="submitCircleJoinRequest(${c.id})" class="btn btn-primary btn-sm">Submit Application</button>
          </div>
        </div>

        <div style="display: flex; justify-content: space-between; align-items: center;">
          <button onclick="closeCircleDetailsModal()" class="btn btn-secondary btn-sm">Close</button>
          ${isFull ? `
            <button class="btn btn-secondary btn-sm" disabled>Circle is at Capacity (${c.maxMembers}/${c.maxMembers})</button>
          ` : `
            <button id="btn-join-circle" onclick="document.getElementById('join-circle-form-box').style.display = 'block'" class="btn btn-primary btn-sm">
              Apply to Join Circle
            </button>
          `}
        </div>
      </div>
    `;
  } catch (err) {
    content.innerHTML = `<p style="color: var(--rose); text-align: center;">Could not load Build Circle details.</p>`;
  }
}

function closeCircleDetailsModal() {
  document.getElementById('modal-circle-details')?.classList.remove('active');
}

function toggleAddMilestoneForm() {
  const box = document.getElementById('add-milestone-box');
  if (box) box.style.display = box.style.display === 'none' ? 'block' : 'none';
}

async function submitNewMilestone(circleId) {
  const title = document.getElementById('new-milestone-title')?.value.trim();
  const description = document.getElementById('new-milestone-desc')?.value.trim();
  if (!title) {
    showToast('Milestone title is required', 'error');
    return;
  }

  try {
    await API.post(`/api/circles/${circleId}/milestones`, { title, description });
    showToast('Milestone added!', 'success');
    openCircleDetailsModal(circleId);
    loadCircles();
  } catch (err) {
    showToast(err.message || 'Failed to add milestone', 'error');
  }
}

async function toggleMilestone(circleId, milestoneId) {
  try {
    await API.put(`/api/circles/milestones/${milestoneId}/toggle`);
    showToast('Milestone status updated', 'success');
    openCircleDetailsModal(circleId);
    loadCircles();
  } catch (err) {
    showToast(err.message || 'Failed to update milestone', 'error');
  }
}

async function submitCircleJoinRequest(circleId) {
  const message = document.getElementById('join-circle-message')?.value.trim();
  try {
    await API.post(`/api/circles/${circleId}/join`, { message });
    showToast('Join request submitted to circle creator!', 'success');
    document.getElementById('join-circle-form-box').style.display = 'none';
    const btn = document.getElementById('btn-join-circle');
    if (btn) {
      btn.textContent = 'Application Pending';
      btn.disabled = true;
    }
  } catch (err) {
    if (err.status === 401) {
      window.location.href = '/login.html';
    } else {
      showToast(err.message || 'Failed to submit application', 'error');
    }
  }
}

// Create Circle Modal
function openCreateCircleModal() {
  document.getElementById('modal-create-circle')?.classList.add('active');
}

function closeCreateCircleModal() {
  document.getElementById('modal-create-circle')?.classList.remove('active');
}

async function handleCreateCircleSubmit(e) {
  e.preventDefault();
  const payload = {
    name: document.getElementById('circle-name').value.trim(),
    description: document.getElementById('circle-desc').value.trim(),
    technologies: document.getElementById('circle-tech').value.trim(),
    maxMembers: parseInt(document.getElementById('circle-maxMembers').value, 10),
    openPositions: document.getElementById('circle-openPositions').value.trim(),
    projectGoal: document.getElementById('circle-projectGoal').value.trim(),
    progressStatus: document.getElementById('circle-progressStatus').value
  };

  try {
    await API.post('/api/circles', payload);
    showToast('Build Circle created!', 'success');
    closeCreateCircleModal();
    document.getElementById('form-create-circle').reset();
    await loadCircles();
  } catch (err) {
    if (err.status === 401) {
      window.location.href = '/login.html';
    } else {
      showToast(err.message || 'Failed to create circle', 'error');
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
