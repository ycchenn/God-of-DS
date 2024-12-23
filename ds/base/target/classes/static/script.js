document.getElementById('search-form').addEventListener('submit', function(e) {
    e.preventDefault(); // 防止表單提交刷新頁面
    const query = document.getElementById('query').value;
    const resultsDiv = document.getElementById('results');
    resultsDiv.innerHTML = '<p>搜尋中...</p>';

    fetch(`/api/search?q=${encodeURIComponent(query)}`)
        .then(response => response.json())
        .then(data => {
            resultsDiv.innerHTML = '';
            if (Object.keys(data).length === 0) {
                resultsDiv.innerHTML = '<p>沒有找到結果。</p>';
                return;
            }
            for (const [title, url] of Object.entries(data)) {
                const itemDiv = document.createElement('div');
                itemDiv.classList.add('result-item');

                const link = document.createElement('a');
                link.href = url;
                link.target = '_blank';
                link.textContent = title;

                const urlDiv = document.createElement('div');
                urlDiv.classList.add('result-url');
                urlDiv.textContent = url;

                itemDiv.appendChild(link);
                itemDiv.appendChild(urlDiv);
                resultsDiv.appendChild(itemDiv);
            }
        })
        .catch(error => {
            console.error('錯誤:', error);
            resultsDiv.innerHTML = '<p>發生錯誤，請稍後再試。</p>';
        });
});
