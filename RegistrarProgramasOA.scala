package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response._

object CrearPrograma {
  var token = ""
}

class CrearPrograma extends Simulation {
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
      CrearPrograma.token = session("tokenUnico").as[String]
      session
    }).pause(4)

  val crearPrograma = scenario("Crear Programa")
    .exec((session: Session) => session.set("tokenUnico", CrearPrograma.token))
    .exec(http("Crear Programa")
      .post(urlBase + ":8449/iesCursos")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Authorization", "${tokenUnico}")
      .body(StringBody(
        """{
           "institucion":{
              "id":1035,
              "nombre":"ESCUELA SUPERIOR POLITECNICA ECOLOGICA AMAZONICA",
              "tipoInstitucion":{"id":1,"tipo":"Universidad"},
              "categoria":null,
              "estadoInstitucion":{"id":3,"estado":"Suspendida"},
              "observaciones":"SUSPENDIDA MEDIANTE RESOLUCION RPC-SO-012-No.066-2012",
              "tipoRegimen":null},
           "informacionGeneral":{
              "regimenAcademico":{
                 "id":2,
                 "nombre":"2013",
                 "anadirElementos":true,
                 "cineClasificacion":{"id":"002","nombre":"CINE-UNESCO 2013"}},
              "tipoFormacion":{
                 "id":18,
                 "nombre":"Especialización",
                 "nivelesFormacion":null},
              "modalidad":{
                 "id":5,
                 "nombre":"Semipresencial",
                 "regimenAcademico":{
                    "id":2,
                    "nombre":"2013",
                    "anadirElementos":true,
                    "cineClasificacion":{"id":"002","nombre":"CINE-UNESCO 2013"}},
                 "estructuraImplantacionTerritorial":{"id":1,"nombre":"Campus"}},
              "estadoVigencia":{"id":1,"nombre":"Vigente"},
              "cursoAcademico":{
                 "id":166,
                 "cursoCodigo":{"id":1,"codigo":"A"},
                 "cursoNombre":{"id":175,"nombre":"PSICOPEDAGOGIA"},
                 "campoDetallado":{
                    "id":109,
                    "nombre":"PEDAGOGIA",
                    "codigo":81,
                    "campoEspecifico":{
                       "id":16,
                       "nombre":"EDUCACION",
                       "codigo":1,
                       "campoAmplio":{"id":14,"nombre":"EDUCACION","codigo":1,"idRegimenAcademico":2}
                    }},
                 "nivelFormacionCine":{"id":73,"nombre":"ESPECIALIZACION"}}
           },
           "duracion":{
              "valorReferencialPrograma":{
                 "id":19,
                 "tipoUnidad":{"id":2,"nombre":"horas"},
                 "tipoDuracion":{"id":2,"nombre":"meses"},
                 "tipoFormacion":{"id":18,"nombre":"Especialización","nivelesFormacion":null},
                 "nivelFormacion":{
                    "id":7,
                    "nombre":"Educación Superior de Posgrado o Cuarto Nivel",
                    "idRegimen":2},
                 "numeroTotalUnidades":1000,
                 "duracionTotal":9},
              "tiempo":9},
           "lugaresInstruccion":[{
             "estructuraGestionTerritorial":{"id":1,"nombre":"Sede Matriz"},
             "idCanton":"0801"}],
           "archivo":{
              "titulacion":{
                 "id":166,
                 "nombre":{"id":166,"nombre":"ESPECIALISTA EN PSICOPEDAGOGIA"},
                 "codigo":{"id":1,"codigo":"01"},
                 "cursosAcademicos":null},
              "idArchivo":58
           },
           "requisitos":[
              {"nombre":"Título de tercer nivel registrado en el SNIESE"},
              {"nombre":"Suficiencia en el idioma inglés"}],
           "menciones":[{"nombre":"mencion 1"}],
           "redesConvenios":[
              {"tipoVinculo":{"id":1,"tipoVinculo":"Red"},
               "areaIntereses":[
                    {"id":1,"areaInteres":"Oferta de carreras y/o programas"},
                    {"id":6,"areaInteres":"Prácticas preprofesionales y/o pasantías"}],
                     "idArchivo":59}],
           "resolucion":{
              "detallesDeRegularizaciones":[
                 {  "fecha":"2014-12-01",
                    "url":"http://www.abc.com",
                    "tipo":{"nombre":"ingreso"}},
                 {  "fecha":"2014-12-05",
                    "url":"http://www.abc.com",
                    "tipo":{"nombre":"aprobacion"}},
                 {  "fecha":"2014-12-06",
                    "url":"http://www.abc.com",
                    "tipo":{"nombre":"fin-de-vigencia"}},
                 {  "fecha":"2014-11-16",
                    "url":"http://www.abc.com",
                    "tipo":{"nombre":"acreditacion"}},
                 {  "fecha":"2014-12-31",
                    "url":"http://www.abc.com",
                    "tipo":{"nombre":"fin-acreditacion"}
                 }],
              "seAcredito":false,
              "paralelos":1,
              "numeroDeEstudiantes":1,
              "cohortesPorAno":1
           }
        }""")).asJSON
      .check(status.is(201))
    ).pause(3)

  setUp(
      login.inject(atOnceUsers(1)),
      crearPrograma.inject(nothingFor(1),
        constantUsersPerSec(2) during(5 minutes))
    ).protocols(httpConf)
}
