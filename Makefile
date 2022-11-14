all:

backend:
	sbt '~backend/reStart'

frontend:
	sbt '~frontend/fastLinkJS'


frontend-vite:
	cd modules/frontend && \
		npm install && \
		./node_modules/yarn/bin/yarn run vite

test-ping:
	curl http://127.0.0.1:8080/api/system/ping

test-ws:
	echo TODO

clean:
	sbt clean
