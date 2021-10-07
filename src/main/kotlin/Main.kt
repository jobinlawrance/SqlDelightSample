import com.jobinlawrance.Database
import com.jobinlawrance.MovieQueries
import com.jobinlawrance.Movies
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariDataSource

lateinit var moviesQueries: MovieQueries

fun main(args: Array<String>) {

    val database = Database(getSqlDriver())
    moviesQueries = database.movieQueries

    /**
     * Insert Operations
     * */

    // Insert by values
    insert(
        "Deadpool",
        "Action/Adventure",
        "20th Century Fox",
        "$734",
        2016
    )

    // Insert object
    val movie = Movies(
        Film = "Wonder Woman",
        Genre = "Action/Adventure",
        Lead_Studio = "DC Films",
        Audience_score = null,
        Profitability = null,
        Rotten_Tomatoes = null,
        Worldwide_Gross = "$66",
        Year = 2017
    )
    insert(movie)

    /**
     * Update
     * */
    update("$72","Wonder Woman")

    /**
     * Delete
     * */
    delete("27 Dresses")

    /**
     * Select query
     * */
    val movieOrGenre = searchFilmOrGenre("Comedy")
    println(movieOrGenre)

    val movieByNames = searchFilmsByName(listOf("Penelope", "Valentine's Day", "Mamma Mia!"))
    println(movieByNames)

    val movies = moviesQueries.selectAll().executeAsList()
    println(movies)

    /**
     * Transaction
     * */
    val bunchOfMovies = listOf(
        Movies(
            Film = "Sunny",
            Genre = "",
            Lead_Studio = "Dreams N Beyond",
            Audience_score = null,
            Profitability = null,
            Rotten_Tomatoes = null,
            Worldwide_Gross = "",
            Year = 2021
        ),
        Movies(
            Film = "Kala",
            Genre = "Crime",
            Lead_Studio = "Juvis Productions",
            Audience_score = null,
            Profitability = null,
            Rotten_Tomatoes = null,
            Worldwide_Gross = "",
            Year = 2020
        ),
    )

    moviesQueries.transaction {
        bunchOfMovies.forEach { movie ->
            moviesQueries.insertObject(movie)
        }
    }

    // Rollback
    moviesQueries.transaction {
        afterCommit {
            println(
                "Transaction complete: ${bunchOfMovies.size} movies inserted"
            )
        }
        afterRollback { println("Rollback: No movies were inserted") }

        bunchOfMovies.forEach { movie ->
            if (movie.Genre.isNullOrEmpty())
                rollback()
            moviesQueries.insertObject(movie)
        }
    }

}

private fun getSqlDriver(): SqlDriver {
    val ds = HikariDataSource()
    ds.jdbcUrl = "jdbc:mysql://localhost:3306/movies_db"
    ds.driverClassName = "com.mysql.jdbc.Driver"
    ds.username = "root"
    ds.password = "mysqlroot"
    return ds.asJdbcDriver()
}

private fun insert(
    film: String,
    genre: String,
    leadStudio: String,
    worldwideGross: String,
    year: Int
) {
    moviesQueries.insert(film, genre, leadStudio, worldwideGross, year)
}

private fun insert(movies: Movies) {
    moviesQueries.insertObject(movies)
}

private fun update(worldwideGross: String, film: String) {
    moviesQueries.update(worldwideGross, film)
}

private fun delete(film: String) {
    moviesQueries.delete(film)
}

private fun searchFilmOrGenre(query: String): List<Movies> {
    return moviesQueries.filmOrGenre(searchQuery = query).executeAsList()
}

private fun searchFilmsByName(films: List<String>): List<Movies> {
    return moviesQueries.filmByNames(films).executeAsList()
}