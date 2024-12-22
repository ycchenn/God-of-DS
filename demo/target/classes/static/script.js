document.getElementById('search-form').addEventListener('submit', function (e) {
    e.preventDefault();
    const query = document.getElementById('query').value;
    const resultsDiv = document.getElementById('results');
    resultsDiv.innerHTML = '<p>搜尋中...</p>';

    fetch(`/api/search?q=${encodeURIComponent(query)}`)
        .then(response => response.json())
        .then(data => {
            resultsDiv.innerHTML = '';

            // 顯示搜尋結果
            const results = data.results;
            if (!results || Object.keys(results).length === 0) {
                resultsDiv.innerHTML = '<p>沒有找到結果。</p>';
                return;
            }

            for (const [title, url] of Object.entries(results)) {
                const itemDiv = document.createElement('div');
                itemDiv.classList.add('result-item');

                const link = document.createElement('a');
                link.href = url;
                link.target = '_blank';
                link.textContent = title;

                itemDiv.appendChild(link);
                resultsDiv.appendChild(itemDiv);
            }

            // 顯示相關搜尋關鍵字
            const relatedKeywords = data.relatedKeywords;
            if (relatedKeywords && relatedKeywords.length > 0) {
                const relatedDiv = document.createElement('div');
                relatedDiv.classList.add('related-keywords');
                relatedDiv.innerHTML = '<h3>其他人也搜尋：</h3>';

                relatedKeywords.forEach(keyword => {
                    const keywordButton = document.createElement('button');
                    keywordButton.textContent = keyword;
                    keywordButton.classList.add('keyword-button');
                    keywordButton.addEventListener('click', () => {
                        document.getElementById('query').value = keyword;
                        document.getElementById('search-form').dispatchEvent(new Event('submit'));
                    });
                    relatedDiv.appendChild(keywordButton);
                });

                resultsDiv.appendChild(relatedDiv);
            }
        })
        .catch(error => {
            console.error('錯誤:', error);
            resultsDiv.innerHTML = '<p>發生錯誤，請稍後再試。</p>';
        });
});
