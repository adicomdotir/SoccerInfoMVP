package ir.adicom.app.soccerinfomvp.teams;

/**
 * Used with the filter spinner in the teams list.
 */
public enum TeamsFilterType {
    /**
     * Do not filter teams.
     */
    ALL_TEAMS,

    /**
     * Filters only the top (champion) teams.
     */
    TOP_TEAMS,

    /**
     * Filters only the normal teams.
     */
    NORMAL_TEAMS
}
