// 전역 상태
let dashboardData = null;
let chartInstance = null;
const AUTO_REFRESH_INTERVAL = 5 * 60 * 1000; // 5분

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
    setupEventListeners();
    setAutoRefresh();
});

// 이벤트 리스너 설정
function setupEventListeners() {
    document.getElementById('friendSelect').addEventListener('change', updateChart);
    document.getElementById('queueSelect').addEventListener('change', updateChart);
    document.getElementById('addFriendForm').addEventListener('submit', (e) => {
        e.preventDefault();
        addFriend();
    });
}

// 자동 갱신 설정
function setAutoRefresh() {
    setInterval(() => {
        loadDashboard();
    }, AUTO_REFRESH_INTERVAL);
}

// 친구 추가
async function addFriend() {
    const gameName = document.getElementById('gameName').value.trim();
    const tagLine = document.getElementById('tagLine').value.trim();
    const platform = document.getElementById('platform').value;

    if (!gameName || !tagLine) {
        showErrorMessage('게임명과 태그를 입력해주세요');
        return;
    }

    try {
        showLoadingSpinner(true);

        const response = await fetch('/api/friends', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                gameName,
                tagLine,
                platform
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || `오류: ${response.status}`);
        }

        const apiResponse = await response.json();
        if (!apiResponse.success) {
            throw new Error(apiResponse.message || '친구 추가 실패');
        }

        // 폼 초기화
        document.getElementById('gameName').value = '';
        document.getElementById('tagLine').value = '';

        showLoadingSpinner(false);
        showSuccessMessage(`${gameName}#${tagLine}이 추가되었습니다`);

        // 대시보드 다시 로드
        setTimeout(() => {
            loadDashboard();
        }, 500);
    } catch (error) {
        console.error('친구 추가 실패:', error);
        showLoadingSpinner(false);
        showErrorMessage(`친구 추가 실패: ${error.message}`);
    }
}

