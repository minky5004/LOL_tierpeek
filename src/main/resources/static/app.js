// 전역 상태
let dashboardData = null;
let chartInstance = null;
const AUTO_REFRESH_INTERVAL = 5 * 60 * 1000; // 5분
let isRefreshing = false;

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
    document.getElementById('dateRangeSelect').addEventListener('change', updateChart);
    document.getElementById('addFriendForm').addEventListener('submit', (e) => {
        e.preventDefault();
        addFriend();
    });
    document.getElementById('refreshBtn').addEventListener('click', runRefresh);
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

    const isMasterAbove = ['MASTER', 'GRANDMASTER', 'CHALLENGER'].includes(friend.tier);

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

    // 현재 랭크 (마스터+ 는 division 없음)
    const rankInfo = document.createElement('div');
    rankInfo.className = 'rank-info';
    const rankLabel = document.createElement('div');
    rankLabel.className = 'rank-label';
    rankLabel.textContent = '현재 랭크';
    const rankValue = document.createElement('div');
    rankValue.className = 'rank-value';
    rankValue.style.color = tierColor;
    rankValue.textContent = isMasterAbove ? friend.tier : `${friend.tier} ${friend.division || ''}`;
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
    lpInfo.appendChild(lpLabel);
    lpInfo.appendChild(lpValue);

    // 마스터 이상이 아닌 경우에만 바 표시
    if (!isMasterAbove) {
        const lpPercentage = (friend.leaguePoints / 100) * 100;
        const lpBar = document.createElement('div');
        lpBar.className = 'lp-bar';
        const lpFill = document.createElement('div');
        lpFill.className = 'lp-fill';
        lpFill.style.width = lpPercentage + '%';
        lpBar.appendChild(lpFill);
        lpInfo.appendChild(lpBar);
    }

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
            chartContainer.innerHTML = '';
        }
        const chartInfo = document.getElementById('chartInfo');
        if (chartInfo) {
            chartInfo.style.display = 'none';
            chartInfo.innerHTML = '';
        }
        showErrorMessage('그래프 데이터가 없습니다');
        return;
    }

    const chartContainer = document.getElementById('lpChart');
    if (chartContainer) {
        chartContainer.style.display = 'block';
    }

    // 날짜순으로 정렬 (오래된 것부터)
    let sorted = [...rankHistory].sort((a, b) =>
        new Date(a.recordedAt) - new Date(b.recordedAt)
    );

    // 기간 필터링
    const dateRange = document.getElementById('dateRangeSelect').value;
    const now = new Date();

    if (dateRange === '7') {
        const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        sorted = sorted.filter(point => new Date(point.recordedAt) >= sevenDaysAgo);
    } else if (dateRange === '30') {
        const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
        sorted = sorted.filter(point => new Date(point.recordedAt) >= thirtyDaysAgo);
    } else if (dateRange === 'latest') {
        sorted = sorted.slice(-20);
    }

    if (sorted.length === 0) {
        showErrorMessage('선택한 기간에 데이터가 없습니다');
        return;
    }

    // 데이터 샘플링: 포인트가 50개 이상이면 자동 샘플링
    const MAX_CHART_POINTS = 20;
    let displayData = sorted;
    if (sorted.length > MAX_CHART_POINTS) {
        displayData = sampleData(sorted, MAX_CHART_POINTS);
    }

    const labels = displayData.map(point => {
        const date = new Date(point.recordedAt);
        return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    });

    const lpData = displayData.map(point => point.leaguePoints);
    const tierData = displayData.map(point => point.tier);
    const divisionData = displayData.map(point => point.division);
    const recordedAtData = displayData.map(point => point.recordedAt);

    const maxLp = Math.max(...lpData);
    const minLp = Math.min(...lpData);
    const yAxisMax = Math.max(100, Math.ceil(maxLp * 1.1));
    const yAxisMin = Math.max(0, Math.floor(minLp * 0.9));

    // 현재(최신) 티어 정보 표시
    const latestData = sorted[sorted.length - 1];
    const tierInfoDiv = document.getElementById('chartInfo');
    if (tierInfoDiv) {
        tierInfoDiv.style.display = 'block';
        const tierStr = ['MASTER', 'GRANDMASTER', 'CHALLENGER'].includes(latestData.tier)
            ? latestData.tier
            : `${latestData.tier} ${latestData.division || ''}`;
        document.getElementById('currentTierInfo').textContent = `현재 티어: ${tierStr} (${latestData.leaguePoints} LP)`;
    }

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
                    pointRadius: 5,
                    pointBackgroundColor: '#667eea',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointHoverRadius: 7,
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
                    backgroundColor: 'rgba(0, 0, 0, 0.9)',
                    padding: 12,
                    cornerRadius: 6,
                    titleFont: {
                        size: 13,
                        weight: 'bold'
                    },
                    bodyFont: {
                        size: 12
                    },
                    callbacks: {
                        title: (context) => {
                            const index = context[0].dataIndex;
                            const date = new Date(recordedAtData[index]);
                            return date.toLocaleString('ko-KR');
                        },
                        label: (context) => {
                            const index = context.dataIndex;
                            const tier = tierData[index];
                            const division = divisionData[index];
                            const lp = context.parsed.y;
                            return `${tier} ${division ? division : ''} - ${lp} LP`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    min: yAxisMin,
                    max: yAxisMax,
                    ticks: {
                        font: {
                            size: 11,
                            weight: '500'
                        },
                        color: '#666',
                        stepSize: 25
                    },
                    title: {
                        display: true,
                        text: 'League Points',
                        font: {
                            size: 12,
                            weight: 'bold'
                        },
                        color: '#333'
                    },
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)'
                    }
                },
                x: {
                    ticks: {
                        font: {
                            size: 10
                        },
                        color: '#666',
                        maxRotation: 45,
                        minRotation: 0,
                        maxTicksLimit: 12
                    },
                    title: {
                        display: true,
                        text: '날짜/시간',
                        font: {
                            size: 12,
                            weight: 'bold'
                        },
                        color: '#333'
                    },
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)'
                    }
                }
            }
        }
    });
}

