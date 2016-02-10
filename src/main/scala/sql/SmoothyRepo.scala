package sql
import sqlest._
import domain._
import sql._
import sqlest.executor.Transaction

/**
 * Created by p14n on 02/12/15.
 */
class SmoothyRepo extends DatabaseExample {

  lazy val fruitExtractor = extract[Fruit](
    id = FruitTable.id,
    name = FruitTable.name,
    juiciness = FruitTable.juiciness
  )

  lazy val smoothyExtractor = extract[Smoothy](
    id = SmoothyTable.id,
    description = SmoothyTable.description
  )

  lazy val baseSmoothyQuery = select.from(SmoothyTable)

  lazy val ingredientsQuery =
    select.from(IngredientsTable)
      .innerJoin(FruitTable).on(FruitTable.id === IngredientsTable.fruitId)

  def smoothies():List[Smoothy] = {
    baseSmoothyQuery.extractAll(smoothyExtractor)
  }
  def smoothy(id: Int):Option[Smoothy] = {
    baseSmoothyQuery.where(SmoothyTable.id === id).extractHeadOption(smoothyExtractor)
  }

  def fruits(smoothyId:Int):List[Fruit] = {
    ingredientsQuery.where(IngredientsTable.smoothyId === smoothyId)
      .extractAll(fruitExtractor)
  }

  def addFruit(name: String,juiciness: Int):Option[Fruit] = {
    val keys = database.withTransaction { implicit transaction =>
      insert
        .into(FruitTable)
        .columns(
        FruitTable.name,
        FruitTable.juiciness
        )
        .values(
         FruitTable.name -> name,
         FruitTable.juiciness -> juiciness
        )
        .executeReturningKeys[Int]
    }
    Some(new Fruit(id=keys(0),name = name,juiciness = juiciness))
  }

  def addSmoothy(description: String):Option[Smoothy] = {
    val s = new Smoothy(id=0,description = description)
    val rowsInserted = database.withTransaction { implicit transaction =>
      insert
        .into(SmoothyTable)
        .columns(
        SmoothyTable.description
        )
        .values(
         SmoothyTable.description -> description
        )
        .executeReturningKeys[Int]
    }
    Some(s.copy(id=rowsInserted(0)))
  }
  def addIngredient(smoothyId: Int, fruitId:Int ): Option[Smoothy] = {
    database.withTransaction { implicit transaction =>
      insert
        .into(IngredientsTable)
        .columns(
        IngredientsTable.smoothyId,
        IngredientsTable.fruitId
        )
        .values(
        IngredientsTable.smoothyId -> smoothyId,
          IngredientsTable.fruitId -> fruitId
        )
        .execute
    }
    smoothy(smoothyId)
  }


}
