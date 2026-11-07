const fs = require('node:fs');
const path = require('node:path');
const { marked } = require('marked');
const { chromium } = require('playwright-core');

const root = path.resolve(__dirname, '../..');
const read = name => fs.readFileSync(path.join(root, name), 'utf8');

function javaSources(directory) {
    return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
        const file = path.join(directory, entry.name);
        return entry.isDirectory() ? javaSources(file) : (file.endsWith('.java') ? [file] : []);
    }).sort();
}

function figure(_, caption, filename) {
    const file = fs.readFileSync(path.join(root, filename));
    const mime = filename.endsWith('.svg') ? 'image/svg+xml' : 'image/png';
    return '<figure><img alt="" src="data:' + mime + ';base64,' + file.toString('base64')
        + '"><figcaption>' + caption + '</figcaption></figure>';
}

function reportBody() {
    const sheets = read('REPORT.md').replaceAll('\r\n', '\n').trim().split(/\n---\n/);
    return sheets.map(sheet => {
        const kind = sheet.includes('(docs/uml/class-model.svg)') ? 'model-sheet' : 'sheet';
        const content = sheet.replace(/^!\[([^\]]+)\]\((docs\/[^)]+)\)$/gm, figure);
        return '<section class="' + kind + '">' + marked.parse(content) + '</section>';
    }).join('\n');
}

async function build() {
    const candidates = [process.env.CHROME_PATH,
        'C:/Program Files/Google/Chrome/Application/chrome.exe',
        'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
        '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
        '/usr/bin/google-chrome', '/usr/bin/chromium', '/usr/bin/chromium-browser'];
    const executablePath = candidates.find(file => file && fs.existsSync(file));
    if (!executablePath) throw new Error('Set CHROME_PATH to a Chromium browser executable.');
    const browser = await chromium.launch({ executablePath, headless: true });
    try {
        const page = await browser.newPage();
        await page.setContent('<!doctype html><html lang="en"><head><meta charset="utf-8">'
            + '<title>CampusPrint | Amir Baiseitov | Software Design Patterns</title>'
            + '<style>' + read('tools/report/print.css') + '</style></head><body>'
            + reportBody() + '</body></html>', { waitUntil: 'load' });

        const files = javaSources(path.join(root, 'src')).map(file => ({
            name: path.relative(path.join(root, 'src'), file).split(path.sep).join('/'),
            source: fs.readFileSync(file, 'utf8').replaceAll('\r\n', '\n').trimEnd()
        }));
        await page.evaluate(files => {
            // Keep PlantUML labels selectable when printing diagrams with nested SVGs.
            const decode = data => new TextDecoder().decode(
                Uint8Array.from(atob(data.slice(data.indexOf(',') + 1)), ch => ch.charCodeAt(0)));
            const parse = data => new DOMParser().parseFromString(decode(data), 'image/svg+xml').documentElement;
            for (const image of document.querySelectorAll('figure > img[src^="data:image/svg+xml"]')) {
                const svg = parse(image.src);
                for (const child of svg.querySelectorAll('image')) {
                    const href = child.getAttributeNS('http://www.w3.org/1999/xlink', 'href');
                    if (!href?.startsWith('data:image/svg+xml')) continue;
                    const inner = parse(href);
                    for (const attr of ['x', 'y', 'width', 'height']) {
                        inner.setAttribute(attr, child.getAttribute(attr));
                    }
                    child.replaceWith(inner);
                }
                svg.removeAttribute('style');
                svg.removeAttribute('width');
                svg.removeAttribute('height');
                svg.setAttribute('preserveAspectRatio', 'xMidYMid meet');
                image.replaceWith(svg);
            }

            const listing = document.querySelector('section:last-of-type');
            for (const { name, source } of files) {
                const label = document.createElement('h3');
                label.textContent = name;
                const code = document.createElement('pre');
                code.className = 'source';
                code.textContent = source;
                listing.append(label, code);
            }
        }, files);
        await page.evaluate(() => document.fonts.ready);
        const filename = path.join(root, 'output/pdf/CampusPrint-Report.pdf');
        fs.mkdirSync(path.dirname(filename), { recursive: true });
        await page.pdf({ path: filename, printBackground: true, preferCSSPageSize: true, tagged: true, outline: true });
        console.log(filename);
    } finally {
        await browser.close();
    }
}

build().catch(error => { console.error(error.message); process.exitCode = 1; });
