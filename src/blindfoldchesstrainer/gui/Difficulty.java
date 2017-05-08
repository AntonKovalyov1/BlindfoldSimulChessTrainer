package blindfoldchesstrainer.gui;

/**
 * Created by Anton on 3/15/2017.
 */
public enum Difficulty {

    EASY {
        @Override
        public String toString() {
            return "Easy";
        }

        @Override
        public int getDepth() {
            return 2;
        }
    },
    MEDIUM {
        @Override
        public String toString() {
            return "Medium";
        }

        @Override
        public int getDepth() {
            return 5;
        }
    },
    HARD {
        @Override
        public String toString() {
            return "Hard";
        }

        @Override
        public int getDepth() {
            return 8;
        }
        
    },
    INSANE {
        @Override
        public String toString() {
            return "Insane";
        }

        @Override
        public int getDepth() {
            return 12;
        }
        
    },
    RANDOM {
        @Override
        public String toString() {
            return "Random";
        }

        @Override
        public int getDepth() {
            return (int)(Math.random() * 3 + 1);
        }
    };

    public abstract int getDepth();
}