// 친구 제거
async function removeFriend(puuid, friendName) {
    if (!confirm(`${friendName}을(를) 삭제하시겠습니까?`)) {
        return;
    }

    try {
        showLoadingSpinner(true);

        const response = await fetch(`/api/friends/${puuid}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error(`오류: ${response.status}`);
        }

        showLoadingSpinner(false);
        showSuccessMessage(`${friendName}이 제거되었습니다`);

        // 대시보드 다시 로드
        setTimeout(() => {
            loadDashboard();
        }, 500);
    } catch (error) {
        console.error('친구 제거 실패:', error);
        showLoadingSpinner(false);
        showErrorMessage(`친구 제거 실패: ${error.message}`);
    }
}

// 대시보드 데이터 로드
async function loadDashboard() {
    try {
        showLoadingSpinner(true);

        const response = await fetch('/api/dashboard');
        if (!response.ok) {
            throw new Error(`API 오류: ${response.status}`);
        }

        const apiResponse = await response.json();
        if (!apiResponse.success) {
            throw new Error(apiResponse.message || '데이터 로드 실패');
        }

        dashboardData = apiResponse.data;
        updateLastUpdateTime();
        renderFriendsCards();
        populateFriendSelect();

        // 친구가 있으면 그래프 섹션 표시
        if (dashboardData && dashboardData.length > 0) {
            document.getElementById('graphSection').style.display = 'block';
            document.getElementById('friendSelect').value = dashboardData[0].puuid;
            updateChart();
        } else {
            document.getElementById('graphSection').style.display = 'none';
        }

        showLoadingSpinner(false);
    } catch (error) {
        console.error('대시보드 로드 실패:', error);
        showLoadingSpinner(false);
        showErrorMessage(`오류: ${error.message}`);
    }
}

// 친구 카드 렌더링
function renderFriendsCards() {
    const container = document.getElementById('friendsContainer');
    container.innerHTML = '';

    if (!dashboardData || dashboardData.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <p>등록된 친구가 없습니다. 위에서 친구를 추가해주세요!</p>
            </div>
        `;
        return;
    }

    dashboardData.forEach(friend => {
        const card = createFriendCard(friend);
        container.appendChild(card);
    });
}

// 친구 카드 생성
function createFriendCard(friend) {
    const card = document.createElement('div');
    card.className = 'friend-card';

    const tierColor = getTierColor(friend.tier);
    const winRate = friend.wins + friend.losses > 0
        ? Math.round((friend.wins / (friend.wins + friend.losses)) * 100)
        : 0;

    const lpPercentage = (friend.leaguePoints / 100) * 100;

    // 헤더 (친구명 + 삭제 버튼)
    const header = document.createElement('div');
    header.style.display = 'flex';
    header.style.justifyContent = 'space-between';
    header.style.alignItems = 'center';
    header.style.marginBottom = '15px';

    const friendName = document.createElement('div');
    friendName.className = 'friend-name';
    friendName.textContent = friend.gameName;

    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'btn btn-danger';
    deleteBtn.textContent = '삭제';
    deleteBtn.addEventListener('click', () => removeFriend(friend.puuid, friend.gameName));

    header.appendChild(friendName);
    header.appendChild(deleteBtn);
    card.appendChild(header);

    // 현재 랭크
    const rankInfo = document.createElement('div');
    rankInfo.className = 'rank-info';
    const rankLabel = document.createElement('div');
    rankLabel.className = 'rank-label';
    rankLabel.textContent = '현재 랭크';
    const rankValue = document.createElement('div');
    rankValue.className = 'rank-value';
    rankValue.style.color = tierColor;
    rankValue.textContent = `${friend.tier} ${friend.division ? friend.division : ''}`;
    rankInfo.appendChild(rankLabel);
    rankInfo.appendChild(rankValue);
    card.appendChild(rankInfo);

    // LP
    const lpInfo = document.createElement('div');
    lpInfo.className = 'rank-info';
    const lpLabel = document.createElement('div');
    lpLabel.className = 'rank-label';
    lpLabel.textContent = 'LP';
    const lpValue = document.createElement('div');
    lpValue.className = 'rank-value';
    lpValue.textContent = friend.leaguePoints;
    const lpBar = document.createElement('div');
    lpBar.className = 'lp-bar';
    const lpFill = document.createElement('div');
    lpFill.className = 'lp-fill';
    lpFill.style.width = lpPercentage + '%';
    lpBar.appendChild(lpFill);
    lpInfo.appendChild(lpLabel);
    lpInfo.appendChild(lpValue);
    lpInfo.appendChild(lpBar);
    card.appendChild(lpInfo);

    // 전적
    const recordInfo = document.createElement('div');
    recordInfo.className = 'rank-info';
    const recordLabel = document.createElement('div');
    recordLabel.className = 'rank-label';
    recordLabel.textContent = '전적';
    const recordValue = document.createElement('div');
    recordValue.className = 'rank-value';
    recordValue.textContent = `${friend.wins}승 ${friend.losses}패 (${winRate}%)`;
    recordInfo.appendChild(recordLabel);
    recordInfo.appendChild(recordValue);
    card.appendChild(recordInfo);

    // 최근 전적
    if (friend.recentMatches && friend.recentMatches.length > 0) {
        const matchHistory = document.createElement('div');
        matchHistory.className = 'match-history';
        const matchHistoryLabel = document.createElement('div');
        matchHistoryLabel.className = 'match-history-label';
        matchHistoryLabel.textContent = '최근 전적';
        const recentMatches = document.createElement('div');
        recentMatches.className = 'recent-matches';

        friend.recentMatches.forEach(match => {
            const badge = document.createElement('span');
            badge.className = `match-badge ${match.win ? 'match-win' : 'match-loss'}`;
            badge.title = match.championName || '';
            badge.textContent = match.win ? 'W' : 'L';
            recentMatches.appendChild(badge);
        });

        matchHistory.appendChild(matchHistoryLabel);
        matchHistory.appendChild(recentMatches);
        card.appendChild(matchHistory);
    }

    return card;
}

// 티어별 색상
function getTierColor(tier) {
    const colors = {
        'IRON': '#8B7355',
        'BRONZE': '#CD7F32',
        'SILVER': '#C0C0C0',
        'GOLD': '#FFD700',
        'PLATINUM': '#E5E4E2',
        'DIAMOND': '#7851A9',
        'MASTER': '#C0C0C0',
        'GRANDMASTER': '#CC0000',
        'CHALLENGER': '#0096FF'
    };
    return colors[tier] || '#333';
}

// 친구 선택 드롭다운 채우기
function populateFriendSelect() {
    const select = document.getElementById('friendSelect');
    select.innerHTML = '<option value="">친구를 선택하세요</option>';

    if (dashboardData && dashboardData.length > 0) {
        dashboardData.forEach(friend => {
            const option = document.createElement('option');
            option.value = friend.puuid;
            option.textContent = `${friend.gameName} (${friend.tier} ${friend.division || ''})`;
            select.appendChild(option);
        });
    }
}

// 차트 업데이트
async function updateChart() {
    const puuid = document.getElementById('friendSelect').value;
    const queue = document.getElementById('queueSelect').value;

    if (!puuid) {
        return;
    }

    try {
        showLoadingSpinner(true);

        const response = await fetch(`/api/friends/${puuid}/rank-history?queue=${queue}`);
        if (!response.ok) {
            throw new Error(`API 오류: ${response.status}`);
        }

        const apiResponse = await response.json();
        if (!apiResponse.success) {
            throw new Error(apiResponse.message || '그래프 데이터 로드 실패');
        }

        const rankHistory = apiResponse.data;
        renderChart(rankHistory, puuid, queue);
        showLoadingSpinner(false);
    } catch (error) {
        console.error('그래프 로드 실패:', error);
        showLoadingSpinner(false);
        showErrorMessage(`그래프 로드 실패: ${error.message}`);
    }
}

// Chart.js로 그래프 렌더링
function renderChart(rankHistory, puuid, queue) {
    if (!rankHistory || rankHistory.length === 0) {
        if (chartInstance) {
            chartInstance.destroy();
            chartInstance = null;
        }
        const chartContainer = document.getElementById('lpChart');
        if (chartContainer) {
            chartContainer.style.display = 'none';
        }
        showErrorMessage('그래프 데이터가 없습니다');
        return;
    }

    const chartContainer = document.getElementById('lpChart');
    if (chartContainer) {
        chartContainer.style.display = 'block';
    }

    // 날짜순으로 정렬 (오래된 것부터)
    const sorted = [...rankHistory].sort((a, b) =>
        new Date(a.recordedAt) - new Date(b.recordedAt)
    );

    const labels = sorted.map(point => {
        const date = new Date(point.recordedAt);
        return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    });

    const lpData = sorted.map(point => point.leaguePoints);
    const tierData = sorted.map(point => point.tier);

    const maxLp = Math.max(...lpData);
    const yAxisMax = Math.max(100, Math.ceil(maxLp * 1.1));

    const ctx = document.getElementById('lpChart').getContext('2d');

    // 기존 차트가 있으면 제거
    if (chartInstance) {
        chartInstance.destroy();
    }

    chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'LP',
                    data: lpData,
                    borderColor: '#667eea',
                    backgroundColor: 'rgba(102, 126, 234, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 4,
                    pointBackgroundColor: '#667eea',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointHoverRadius: 6,
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    padding: 12,
                    cornerRadius: 6,
                    callbacks: {
                        afterLabel: (context) => {
                            const index = context.dataIndex;
                            return `Tier: ${tierData[index]}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: yAxisMax,
                    title: {
                        display: true,
                        text: 'League Points'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: '시간'
                    }
                }
            }
        }
    });
}

// 마지막 갱신 시간 업데이트
function updateLastUpdateTime() {
    const now = new Date();
    const timeStr = now.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
    document.getElementById('lastUpdate').textContent = timeStr;
}

// 로딩 스피너 표시/숨김
function showLoadingSpinner(show) {
    const spinner = document.getElementById('loadingSpinner');
    if (show) {
        spinner.classList.remove('hidden');
    } else {
        spinner.classList.add('hidden');
    }
}

// 에러 메시지 표시
function showErrorMessage(message) {
    const mainElement = document.querySelector('main');
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    mainElement.insertBefore(errorDiv, mainElement.firstChild);

    setTimeout(() => {
        errorDiv.remove();
    }, 5000);
}

// 성공 메시지 표시
function showSuccessMessage(message) {
    const mainElement = document.querySelector('main');
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.textContent = message;
    mainElement.insertBefore(successDiv, mainElement.firstChild);

    setTimeout(() => {
        successDiv.remove();
    }, 3000);
}