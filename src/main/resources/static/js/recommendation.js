console.log('recommendation.js loaded');

async function loadRecommendations() {
  const container = document.getElementById('recommendationsList');
  try {
    const res = await fetch('/api/energy/recommendations');
    if (!res.ok) throw new Error('Failed to load recommendations');
    const recs = await res.json();

    if (!recs || recs.length === 0) {
      container.innerHTML = '<p>No recommendations at this time. Your devices look efficient 😊</p>';
      return;
    }

    container.innerHTML = recs.map(r => {
      return `
        <div class="card" style="margin-bottom:10px;padding:12px;">
          <h3>${escapeHtml(r.deviceName)} <small style="color:#666;">${escapeHtml(r.deviceType)} - ${r.status}</small></h3>
          <p style="margin-top:6px;font-weight:700;color:#b91c1c">${escapeHtml(r.reason)}</p>
          <p style="margin:6px 0">${escapeHtml(r.suggestion)}</p>
          <p style="font-size:12px;color:#444">Consumption: ${r.consumption.toFixed(3)} kWh · Active: ${r.activeHours.toFixed(1)}h</p>
        </div>
      `;
    }).join('');

  } catch (err) {
    container.innerHTML = '<p>Error loading recommendations</p>';
  }
}

function escapeHtml(s){ if (!s) return ''; return s.replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;'); }

window.addEventListener('DOMContentLoaded', loadRecommendations);
