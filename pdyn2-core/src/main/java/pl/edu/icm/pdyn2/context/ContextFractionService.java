package pl.edu.icm.pdyn2.context;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.AgeRange;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;

/**
 * Logic for fractional membership in contexts.
 */
public class ContextFractionService {

    public static final int AGE_RANGE_COUNT = AgeRange.values().length;
    public static final int CONTEXT_TYPE_COUNT = ContextType.values().length;

    private final float[] fractions = new float[AGE_RANGE_COUNT * CONTEXT_TYPE_COUNT];

    @WithFactory
    public ContextFractionService() {
        fillWithOnes();
        fillFractionsForStreets();
    }

    /**
     * Returns fraction (between 0 and 1) of person's viral load to be taken into account
     * in the given context.
     *
     * The current implementation generally returns 1, except for street contexts.
     * Street contexts are age-specific (to allow for mixing), and the fraction of the load
     * per context depends on person's age and the exact street context type.
     *
     * @param person
     * @param context
     * @return
     */
    public float calculateLoadFractionFor(Person person, Context context) {
        AgeRange ageRange = AgeRange.of(person.getAge());
        return fractions[idxFor(ageRange, context.getContextType())];
    }

    private int idxFor(AgeRange ageRange, ContextType contextType) {
        return ageRange.ordinal() * CONTEXT_TYPE_COUNT + contextType.ordinal();
    }

    private void fillWithOnes() {
        for (int i = 0; i < fractions.length; i++) {
            fractions[i] = 1;
        }
    }

    private void fillFractionsForStreets() {
        for (AgeRange ageRange : AgeRange.values()) {
            EnumSampleSpace<ContextType> sampleSpace = new EnumSampleSpace<>(ContextType.class);
            int ageBin = ageRange.ordinal();
            for (ContextType streetContext : ContextType.streetContexts()) {
                int distance = Math.abs(ageBin - (streetContext.ordinal() - ContextType.STREET_00.ordinal()));
                sampleSpace.changeOutcome(streetContext, (float) Math.exp( - distance / 2.0));
            }
            sampleSpace.normalize();

            for (ContextType streetContext : ContextType.streetContexts()) {
                int idx = idxFor(ageRange, streetContext);
                fractions[idx] = sampleSpace.getProbability(streetContext);
            }
        }
    }

}
