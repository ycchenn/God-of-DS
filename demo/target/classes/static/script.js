document.getElementById('search-form').addEventListener('submit', function (e) {
    e.preventDefault();
    const query = document.getElementById('query').value;
    const resultsDiv = document.getElementById('results');
    resultsDiv.innerHTML = '<p>搜尋中...</p>';

    fetch(`/api/search?q=${encodeURIComponent(query)}`)
        .then(response => response.json())
        .then(data => {
            resultsDiv.innerHTML = '';

            // 檢查數據是否為空
            if (!data || Object.keys(data).length === 0) {
                resultsDiv.innerHTML = '<p>沒有找到結果。</p>';
                return;
            }

            // 迭代後端返回的結果並生成HTML
            for (const [title, url] of Object.entries(data)) {
                const itemDiv = document.createElement('div');
                itemDiv.classList.add('result-item');

                const link = document.createElement('a');
                link.href = url;
                link.target = '_blank';
                link.textContent = title;

                itemDiv.appendChild(link);
                resultsDiv.appendChild(itemDiv);
            }
        })
        .catch(error => {
            console.error('錯誤:', error);
            resultsDiv.innerHTML = '<p>發生錯誤，請稍後再試。</p>';
        });
});
