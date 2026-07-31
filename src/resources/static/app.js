const API_BASE = '/api/v1/expenses';

let currentCategoryFilter = 'ALL';

document.addEventListener('DOMContentLoaded', () => {
    // Initialize default date in form to today
    const dateInput = document.getElementById('expense-date');
    if (dateInput) {
        dateInput.value = new Date().toISOString().split('T')[0];
    }

    // Bind event listeners
    document.getElementById('expense-form').addEventListener('submit', handleAddExpense);
    document.getElementById('refresh-btn').addEventListener('click', () => {
        loadDashboardData();
        showToast('Data refreshed successfully', 'success');
    });

    // Category Filter Pills
    const filterPills = document.querySelectorAll('#category-filters .pill');
    filterPills.forEach(pill => {
        pill.addEventListener('click', () => {
            filterPills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            currentCategoryFilter = pill.dataset.category;
            loadExpenses();
        });
    });

    // Load initial data
    loadDashboardData();
    if (window.lucide) {
        lucide.createIcons();
    }
});

async function loadDashboardData() {
    await Promise.all([
        loadExpenses(),
        loadSummary(),
        loadMonthlySummary()
    ]);
    if (window.lucide) {
        lucide.createIcons();
    }
}

async function loadExpenses() {
    const tbody = document.getElementById('expenses-tbody');
    try {
        let url = API_BASE;
        if (currentCategoryFilter && currentCategoryFilter !== 'ALL') {
            url += `?category=${encodeURIComponent(currentCategoryFilter)}`;
        }

        const response = await fetch(url);
        if (!response.ok) throw new Error('Failed to fetch expenses');
        const expenses = await response.json();

        if (expenses.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="empty-table">No expenses recorded yet. Create one on the left!</td></tr>`;
            return;
        }

        // Sort by date descending
        expenses.sort((a, b) => b.date.localeCompare(a.date));

        tbody.innerHTML = expenses.map(expense => `
            <tr>
                <td>${formatDate(expense.date)}</td>
                <td><strong>${escapeHtml(expense.title)}</strong></td>
                <td><span class="category-badge category-${expense.category}">${expense.category}</span></td>
                <td class="text-right amount-val">$${Number(expense.amount).toFixed(2)}</td>
                <td class="text-right">
                    <button class="btn-delete" onclick="deleteExpense('${expense.id}', '${escapeHtml(expense.title)}')">
                        <i data-lucide="trash-2" style="width: 16px; height: 16px; vertical-align: middle;"></i> Delete
                    </button>
                </td>
            </tr>
        `).join('');

        if (window.lucide) {
            lucide.createIcons();
        }
    } catch (error) {
        console.error(error);
        tbody.innerHTML = `<tr><td colspan="5" class="empty-table" style="color: var(--accent-red)">Error loading expenses. Ensure API server is running!</td></tr>`;
    }
}

async function loadSummary() {
    try {
        const response = await fetch(`${API_BASE}/summary`);
        if (!response.ok) throw new Error('Failed to fetch summary');
        const summary = await response.json();

        const totalDisplay = document.getElementById('total-amount-display');
        totalDisplay.textContent = `$${Number(summary.totalAmount || 0).toFixed(2)}`;

        // Calculate breakdown bars and top category
        const breakdownList = document.getElementById('category-breakdown-list');
        const breakdownMap = summary.categoryBreakdown || {};
        const categories = Object.keys(breakdownMap);

        let totalCount = 0;
        let topCat = '—';
        let topAmount = 0;

        if (categories.length === 0) {
            breakdownList.innerHTML = `<p class="empty-state">No categories recorded yet.</p>`;
            document.getElementById('top-category-display').textContent = '—';
            document.getElementById('top-category-amount').textContent = '$0.00 (0%)';
            document.getElementById('total-count-display').textContent = '0';
            return;
        }

        // Determine top category
        for (const cat of categories) {
            const val = Number(breakdownMap[cat]);
            if (val > topAmount) {
                topAmount = val;
                topCat = cat;
            }
        }

        const totalVal = Number(summary.totalAmount || 0);
        const topPct = totalVal > 0 ? ((topAmount / totalVal) * 100).toFixed(0) : 0;
        document.getElementById('top-category-display').textContent = topCat;
        document.getElementById('top-category-amount').textContent = `$${topAmount.toFixed(2)} (${topPct}%)`;

        // Render breakdown bars
        breakdownList.innerHTML = categories.map(cat => {
            const val = Number(breakdownMap[cat]);
            const pct = totalVal > 0 ? ((val / totalVal) * 100).toFixed(0) : 0;
            return `
                <div class="breakdown-item">
                    <div class="breakdown-top">
                        <span>${cat}</span>
                        <span>$${val.toFixed(2)} (${pct}%)</span>
                    </div>
                    <div class="breakdown-bar-bg">
                        <div class="breakdown-bar-fill" style="width: ${pct}%"></div>
                    </div>
                </div>
            `;
        }).join('');
    } catch (error) {
        console.error('Error loading summary:', error);
    }
}

async function loadMonthlySummary() {
    try {
        const response = await fetch(`${API_BASE}/summary/monthly`);
        if (!response.ok) throw new Error('Failed to fetch monthly summary');
        const monthlyData = await response.json();

        const monthlyList = document.getElementById('monthly-summary-list');
        document.getElementById('active-months-display').textContent = monthlyData.length;

        // Calculate total count across all months
        let totalCount = 0;
        monthlyData.forEach(m => totalCount += (m.expenseCount || 0));
        document.getElementById('total-count-display').textContent = totalCount;

        if (monthlyData.length === 0) {
            monthlyList.innerHTML = `<p class="empty-state">No monthly records yet.</p>`;
            return;
        }

        monthlyList.innerHTML = monthlyData.map(m => `
            <div class="monthly-item">
                <div>
                    <div class="monthly-item-title">${m.month}</div>
                    <div class="monthly-item-meta">${m.expenseCount} expense(s) recorded</div>
                </div>
                <div class="monthly-item-amount">$${Number(m.totalAmount).toFixed(2)}</div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading monthly summary:', error);
    }
}

async function handleAddExpense(e) {
    e.preventDefault();
    const titleInput = document.getElementById('expense-title');
    const amountInput = document.getElementById('expense-amount');
    const categorySelect = document.getElementById('expense-category');
    const dateInput = document.getElementById('expense-date');

    const requestBody = {
        title: titleInput.value.trim(),
        amount: parseFloat(amountInput.value),
        category: categorySelect.value,
        date: dateInput.value
    };

    try {
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Failed to add expense');
        }

        showToast('Expense created successfully!', 'success');
        titleInput.value = '';
        amountInput.value = '';
        categorySelect.value = '';
        dateInput.value = new Date().toISOString().split('T')[0];

        loadDashboardData();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

async function deleteExpense(id, title) {
    if (!confirm(`Are you sure you want to delete expense "${title}"?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok && response.status !== 204) {
            throw new Error('Failed to delete expense');
        }

        showToast('Expense deleted successfully', 'success');
        loadDashboardData();
    } catch (error) {
        console.error(error);
        showToast(error.message, 'error');
    }
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span>${escapeHtml(message)}</span>
    `;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

function formatDate(isoDateStr) {
    if (!isoDateStr) return '—';
    const parts = isoDateStr.split('-');
    if (parts.length === 3) {
        const [year, month, day] = parts;
        return `${month}/${day}/${year}`;
    }
    return isoDateStr;
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
