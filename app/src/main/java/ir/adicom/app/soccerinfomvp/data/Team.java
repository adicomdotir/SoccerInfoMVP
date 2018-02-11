package ir.adicom.app.soccerinfomvp.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.util.UUID;

/**
 * Immutable model class for a Team.
 */
public final class Team {

    @NonNull
    private final String mId;

    @Nullable
    private final String mTitle;

    @Nullable
    private final String mDescription;

    private final boolean mChampion;

    /**
     * Use this constructor to create a new normal Team.
     *
     * @param title       title of the team
     * @param description description of the team
     */
    public Team(@Nullable String title, @Nullable String description) {
        this(title, description, UUID.randomUUID().toString(), false);
    }

    /**
     * Use this constructor to create an normal Team if the Team already has an id (copy of another
     * Team).
     *
     * @param title       title of the team
     * @param description description of the team
     * @param id          id of the team
     */
    public Team(@Nullable String title, @Nullable String description, @NonNull String id) {
        this(title, description, id, false);
    }

    /**
     * Use this constructor to create a new champion Team.
     *
     * @param title       title of the team
     * @param description description of the team
     * @param champion   true if the team is champion, false if it's normal
     */
    public Team(@Nullable String title, @Nullable String description, boolean champion) {
        this(title, description, UUID.randomUUID().toString(), champion);
    }

    /**
     * Use this constructor to specify a champion Team if the Team already has an id (copy of
     * another Team).
     *
     * @param title       title of the team
     * @param description description of the team
     * @param id          id of the team
     * @param champion   true if the team is champion, false if it's normal
     */
    public Team(@Nullable String title, @Nullable String description,
                @NonNull String id, boolean champion) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mChampion = champion;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getTitleForList() {
        if (!Strings.isNullOrEmpty(mTitle)) {
            return mTitle;
        } else {
            return mDescription;
        }
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    public boolean isCompleted() {
        return mChampion;
    }

    public boolean isActive() {
        return !mChampion;
    }

    public boolean isEmpty() {
        return Strings.isNullOrEmpty(mTitle) &&
               Strings.isNullOrEmpty(mDescription);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equal(mId, team.mId) &&
               Objects.equal(mTitle, team.mTitle) &&
               Objects.equal(mDescription, team.mDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mTitle, mDescription);
    }

    @Override
    public String toString() {
        return "Team with title " + mTitle;
    }
}
