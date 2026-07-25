/* =============================================================
   Banking Management System — App JavaScript
   Features: Dark/light mode, sidebar, toasts, charts, utils
   ============================================================= */

'use strict';

// ── Theme Management ──────────────────────────────────────────
const ThemeManager = {
  STORAGE_KEY: 'banking-theme',

  init() {
    const saved = localStorage.getItem(this.STORAGE_KEY) || 'dark';
    this.apply(saved);
  },

  apply(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem(this.STORAGE_KEY, theme);
    const toggle = document.getElementById('themeToggle');
    if (toggle) {
      toggle.innerHTML = theme === 'dark'
        ? '<i class="bi bi-sun-fill"></i>'
        : '<i class="bi bi-moon-fill"></i>';
      toggle.title = theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode';
    }
  },

  toggle() {
    const current = document.documentElement.getAttribute('data-theme') || 'dark';
    this.apply(current === 'dark' ? 'light' : 'dark');
  }
};

// ── Sidebar Management ────────────────────────────────────────
const Sidebar = {
  sidebar: null,
  backdrop: null,

  init() {
    this.sidebar  = document.querySelector('.sidebar');
    this.backdrop = document.querySelector('.sidebar-backdrop');

    const toggleBtn = document.getElementById('sidebarToggle');
    if (toggleBtn) {
      toggleBtn.addEventListener('click', () => this.toggle());
    }
    if (this.backdrop) {
      this.backdrop.addEventListener('click', () => this.close());
    }

    // Highlight active nav item
    this.highlightActive();
  },

  toggle() {
    if (!this.sidebar) return;
    const isOpen = this.sidebar.classList.contains('open');
    isOpen ? this.close() : this.open();
  },

  open() {
    this.sidebar?.classList.add('open');
    this.backdrop?.classList.add('visible');
    document.body.style.overflow = 'hidden';
  },

  close() {
    this.sidebar?.classList.remove('open');
    this.backdrop?.classList.remove('visible');
    document.body.style.overflow = '';
  },

  highlightActive() {
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-item').forEach(item => {
      const href = item.getAttribute('href');
      if (href && currentPath.startsWith(href) && href !== '/') {
        item.classList.add('active');
      }
    });
  }
};

