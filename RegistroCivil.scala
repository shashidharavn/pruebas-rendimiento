package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response._


  class registroCivil extends Simulation {

  val httpConf = http
    .baseURL("https://10.0.9.212:8443/")
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-us,en;q=0.5")
    .userAgentHeader("foo")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")

   val login = scenario("Validar cedula")
    .exec(http("Identificacion")
      .post("identificacion")
      .header("Content-Type", "application/json; charset=UTF-8")
      .body(StringBody(
        """{
              "nombreUsuario": "admin",
              "contrasenia": "Sni3s3Adm1nTW"
        }""")).asJSON
      .check(status.is(201), bodyString.saveAs("bearer"))
    )
   .pause(4)

    .exec(http("Registro civil")
      .get("busqueda?cedula=1111111116")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "Bearer ${bearer}")
      .check(status.is(200))
    )
    .pause(4)

    setUp(
    login.inject(atOnceUsers(10)).protocols(httpConf)
    )
  }
