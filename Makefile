all:

# ---------------------------------------------------------
backend:
	sbt '~backend/reStart'

frontend:
	sbt '~frontend/fastLinkJS'


frontend-vite: frontend-vite-req
	cd modules/frontend && npx yarn run vite

# ---------------------------------------------------------

frontend-vite-req:
	cd modules/frontend && npm install

# ---------------------------------------------------------

clean:
	sbt clean

# ---------------------------------------------------------

test-ping:
	curl http://127.0.0.1:8080/api/system/ping

test-ping-vite:
	curl http://127.0.0.1:3000/api/system/ping

test-ws:
	scala-cli wscat.sc -- "ws://127.0.0.1:8080/ws/test/stream"

test-ws-vite:
	scala-cli wscat.sc -- "ws://127.0.0.1:3000/ws/test/stream"

# ---------------------------------------------------------

test-ws-node: test-req-node
	npx wscat -c "ws://127.0.0.1:8080/ws/test/stream"

test-ws-vite-node: test-req-node
	npx wscat -c "ws://127.0.0.1:3000/ws/test/stream"

# ---------------------------------------------------------
test-req-node:
	npm install wscat
