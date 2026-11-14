# Учебная платформа (ORM / Hibernate)

Серверное приложение для онлайн-курсов: пользователи и роли, каталог курсов, запись студентов, уроки и домашние задания, тесты и отзывы. Данные хранятся в PostgreSQL, доступ к ним идёт через Hibernate и Spring Data JPA.

## Стек

- Java 26, Gradle
- Spring Boot 4.1 (Web MVC, Spring Data JPA, Validation)
- Hibernate, PostgreSQL 17
- springdoc OpenAPI (Swagger UI)
- Testcontainers для интеграционных тестов

## Структура проекта

```
com.example.learningplatform
├── config          конфигурация, OpenAPI, демо-данные
├── controller      REST API
├── domain          JPA-сущности
├── dto
│   ├── request
│   └── response
├── enums           Role, EnrollmentStatus, QuestionType
├── exception       доменные ошибки и общий обработчик
├── mapper          сущности → DTO
├── repository      Spring Data JPA
└── service         бизнес-сценарии
```

Контроллеры не отдают сущности наружу. Сервис внутри транзакции собирает DTO, поэтому REST-слой не сталкивается с ленивыми прокси.

## Модель данных

15 сущностей, как на схеме задания.

| Сущность | Связи |
|---|---|
| User | 1—1 Profile; 1—N курсы преподавателя, записи, решения, результаты тестов, отзывы |
| Profile | 1—1 User |
| Category | 1—N Course |
| Course | N—1 Category, N—1 User (преподаватель), 1—N Module, Enrollment, CourseReview, N—N Tag |
| Tag | N—N Course через таблицу `course_tags` |
| Enrollment | N—1 User, N—1 Course. Уникальна пара студент + курс |
| CourseReview | N—1 User, N—1 Course. Один отзыв студента на курс |
| Module | N—1 Course, 1—N Lesson, 1—1 Quiz |
| Lesson | N—1 Module, 1—N Assignment |
| Assignment | N—1 Lesson, 1—N Submission |
| Submission | N—1 Assignment, N—1 User. Одно решение студента на задание |
| Quiz | 1—1 Module, 1—N Question, 1—N QuizSubmission |
| Question | N—1 Quiz, 1—N AnswerOption |
| AnswerOption | N—1 Question, признак правильного ответа |
| QuizSubmission | N—1 Quiz, N—1 User. Хранится итог в процентах |

Коллекции и ссылки `@ManyToOne` / `@OneToOne` помечены `FetchType.LAZY`. Удаление курса каскадно удаляет модули, уроки, задания, решения, тест, вопросы, записи и отзывы. Теги при этом остаются: это общая классификация. Удаление преподавателя, у которого есть курсы, запрещено.

`spring.jpa.open-in-view=false`. Обращение к `course.getModules()` после закрытия сессии даёт `LazyInitializationException`. Это проверяет тест `LazyLoadingIntegrationTest`. Рабочий путь — читать структуру методом сервиса: транзакция ещё открыта, и наружу уходит уже собранный DTO.

## Запуск

Нужны JDK 26 и Docker.

Только база, приложение из Gradle:

```bash
docker compose up -d postgres
./gradlew bootRun
```

