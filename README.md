
# ПР 4 — RSocket, АС Поставщиков


## Структура проекта

```
rsocket-suppliers/
├── docker-compose.yml        # Postgres для сервера
├── server/                   # RSocket-сервер, порт 5200
│   └── src/main/java/ru/white/rsserver/
│       ├── model/            # Darkstore, Goods, Delivery (JPA-сущности), DeliveryView (DTO)
│       ├── repository/       # JpaRepository на каждую сущность
│       └── controller/       # MainSocketController — все 4 @MessageMapping
└── client/                   # RSocket-клиент + REST-обёртка, порт 8081
    └── src/main/java/ru/white/rsclient/
        ├── config/           # настройка RSocketRequester
        └── controller/       # 4 REST-контроллера, по одному на модель
```

## Запуск на Mac (M1/arm64)

Нужны: Docker Desktop и JDK 17+. Отдельно ставить Gradle **не нужно** — в
проекте есть Gradle Wrapper (`gradlew`), он сам скачивает и использует
закреплённую версию Gradle (8.8), независимо от того, что стоит в системе
через Homebrew. Это важно: `brew install gradle` сейчас ставит Gradle 9.x,
а Spring Boot Gradle-плагин, на котором собран этот проект (3.3.0),
официально совместим только с Gradle 7.6–8.8 — на 9.x сборка падает на
`compileJava` с `ExceptionInInitializerError`. `./gradlew` эту проблему
снимает сама, без вмешательства.

```
brew install openjdk@17
```
Если не встал в PATH:
```
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
```
Новый терминал, проверка: `java -version` → должно быть 17.

**1. Поднять БД** (один раз, из корня проекта, рядом с `docker-compose.yml`):
```
docker compose up -d
```
Образ подтягивается как `mirror.gcr.io/library/postgres:16-alpine` —
зеркало Google, а не напрямую `docker.io`. Это осознанно: у Docker Hub
(`registry-1.docker.io`) бывают блокировки по IP для российских адресов,
и `docker pull postgres:16-alpine` может упасть с `403 Forbidden` на
обычном домашнем/рабочем интернете, хотя сам compose-файл тут ни при чём.
Образ мультиплатформенный — на M1 подтянется нативная arm64-версия, без
Rosetta. Проверить, что контейнер жив:
```
docker compose ps
```

Если 403 вылезает и на зеркале Google (бывает при VPN/прокси, которые режут
гугловские сервисы), варианты:
- включить VPN и повторить `docker compose up -d`;
- залогиниться в Docker Hub (`docker login`) — иногда авторизованным
  аккаунтам отдают образ там, где анонимным — нет;
- подставить другое зеркало в `docker-compose.yml`, например
  `registry.yandexcloud.net/postgres:16-alpine` (если есть доступ к
  Yandex Cloud Container Registry) или любой другой доступный у вас прокси
  для Docker Hub.

**2. Запустить сервер и клиент** в двух вкладках терминала:
```
cd server && ./gradlew bootRun
cd client && ./gradlew bootRun
```
При самом первом запуске `./gradlew` скачает Gradle 8.8 (нужен интернет,
один раз, дальше берётся из кеша `~/.gradle`). Сервер слушает 5200 (это
RSocket, в браузере не откроется — нормально) и при старте подключается к
Postgres на `localhost:5432`. Клиент — 8081 (обычный HTTP, проверяется
curl'ом/Postman'ом).

**3. Остановить БД**, когда закончили:
```
docker compose down          # данные останутся в volume
docker compose down -v       # либо снести вместе с данными
```

Альтернатива Homebrew-установке Java — открыть `server` и `client` как два
отдельных проекта в IntelliJ IDEA, она сама подтянет JDK и будет
использовать Gradle Wrapper проекта; Docker Desktop для БД всё равно нужен
отдельно.

### Если всё равно падает на `compileJava` с `ExceptionInInitializerError`

Если команда запущена как `./gradlew` (с `./`, не просто `gradle`), эта
ошибка уже не должна возникать — Wrapper как раз и существует, чтобы не
зависеть от версии Gradle, установленной глобально через Homebrew. Если
ошибка всё же повторяется, проверьте:

1. Что вы точно вызываете `./gradlew`, а не `gradle` — через `which gradle`
   видно, что стоит глобально, но это не должно использоваться.
2. Какой JDK видит система — если это что-то совсем новое (JDK 22+),
   Gradle 8.8 может быть с ним несовместим так же, как был несовместим
   со старым Lombok:
   ```
   /usr/libexec/java_home -V
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ```
   и повторить `./gradlew bootRun`. Чтобы не делать `export` каждый раз,
   добавьте строку в `~/.zshrc`:
   ```
   echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
   ```

## Как проверить каждую модель взаимодействия

Request-Response (товар по id, 999 — несуществующий, проверка п.5):
```
curl http://localhost:8081/api/goods/1
curl -i http://localhost:8081/api/goods/999   # 404
```

Request-Stream (доставки с задержкой 700мс; `curl -N` держит соединение
открытым — видно построчное поступление, а не один блок, п.3 и п.7):
```
curl -N http://localhost:8081/api/deliveries/stream
```

Fire-and-Forget (удаление адреса склада):
```
curl -X DELETE http://localhost:8081/api/darkstores/2
```

Channel (несколько товаров одним запросом, ответ потоком — п.7):
```
curl -X POST http://localhost:8081/api/goods/channel \
  -H "Content-Type: application/json" \
  -d '{"goods":[{"name":"Мышь","price":1500},{"name":"Коврик","price":500}]}'
```

## Тесты сервера

БД должна быть поднята (`docker compose up -d`), иначе тесты не смогут
подключиться:
```
cd server && ./gradlew test
```
Четыре теста — по одному на каждую модель взаимодействия, включая проверку
`404` на несуществующий товар.


# Сгенерирован ИИ, так как были проблемы с запуском