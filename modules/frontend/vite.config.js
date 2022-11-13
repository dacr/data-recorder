import {resolve} from 'path'
import {minifyHtml, injectHtml} from 'vite-plugin-html'

const scalaVersion = '3.2.1'

export default ({mode}) => {
    const mainJS = `./target/scala-${scalaVersion}/frontend-${mode === 'production' ? 'opt' : 'fastopt'}/main.js`
    const script = `<script type="module" src="${mainJS}"></script>`
    return {
        publicDir: './src/main/static/public',
        plugins: [
            ...(process.env.NODE_ENV === 'production' ? [minifyHtml(),] : []),
            injectHtml({
                injectData: {
                    script
                }
            })
        ],
        server: {
            proxy: {
                '/api': {
                    target: 'http://127.0.0.1:8080/api',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api/, '')
                },
                '/ws': {
                    target: 'http://127.0.0.1:8080/ws',
                    changeOrigin: true,
                    ws: true,
                    rewrite: (path) => path.replace(/^\/ws/, '')
                }
            }
        },
        resolve: {
            alias: {
                'stylesheets': resolve(__dirname, './src/main/static/stylesheets'),
            }
        }
    }
}