Профиль `dev` подключается к `localhost:5432`, база `learning_platform`, пользователь и пароль `learning`. Эти значения можно переопределить переменными `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

Приложение и база вместе:

```bash
docker compose up --build
```

После старта:

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs

При первом запуске с профилем `dev` загружаются демо-данные. Повторный запуск их не дублирует.

Демо-пользователи (паролей нет, аутентификация в задание не входит):

| Email | Роль |
|---|---|
| teacher@example.com | TEACHER |
| student1@example.com | STUDENT |
| student2@example.com | STUDENT |
| admin@example.com | ADMIN |

Демо-курс «Основы Hibernate»: два модуля, уроки, задания, тест. Иван записан на курс, отправил решение (оценка 90), прошёл тест и оставил отзыв. Мария только записана.

Роли проверяются в сервисах: студентом нельзя назначить курс, преподаватель не записывается на курс, оценку ставит преподаватель этого курса или администратор. Отдельного входа в систему нет, поэтому клиент передаёт id пользователя в теле запроса.

## Основные сценарии API

| Метод | Путь | Назначение |
|---|---|---|
| POST | `/api/users` | Создать пользователя, необязательно с профилем |
| GET | `/api/users?role=STUDENT` | Список пользователей |
| PUT | `/api/users/{id}` | Обновить имя, email и роль |
| PUT | `/api/users/{id}/profile` | Создать или обновить профиль |
| DELETE | `/api/users/{id}` | Удалить пользователя |
| GET | `/api/users/{id}/courses` | Курсы студента |
| GET | `/api/users/{id}/submissions` | Решения студента |
| GET | `/api/users/{id}/quiz-results` | Результаты тестов студента |
| POST/GET | `/api/categories` | Категории |
| POST/GET | `/api/tags` | Теги |
| POST | `/api/courses` | Создать курс: название, категория, преподаватель, теги |
| GET | `/api/courses?categoryId=&teacherId=&tagId=` | Список курсов |
| GET | `/api/courses/{id}` | Курс с модулями, уроками и заданиями |
| PUT | `/api/courses/{id}` | Обновить курс, в том числе заменить преподавателя |
| PUT | `/api/courses/{id}/tags` | Заменить набор тегов |
| DELETE | `/api/courses/{id}` | Удалить курс и зависимое содержимое |
| POST | `/api/courses/{courseId}/modules` | Добавить модуль |
| POST | `/api/modules/{moduleId}/lessons` | Добавить урок |
| POST | `/api/lessons/{lessonId}/assignments` | Добавить задание |
| POST | `/api/courses/{courseId}/enrollments` | Записать студента |
| DELETE | `/api/courses/{courseId}/enrollments/{studentId}` | Отписать |
| GET | `/api/courses/{courseId}/students` | Студенты курса |
| PATCH | `/api/enrollments/{id}` | Статус записи: ACTIVE, COMPLETED, CANCELLED |
| POST | `/api/assignments/{id}/submissions` | Сдать решение |
| PATCH | `/api/submissions/{id}/grade` | Поставить оценку и комментарий |
| POST | `/api/modules/{moduleId}/quiz` | Один тест на модуль |
| POST | `/api/quizzes/{id}/questions` | Вопрос с вариантами |
| POST | `/api/quizzes/{id}/attempts` | Пройти тест, балл считается на сервере |
| GET | `/api/quizzes/{id}/results` | Результаты теста |
| POST | `/api/courses/{courseId}/reviews` | Отзыв 1–5 |

Создание возвращает `201`, удаление — `204`. Ошибки приходят как Problem Details:

- `400` — невалидное тело, в поле `errors` перечислены поля
- `404` — сущность не найдена
- `409` — дубликат email, записи, решения, отзыва или теста модуля
- `422` — нарушено правило: не та роль, нет записи на курс, истёк срок, оценка выше максимума

Пример:

```bash
curl -s http://localhost:8080/api/courses | jq
curl -s -X POST http://localhost:8080/api/courses/1/enrollments \
  -H 'Content-Type: application/json' \
  -d '{"studentId":2}'
```

Актуальные id смотрите в списках пользователей и курсов: они зависят от порядка запуска.

## Тесты

Нужен запущенный Docker: интеграционные тесты поднимают PostgreSQL 17 через Testcontainers. H2 не используется.

```bash
./gradlew test
```

Покрыты создание, чтение, обновление и удаление, каскадное сохранение и удаление, уникальность записи и решения, оценка, прохождение теста, схема таблиц, коды HTTP и ленивая загрузка. Подсчёт балла и проверка ответов теста дополнительно покрыты модульными тестами без Spring.

## Профили

- `dev` — локальный PostgreSQL, `ddl-auto: update`, демо-данные. Включён по умолчанию.
- `test` — база Testcontainers, `ddl-auto: create-drop`, без демо-данных.
