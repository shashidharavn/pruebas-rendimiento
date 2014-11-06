package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response._


object RegistroCivil {
  var token = ""
}

class RegistroCivil extends Simulation {

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
    .exec(http("Identificacion")
      .post(urlBase + ":8447/autenticacion")
      .header("Content-Type", "application/json;charset=UTF-8")
      .body(StringBody(
        """{
              "nombreUsuario":"${usuario}",
              "contrasenia":"${contrasenia}"
        }""")).asJSON
      .check(jsonPath("$.token").saveAs("tokenUnico"))
      .check(status.is(201))
    )
    .exec((session: Session) => {
      RegistroCivil.token = session("tokenUnico").as[String]
      session
    })
    .pause(4)

  val cedulas = csv("cedulas.csv").random
  val validarCedula = scenario("Validar cedula")
    .feed(cedulas)
    .exec((session: Session) => session.set("tokenUnico", RegistroCivil.token))
    .exec(http("Registro civil")
      .get(urlBase + ":8443/busqueda?cedula=${cedula}")
      .header("Content-Type", "application/json;charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)


  setUp(
    login.inject(atOnceUsers(1)),
    validarCedula.inject(nothingFor(5),
      rampUsersPerSec(1) to(4) during(30 minutes)
    )
  ).protocols(httpConf)
}
