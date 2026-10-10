/**
 * NEXUS - Developer Discovery & Skill DNA Compatibility
 */
let allDevelopers = [];

document.addEventListener('DOMContentLoaded', async () => {
  await loadAvailableSkills();
  await searchDevelopers();

  const searchInput = document.getElementById('search-query');
  const skillSelect = document.getElementById('filter-skill');
  const collegeInput = document.getElementById('filter-college');

  if (searchInput) searchInput.addEventListener('input', debounce(searchDevelopers, 300));
  if (skillSelect) skillSelect.addEventListener('change', searchDevelopers);
  if (collegeInput) collegeInput.addEventListener('input', debounce(searchDevelopers, 300));
});

async function loadAvailableSkills() {
  try {
    const skills = await API.get('/api/skills');
    const select = document.getElementById('filter-skill');
    if (!select) return;
    
    // Sort and deduplicate skill names
    const names = [...new Set(skills.map(s => s.name))].sort();
    names.forEach(name => {
      const opt = document.createElement('option');
      opt.value = name;
      opt.textContent = name;
      select.appendChild(opt);
    });
  } catch (e) {
    console.warn('Could not load skills list');
  }
}

async function searchDevelopers() {
  const container = document.getElementById('developers-grid');
  if (!container) return;

  const query = document.getElementById('search-query')?.value || '';
  const skill = document.getElementById('filter-skill')?.value || '';
  const college = document.getElementById('filter-college')?.value || '';

  container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 3rem;"><span style="color: var(--indigo); font-weight: 600;">Scanning student builder graph...</span></div>`;

  try {
    const params = new URLSearchParams();
    if (query.trim()) params.append('query', query.trim());
    if (skill.trim()) params.append('skill', skill.trim());
    if (college.trim()) params.append('college', college.trim());

    const developers = await API.get(`/api/users/search?${params.toString()}`);
    allDevelopers = developers;

    if (!developers || developers.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="grid-column: 1/-1;">
          <div class="empty-state-icon">🔍</div>
          <h3>No developers match your filters</h3>
          <p>Try clearing your skill or university search parameters to discover other builders.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = developers.map(dev => renderDeveloperCard(dev)).join('');
  } catch (err) {
    container.innerHTML = `
      <div class="empty-state" style="grid-column: 1/-1;">
        <h3>Could not load developers</h3>
        <p>Ensure the Spring Boot backend and database are reachable.</p>
      </div>
    `;
  }
}

function renderDeveloperCard(dev) {
  const initials = getInitials(dev.fullName);
  const skills = dev.skills || [];
  const proficient = skills.filter(s => s.skillType === 'PROFICIENT').slice(0, 5);

  return `
    <div class="card">
      <div class="card-body" style="display: flex; flex-direction: column; height: 100%;">
        <div style="display: flex; align-items: flex-start; gap: 1rem; margin-bottom: 1rem;">
          <div class="avatar" style="background-color: ${dev.avatarColor || '#625BEE'}; width: 48px; height: 48px; font-size: 1rem;">
            ${initials}
          </div>
          <div style="flex: 1; min-width: 0;">
            <h3 style="font-size: 1.15rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              ${escapeHtml(dev.fullName)}
            </h3>
            <p style="font-size: 0.8rem; color: var(--ink-secondary); margin-top: 2px;">
              ${escapeHtml(dev.college || 'University')} • ${escapeHtml(dev.studyYear || 'Student')}
            </p>
          </div>
        </div>

        <p style="font-size: 0.875rem; color: var(--ink-secondary); line-height: 1.5; margin-bottom: 1rem; flex: 1; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
          ${escapeHtml(dev.bio || 'Exploring new technologies and looking for collaborative projects.')}
        </p>

        <!-- Skill chips preview -->
        <div style="margin-bottom: 1.25rem;">
          <div style="font-size: 0.725rem; font-weight: 700; text-transform: uppercase; color: var(--ink-muted); margin-bottom: 0.4rem; letter-spacing: 0.05em;">Proficient In</div>
          <div style="display: flex; flex-wrap: wrap; gap: 0.4rem;">
            ${proficient.length > 0 ? proficient.map(s => {
              const catClass = s.category ? s.category.toLowerCase().replace(/[^a-z0-9]/g, '-') : 'general';
              return `<span class="skill-chip ${catClass}" style="font-size: 0.7rem;">${escapeHtml(s.skillName)}</span>`;
            }).join('') : '<span style="font-size: 0.75rem; color: var(--ink-muted);">No skills added</span>'}
          </div>
        </div>

        <!-- Action footer -->
        <div style="display: flex; gap: 0.5rem; border-top: 1px solid var(--border); padding-top: 0.85rem; margin-top: auto;">
          <button onclick="openChemistryModal(${dev.id})" class="btn btn-lime btn-sm" style="flex: 1;">
            ⚡ Why this match?
          </button>
          <button id="btn-connect-${dev.id}" onclick="connectWithDeveloper(${dev.id})" class="btn btn-secondary btn-sm">
            Connect
          </button>
        </div>
      </div>
    </div>
  `;
}

// Skill Chemistry & Compatibility Modal
async function openChemistryModal(targetUserId) {
  const modal = document.getElementById('modal-chemistry');
  const content = document.getElementById('chemistry-modal-content');
  if (!modal || !content) return;

  modal.classList.add('active');
  content.innerHTML = `<div style="text-align: center; padding: 2rem;"><span style="color: var(--indigo); font-weight: 600;">Calculating Skill DNA Chemistry...</span></div>`;

  try {
    const comp = await API.get(`/api/compatibility/${targetUserId}`);
    const dev = allDevelopers.find(d => d.id === targetUserId);

    let achievementsHtml = '';
    try {
      const publicAchievements = await API.get(`/api/achievements/user/${targetUserId}`);
      if (publicAchievements && publicAchievements.length > 0) {
        achievementsHtml = `
          <div style="background: var(--bg); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 1.15rem; margin-bottom: 1.25rem;">
            <h4 style="font-size: 0.8rem; text-transform: uppercase; color: var(--indigo); letter-spacing: 0.05em; margin-bottom: 0.75rem;">Academic & Professional Achievements</h4>
            <div style="display: flex; flex-direction: column; gap: 0.65rem;">
              ${publicAchievements.map(a => `
                <div style="background: #FFFFFF; border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 0.75rem; font-size: 0.85rem;">
                  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.25rem;">
                    <strong style="color: var(--ink);">${escapeHtml(a.title)}</strong>
                    <span class="badge" style="background: var(--indigo-light); color: var(--indigo); font-size: 0.7rem;">${escapeHtml(a.category)}</span>
                  </div>
                  <div style="color: var(--ink-secondary); font-size: 0.8rem;">
                    ${escapeHtml(a.issuingOrganization)} • ${escapeHtml(a.issueDate || '')}
                  </div>
                  ${a.credentialUrl ? `
                    <div style="margin-top: 0.35rem;">
                      <a href="${escapeHtml(a.credentialUrl)}" target="_blank" rel="noopener noreferrer" style="font-size: 0.8rem; font-weight: 600;">Verify Credential ↗</a>
                    </div>
                  ` : ''}
                </div>
              `).join('')}
            </div>
          </div>
        `;
      }
    } catch (e) {
      // Ignored
    }

    content.innerHTML = `
      <div style="text-align: center; margin-bottom: 1.5rem;">
        <div style="display: inline-flex; align-items: center; justify-content: center; width: 88px; height: 88px; border-radius: 50%; background: ${comp.score >= 70 ? 'var(--lime)' : 'var(--indigo-light)'}; margin-bottom: 0.75rem; border: 4px solid #FFFFFF; box-shadow: var(--shadow-md);">
          <span style="font-family: var(--font-heading); font-size: 2rem; font-weight: 800; color: #171923;">${comp.score}%</span>
        </div>
        <h3 style="font-size: 1.35rem;">Project Chemistry with ${escapeHtml(comp.targetUserName)}</h3>
        ${comp.preliminary ? '<span class="badge" style="background: #FEF3C7; color: #92400E; margin-top: 0.25rem;">Preliminary Analysis</span>' : ''}
      </div>

      <!-- Explanation Box -->
      <div style="background: var(--bg); border: 1px solid var(--border); border-radius: var(--radius-md); padding: 1.15rem; margin-bottom: 1.25rem;">
        <h4 style="font-size: 0.8rem; text-transform: uppercase; color: var(--indigo); letter-spacing: 0.05em; margin-bottom: 0.35rem;">Compatibility Rationale</h4>
        <p style="font-size: 0.925rem; color: var(--ink); line-height: 1.5;">${escapeHtml(comp.matchExplanation)}</p>
      </div>

      <!-- Suggested Project Direction -->
      <div style="background: #F0FDF4; border: 1px solid #BBF7D0; border-radius: var(--radius-md); padding: 1.15rem; margin-bottom: 1.25rem;">
        <h4 style="font-size: 0.8rem; text-transform: uppercase; color: #166534; letter-spacing: 0.05em; margin-bottom: 0.35rem;">Suggested Project Direction</h4>
        <p style="font-size: 0.925rem; color: #14532D; line-height: 1.5;">${escapeHtml(comp.projectChemistrySuggestion)}</p>
      </div>

      <!-- Public Academic Achievements -->
      ${achievementsHtml}

      <!-- Complementary & Reciprocal Skills -->
      <div style="display: grid; grid-template-columns: 1fr; gap: 1rem; margin-bottom: 1.5rem;">
        ${comp.complementarySkills && comp.complementarySkills.length > 0 ? `
          <div>
            <h4 style="font-size: 0.8rem; text-transform: uppercase; color: var(--ink-muted); margin-bottom: 0.4rem;">Complementary Advantages</h4>
            <ul style="padding-left: 1.25rem; font-size: 0.875rem; color: var(--ink-secondary);">
              ${comp.complementarySkills.map(c => `<li>${escapeHtml(c)}</li>`).join('')}
            </ul>
          </div>
        ` : ''}

        ${comp.reciprocalOpportunities && comp.reciprocalOpportunities.length > 0 ? `
          <div>
            <h4 style="font-size: 0.8rem; text-transform: uppercase; color: var(--ink-muted); margin-bottom: 0.4rem;">Knowledge Exchange</h4>
            <ul style="padding-left: 1.25rem; font-size: 0.875rem; color: var(--ink-secondary);">
              ${comp.reciprocalOpportunities.map(r => `<li>${escapeHtml(r)}</li>`).join('')}
            </ul>
          </div>
        ` : ''}
      </div>

      <div style="display: flex; gap: 0.75rem; justify-content: flex-end; border-top: 1px solid var(--border); padding-top: 1rem;">
        <button onclick="closeChemistryModal()" class="btn btn-secondary btn-sm">Close</button>
        <button onclick="connectWithDeveloper(${targetUserId}); closeChemistryModal();" class="btn btn-primary btn-sm">Send Connection Request</button>
      </div>
    `;
  } catch (err) {
    if (err.status === 401) {
      content.innerHTML = `
        <div style="text-align: center; padding: 2rem;">
          <h3>Sign in to calculate chemistry</h3>
          <p style="color: var(--ink-secondary); margin: 0.75rem 0 1.5rem 0;">Join NEXUS to match your Skill DNA with student builders across universities.</p>
          <a href="/login.html" class="btn btn-primary btn-sm">Sign in now</a>
        </div>
      `;
    } else {
      content.innerHTML = `<p style="color: var(--rose); text-align: center;">Unable to calculate chemistry at this time.</p>`;
    }
  }
}

function closeChemistryModal() {
  document.getElementById('modal-chemistry')?.classList.remove('active');
}

async function connectWithDeveloper(targetUserId) {
  try {
    await API.post(`/api/connections/request/${targetUserId}`);
    showToast('Connection request sent!', 'success');
    const btn = document.getElementById(`btn-connect-${targetUserId}`);
    if (btn) {
      btn.textContent = 'Requested';
      btn.disabled = true;
    }
  } catch (err) {
    if (err.status === 401) {
      window.location.href = '/login.html';
    } else {
      showToast(err.message || 'Could not send request', 'error');
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
