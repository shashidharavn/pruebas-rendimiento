package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response._


  class loginPage extends Simulation {

  val httpConf = http
    .baseURL("https://10.0.9.212:8443/")
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-us,en;q=0.5")
    .userAgentHeader("foo")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")

  val credenciales = csv("credenciales.csv").random
  val login = scenario("Administrar usuarios")
    .feed(credenciales)
    .exec(http("Login")
      .post("identificacion")
      .header("Content-Type", "application/json; charset=UTF-8")
      .body(StringBody(
        """{
              "nombreUsuario":"${usuario}",
              "contrasenia":"${contrasenia}"
        }""")).asJSON
      .check(status.is(201), bodyString.saveAs("bearer"))
    )
    .pause(4)

    .exec(http("Crear perfil")
      .post("perfiles")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "Bearer ${bearer}")
      .body(StringBody(
        """{
              "nombre":"administrador",
              "permisos":[{"id":9}]

        }""")).asJSON
            .check(status.is(201))
    )
    .pause(4)

    .exec(http("Crear usuario")
      .post("usuario")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "Bearer ${bearer}")
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
    )
    .pause(4)

     .exec(http("Listar usuarios")
      .get("usuario/todos")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "Bearer ${bearer}")
      .check(status.is(200))
    )
    .pause(4)

    .exec(http("paises")
      .get("paises")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "Bearer ${bearer}")
      .check(status.is(200))
    )
    .pause(4)

  setUp(
    login.inject(
      atOnceUsers(10))
      .protocols(httpConf)
  )
}
