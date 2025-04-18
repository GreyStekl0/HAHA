# HAHA

**H**umor **A**nalysis via **H**uman-like **A**I

## Описание

**H**A**H**A — это веб-приложение для анализа анекдотов и шуток с помощью искусственного интеллекта. Система оценивает текст шутки по нескольким критериям: смешнявость, кринжовость, приличность, а также выдает краткое текстовое ревью.

## Основные функции
- Веб-интерфейс для ввода анекдотов/шуток
- Оценка по шкале (0–10) по трем критериям: смешнявость, кринжовость, приличность
- Краткое текстовое ревью
- API для оценки шутки (GET /api/evaluate?joke=...)
- Кастомный DNS-сервер

## Технологии
- Kotlin (JVM 21)
- Ktor (сервер, маршрутизация, статические ресурсы)
- Koin (DI)
- kotlinx.serialization (JSON)
- Logback (логирование)
- OkHttp + dnsjava (HTTP клиент и кастомный DNS)
- Docker (сборка через ktor plugin)

## Требования
- JDK 21
- Docker (опционально)

## Быстрый старт

1. **Сборка проекта**

```sh
./gradlew build
```

2. **Запуск локального сервера**

```sh
export API_KEY=your_api_key
export DNS_SERVER=your_dns_server # опционально
./gradlew run
```

3. **Доступ к приложению**

Перейдите в браузере по адресу: http://localhost:80

4. **Оценка шутки через API**

```
GET /api/evaluate?joke=Текст_шутки
```

## Docker

Сборка и запуск контейнера:

```sh
./gradlew buildImage
docker load -i build/jib-image.tar
docker run -e API_KEY=your_api_key -e DNS_SERVER=your_dns_server -p 80:80 haha-app # DNS_SERVER опционально
```
### или
```sh
docker pull ghcr.io/greystekl0/haha:1.0.0
docker run -e API_KEY=your_api_key -e DNS_SERVER=your_dns_server -p 80:80 haha-app # DNS_SERVER опционально
```

## Структура проекта
- `src/main/kotlin/` — исходный код приложения
  - `server/` — запуск и маршрутизация Ktor
  - `domain/` — бизнес-логика и сущности
  - `data/` — доступ к данным, интеграция с AI
  - `di/` — зависимости Koin
- `src/main/resources/` — ресурсы 
  - `static/` — стили
  - `templates/` — HTML-страницы
  - `logback.xml` — конфигурация логирования

## Авторы
- [Stekl0](https://github.com/stekl0) (backend)
- [Kira Arensky](https://github.com/KiraArensky) (frontend)

---

Лицензия MIT. Используйте на здоровье, но не забывайте про авторские права.