package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response._

object CatalogosTitulosExtranjeros {
  var token = ""
}

class CatalogosTitulosExtranjeros extends Simulation {
  val urlBase = "https://10.0.9.212"
  val httpConf = http
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-us,en;q=0.5")
    .userAgentHeader("foo")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")

  val credenciales = csv("credenciales.csv").random
  val login = scenario("Login")
    .feed(credenciales)
    .exec(http("Autenticacion")
      .post(urlBase + ":8447/autenticacion")
      .header("Content-Type", "application/json; charset=UTF-8")
      .body(StringBody(
        """{
              "nombreUsuario":"${usuario}",
              "contrasenia":"${contrasenia}"
        }""")).asJSON
      .check(jsonPath("$.token").saveAs("tokenUnico"))
      .check(status.is(201))
    )
   .exec((session: Session) => {
      CatalogosTitulosExtranjeros.token = session("tokenUnico").as[String]
      session
    }).pause(4)

  val catalogo = scenario("Catalogos de registro de titulo")
    .exec((session: Session) => session.set("tokenUnico", CatalogosTitulosExtranjeros.token))

    .exec(http("paises")
      .get(urlBase + ":8452/paises")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

    .exec(http("etnias")
      .get(urlBase + ":8455/etnias")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

    .exec(http("provincias")
      .get(urlBase + ":8452/provincias")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

    .exec(http("tiposDeVisa")
      .get(urlBase + ":8455/tiposDeVisa")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

    .exec(http("nivelesDeFormacion")
      .get(urlBase + ":8455/catalogos/nivelesDeFormacion")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

    .exec(http("modalidadesDeEducacion")
      .get(urlBase + ":8455/catalogos/modalidadesDeEducacion")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

  setUp(
      login.inject(atOnceUsers(1)),
      catalogo.inject(nothingFor(1),
        rampUsersPerSec(1) to(3) during(5 minutes))
    ).protocols(httpConf)
}
