package pl.edu.icm.pdyn2.transmission;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.AgeRange;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;

/**
 * Logic for fractional membership in contexts.
 */
public class ContextImpactService {

    public static final int AGE_RANGE_COUNT = AgeRange.values().length;
    public static final int CONTEXT_TYPE_COUNT = ContextType.values().length;

    private final float[] fractions = new float[AGE_RANGE_COUNT * CONTEXT_TYPE_COUNT];

    @WithFactory
    public ContextImpactService() {
        fillWithOnes();
        fillFractionsForStreets();
    }

    /**
     * Returns fraction (between 0 and 1) of person's viral load and count to be taken into account
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
    public float calculateInfluenceFractionFor(Person person, Context context) {
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
                int distance = ageBin - (streetContext.ordinal() - ContextType.STREET_00.ordinal());
                int distanceSquared = distance * distance;
                if (ageBin == 0 && streetContext == ContextType.STREET_00) {
                    sampleSpace.changeOutcome(streetContext, (float) 1);
                } else {
                    sampleSpace.changeOutcome(streetContext, (float) 0);
                }
            }
//            sampleSpace.normalize();

            for (ContextType streetContext : ContextType.streetContexts()) {
                int idx = idxFor(ageRange, streetContext);
                fractions[idx] = sampleSpace.getProbability(streetContext);
            }
        }
    }

}
