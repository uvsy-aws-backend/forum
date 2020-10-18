.PHONY: init \
init-npm \
clean \
clean-npm \
clean-sls \
clean-test \
test \
coverage \
deploy \

.DEFAULT_GOAL := help

VENV ?= venv

# Deploy configuration
STAGE ?= dev
AWS_PROFILE ?= uvsy-dev

help:
	@echo "    init"
	@echo "        Initialize development environment."
	@echo "    init-npm"
	@echo "        Initialize npm environment."
	@echo "    clean"
	@echo "        Remove all the development environment files."
	@echo "    clean-npm"
	@echo "        Remove Node modules."
	@echo "    clean-sls"
	@echo "        Remove Serverless artifacts."
	@echo "    clean-test"
	@echo "        Remove Test data."
	@echo "    test"
	@echo "        Run tests."
	@echo "    deploy"
	@echo "        Build and deploy to AWS."

init: clean init-npm init-gradle init-venv
	@echo "Project initialized"

init-npm:
	@npm install

init-gradle:
	@./gradlew compileJava

init-venv: clean-venv create-venv update-venv
	@echo ""
	@echo "Do not forget to activate your new virtual environment"

create-venv:
	@echo "Creating virtual environment: $(VENV)..."
	@python3 -m venv $(VENV)

update-venv:
	@echo "Updating virtual environment: $(VENV)..."
	@( \
		. $(VENV)/bin/activate; \
		pip install --upgrade pip; \
		pip install pre-commit; \
		pre-commit install; \
	)

clean: clean-npm clean-sls clean-out clean-build

clean-npm:
	@echo "Removing node modules..."
	@rm -rf node_modules

clean-sls:
	@echo "Removing serverless files..."
	@rm -rf .serverless

clean-venv:
	@echo "Removing virtual environment: $(VENV)..."
	@rm -rf $(VENV)

clean-build:
	@echo "Removing build artifacts..."
	@./gradlew clean

clean-out:
	@echo "Removing compiled artifacts..."
	@rm -rf out
test:
	@./gradlew test

build: clean-build
	@./gradlew build

domain:
	@echo "Creating domain for service"
	@npx serverless create_domain

deploy: build
	@echo "Deploying to '$(STAGE)' with profile '$(AWS_PROFILE)'..."
	@npx serverless deploy -v --stage $(STAGE) --profile $(AWS_PROFILE)

run: migrate clean-build build
	@serverless offline start -v --stage local --noAuth


migrate:
	@./gradlew flywayMigrate
