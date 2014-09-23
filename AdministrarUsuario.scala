package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response._

object AdministrarUsuario {
  var token = ""
}

class AdministrarUsuario extends Simulation {
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
      AdministrarUsuario.token = session("tokenUnico").as[String]
      session
    }).pause(4)

  val administrarUsuario = scenario("Administrar usuario")
    .exec((session: Session) => session.set("tokenUnico", AdministrarUsuario.token))
    .exec(http("Crear usuario")
      .post(urlBase + ":8443/usuario")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .body(StringBody(
        """{
              "identificacion":{"tipoDocumento":"pasaporte","numeroIdentificacion":"AGAH"},
              "nombre":{"primerNombre":"ana","primerApellido":"ana"},
              "institucion":{"id":"2005"},"perfiles":[1],
              "emailInstitucional":"seneteam@gmail.com",
              "numeroAutorizacionQuipux":"SENESCYT-AB-2014-12345-MI",
              "finDeVigencia":"2014-10-30",
              "nombreUsuario":"lala","estado":"ACTIVO"
        }""")).asJSON
      .check(status.is(201))
    ).pause(4)

    .exec(http("Crear perfil")
      .post(urlBase + ":8443/perfiles")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .body(StringBody(
        """{
              "nombre":"administrador",
              "permisos":[{"id":9}]

        }""")).asJSON
      .check(status.is(201))
    ).pause(4)

    .exec(http("Listar usuarios")
      .get(urlBase + ":8443/usuario/todos")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

    .exec(http("instituciones")
      .get(urlBase + ":8443/instituciones")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

    .exec(http("perfiles")
      .get(urlBase + ":8443/perfiles")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)
  
  setUp(
      login.inject(atOnceUsers(1)),
      administrarUsuario.inject(
        nothingFor(10),
        rampUsersPerSec(1) to(5) during(30 minutes)
      )
    ).protocols(httpConf)
}
