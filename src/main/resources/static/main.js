// main.js
// Mock data for the voting system
const mockData = {
    students: [
        {
            email: "student@university.edu",
            regNumber: "U-2024-00123",
            password: "password123",
            name: "Alex Johnson",
            faculty: "Computer Science",
            year: "3rd Year",
            accountStatus: "INACTIVE" // This will change when student tries to vote
        }
    ],
    candidates: [
        {
            id: 1,
            name: "Sarah Williams",
            position: "Student Union President",
            photo: "SW",
            platform: "Focused on improving student facilities, mental health support, and increasing student participation in university governance."
        },
        {
            id: 2,
            name: "Michael Chen",
            position: "Student Union President",
            photo: "MC",
            platform: "Advocating for better campus sustainability, improved WiFi coverage, and more diverse food options in cafeterias."
        },
        {
            id: 3,
            name: "Priya Sharma",
            position: "Student Union President",
            photo: "PS",
            platform: "Committed to enhancing career services, establishing more student clubs, and creating a more inclusive campus environment."
        },
        {
            id: 4,
            name: "David Okafor",
            position: "Student Union President",
            photo: "DO",
            platform: "Focused on reducing tuition fees, increasing financial aid opportunities, and improving library resources."
        }
    ],
    // Election status can be: "PENDING", "ACTIVE", "CLOSED"
    electionStatus: "PENDING",
    // Track if user has voted
    hasVoted: false
};

// Application state
let currentUser = null;
let selectedCandidateId = null;
let electionStatus = mockData.electionStatus;

// DOM Elements
const loginPage = document.getElementById('login-page');
const dashboardPage = document.getElementById('dashboard-page');
const votingPage = document.getElementById('voting-page');
const loginForm = document.getElementById('login-form');
const logoutBtn = document.getElementById('logout-btn');
const backToDashboardBtn = document.getElementById('back-to-dashboard');
const voteBtn = document.getElementById('vote-btn');
const submitVoteBtn = document.getElementById('submit-vote-btn');
const cancelVoteBtn = document.getElementById('cancel-vote-btn');
const successOkBtn = document.getElementById('success-ok');
const confirmationModal = document.getElementById('confirmation-modal');
const closeModalBtn = document.getElementById('close-modal');
const modalCancelBtn = document.getElementById('modal-cancel');
const modalConfirmBtn = document.getElementById('modal-confirm');
const successMessage = document.getElementById('success-message');

/**
 * Initialize the application
 */
function initApp() {
    // Set initial election status
    updateElectionStatus(electionStatus);

    // Event listeners for login form
    loginForm.addEventListener('submit', handleLogin);

    // Event listeners for navigation
    logoutBtn.addEventListener('click', handleLogout);
    backToDashboardBtn.addEventListener('click', () => showPage('dashboard-page'));
    cancelVoteBtn.addEventListener('click', () => showPage('dashboard-page'));
    successOkBtn.addEventListener('click', () => {
        successMessage.style.display = 'none';
        showPage('dashboard-page');
    });

    // Event listeners for voting
    voteBtn.addEventListener('click', handleVoteButtonClick);
    submitVoteBtn.addEventListener('click', handleSubmitVote);

    // Event listeners for modal
    closeModalBtn.addEventListener('click', closeModal);
    modalCancelBtn.addEventListener('click', closeModal);
    modalConfirmBtn.addEventListener('click', handleConfirmVote);

    // Display login page initially
    showPage('login-page');
}

/**
 * Show a specific page and hide others
 * @param {string} pageId - The ID of the page to show
 */
function showPage(pageId) {
    // Hide all pages
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });

    // Show the requested page
    const page = document.getElementById(pageId);
    if (page) {
        page.classList.add('active');

        // Update dashboard if showing it
        if (pageId === 'dashboard-page' && currentUser) {
            updateDashboard();
        }

        // Render candidates if showing voting page
        if (pageId === 'voting-page') {
            renderCandidates();
        }
    }
}

/**
 * Handle login form submission
 * @param {Event} e - The form submit event
 */
function handleLogin(e) {
    e.preventDefault();

    // Get form values
    const email = document.getElementById('email').value;
    const regNumber = document.getElementById('reg-number').value;
    const password = document.getElementById('password').value;

    // Simple validation
    if (!email || !regNumber || !password) {
        showLoginMessage('Please fill in all fields', 'error');
        return;
    }

    // Check mock data (in a real app, this would be a server call)
    const student = mockData.students.find(s =>
        s.email === email && s.regNumber === regNumber
    );

    if (student) {
        // In a real app, we would verify password hash
        if (password.length < 6) {
            showLoginMessage('Password must be at least 6 characters', 'error');
            return;
        }

        // Set current user
        currentUser = { ...student };

        // Show success message
        showLoginMessage('Login successful! Redirecting to dashboard...', 'success');

        // Redirect to dashboard after a brief delay
        setTimeout(() => {
            showPage('dashboard-page');
        }, 1500);
    } else {
        showLoginMessage('Invalid credentials. Please try again.', 'error');
    }
}

