document.getElementById('search-form').addEventListener('submit', function (e) {
    e.preventDefault();
    const query = document.getElementById('query').value;
    const resultsDiv = document.getElementById('results');
    resultsDiv.innerHTML = '<p>搜尋中...</p>';

    fetch(`/api/search?q=${encodeURIComponent(query)}`)
        .then(response => response.json())
        .then(data => {
            resultsDiv.innerHTML = '';

            
            const results = data.results;
            if (!results || Object.keys(results).length === 0) {
                resultsDiv.innerHTML = '<p>沒有找到結果。</p>';
                return;
            }

            const resultsContainer = document.createElement('div');
            resultsContainer.classList.add('results-container');

            for (const [title, url] of Object.entries(results)) {
                const card = document.createElement('div');
                card.classList.add('result-card');

                
                card.addEventListener('click', () => {
                    window.open(url, '_blank');
                });

                const cardTitle = document.createElement('h3');
                cardTitle.textContent = title;

                card.appendChild(cardTitle);
                resultsContainer.appendChild(card);
            }

            resultsDiv.appendChild(resultsContainer);

            
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
