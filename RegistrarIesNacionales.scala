package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response._

object CrearIes {
  var token = ""
}

class CrearIes extends Simulation {
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
      CrearIes.token = session("tokenUnico").as[String]
      session
    }).pause(4)

  val ies = csv("ies.csv").random
  val crearIes = scenario("Crear IES")
    .feed(ies)
    .exec((session: Session) => session.set("tokenUnico", CrearIes.token))
    .exec(http("Crear IES")
      .post(urlBase + ":8449/instituciones/nacionales")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .body(StringBody(
        """{
            "estadoInstitucion":{"id":1,"estado":"Vigente"},
            "tipoInstitucion":{"id":1,"tipo":"${tipoInstitucion}"},
            "nombre":"Universidad de ${nombreInstitucion}",
            "tipoRegimen":{"id":1,"regimen":"${tipoRegimen}"}
            }""")).asJSON
      .check(status.is(201))
    ).pause(3)

  setUp(
      login.inject(atOnceUsers(1)),
      crearIes.inject(nothingFor(1),
        constantUsersPerSec(1) during(5 minutes))
    ).protocols(httpConf)
}
