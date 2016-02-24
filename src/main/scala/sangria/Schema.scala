package sangria

import sangria.schema._
import domain.{Fruit,Smoothy}
import sql.SmoothyRepo
/**
 * Created by p14n on 02/12/15.
 */
object SmoothySchema {

  /*
  This file defines the GraphQL schema - the set of data exposed to a client.
  We want to be able to send a query like this:

  smoothy {
    description,
    fruits {
      name
    }
  }

  Which would return:

  {
   description: pineapple dream,
   fruits [
    {
     name: pineapple
    }
   ]
  }
  */
  

//We start by defining the Fruit object type
val Fruit =
  ObjectType(
    "Fruit", // name
    "A fruit", // description
    fields[Unit,Fruit]( /* <- Unit, Fruit: 
      Fruit because this object type uses the Fruit case class imported from domain (on line 5).
      Unit because we don't need any other context to resolve our field values */
      Field( //Our first field, called 'name', which is a string, and is described as 'The name of the fruit'
        "name",StringType,Some("The name of the fruit"),
        resolve = _.value.name), /*How we get this data:
      we take the context _ and ask for the value, which is a domain.Fruit, and then get the 'name' field from it*/
      Field(
        "id",IntType,Some("The id of the fruit"),
        resolve = _.value.id)))

//And now the Smoothy object type
val Smoothy =
  ObjectType(
    "Smoothy",
    "A smoothy",
    fields[SmoothyRepo, Smoothy]( /* This time we say we need a sql.SmoothyRepo to get all our data */
      Field("id", IntType,
        Some("The id of the smoothy."),
        resolve = _.value.id),
      Field("description", StringType,
        Some("The description of the smoothy."),
        resolve = _.value.description),
      Field("fruits", OptionType(ListType(Fruit)),
        Some("The fruits in this smoothy."),
        /* This field is not a simple field value from a domain.Smoothy - we need to 
           call some sql to get the values.  A sql.SmoothyRepo is passed to us in the ctx field of the context */
        resolve = (ctx)
          => ctx.ctx.fruits(ctx.value.id)) 
    ))

//Now we define an argument we will use on our query - the id of the smooth
  val ID = Argument("id", IntType, description = "id of the smoothy")

//And now the root query.  This also uses the sql.SmoothyRepo.
  val Query = ObjectType[SmoothyRepo, Unit](
    "Query", fields[SmoothyRepo, Unit](
      Field("smoothy",OptionType(Smoothy), //We have only one root type - the smoothy
        arguments = ID :: Nil, //We need an ID
        resolve = (ctx)
          => ctx.ctx.smoothy(ctx.arg(ID))))) //We call the sql.SmoothyRepo with this argument


//That's the query part done, now we want to define some mutations (updates).
//First the  arguments we're going to need
  val NameArg = Argument("name",StringType)
  val JuicinessArg = Argument("juiciness",IntType)
  val DescriptionArg = Argument("description",StringType)
  val SmoothyIdArg = Argument("smoothyId",IntType)
  val FruitIdArg = Argument("fruitId",IntType)


  val Mutation = ObjectType("Mutation", fields[SmoothyRepo,Unit]( //We need a sql.SmoothyRepo for these updates
    Field("addSmoothy",OptionType(Smoothy), //This defines an 'addSmoothy' mutation, returning a domain.Smoothy
      arguments = DescriptionArg :: Nil, //We just need a description
      resolve = (ctx) => {
        ctx.ctx.addSmoothy(ctx.arg(DescriptionArg)) //Call the method to create (and return) the domain.Smoothy
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

  //This completes our schema
  val fruitSchema = Schema(Query,Some(Mutation))

  /* Running the code in CreateSmoothyWithMutationsAndQuery creates output like this:
    Map(data -> 
      Map(smoothy -> 
        Map(
         description -> MonkeyApple, 
         fruits -> List(Map(
                          name -> Apple)))))
  /*

}
