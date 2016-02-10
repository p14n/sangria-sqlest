package sangria

import sangria.schema._
import domain.{Fruit,Smoothy}
import sql.SmoothyRepo
/**
 * Created by p14n on 02/12/15.
 */
object SmoothySchema {

val Fruit =
  ObjectType(
    "Fruit",
    "A fruit",
    fields[Unit,Fruit](
      Field(
        "name",StringType,Some("The name of the fruit"),
        resolve = _.value.name),
      Field(
        "id",IntType,Some("The id of the fruit"),
        resolve = _.value.id)))

val Smoothy =
  ObjectType(
    "Smoothy",
    "A smoothy",
    fields[SmoothyRepo, Smoothy](
      Field("id", IntType,
        Some("The id of the smoothy."),
        resolve = _.value.id),
      Field("description", StringType,
        Some("The description of the smoothy."),
        resolve = _.value.description),
      Field("fruits", OptionType(ListType(Fruit)),
        Some("The fruits in this smoothy."),
        resolve = (ctx)
          => ctx.ctx.fruits(ctx.value.id))
    ))

  val ID = Argument("id", IntType, description = "id of the smoothy")

  val Query = ObjectType[SmoothyRepo, Unit](
    "Query", fields[SmoothyRepo, Unit](
      Field("smoothy",OptionType(Smoothy),
        arguments = ID :: Nil,
        resolve = (ctx)
          => ctx.ctx.smoothy(ctx.arg(ID)))))

  val NameArg = Argument("name",StringType)
  val JuicinessArg = Argument("juiciness",IntType)
  val DescriptionArg = Argument("description",StringType)
  val SmoothyIdArg = Argument("smoothyId",IntType)
  val FruitIdArg = Argument("fruitId",IntType)

  val Mutation = ObjectType("Mutation", fields[SmoothyRepo,Unit](
    Field("addSmoothy",OptionType(Smoothy),
      arguments = DescriptionArg :: Nil,
      resolve = (ctx) => {
        ctx.ctx.addSmoothy(ctx.arg(DescriptionArg))
      }),
    Field("addFruit",OptionType(Fruit),
      arguments = NameArg :: JuicinessArg :: Nil,
      resolve = (ctx) => {
        ctx.ctx.addFruit(ctx.arg(NameArg),ctx.arg(JuicinessArg))
      }),
    Field("addIngredient",OptionType(Smoothy),
      arguments = SmoothyIdArg :: FruitIdArg :: Nil,
      resolve = (ctx) => 
        ctx.ctx.addIngredient(ctx.arg(SmoothyIdArg),ctx.arg(FruitIdArg)))
  ))
  val fruitSchema = Schema(Query,Some(Mutation))

}
