.PHONY: help clean compile run test package install

MAVEN = mvn
JAVA = java
MAIN_CLASS = com.urlshortener.Main

help:
	@echo "Доступные команды:"
	@echo "  make compile  - Скомпилировать проект"
	@echo "  make run      - Запустить приложение (использует config.yaml из корня проекта)"
	@echo "  make run CONFIG=/path/to/config.yaml - Запустить с указанным конфигом"
	@echo "  make clean    - Очистить скомпилированные файлы"
	@echo "  make package  - Создать JAR файл"
	@echo "  make install  - Установить в локальный репозиторий Maven"
	@echo "  make test     - Запустить тесты (если есть)"
	@echo "  make all      - Очистить, скомпилировать и упаковать"

compile:
	$(MAVEN) clean compile

run: compile
	@if [ -n "$(CONFIG)" ]; then \
		$(MAVEN) exec:java -Dexec.mainClass="$(MAIN_CLASS)" -Dexec.args="--config $(CONFIG)"; \
	else \
		$(MAVEN) exec:java -Dexec.mainClass="$(MAIN_CLASS)"; \
	fi

run-classpath: compile
	$(JAVA) -cp target/classes:$(shell $(MAVEN) dependency:build-classpath -q -DincludeScope=compile) $(MAIN_CLASS)

clean:
	$(MAVEN) clean
	rm -rf out

package: compile
	$(MAVEN) package

run-jar: package
	$(JAVA) -jar target/url-shortener-1.0-SNAPSHOT.jar

install: package
	$(MAVEN) install

test:
	$(MAVEN) test

all: clean compile package

check:
	$(MAVEN) checkstyle:check || true



