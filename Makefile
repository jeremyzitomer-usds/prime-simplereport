start:
	make -j start-backend start-frontend watch-backend
watch-backend:
	cd backend;\
	./gradlew build --continuous -xtest -xcheckstyleMain -xcheckstyleTest -xspotlessCheck -xbootBuildInfo
start-backend:
	cd backend;\
	docker-compose --env-file .env.development up -d db && \
	SR_DB_PORT=5433 ./gradlew bootRun --args='--spring.profiles.active=dev';

start-frontend:
	cd frontend;\
	bash -l -c 'nvm use && yarn start';