// ── Toast Notifications ───────────────────────────────────────
const Toast = {
  container: null,

  init() {
    this.container = document.getElementById('toastContainer');
    if (!this.container) {
      this.container = document.createElement('div');
      this.container.id = 'toastContainer';
      this.container.className = 'toast-container';
      document.body.appendChild(this.container);
    }
  },

  show(message, type = 'info', duration = 5000) {
    this.init();

    const icons = {
      success: '✅',
      error:   '❌',
      warning: '⚠️',
      info:    'ℹ️'
    };

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
      <span class="toast-icon">${icons[type] || icons.info}</span>
      <span class="toast-msg">${message}</span>
      <button class="toast-close" onclick="this.parentElement.remove()">✕</button>
    `;

    this.container.appendChild(toast);

    if (duration > 0) {
      setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
      }, duration);
    }
  },

  success: (msg, dur) => Toast.show(msg, 'success', dur),
  error:   (msg, dur) => Toast.show(msg, 'error',   dur),
  warning: (msg, dur) => Toast.show(msg, 'warning', dur),
  info:    (msg, dur) => Toast.show(msg, 'info',    dur)
};

// ── Alert Auto-dismiss ────────────────────────────────────────
function initAlerts() {
  document.querySelectorAll('.alert').forEach(alert => {
    const closeBtn = alert.querySelector('.alert-close');
    if (closeBtn) {
      closeBtn.addEventListener('click', () => {
        alert.style.opacity = '0';
        alert.style.transform = 'translateY(-10px)';
        alert.style.transition = 'all 0.3s ease';
        setTimeout(() => alert.remove(), 300);
      });
    }

    // Auto-dismiss success alerts after 5s
    if (alert.classList.contains('alert-success')) {
      setTimeout(() => {
        if (alert.isConnected) {
          alert.style.opacity = '0';
          alert.style.transition = 'opacity 0.5s ease';
          setTimeout(() => alert.remove(), 500);
        }
      }, 5000);
    }
  });
}

// ── Modal Management ──────────────────────────────────────────
const Modal = {
  open(modalId) {
    const overlay = document.getElementById(modalId);
    if (overlay) {
      overlay.classList.add('open');
      document.body.style.overflow = 'hidden';
    }
  },

  close(modalId) {
    const overlay = document.getElementById(modalId);
    if (overlay) {
      overlay.classList.remove('open');
      document.body.style.overflow = '';
    }
  },

  closeOnOverlay(event) {
    if (event.target === event.currentTarget) {
      event.currentTarget.classList.remove('open');
      document.body.style.overflow = '';
    }
  }
};

// ── Password Toggle ───────────────────────────────────────────
function initPasswordToggles() {
  document.querySelectorAll('.password-toggle').forEach(btn => {
    btn.addEventListener('click', () => {
      const input = btn.closest('.input-group')?.querySelector('input');
      if (!input) return;

      if (input.type === 'password') {
        input.type = 'text';
        btn.innerHTML = '<i class="bi bi-eye-slash"></i>';
      } else {
        input.type = 'password';
        btn.innerHTML = '<i class="bi bi-eye"></i>';
      }
    });
  });
}

// ── Copy to Clipboard ─────────────────────────────────────────
function copyToClipboard(text, btnEl) {
  navigator.clipboard.writeText(text).then(() => {
    const original = btnEl.innerHTML;
    btnEl.innerHTML = '<i class="bi bi-check2"></i>';
    btnEl.style.color = '#10B981';
    setTimeout(() => {
      btnEl.innerHTML = original;
      btnEl.style.color = '';
    }, 2000);
    Toast.success('Copied to clipboard!', 2000);
  }).catch(() => {
    Toast.error('Failed to copy');
  });
}

// ── Number Formatting ─────────────────────────────────────────
const Formatter = {
  currency(amount, currency = '₹') {
    if (amount == null) return `${currency}0.00`;
    return `${currency}${Number(amount).toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })}`;
  },

  number(n) {
    if (n == null) return '0';
    if (n >= 10000000) return (n / 10000000).toFixed(1) + 'Cr';
    if (n >= 100000)   return (n / 100000).toFixed(1) + 'L';
    if (n >= 1000)     return (n / 1000).toFixed(1) + 'K';
    return String(n);
  },

  date(dateStr) {
    if (!dateStr) return '-';
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    }).format(new Date(dateStr));
  }
};

// ── Chart Defaults (Chart.js) ─────────────────────────────────
function configureChartDefaults() {
  if (typeof Chart === 'undefined') return;

  Chart.defaults.color = '#94A3B8';
  Chart.defaults.borderColor = 'rgba(255,255,255,0.06)';
  Chart.defaults.font.family = 'Inter, sans-serif';
  Chart.defaults.font.size = 12;

  Chart.defaults.plugins.legend.labels.usePointStyle = true;
  Chart.defaults.plugins.legend.labels.pointStyle = 'circle';
  Chart.defaults.plugins.tooltip.backgroundColor = '#1E2435';
  Chart.defaults.plugins.tooltip.borderColor = 'rgba(255,255,255,0.1)';
  Chart.defaults.plugins.tooltip.borderWidth = 1;
  Chart.defaults.plugins.tooltip.padding = 12;
  Chart.defaults.plugins.tooltip.titleColor = '#F1F5F9';
  Chart.defaults.plugins.tooltip.bodyColor = '#94A3B8';
  Chart.defaults.plugins.tooltip.cornerRadius = 8;
}

// ── Balance Chart (customer dashboard) ───────────────────────
function initBalanceChart(canvasId, labels, data) {
  const ctx = document.getElementById(canvasId);
  if (!ctx || typeof Chart === 'undefined') return;

  return new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: 'Balance',
        data,
        borderColor: '#4F6EF7',
        backgroundColor: 'rgba(79,110,247,0.08)',
        borderWidth: 2,
        pointRadius: 4,
        pointBackgroundColor: '#4F6EF7',
        pointBorderColor: '#0A0E1A',
        pointBorderWidth: 2,
        fill: true,
        tension: 0.4
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: '#64748B', font: { size: 11 } }
        },
        y: {
          grid: { color: 'rgba(255,255,255,0.04)' },
          ticks: {
            color: '#64748B',
            font: { size: 11 },
            callback: v => '₹' + Formatter.number(v)
          }
        }
      }
    }
  });
}

// ── Admin Chart ───────────────────────────────────────────────
function initAdminTransactionChart(canvasId, labels, deposits, withdrawals) {
  const ctx = document.getElementById(canvasId);
  if (!ctx || typeof Chart === 'undefined') return;

  return new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: 'Deposits',
          data: deposits,
          backgroundColor: 'rgba(16,185,129,0.7)',
          borderRadius: 6,
          borderSkipped: false
        },
        {
          label: 'Withdrawals',
          data: withdrawals,
          backgroundColor: 'rgba(239,68,68,0.7)',
          borderRadius: 6,
          borderSkipped: false
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { position: 'top' } },
      scales: {
        x: { grid: { display: false } },
        y: {
          grid: { color: 'rgba(255,255,255,0.04)' },
          ticks: { callback: v => '₹' + Formatter.number(v) }
        }
      }
    }
  });
}

// ── Table Search Filter ───────────────────────────────────────
function initTableSearch(inputId, tableId) {
  const input = document.getElementById(inputId);
  const table = document.getElementById(tableId);
  if (!input || !table) return;

  input.addEventListener('input', () => {
    const term = input.value.toLowerCase();
    table.querySelectorAll('tbody tr').forEach(row => {
      row.style.display = row.textContent.toLowerCase().includes(term) ? '' : 'none';
    });
  });
}

// ── Form Validation Helpers ───────────────────────────────────
function initFormValidation() {
  document.querySelectorAll('form[data-validate]').forEach(form => {
    form.addEventListener('submit', e => {
      let valid = true;
      form.querySelectorAll('[required]').forEach(field => {
        if (!field.value.trim()) {
          field.classList.add('is-invalid');
          valid = false;
        } else {
          field.classList.remove('is-invalid');
        }
      });
      if (!valid) {
        e.preventDefault();
        Toast.error('Please fill in all required fields.');
      }
    });

    form.querySelectorAll('input, select, textarea').forEach(field => {
      field.addEventListener('input', () => {
        if (field.value.trim()) field.classList.remove('is-invalid');
      });
    });
  });
}

// ── Confirmation Dialogs ──────────────────────────────────────
function initConfirmButtons() {
  document.querySelectorAll('[data-confirm]').forEach(btn => {
    btn.addEventListener('click', e => {
      const msg = btn.getAttribute('data-confirm') || 'Are you sure?';
      if (!confirm(msg)) {
        e.preventDefault();
        e.stopPropagation();
      }
    });
  });
}

// ── Amount Formatting in Inputs ───────────────────────────────
function initAmountInputs() {
  document.querySelectorAll('input[data-type="amount"]').forEach(input => {
    input.addEventListener('input', () => {
      // Remove non-numeric except decimal
      let val = input.value.replace(/[^0-9.]/g, '');
      const parts = val.split('.');
      if (parts.length > 2) val = parts[0] + '.' + parts.slice(1).join('');
      if (parts[1] && parts[1].length > 2) val = parts[0] + '.' + parts[1].slice(0, 2);
      input.value = val;
    });
  });
}

// ── Animate stat values ───────────────────────────────────────
function animateCounters() {
  document.querySelectorAll('.stat-value[data-count]').forEach(el => {
    const target = parseFloat(el.getAttribute('data-count'));
    if (isNaN(target)) return;

    const prefix = el.getAttribute('data-prefix') || '';
    const suffix = el.getAttribute('data-suffix') || '';
    const isFloat = el.getAttribute('data-float') === 'true';
    const duration = 1200;
    const start = performance.now();

    function update(now) {
      const elapsed = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - elapsed, 3); // ease-out cubic
      const current = target * eased;
      el.textContent = prefix + (isFloat
        ? current.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
        : Math.floor(current).toLocaleString('en-IN')) + suffix;
      if (elapsed < 1) requestAnimationFrame(update);
    }

    requestAnimationFrame(update);
  });
}

// ── Main Init ─────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  ThemeManager.init();
  Sidebar.init();
  Toast.init();
  initAlerts();
  initPasswordToggles();
  initFormValidation();
  initConfirmButtons();
  initAmountInputs();
  configureChartDefaults();

  // Animate stat counters if present
  setTimeout(animateCounters, 100);

  // Theme toggle button
  const themeBtn = document.getElementById('themeToggle');
  if (themeBtn) {
    themeBtn.addEventListener('click', () => ThemeManager.toggle());
  }

  // Close modals on overlay click
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', Modal.closeOnOverlay);
  });

  // Close modals on Escape
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-overlay.open').forEach(overlay => {
        overlay.classList.remove('open');
        document.body.style.overflow = '';
      });
    }
  });
});

// ── Global exports ────────────────────────────────────────────
window.BankingApp = {
  Modal,
  Toast,
  Formatter,
  ThemeManager,
  copyToClipboard,
  initBalanceChart,
  initAdminTransactionChart,
  initTableSearch
};