// 데이터 샘플링: 많은 데이터를 균등하게 줄임
function sampleData(data, maxPoints) {
    if (data.length <= maxPoints) {
        return data;
    }

    const sampled = [];
    const step = Math.ceil(data.length / maxPoints);

    for (let i = 0; i < data.length; i += step) {
        sampled.push(data[i]);
    }

    // 마지막 데이터포인트 항상 포함
    if (sampled[sampled.length - 1] !== data[data.length - 1]) {
        sampled.push(data[data.length - 1]);
    }

    return sampled;
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

// 전체 갱신 (랭크 + 매치)
async function runRefresh() {
    // 동시 실행 방지
    if (isRefreshing) {
        showErrorMessage('갱신이 이미 진행 중입니다. 잠시 후 다시 시도해주세요.');
        return;
    }

    isRefreshing = true;
    const refreshBtn = document.getElementById('refreshBtn');

    try {
        showLoadingSpinner(true);
        if (refreshBtn) refreshBtn.disabled = true;

        const response = await fetch('/api/scheduler/refresh', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const apiResponse = await response.json();

        showLoadingSpinner(false);

        if (apiResponse.success) {
            showSuccessMessage('✓ ' + apiResponse.message);
            setTimeout(() => {
                loadDashboard();
            }, 500);
        } else {
            showErrorMessage('갱신 실패: ' + apiResponse.message);
        }
    } catch (error) {
        console.error('갱신 오류:', error);
        showLoadingSpinner(false);
        showErrorMessage('갱신 중 오류 발생');
    } finally {
        isRefreshing = false;
        if (refreshBtn) refreshBtn.disabled = false;
    }
}