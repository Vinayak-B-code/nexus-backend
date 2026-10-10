/**
 * NEXUS - Skill Bounty Board
 */
let allBounties = [];

document.addEventListener('DOMContentLoaded', async () => {
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get('action') === 'new') {
    openCreateBountyModal();
  }
  if (urlParams.get('id')) {
    openBountyDetailsModal(urlParams.get('id'));
  }

  await loadBounties();

  const searchInput = document.getElementById('search-query');
  const typeSelect = document.getElementById('filter-type');
  const statusSelect = document.getElementById('filter-status');

  if (searchInput) searchInput.addEventListener('input', debounce(loadBounties, 300));
  if (typeSelect) typeSelect.addEventListener('change', loadBounties);
  if (statusSelect) statusSelect.addEventListener('change', loadBounties);
});

async function loadBounties() {
  const container = document.getElementById('bounties-grid');
  if (!container) return;

  const query = document.getElementById('search-query')?.value || '';
  const bountyType = document.getElementById('filter-type')?.value || '';
  const status = document.getElementById('filter-status')?.value || '';

  container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 3rem;"><span style="color: var(--indigo); font-weight: 600;">Loading Skill Bounties...</span></div>`;

  try {
    const params = new URLSearchParams();
    if (query.trim()) params.append('query', query.trim());
    if (bountyType.trim()) params.append('bountyType', bountyType.trim());
    if (status.trim()) params.append('status', status.trim());

    const bounties = await API.get(`/api/bounties?${params.toString()}`);
    allBounties = bounties;

    if (!bounties || bounties.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="grid-column: 1/-1;">
          <div class="empty-state-icon">🎯</div>
          <h3>No skill bounties found</h3>
          <p>Stuck on an obscure error or need an architectural pair review? Post a challenge.</p>
          <button onclick="openCreateBountyModal()" class="btn btn-primary btn-sm">+ Post Skill Bounty</button>
        </div>
      `;
      return;
    }

    container.innerHTML = bounties.map(b => renderBountyCard(b)).join('');
  } catch (err) {
    container.innerHTML = `<div class="empty-state" style="grid-column: 1/-1;"><p style="color: var(--rose);">Could not load bounties.</p></div>`;
  }
}

function renderBountyCard(b) {
  return `
    <div class="card" onclick="openBountyDetailsModal(${b.id})" style="cursor: pointer;">
      <div class="card-body">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
          <span class="badge ${b.status === 'COMPLETED' ? 'badge-completed' : 'badge-in-progress'}">
            ${b.status}
          </span>
          <span class="badge" style="background: var(--indigo-light); color: var(--indigo); font-weight: 700;">
            ${escapeHtml(b.bountyType)}
          </span>
        </div>

        <h3 style="font-size: 1.15rem; margin-bottom: 0.5rem; line-height: 1.35;">
          ${escapeHtml(b.title)}
        </h3>

        <p style="font-size: 0.875rem; color: var(--ink-secondary); line-height: 1.5; margin-bottom: 1rem; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;">
          ${escapeHtml(b.description)}
        </p>

        <div style="margin-bottom: 1rem;">
          <span class="skill-chip backend">
            Skill Needed: <strong>${escapeHtml(b.skillName)}</strong>
          </span>
        </div>

        <div style="display: flex; align-items: center; justify-content: space-between; border-top: 1px solid var(--border); padding-top: 0.85rem; font-size: 0.8rem; color: var(--ink-secondary);">
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <div class="avatar avatar-sm" style="background: ${b.creatorAvatarColor || '#625BEE'};">${getInitials(b.creatorName)}</div>
            <span>${escapeHtml(b.creatorName)}</span>
          </div>
          <span style="color: var(--indigo); font-weight: 600;">${b.responseCount} offer(s)</span>
        </div>
      </div>
    </div>
  `;
}

// Bounty Details Modal
async function openBountyDetailsModal(id) {
  const modal = document.getElementById('modal-bounty-details');
  const content = document.getElementById('bounty-details-content');
  if (!modal || !content) return;

  modal.classList.add('active');
  content.innerHTML = `<div style="text-align: center; padding: 2rem;"><span style="color: var(--indigo); font-weight: 600;">Loading Bounty details...</span></div>`;

  try {
    const b = await API.get(`/api/bounties/${id}`);
    const user = await API.getCurrentUser();
    const isOwner = user && user.id === b.creatorId;

    content.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
        <div>
          <div style="display: flex; gap: 0.5rem; margin-bottom: 0.5rem;">
            <span class="badge ${b.status === 'COMPLETED' ? 'badge-completed' : 'badge-in-progress'}">${b.status}</span>
            <span class="badge" style="background: var(--indigo-light); color: var(--indigo);">${escapeHtml(b.bountyType)}</span>
          </div>
          <h2 style="font-size: 1.5rem;">${escapeHtml(b.title)}</h2>
          <p style="font-size: 0.85rem; color: var(--ink-secondary); margin-top: 2px;">
            Posted by <strong>${escapeHtml(b.creatorName)}</strong> (${escapeHtml(b.creatorCollege || 'University')})
          </p>
        </div>
        ${isOwner && b.status !== 'COMPLETED' ? `
          <button onclick="markBountyComplete(${b.id})" class="btn btn-lime btn-sm">✓ Mark Completed</button>
        ` : ''}
      </div>

      <div style="background: var(--bg); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 1.25rem; margin-bottom: 1.25rem; font-size: 0.925rem; line-height: 1.6;">
        ${escapeHtml(b.description)}
      </div>

      <div style="margin-bottom: 1.25rem;">
        <span class="skill-chip backend">Target Skill: ${escapeHtml(b.skillName)}</span>
      </div>

      <!-- Helper Offers Section -->
      <div style="margin-bottom: 1.5rem; border-top: 1px solid var(--border); padding-top: 1rem;">
        <h4 style="font-size: 0.875rem; text-transform: uppercase; color: var(--ink-muted); margin-bottom: 0.75rem;">Helper Offers (${b.responseCount})</h4>
        <div style="display: flex; flex-direction: column; gap: 0.75rem;">
          ${b.responses && b.responses.length > 0 ? b.responses.map(r => `
            <div style="display: flex; justify-content: space-between; align-items: center; background: #FFFFFF; border: 1px solid var(--border); border-radius: var(--radius-md); padding: 0.75rem 1rem;">
              <div style="display: flex; align-items: center; gap: 0.75rem;">
                <div class="avatar avatar-sm" style="background: ${r.responderAvatarColor || '#625BEE'};">${getInitials(r.responderName)}</div>
                <div>
                  <div style="font-weight: 600; font-size: 0.875rem;">${escapeHtml(r.responderName)} <span style="font-weight: normal; color: var(--ink-muted); font-size: 0.75rem;">(${escapeHtml(r.responderCollege || '')})</span></div>
                  <div style="font-size: 0.8rem; color: var(--ink-secondary); font-style: italic;">"${escapeHtml(r.message)}"</div>
                </div>
              </div>
              <div>
                ${isOwner && r.status === 'PENDING' ? `
                  <button onclick="handleHelperOffer(${b.id}, ${r.id}, true)" class="btn btn-primary btn-sm" style="font-size: 0.75rem; padding: 0.2rem 0.6rem;">Accept & Pair</button>
                ` : `
                  <span class="badge ${r.status === 'ACCEPTED' ? 'badge-completed' : 'badge-in-progress'}">${r.status}</span>
                `}
              </div>
            </div>
          `).join('') : '<p style="color: var(--ink-muted); font-size: 0.85rem;">No offers yet. Be the first to help!</p>'}
        </div>
      </div>

      <!-- Action -->
      <div style="border-top: 1px solid var(--border); padding-top: 1rem;">
        ${!isOwner && b.status === 'OPEN' ? `
          <div id="offer-help-box" style="display: none; background: var(--bg); padding: 1rem; border-radius: var(--radius-md); margin-bottom: 1rem; border: 1px solid var(--border);">
            <label class="form-label">How can you assist?</label>
            <textarea id="offer-message" class="form-control" rows="2" placeholder="Explain your familiarity with this issue or proposed fix..."></textarea>
            <div style="display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 0.5rem;">
              <button onclick="document.getElementById('offer-help-box').style.display = 'none'" class="btn btn-secondary btn-sm">Cancel</button>
              <button onclick="submitHelpOffer(${b.id})" class="btn btn-primary btn-sm">Send Offer</button>
            </div>
          </div>
          <div style="display: flex; justify-content: space-between;">
            <button onclick="closeBountyDetailsModal()" class="btn btn-secondary btn-sm">Close</button>
            <button id="btn-offer-help" onclick="document.getElementById('offer-help-box').style.display = 'block'" class="btn btn-primary btn-sm">
              🤝 Offer to Help
            </button>
          </div>
        ` : `
          <div style="display: flex; justify-content: flex-end;">
            <button onclick="closeBountyDetailsModal()" class="btn btn-secondary btn-sm">Close</button>
          </div>
        `}
      </div>
    `;
  } catch (err) {
    content.innerHTML = `<p style="color: var(--rose); text-align: center;">Could not load bounty details.</p>`;
  }
}

function closeBountyDetailsModal() {
  document.getElementById('modal-bounty-details')?.classList.remove('active');
}

async function submitHelpOffer(bountyId) {
  const message = document.getElementById('offer-message')?.value.trim();
  try {
    await API.post(`/api/bounties/${bountyId}/respond`, { message });
    showToast('Offer submitted to bounty author!', 'success');
    openBountyDetailsModal(bountyId);
    loadBounties();
  } catch (err) {
    if (err.status === 401) {
      window.location.href = '/login.html';
    } else {
      showToast(err.message || 'Failed to submit offer', 'error');
    }
  }
}

async function handleHelperOffer(bountyId, responseId, accept) {
  try {
    await API.post(`/api/bounties/responses/${responseId}/handle?accept=${accept}`);
    showToast(accept ? 'Helper paired with bounty!' : 'Offer declined', 'info');
    openBountyDetailsModal(bountyId);
    loadBounties();
  } catch (err) {
    showToast(err.message || 'Action failed', 'error');
  }
}

async function markBountyComplete(bountyId) {
  try {
    await API.put(`/api/bounties/${bountyId}/complete`);
    showToast('Bounty marked as completed! Great job.', 'success');
    openBountyDetailsModal(bountyId);
    loadBounties();
  } catch (err) {
    showToast(err.message || 'Failed to mark complete', 'error');
  }
}

// Create Bounty Modal
function openCreateBountyModal() {
  document.getElementById('modal-create-bounty')?.classList.add('active');
}

function closeCreateBountyModal() {
  document.getElementById('modal-create-bounty')?.classList.remove('active');
}

async function handleCreateBountySubmit(e) {
  e.preventDefault();
  const payload = {
    title: document.getElementById('bounty-title').value.trim(),
    description: document.getElementById('bounty-desc').value.trim(),
    skillName: document.getElementById('bounty-skill').value.trim(),
    bountyType: document.getElementById('bounty-type').value
  };

  try {
    await API.post('/api/bounties', payload);
    showToast('Skill Bounty posted to the board!', 'success');
    closeCreateBountyModal();
    document.getElementById('form-create-bounty').reset();
    await loadBounties();
  } catch (err) {
    if (err.status === 401) {
      window.location.href = '/login.html';
    } else {
      showToast(err.message || 'Failed to post bounty', 'error');
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
