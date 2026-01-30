// Static pie/bar charts removed — only live block timeseries is rendered
const pieCanvas = null;
const barCanvas = null;
const pieCtx = null;
const barCtx = null;


async function fetchAdminEnergy() {
  try {
    const [flatsRes, blocksRes, commonRes, statsRes] = await Promise.all([
      fetch('/api/admin/energy-summary'),
      fetch('/api/admin/energy-block-summary'),
      fetch('/api/admin/energy-common'),
      fetch('/api/admin/dashboard')
    ]);

    const flats = await flatsRes.json();
    const blocks = await blocksRes.json();
    const common = await commonRes.json();
    const stats = await statsRes.json();

    console.debug('Admin energy fetched', {flatsLength: (flats||[]).length, blocksLength: (blocks||[]).length, common, stats});

    const flatsTotal = (flats || []).reduce((s, f) => s + (f.totalConsumption || 0), 0);
    const blocksTotal = (blocks || []).reduce((s, b) => s + (b.totalConsumption || 0), 0);
    const total = flatsTotal + (common || 0);

    // Update summary cards
    document.getElementById('totalEnergy').innerText = total.toFixed(2) + ' kWh';
    document.getElementById('flatsUsage').innerText = flatsTotal.toFixed(2) + ' kWh';
    document.getElementById('commonUsage').innerText = (common || 0).toFixed(2) + ' kWh';
    document.getElementById('activeUsers').innerText = stats.users || 0;

    // PIE: flat consumption top 10
    const labels = flats.map(f => f.flatNumber + ' (' + f.blockName + ')');
    const data = flats.map(f => f.totalConsumption || 0);

    if (pieCtx) {
      if (pieChart) pieChart.destroy();
      pieChart = new Chart(pieCtx, {
        type: 'pie',
        data: {labels, datasets:[{data, backgroundColor: generateColors(data.length)}]}
      });
    } else {
      console.warn('pieCtx missing - cannot render pie chart');
    }

    // BAR: blocks
    const bLabels = blocks.map(b => b.blockName);
    const bData = blocks.map(b => b.totalConsumption || 0);

    if (barCtx) {
      if (barChart) barChart.destroy();
      barChart = new Chart(barCtx, {
        type: 'bar',
        data: {labels: bLabels, datasets:[{label:'kWh', data: bData, backgroundColor: generateColors(bData.length)}]},
        options: {scales: {y:{beginAtZero:true}}}
      });
    } else {
      console.warn('barCtx missing - cannot render bar chart');
    }

    // Update live block timeseries buffer (client-side rolling window)
    try {
      if (!Array.isArray(blocks)) throw new Error('blocks not an array');
      const ts = Math.floor(Date.now() / 1000);
      // align timestamp only if blocks exist
      if (blocks.length > 0) {
        liveTimestamps.push(ts);
        if (liveTimestamps.length > MAX_SERIES) liveTimestamps.shift();
      }

      // update per-block series (keep consistent order)
      blocks.forEach(b => {
        const name = b.blockName || 'Unknown';
        if (!blockSeries[name]) blockSeries[name] = [];
        blockSeries[name].push(b.totalConsumption || 0);
        if (blockSeries[name].length > MAX_SERIES) blockSeries[name].shift();
      });

      // prune series not in current blocks (avoid showing stale blocks)
      Object.keys(blockSeries).forEach(k => {
        if (!blocks.find(b => b.blockName === k)) {
          delete blockSeries[k];
        }
      });

      renderLiveBlockTimeseries();
    } catch (e) { console.warn('live block timeseries failed', e); }

    // Fill flat table
    const flatBody = document.getElementById('flatBody');
    if (flats.length === 0) {
      flatBody.innerHTML = '<tr><td colspan="3">No data</td></tr>';
    } else {
      flatBody.innerHTML = flats.map(f => `
        <tr>
          <td>${escapeHtml(f.blockName)}</td>
          <td>${escapeHtml(f.flatNumber)}</td>
          <td>${(f.totalConsumption || 0).toFixed(3)}</td>
        </tr>
      `).join('');
    }

  } catch (err) {
    console.error('Failed to load admin energy', err);
    try { showAdminError('Failed to load admin energy: ' + (err && err.message ? err.message : err)); } catch(e) { console.error(e); }
  }
}

function generateColors(n){
  const out = [];
  for(let i=0;i<n;i++){
    const h = Math.floor((i*47) % 360);
    out.push(`hsl(${h} 70% 60%)`);
  }
  return out;
}

function escapeHtml(s){
  if (!s) return '';
  return (''+s).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;');
}

// --- LIVE ROLLING TIMESERIES (client-side) ---
const MAX_SERIES = 60; // keep last 60 samples
const liveTimestamps = [];
const blockSeries = {}; // {blockName: [values,...]}
let blockTimeseriesChart;

function renderLiveBlockTimeseries() {
  const labels = liveTimestamps.map(t => new Date(t*1000).toLocaleTimeString());
  const seriesNames = Object.keys(blockSeries);

  const labelsLength = liveTimestamps.length;
  const datasets = seriesNames.map((name, i) => {
    let data = (blockSeries[name] || []).slice();

    // pad with nulls at the front so data length matches labels (Chart.js will show gaps)
    if (data.length < labelsLength) {
      const padding = new Array(labelsLength - data.length).fill(null);
      data = padding.concat(data);
    } else if (data.length > labelsLength) {
      // trim to the most recent values
      data = data.slice(data.length - labelsLength);
    }

    return {
      label: name,
      data,
      borderColor: generateColors(seriesNames.length)[i],
      fill: false
    };
  });

  const infoEl = document.getElementById('blockTimeseriesInfo');
  if (labels.length === 0) {
    // show message and clear any existing chart
    if (infoEl) infoEl.innerText = 'No recent samples available';
    if (blockTimeseriesChart) { blockTimeseriesChart.destroy(); blockTimeseriesChart = null; }
    return;
  } else {
    if (infoEl) infoEl.innerText = '';
  }

  const ctx = document.getElementById('blockTimeseries').getContext('2d');
  if (blockTimeseriesChart) blockTimeseriesChart.destroy();
  blockTimeseriesChart = new Chart(ctx, {
    type: 'line',
    data: { labels, datasets },
    options: { scales:{ y:{ beginAtZero:true } } }
  });
}

// use wrapper only (consolidated polling)
// update last-updated timestamp in UI
function setLastUpdated() {
  const el = document.getElementById('lastUpdated');
  if (!el) return;
  const now = new Date();
  el.innerText = `(updated: ${now.toLocaleTimeString()})`;
}

// wrap fetchAdminEnergy to update timestamp after successful run
async function fetchAdminEnergyWrapper(){
  try {
    await fetchAdminEnergy();
    setLastUpdated();
  } catch (e) {
    console.error('fetchAdminEnergyWrapper failure', e);
  }
}

// start with wrapper
fetchAdminEnergyWrapper();
setInterval(fetchAdminEnergyWrapper, 3000);
