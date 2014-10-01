package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response._

object IngresoTitloExtranjero {
  var token = ""
}

class IngresoTitloExtranjero extends Simulation {
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
      IngresoTitloExtranjero.token = session("tokenUnico").as[String]
      session
    }).pause(4)

  val ingresarTitulo = scenario("Ingresar titulo extranjero")
    .exec((session: Session) => session.set("tokenUnico", IngresoTitloExtranjero.token))
   
    .exec(http("Ingresar titulo modalidad listado")
        .post(urlBase + ":8443/perfiles")
        .header("Content-Type", "application/json; charset=UTF-8")
        .header("Authorization", "${tokenUnico}")
        .body(StringBody(
          """{
                "nombre":"administrador",
                "permisos":[{"id":9}]

          }""")).asJSON
        .check(status.is(200))
      ).pause(4)

  setUp(
      login.inject(atOnceUsers(1)),
      ingresarTituloListado.inject(nothingFor(10),
        rampUsersPerSec(1) to(5) during(10 minutes))
    ).protocols(httpConf)
}
