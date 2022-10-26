all:

backend:
	sbt "~backend/reStart"

frontend:
	sbt "~frontend/fastLinkJS"


frontend-init:
	cd frontend
	npm install
	yarn exec vite
