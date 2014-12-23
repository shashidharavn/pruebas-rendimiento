package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response._

object RegistrarProyecto {
  var token = ""
}

class RegistrarProyecto extends Simulation {
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
      RegistrarProyecto.token = session("tokenUnico").as[String]
      session
    }).pause(4)

  val crearProyecto = scenario("Crear proyecto")
    .exec((session: Session) => session.set("tokenUnico", RegistrarProyecto.token))
    .exec(http("Crear proyecto")
      .post(urlBase + ":8445/proyectos")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .body(StringBody(
        """{
          "investigadores":[
          {
            "nombre":"Luis Gonzalez",
            "institucion":"SENESCYT"
          }
          ],
          "outcomes":[
          {
            "descripcion":"objetivo 1",
            "presupuestoPlanificado":10,
            "presupuestoEjecutado":0,
            "resultadoAlcanzado":0
          }
          ],
          "politicaYMetaIds":[
          "26"
          ],
          "institucionEjecutora":{
            "tipoInstitucionEjecutora":{
              "id":1,
              "nombre":"IES",
              "descripcion":"Institución de Educación Superior"
            },
            "id":"1035"
          },
          "nombre":"Proyecto 1",
          "descripcion":"Ejemplo de proyecto",
          "codigo":"PIC-12-SEN-123",
          "fechaInicio":"2014-12-23",
          "fechaFinalizacion":"2015-12-23",
          "fechaReporte":"2014-12-23",
          "areasTematicas":[
          {
            "id":13,
            "nombre":"MINERIA Y METALES ESTRATEGICOS",
            "tipoInstitucionEjecutora":{
              "id":1,
              "nombre":"IES",
              "descripcion":"Institución de Educación Superior"
            },
            "subAreas":[]
          }
          ]
        }""")).asJSON
      .check(status.is(201))
    ).pause(8)

  val catalogo = scenario("Catalogos de proyectos de investigacion")
    .exec((session: Session) => session.set("tokenUnico", RegistrarProyecto.token))

    .exec(http("Recurso: objetivos Buen Vivir")
      .get(urlBase + ":8445/objetivos")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

    .exec(http("Recurso: instituciones educacion superior")
      .get(urlBase + ":8449/instituciones/nacionales")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

    .exec(http("Recurso: instituciones proyectos investigacion")
      .get(urlBase + ":8445/instituciones/investigacion")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

    .exec(http("Listado de proyectos vigentes")
      .get(urlBase + ":8445/proyectos?estado=vigente")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

    .exec(http("Listado de proyectos culminados")
      .get(urlBase + ":8445/proyectos?estado=culminado")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .check(status.is(200))
    ).pause(4)

  setUp(
      login.inject(atOnceUsers(1)),
      crearProyecto.inject(nothingFor(1),
        rampUsersPerSec(1) to(10) during(5 minutes)),
      catalogo.inject(nothingFor(1),
        rampUsersPerSec(1) to(3) during(5 minutes))
    ).protocols(httpConf)
}
