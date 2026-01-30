console.log('energy-user.js loaded');
const devicesBody = document.getElementById('devicesBody');
const refreshBtn = document.getElementById('refreshBtn');

async function fetchDevices() {
  try {
    const res = await fetch('/api/energy/devices');
    if (!res.ok) throw new Error('Failed to load devices');
    const devices = await res.json();

    if (devices.length === 0) {
      devicesBody.innerHTML = '<tr><td colspan="7">No devices found</td></tr>';
      return;
    }

    devicesBody.innerHTML = devices.map(d => {
      const power = (d.powerRating ?? 0).toFixed(1);
      const consumption = (d.consumption ?? 0).toFixed(3);
      const activeSec = d.totalActiveSeconds ?? 0;
      const activeStr = formatActiveTime(activeSec);
      const isOn = d.status === 'ON';
      const toggleText = isOn ? 'Turn OFF' : 'Turn ON';
      const toggleClass = isOn ? 'btn btn-danger' : 'btn btn-success';
      const statusBadge = isOn ? '<span class="status-pill status-pill-green">ON</span>' : '<span class="status-pill status-pill-gray">OFF</span>';

      return `
        <tr data-id="${d.id}">
          <td>${escapeHtml(d.deviceName)}</td>
          <td>${escapeHtml(d.deviceType)}</td>
          <td>${power}</td>
          <td>${consumption}</td>
          <td>${activeStr}</td>
          <td>${statusBadge}</td>
          <td>
            <button class="${toggleClass} toggleBtn" data-status="${d.status}">${toggleText}</button>
          </td>
        </tr>`;
    }).join('');

    document.querySelectorAll('.toggleBtn').forEach(btn => {
      btn.addEventListener('click', async (e) => {
        const row = e.target.closest('tr');
        const id = row.getAttribute('data-id');
        const current = e.target.getAttribute('data-status');
        const next = current === 'ON' ? 'OFF' : 'ON';

        try {
          const r = await fetch(`/api/devices/${id}/toggle?status=${next}`, { method: 'PUT' });
          if (!r.ok) throw new Error('Toggle failed');
          await fetchDevices();
        } catch (err) {
          alert('Failed to toggle device');
        }
      });
    });

  } catch (err) {
    devicesBody.innerHTML = `<tr><td colspan="7">Error loading devices</td></tr>`;
  }
}

function formatActiveTime(sec) {
  // totalActiveSeconds is simulated seconds; convert back to readable simulated time
  // Each second represents 10 minutes. Show hh:mm simulated.
  const simulatedMinutes = sec * 10; // minutes
  const hours = Math.floor(simulatedMinutes / 60);
  const minutes = simulatedMinutes % 60;
  return `${hours}h ${minutes}m`;
}

function escapeHtml(s){
  if (!s) return '';
  return s.replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;');
}

refreshBtn.addEventListener('click', fetchDevices);

// poll every 3 seconds
fetchDevices();
setInterval(fetchDevices, 3000);
