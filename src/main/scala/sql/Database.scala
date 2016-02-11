package sql

import sqlest._

trait DatabaseExample {
  // Configure a DataSource
  val dataSource = {
    val dataSource = new org.h2.jdbcx.JdbcDataSource
    dataSource.setURL("jdbc:h2:file:./target/fruit")
    //dataSource.setURL("jdbc:h2:./test")

    dataSource
  }

  // Choose the StatementBuilder that is compatible with the database you are using
  val statementBuilder = sqlest.sql.H2StatementBuilder

  // Use the DataSource and the StatementBuilder to create an implicit database
  // This database is used in all execute calls
  implicit val database = Database.withDataSource(dataSource, statementBuilder)

}
