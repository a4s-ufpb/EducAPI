# Educ API

Springboot API that provides contexts and challenges in a collaborative database.

![GitHub](https://img.shields.io/github/license/a4s-ufpb/EducAPI?label=licence) ![Type Badge](https://img.shields.io/badge/project%3A-Apps4Society-informational) ![enter image description here](https://img.shields.io/badge/project-SisAlfa-yellow)  ![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/a4s-ufpb/EducAPI?color=blueviolet)

## 🛠 Tech Stack

To make this project run you will need to have the following items installed: 

* [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/)
* Java 11 or OpenJDK 11
* PostgreSQL 12.3 (it is recommended to use a Docker container)
* Intelij or other IDE of your choice

This project also uses the following technologies:
* Spring Boot - 2.2.7.RELEASE
* JPA
* Hibernate
* H2 Database
* Maven 4.0.0 
* Swagger 2.9.2
* JUnit 4

 > Note: Use Insomnia or another tool to test calls to API endpoints

## :rocket: Starting the project

You must open a terminal and clone this repository:

To clone this repository via HTTPS:

    a4sufpb@a4sufpb:~$ git clone https://github.com/a4s-ufpb/EducAPI.git

Access the cloned repository through the IDE of your choice and change the `spring.profiles.active` variable in the` EducAPI/src/main/resources/application.properties` file to` dev` or` test`.

Create a `.env` file in the project's root directory, to set the environment variables used in the API, and fill this file with the following content:

    EDUCAPI_VERSION=1.0.4
    PROFILE_ACTIVE=dev
    POSTGRES_USER=postgres
    POSTGRES_PASSWORD=postgres
    POSTGRES_DB=educapi
    DB_URL=jdbc:postgresql://db:5432/${POSTGRES_DB}
    TOKEN_KEY=educapi-dev
    
 

> NOTE: Configure the environment variables above according to the PostgreSQL connection created on your machine.

> NOTE: Remember to create a database with the name defined in the value of the POSTGRES_DB variable.
 


If you are NOT going to run the project using Docker, download the dependencies needed to run the project by right-clicking on the `EducAPI/pom.xml` file.

To run the API locally on your machine, open a terminal at the root of the project and enter the following commands:

    a4sufpb@a4sufpb:~$ export $(cat .env | xargs)
    a4sufpb@a4sufpb:~$ ./mvnw spring-boot:run
    

Go to `http://localhost:8080/`to access the API.

If you are going to run the project USING Docker, just open a terminal at the root of the project and type:

    a4sufpb@a4sufpb:~$ docker-compose up --build

To stop the project just close the terminal.

Go to `http://localhost:8080/`to access the API.

> NOTE: Whenever you want to test any changes that werw made to the code, repeat the commands above to stop and start again.
 
> Note: Pull requests should only be accepted if the project is able to build using Docker.


## :closed_book:Documentation

The API in production at [https://api.apps4society.dcx.ufpb.br/educapi/](https://api.apps4society.dcx.ufpb.br/educapi/) and the documentation can be obtained at [https://api.apps4society.dcx.ufpb.br/educapi/swagger-ui.html](https://api.apps4society.dcx.ufpb.br/educapi/swagger-ui.html). In addition, you can acess the API documentation locally on your machine by accessing `localhost:8080/swagger-ui.html`.

To learn more about [Spring boot](https://spring.io/guides#getting-started-guides), how to install and how to use it, access the documentation.

Also access the [JUnit](https://junit.org/junit4/), [JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#reference) and [Hibernate](https://hibernate.org/orm/documentation/5.4/) documentation.

## :page_facing_up: Licence

This project is licensed under the MIT License, see also [LICENSE.md](https://github.com/a4s-ufpb/LICENSE.md) for more details.

## 👩‍💻👨‍💻 Contributors 

<table>
  <tr>
    <td align="center">
	    <a href="https://github.com/AmandaAzevedo">
		    <img style="border-radius: 50%;" src="https://avatars2.githubusercontent.com/u/44630609?s=460&u=ff38a29379fef41570bb4850ae4ed4f456873d0d&v=4" width="100px;" alt=""/>
		    <br/><sub><b>Amanda Azevedo</b></sub>
		</a></br>
    </td>
    <td align="center">
	    <a href="https://github.com/EmersonDantas">
		    <img style="border-radius: 50%;" src="https://avatars2.githubusercontent.com/u/30247063?s=460&u=51304437a75b46e7048abd5e1cef2128d59c78f5&v=4" width="100px;" alt=""/>
		    <br/><sub><b>Emerson Dantas</b></sub>
		</a></br>
    </td>
    <td align="center">
	    <a href="https://github.com/NapoleaoHerculano">
		    <img style="border-radius: 50%;" src="https://avatars3.githubusercontent.com/u/33008128?s=460&u=ad3187526aff8c3976abf00160c8ddcb5feec685&v=4" width="100px;" alt=""/>
		    <br/><sub><b>Francivaldo Napoleão</b></sub>
		</a></br>
    </td>
    <td align="center">
	    <a href="https://github.com/MarcosLudgerio">
		    <img style="border-radius: 50%;" src="https://avatars0.githubusercontent.com/u/43012976?s=460&u=1163c04d9f35b577063b3f6550ae520c4dd2f866&v=4" width="100px;" alt=""/>
		    <br/><sub><b>Marcos Ludgero</b></sub>
		</a></br>
    </td>
  </tr>
</table>

### Project coordinator
<table>
	<tr>
		<td align="center">
		    <a href="https://github.com/ayladebora">
			    <img style="border-radius: 50%;" src="https://avatars1.githubusercontent.com/u/1224119?s=460&v=4" width="100px;" alt=""/>
		    <br/><sub><b>Ayla Dantas Rebouças</b></sub>
		</a>
		</br>
    </td>
    <td align="center">
	    <a href="https://github.com/cesdias">
		    <img style="border-radius: 50%;" src="https://avatars1.githubusercontent.com/u/18514013?s=460&u=0c92f6ba0e7145b56d4c2a56b92b63eea07855ca&v=4" width="100px;" alt=""/>
		    <br/><sub><b>Carlos Hacks</b></sub>
		</a></br>
    </td>
	</tr>
</table>

### :handshake: How to contribute? 
Please read [CONTRIBUTING.md](https://github.com/a4s-ufpb/EducAPI/blob/master/CONTRIBUTING.md) for more details regarding our content code and the process of submitting pull requests to us.

## :mailbox: Contact us
* To learn more about the Apps4Society and SisAlfa project visit the websites [![Site Apps4Society](https://img.shields.io/twitter/url?color=blue&label=Site%20Apps4Society&logo=Apps4Society&style=plastic&url=https%3A%2F%2Fapps4society.dcx.ufpb.br)](https://apps4society.dcx.ufpb.br) [![Site Sisalfa](https://img.shields.io/twitter/url?color=yellow&label=Site%20SisAlfa&logo=Sisalfa&style=plastic&url=https%3A%2F%2Fsisalfa.dcx.ufpb.br%2F) ](https://sisalfa.dcx.ufpb.br)
* Follow the Apps4Society on Instagram  [![Twitter URL](https://img.shields.io/twitter/url?color=pink&label=Instagram%20Apps4Society&logo=Instagram&logoColor=rose&style=plastic&url=https%3A%2F%2Fwww.instagram.com%2Fapps4society%2F)](https://instagram.com/apps4society) 
* Contact us by email [![Gmail Badge](https://img.shields.io/badge/-apps4society@dcx.ufpb.br-c14438?style=flat-square&logo=Gmail&logoColor=white&link=mailto:apps4society@dcx.ufpb.br)](apps4society@dcx.ufpb.br) [![Gmail Badge](https://img.shields.io/badge/-sisalfa@dcx.ufpb.br-c14438?style=flat-square&logo=Gmail&logoColor=white&link=mailto:sisalfa@dcx.ufpb.br)](sisalfa@dcx.ufpb.br)
---
