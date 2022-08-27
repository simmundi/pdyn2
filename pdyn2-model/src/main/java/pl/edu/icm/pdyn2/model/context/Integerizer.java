package pl.edu.icm.pdyn2.model.context;

/**
 *  Utils for storing float values as fixed point numbers, to be
 *  used in contexts (to store contamination level and agent count).
 *
 *  Keeping three decimal places (i.e. 0.001 is represented as 1).
 *  allows us to store values up to around 500_000f, which
 *  should be enough for storing agents, since the four mostly populated
 *  square kilometers in Poland have - on the whole - less than 100 000 people
 *  in them.
 */

class Integerizer {
    private final static float PRECISION = 1000f;

    public static int toInt(float value) {
        return Math.round(value * PRECISION);
    }

    public static float toFloat(int value) {
        return (float)value / PRECISION;
    }


}
