package blindfoldchesstrainer.engine.player.ai;

import java.util.Comparator;

/**
 *
 * @author Anton
 */
public enum EvaluatedMoveComparator implements Comparator<EvaluatedMove> {
    ASCENDING {
        @Override
        public int compare(EvaluatedMove o1, EvaluatedMove o2) {
            return Integer.compare(o1.getValue(), o2.getValue());
        }
    },
    DESCENDING {
        @Override
        public int compare(EvaluatedMove o1, EvaluatedMove o2) {
            return -Integer.compare(o1.getValue(), o2.getValue());
        }
    };
}
