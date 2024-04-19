# Обзор проекта (Cloud storage)
![image](https://github.com/AleksandrKamen/Galaxy_drive/assets/144233016/a6458857-dfe0-42ab-a9be-ebb06f23c65a)

**Техническое задание проекта** -  [https://zhukovsd.github.io/java-backend-learning-course/Projects/CloudFileStorage/](https://zhukovsd.github.io/java-backend-learning-course/Projects/CloudFileStorage/)

**Суть проекта** - Многопользовательское файловое облако. Пользователи сервиса могут использовать его для загрузки и хранения файлов.

**Демонстрация проекта:** http://194.87.101.114:8080/ (возможность авторизации через Google и GitHub отсутствует)

**Используемые технологии/инструменты:**

•	[Gradle](https://gradle.org/)   

•	[Spring](https://spring.io/projects/spring-boot) (Spring Boot, Spring Security, Spring Sessions, Spring Test, Spring Web) 

•	[Docker](https://www.docker.com/)

•	[Thymeleaf](https://www.thymeleaf.org/)

•	[Bootstrap](https://getbootstrap.com/)

•	[Testcontainers](https://testcontainers.com/)

•	[PostgreSQL](https://www.postgresql.org/)

•	[Redis](https://redis.io/)

•	[Minio](https://min.io/)

**Структура базы данных**

![image](https://github.com/AleksandrKamen/Galaxy_drive/assets/144233016/57ff6657-be2b-419c-9596-0f889647a1b3)

В базе данных содержится информация о личных данных пользователя, зашифрованный пароль и также информация о способе регистрации и роли пользователя в системе 

**Интерфейс Приложения** - для пользователя доступны следующие страницы:

 Для незарегистрированных пользователей доступны страницы регистрации и авторизации

![image](https://github.com/AleksandrKamen/Galaxy_drive/assets/144233016/2d2a104a-f550-4017-971e-8c3d9e0b56d6)

Для авторизации пользователя есть 3 способа: Email, Google, GitHub 

![image](https://github.com/AleksandrKamen/Galaxy_drive/assets/144233016/ab671109-ca10-4318-b8fa-71bdad77f866)

 После успешной регистрации и авторизации пользователь попадает на главную страницу:
   
![image](https://github.com/AleksandrKamen/Galaxy_drive/assets/144233016/7d7b3372-7637-4e71-84c7-3127000cec90)

Если облачно хранилище пустое или путь для папки не задан, по умолчанию будет открыта корневая папка хранилища. 

![create](https://github.com/AleksandrKamen/Galaxy_drive/assets/144233016/0ce55262-6fe2-42da-8ccf-ec9224a13319)

Пользователь может создать новую папку в текущей дирректории с указанным уникальным именем, загрузить в текущую дирректорию файл(ы) или папку. При дублировании имен загружаемых файлов, к их имени будет добавлено уникальное числовое значение  
Для загружаемых файлов установлен лимит по размеру файла, также для пользователя установлен максимальный лимит загруженных файлов. Загруженные объекты подраздуляются на файлы и папки.     
![image](https://github.com/AleksandrKamen/Galaxy_drive/assets/144233016/34403ca4-0827-4eef-a3fd-ee14c3c3d119)

Пользователь может изменить язык интефейса (представлено 2 языка - Русский и Английский), войти в личный кабинет для изменения данных о себе, выйти из своего сеанса. 

![create2](https://github.com/AleksandrKamen/Galaxy_drive/assets/144233016/0d3dc934-57eb-4f02-af93-14de19faf167)

На главной странице реализована форма поиска объектов по названию 

![searh](https://github.com/AleksandrKamen/Galaxy_drive/assets/144233016/766649aa-724f-4cbf-97d8-f8b009f73362)

Результаты поиска отображаются на отдельной странице (search) 

![image](https://github.com/AleksandrKamen/Galaxy_drive/assets/144233016/d6ac8b9b-1b79-47f3-8e3c-b98298ccfb4e)

Для загруженного объекта доступны слудующие функции: удаление, переименование, создание копии с новым именем, загрузка 

![action](https://github.com/AleksandrKamen/Galaxy_drive/assets/144233016/16ce7c21-accf-4feb-85fc-4abb1836a5a6)
