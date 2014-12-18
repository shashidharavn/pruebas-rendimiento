package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response._

object RegistrarExpedientes {
  var token = ""
}

class RegistrarExpedientes extends Simulation {
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
      RegistrarExpedientes.token = session("tokenUnico").as[String]
      session
    }).pause(4)

  val usuarios = csv("usuarios.csv").random
  val crearExpediente = scenario("Crear expediente")
    .feed(usuarios)
    .exec((session: Session) => session.set("tokenUnico", RegistrarExpedientes.token))
    .exec(http("Crear expediente")
      .post(urlBase + ":8455/expedientes")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .body(StringBody(
        """{
           "portadorTitulo":{
              "identificacion":{
                 "tipoDocumento":"pasaporte",
                 "idCategoriaVisa":null,
                 "numeroIdentificacion":"${pasaporte}",
                 "finVigenciaPasaporte":"2014-11-25",
                 "finVigenciaVisa":"2015-11-04"},
              "direccion":{
                 "direccionCompleta":"aa",
                 "idProvincia":"02",
                 "idCanton":"0202",
                 "idParroquia":"020251"},
              "nombresCompletos":"${nombre}",
              "fechaNacimiento":"1999-11-02",
              "genero":"FEMENINO",
              "idPaisNacionalidad":"004",
              "email":"${correo}",
              "idEtnia":"2",
              "telefonoConvencional":"899898989"
           },
           "informacionAcademica":{
              "facultad":{
                 "nombreFacultadDepartamento":"La super facultad",
                 "nombrePersonaContacto":"${apellido}",
                 "emailPersonaContacto":"${correo}"},
              "institucion":{"id":"936"},
              "tituloAcademico":{
                 "mecanismoLegalizacionTitulo":{"tipoDeMecanismo":"NINGUNO"},
                 "tipoDeTitulo":"ACADEMICO",
                 "nombreTitulo":"Tecnólogo en un monton de cosas",
                 "fechaTitulo":"2014-11-02",
                 "nivelDeFormacion":"TECNOLOGICO",
                 "modalidadEducacion":"PRESENCIAL"},
              "tipo":"regular"
           },
           "archivos":[{
              "tipoDocumento":{
                 "id":1,
                 "nombre":"Cédula",
                 "descripcionRequerida":false},
              "archivoId":1
           }],
           "modalidad":{"id":2,"nombre":"Comité"}
        }""")).asJSON
      .check(status.is(201))
    ).pause(8)

  setUp(
      login.inject(atOnceUsers(1)),
      crearExpediente.inject(nothingFor(1),
        rampUsersPerSec(1) to(10) during(5 minutes))
    ).protocols(httpConf)
}
