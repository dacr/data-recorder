all:

backend:
	sbt '~backend/reStart'

frontend:
	sbt '~frontend/fastLinkJS'


frontend-vite:
	cd modules/frontend && \
		npm install && \
		./node_modules/yarn/bin/yarn run vite