/**
 * Display a message on the login form
 * @param {string} text - The message text
 * @param {string} type - The message type (success or error)
 */
function showLoginMessage(text, type) {
    const messageDiv = document.getElementById('login-message');
    messageDiv.textContent = text;
    messageDiv.className = `form-message ${type}`;

    // Clear message after 5 seconds
    setTimeout(() => {
        messageDiv.textContent = '';
        messageDiv.className = 'form-message';
    }, 5000);
}

/**
 * Handle logout
 */
function handleLogout() {
    currentUser = null;
    selectedCandidateId = null;
    showPage('login-page');

    // Clear login form
    loginForm.reset();
}

/**
 * Update the dashboard with current user and election information
 */
function updateDashboard() {
    if (!currentUser) return;

    // Update student info
    document.getElementById('student-name').textContent = currentUser.name;
    document.getElementById('student-reg').textContent = currentUser.regNumber;

    // Update account status
    const accountStatusDot = document.getElementById('account-status-dot');
    const accountStatusText = document.getElementById('account-status-text');
    const accountStatusMessage = document.getElementById('account-status-message');

    if (currentUser.accountStatus === 'ACTIVE') {
        accountStatusDot.className = 'status-dot active';
        accountStatusText.className = 'status-text active';
        accountStatusText.textContent = 'ACTIVE';
        accountStatusMessage.textContent = 'Your account is active and ready for voting.';
        accountStatusMessage.style.backgroundColor = 'rgba(39, 174, 96, 0.1)';
        accountStatusMessage.style.borderLeftColor = 'var(--success-color)';
    } else {
        accountStatusDot.className = 'status-dot inactive';
        accountStatusText.className = 'status-text inactive';
        accountStatusText.textContent = 'INACTIVE';
        accountStatusMessage.textContent = 'Your account is inactive. Voting will be enabled only when an election is active.';
        accountStatusMessage.style.backgroundColor = 'rgba(231, 76, 60, 0.1)';
        accountStatusMessage.style.borderLeftColor = 'var(--danger-color)';
    }

    // Update election status and voting controls
    updateElectionStatus(electionStatus);
}

/**
 * Update the election status display and voting controls
 * @param {string} status - The election status (PENDING, ACTIVE, or CLOSED)
 */
function updateElectionStatus(status) {
    electionStatus = status;

    // Update election status badge
    const electionStatusBadge = document.getElementById('election-status-badge');
    const electionStatusText = document.getElementById('election-status-text');
    const electionMessage = document.getElementById('election-message');
    const voteButton = document.getElementById('vote-btn');

    // Set badge and message based on status
    electionStatusBadge.className = `election-badge ${status.toLowerCase()}`;
    electionStatusText.textContent = status;

    // Configure voting controls based on election status
    switch (status) {
        case 'PENDING':
            electionMessage.textContent = 'Election has not started yet.';
            electionMessage.className = 'election-message pending';
            voteButton.disabled = true;
            voteButton.innerHTML = '<i class="fas fa-clock"></i> Election Pending';
            break;

        case 'ACTIVE':
            electionMessage.textContent = 'Election is now active. You may cast your vote.';
            electionMessage.className = 'election-message active';
            voteButton.disabled = false;
            voteButton.innerHTML = '<i class="fas fa-check-circle"></i> Vote Now';
            break;

        case 'CLOSED':
            electionMessage.textContent = 'Election has ended.';
            electionMessage.className = 'election-message closed';
            voteButton.disabled = true;
            voteButton.innerHTML = '<i class="fas fa-times-circle"></i> Election Closed';
            break;
    }

    // If account is inactive, disable voting button regardless of election status
    if (currentUser && currentUser.accountStatus === 'INACTIVE' && status === 'ACTIVE') {
        voteButton.disabled = false; // Allow clicking to trigger activation
    }
}

/**
 * Handle click on the Vote button
 */
function handleVoteButtonClick() {
    // Check election status
    if (electionStatus !== 'ACTIVE') {
        alert('Voting is not available at this time.');
        return;
    }

    // Check if user has already voted
    if (mockData.hasVoted) {
        alert('You have already voted in this election.');
        return;
    }

    // If account is inactive, show activation confirmation
    if (currentUser.accountStatus === 'INACTIVE') {
        showModal(
            'Account Activation Required',
            'Your account is currently inactive. To vote, your account must be activated. Do you want to activate your account and proceed to voting?'
        );
    } else {
        // Account is already active, proceed to voting page
        showPage('voting-page');
    }
}

/**
 * Show confirmation modal
 * @param {string} title - Modal title
 * @param {string} message - Modal message
 */
