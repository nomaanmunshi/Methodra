.PHONY: validate backend-test backend-run android-test android-apk

validate:
	python3 scripts/validate_catalog.py

backend-test:
	cd backend && ./gradlew test

backend-run:
	docker compose up --build

android-test:
	cd android && ./gradlew :app:testDebugUnitTest

android-apk:
	cd android && ./gradlew :app:assembleDebug
