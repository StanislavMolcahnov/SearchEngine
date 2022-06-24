# SearchEngine
This is a graduation project completed as part of the Java course on the Skillbox platform. This search engine can index sites and then find information on indexed sites. The project has a web interface based on the Spring framework and must connect to a database (SQL) to store indexes and information about the site.

When creating the project, the following technology stack was used:
- JSOUP
- MySQL
- Lemmatizer lib (https://github.com/akuznetsov/russianmorphology)
- Spring framework
- Java (obviously)
- Hibernate

First you need to create a SQL database with utf8mb4 encoding and make the necessary changes to the application.yaml file at the root of the project.

## Usage/Examples
```YML
spring:
  datasource:
    url: jdbc:${CLEARDB_CHARCOAL_URL:mysql://localhost:3306/heroku_829dec9982ec23c} #Write a URL to your DB
    username: search_engine #Write username to your DB
    password: testtest #Write password to your DB
  jpa:
    hibernate:
      show-sql: true
      ddl-auto: none #Specify what you need (create, update etc)
custom:
  sites:
  #here specify the sites you want to index and search for.
    - url: https://ErrorTest.ru #URL without trailing slash
      name: ErrorTest #Any name for your site
    - url: https://www.poelab.com
      name: PoeLab
    - url: https://skillbox.ru
      name: Skillbox
  #You can specify the name of the search engine, for example "HeliontSearchBot"
  userAgent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36
  #where did the search engine come from
  referrer: https://www.google.com
```
