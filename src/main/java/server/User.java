package server;

import org.mindrot.jbcrypt.BCrypt;

public class User {
    public class UserInfo {
        private final String nickname;
        private long userScore;
        private long wins;
        private long losses;
        private float rateo;

        private UserInfo(String nickname) {
            this.nickname = nickname;
            userScore = 0;
            wins = 0;
            losses = 0;
            rateo = 0;
        }

        public String getNickname() {
            return nickname;
        }

        public long getUserScore() {
            return userScore;
        }

        private boolean addMatchScore(long matchScore) {
            if (matchScore < 0) {
                return false;
            }

            userScore += matchScore;

            return true;
        }

        public long getWins() {
            return wins;
        }

        private void addWin() {
            wins++;
            setRateo();
        }

        public long getLosses() {
            return losses;
        }

        private void addLoss() {
            losses++;
            setRateo();
        }

        public float getRateo() {
            return rateo;
        }

        private void setRateo() {
            rateo = wins / losses;
        }
    }

    private final String hash;
    private UserInfo info;

    public User(String nickname, String password) {
        hash = BCrypt.hashpw(password, BCrypt.gensalt());
        info = new UserInfo(nickname);
    }

    public User(String nickname) {
        info = new UserInfo(nickname);
        hash = null;
    }

    public UserInfo getInfo() {
        return info;
    }
}
