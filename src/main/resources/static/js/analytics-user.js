const deviceSelect = document.getElementById('deviceSelect');
const refreshBtnA = document.getElementById('refreshBtn');
const modeSelect = document.getElementById('modeSelect');
const ctx = document.getElementById('deviceChart').getContext('2d');
let deviceChart;

async function loadDevicesForAnalytics() {
  const res = await fetch('/api/energy/devices');
  if (!res.ok) return;
  const devices = await res.json();
  deviceSelect.innerHTML = devices.map(d => `<option value="${d.id}">${escapeHtml(d.deviceName)} (${escapeHtml(d.deviceType)})</option>`).join('');
  if (devices.length > 0) {
    loadDeviceTimeseries(devices[0].id);
  }
}

async function loadDeviceTimeseries(id) {
  const res = await fetch(`/api/energy/device/${id}/timeseries?limit=60`);
  if (!res.ok) return;
  let points = await res.json();

  // If no stored samples exist, seed a single point from the device's current consumption
  if (!points || points.length === 0) {
    try {
      const devRes = await fetch(`/api/energy/device/${id}`);
      if (devRes.ok) {
        const dev = await devRes.json();
        const now = Math.floor(Date.now() / 1000);
        points = [{ timestamp: now - 60, totalConsumption: dev.consumption || 0, delta: 0 }];
      }
    } catch (e) {
      // swallow - will render empty chart with zeros
    }
  }

  // labels are timestamps for the chart
  const labels = points.map(p => new Date(p.timestamp * 1000).toLocaleTimeString());

  // cumulative totals
  const cumulative = points.map(p => p.totalConsumption || 0);

  // per-sample rate (delta) if provided by API; fallback to computed diff
  const rate = [];
  for (let i = 0; i < points.length; i++) {
    const p = points[i];
    if (p.delta !== undefined && p.delta !== null) {
      rate.push(p.delta);
    } else if (i === 0) {
      rate.push(0);
    } else {
      rate.push((points[i].totalConsumption || 0) - (points[i-1].totalConsumption || 0));
    }
  }

  // decide which to show based on mode select
  const mode = (modeSelect && modeSelect.value) ? modeSelect.value : 'cumulative';
  const displayData = mode === 'rate' ? rate : cumulative;
  const labelText = mode === 'rate' ? 'Rate (kWh/sample)' : 'Cumulative consumption (kWh)';

  // info area: show latest cumulative and latest rate
  const lastCum = cumulative.length ? cumulative[cumulative.length - 1] : 0;
  const lastRate = rate.length ? rate[rate.length - 1] : 0;

  // estimate per-hour rate using last two timestamps (fallback to sample interval of 1s)
  let perHourText = '';
  if (points.length >= 2) {
    const lastTs = points[points.length - 1].timestamp;
    const prevTs = points[points.length - 2].timestamp;
    const dt = (lastTs - prevTs) || 1; // seconds
    const perHour = (lastRate / dt) * 3600;
    perHourText = ` — Approx rate: ${perHour.toFixed(3)} kWh/hr`;
  }

  document.getElementById('deviceInfo').innerText = `Current total: ${lastCum.toFixed(3)} kWh — Last interval: ${lastRate.toFixed(4)} kWh${perHourText}`;

  if (deviceChart) deviceChart.destroy();
  deviceChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: labelText,
        data: displayData,
        borderColor: 'hsl(220 80% 50%)',
        backgroundColor: 'rgba(59,130,246,0.1)'
      }]
    },
    options: {scales:{y:{beginAtZero:true}}}
  });
}

function escapeHtml(s){return (''+s).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;');}

deviceSelect.addEventListener('change', () => loadDeviceTimeseries(deviceSelect.value));
refreshBtnA.addEventListener('click', () => loadDeviceTimeseries(deviceSelect.value));
if (modeSelect) modeSelect.addEventListener('change', () => loadDeviceTimeseries(deviceSelect.value));
loadDevicesForAnalytics();
setInterval(() => {
  if (deviceSelect.value) loadDeviceTimeseries(deviceSelect.value);
}, 5000);