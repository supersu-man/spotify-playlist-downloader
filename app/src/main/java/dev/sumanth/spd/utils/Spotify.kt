package dev.sumanth.spd.utils

 object SpotifyManager {

    val jsScript = """
        (async () => {
            const delay = ms => new Promise(res => setTimeout(res, ms));
            await delay(4000);
            console.log("JS: Starting scraper");
            
            const rows = [];
            const set = new Set();

            const handleRows = () => {
                const tracks = [...document.querySelectorAll('[data-testid="track-row"]')]
                  .map(row => {
                    const title = row.querySelector('[data-encore-id="listRowTitle"]')?.innerText;
                    const artist = row.querySelector('.encore-text-body-small')?.innerText;
                
                    return { title, artist };
                  })
                  .filter(t => t.title && t.artist);

                tracks.forEach(track => {
                    const key = track.title + '-' + track.artist;
                    if (!set.has(key)) {
                        set.add(key);
                        rows.push(track);
                    }
                });
                console.log("JS: Found " + rows.length + " rows");
            };

            let lastScroll = -1;
            let scrollCount = 0;

            const interval = setInterval(() => {
              window.scrollBy(0, 400);
              handleRows();
              scrollCount++;
              console.log("JS: Scrolling... (" + scrollCount + ") Found: " + rows.length);

              const currentScroll = window.scrollY;
              if (currentScroll === lastScroll && lastScroll !== -1) {
                console.log("JS: Reached bottom");
                console.log('FINAL_ROWS: ' + JSON.stringify(rows));
                clearInterval(interval);
                return;
              }
              lastScroll = currentScroll;
            }, 1500);
        })();
    """.trimIndent()

}