function showModal(title, message) {
    document.getElementById('modal-message').textContent = message;
    confirmationModal.style.display = 'flex';
}

/**
 * Close the confirmation modal
 */
function closeModal() {
    confirmationModal.style.display = 'none';
}

/**
 * Handle confirmation of account activation and voting
 */
function handleConfirmVote() {
    // Activate the user's account
    currentUser.accountStatus = 'ACTIVE';
    closeModal();

    // Update dashboard to reflect active status
    updateDashboard();

    // Proceed to voting page
    showPage('voting-page');
}

/**
 * Render the list of candidates on the voting page
 */
function renderCandidates() {
    const candidatesList = document.querySelector('.candidates-list');
    candidatesList.innerHTML = '';

    // Clear selected candidate
    selectedCandidateId = null;
    document.getElementById('selected-candidate-text').textContent = 'No candidate selected yet.';
    document.getElementById('submit-vote-btn').disabled = true;

    // Create a card for each candidate
    mockData.candidates.forEach(candidate => {
        const candidateCard = document.createElement('div');
        candidateCard.className = 'candidate-card';
        candidateCard.dataset.candidateId = candidate.id;

        candidateCard.innerHTML = `
            <div class="candidate-header">
                <div class="candidate-photo">${candidate.photo}</div>
                <div>
                    <div class="candidate-name">${candidate.name}</div>
                    <div class="candidate-position">${candidate.position}</div>
                </div>
            </div>
            <div class="candidate-platform">
                <h4>Platform</h4>
                <p>${candidate.platform}</p>
            </div>
            <div class="candidate-select">
                <input type="radio" name="candidate" id="candidate-${candidate.id}" class="select-radio" value="${candidate.id}">
                <label for="candidate-${candidate.id}" class="select-label">Select Candidate</label>
            </div>
        `;

        // Add click event to select candidate
        candidateCard.addEventListener('click', (e) => {
            // Don't trigger if clicking on the radio or label directly
            if (e.target.type === 'radio' || e.target.tagName === 'LABEL') return;

            selectCandidate(candidate.id);
        });

        // Add event to the radio button
        const radioBtn = candidateCard.querySelector('.select-radio');
        const radioLabel = candidateCard.querySelector('.select-label');

        radioBtn.addEventListener('change', () => {
            if (radioBtn.checked) {
                selectCandidate(candidate.id);
            }
        });

        radioLabel.addEventListener('click', () => {
            selectCandidate(candidate.id);
        });

        candidatesList.appendChild(candidateCard);
    });
}

/**
 * Select a candidate for voting
 * @param {number} candidateId - The ID of the selected candidate
 */
function selectCandidate(candidateId) {
    // Deselect all candidates
    document.querySelectorAll('.candidate-card').forEach(card => {
        card.classList.remove('selected');
        const radio = card.querySelector('.select-radio');
        radio.checked = false;
    });

    // Select the clicked candidate
    const selectedCard = document.querySelector(`[data-candidate-id="${candidateId}"]`);
    if (selectedCard) {
        selectedCard.classList.add('selected');
        const radio = selectedCard.querySelector('.select-radio');
        radio.checked = true;

        selectedCandidateId = candidateId;

        // Update selected candidate text
        const candidate = mockData.candidates.find(c => c.id === candidateId);
        document.getElementById('selected-candidate-text').textContent = `Selected: ${candidate.name}`;

        // Enable submit button
        document.getElementById('submit-vote-btn').disabled = false;
    }
}

/**
 * Handle vote submission
 */
function handleSubmitVote() {
    if (!selectedCandidateId) {
        alert('Please select a candidate before submitting your vote.');
        return;
    }

    // In a real app, this would be sent to a server
    mockData.hasVoted = true;

    // Show success message
    successMessage.style.display = 'flex';

    // Update user's account status to active (if not already)
    if (currentUser.accountStatus === 'INACTIVE') {
        currentUser.accountStatus = 'ACTIVE';
    }

    // Update election status to closed after voting (for demo purposes)
    // In a real system, election status would be managed independently
    setTimeout(() => {
        updateElectionStatus('CLOSED');
    }, 3000);
}

/**
 * Simulate election status changes (for demo purposes)
 */
function simulateElectionStatusChanges() {
    // Start with election pending
    setTimeout(() => {
        updateElectionStatus('ACTIVE');
        console.log('Election status changed to ACTIVE');
    }, 5000);

    // Close election after 30 seconds
    setTimeout(() => {
        if (electionStatus === 'ACTIVE') {
            updateElectionStatus('CLOSED');
            console.log('Election status changed to CLOSED');
        }
    }, 30000);
}

// Initialize the application when the DOM is fully loaded
document.addEventListener('DOMContentLoaded', () => {
    initApp();

    // For demo purposes: simulate election status changes
    // In a real app, this would come from a server
    simulateElectionStatusChanges();
});