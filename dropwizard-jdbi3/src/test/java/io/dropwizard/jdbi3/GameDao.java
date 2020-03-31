package io.dropwizard.jdbi3;

import com.codahale.metrics.annotation.Timed;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Timed(name = "game-dao")
public interface GameDao {
    @SqlQuery("SELECT id FROM games ORDER BY id")
    List<Integer> findGameIds();

    @SqlQuery("SELECT distinct home_team FROM games")
    Set<String> findAllUniqueHomeTeams();

    @SqlQuery("SELECT id FROM games " +
        "WHERE home_team = :home_team " +
        "AND   visitor_team = visitor_team " +
        "AND   played_at = :played_at")
    Optional<Integer> findIdByTeamsAndDate(@Bind("home_team") String homeTeam,
                                           @Bind("visitor_team") String visitorTeam,
                                           @Bind("played_at") LocalDate date);

    @SqlQuery("SELECT   played_at FROM games " +
        "WHERE    played_at < :up " +
        "ORDER BY played_at desc")
    LocalDate getFirstPlayedSince(@Bind("up") LocalDate up);

    @SqlQuery("SELECT   played_at FROM games " +
        "WHERE    home_team = :home_team " +
        "AND      visitor_team = :visitor_team " +
        "ORDER BY played_at desc")
    @Timed(name = "last-played-date")
    Optional<LocalDate> getLastPlayedDateByTeams(@Bind("home_team") String homeTeam,
                                                 @Bind("visitor_team") String visitorTeam);

    @SqlQuery("SELECT home_team FROM games WHERE id = :id")
    Optional<String> findHomeTeamByGameId(@Bind("id") Optional<Integer> id);
}
