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

test-ws: test-req
	npx wscat -c "ws://127.0.0.1:8080/ws/system/events"

test-ws-vite: test-req
	npx wscat -c "ws://127.0.0.1:3000/ws/system/events"

# ---------------------------------------------------------
test-req:
	npm install wscat
