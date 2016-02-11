import javax.sql.DataSource

import org.flywaydb.core.Flyway
import sangria.macros._
import sangria.execution.Executor
import sangria.ast.Document
import scala.concurrent.ExecutionContext.Implicits.global
import sangria.marshalling.playJson._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import sangria.parser.QueryParser.parse
import scala.util.Success
import sangria.integration.InputUnmarshaller.mapVars


/**
  * Created by p14n on 12/01/2016.
  */
object DoIt extends App {

  val flyway = new Flyway()
  val dataSource = new org.h2.jdbcx.JdbcDataSource
  dataSource.setURL("jdbc:h2:file:./target/fruit")
  flyway.setDataSource(dataSource)
  flyway.migrate()
 

    val queryAst: Document =
      graphql"""
       mutation {
         addSmoothy(description: "MonkeyApple") {
          id
         }
       }
      """

    val sid:Int = getIn(exec(queryAst,Map()),"data","addSmoothy","id")

    val Success(queryAst2) =
      parse("""
       mutation {
         addFruit(name: "Apple",juiciness: 4) {
          id
         }
       }
      """)

    val fid:Int = getIn(exec(queryAst2,Map()),"data","addFruit","id")

    val Success(queryAst3) = 
     parse("""
       mutation addIngredient($sid: Int!,$fid:Int!){
         addIngredient(smoothyId: $sid,fruitId: $fid) {
          id
         }
       }
      """)

    exec(queryAst3, Map("sid" -> sid, "fid" -> fid ))

    val Success(queryAst4) =
      parse("""
       query getSmoothy($id:Int!){
         smoothy(id: $id) {
          description,
          fruits {
            name
          }
         }
       }
      """)

    println(exec(queryAst4, Map("id" -> sid)))

def getIn[T](map:Any,names:String*): T = {
  var m = map.asInstanceOf[Map[String,_]]
  val l = names.toList
  l.dropRight(1) map { name => m = m(name).asInstanceOf[Map[String,_]] }
  m(l.tail(1)).asInstanceOf[T]
}
def exec(query: Document,vars: Map[String,_]): Any = {
 val res = Await.result(
  Executor(sangria.SmoothySchema.fruitSchema,userContext = new sql.SmoothyRepo)
  .execute(query, variables = mapVars(vars)), Duration.Inf)
 res

}
}
