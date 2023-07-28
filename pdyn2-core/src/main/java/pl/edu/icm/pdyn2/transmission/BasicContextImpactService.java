/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.pdyn2.transmission;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.AgeRange;
import pl.edu.icm.pdyn2.model.AgeRanges;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.context.ContextTypes;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;
import pl.edu.icm.trurl.sampleSpace.SoftEnumSampleSpace;

/**
 * Logic for fractional membership in contexts.
 */
public class BasicContextImpactService implements ContextImpactService {

    private final ContextTypes contextTypes;
    private final AgeRanges ageRanges;
    private final float[] fractions;
    private final int contextTypeCount;

    @WithFactory
    public BasicContextImpactService(ContextTypes contextTypes, AgeRanges ageRanges) {
        this.contextTypes = contextTypes;
        this.ageRanges = ageRanges;
        contextTypeCount = contextTypes.values().size();
        fractions = new float[contextTypes.values().size() * ageRanges.values().size()];
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
     * @param agentEntity
     * @param context
     * @return
     */
    @Override
    public float calculateInfluenceFractionFor(Entity agentEntity, Entity contextEntity) {
        Context context = contextEntity.get(Context.class);
        Person person = agentEntity.get(Person.class);
        AgeRange ageRange = ageRanges.of(person.getAge());
        return fractions[idxFor(ageRange, context.getContextType())];
    }

    private int idxFor(AgeRange ageRange, ContextType contextType) {
        return ageRange.ordinal() * contextTypeCount + contextType.ordinal();
    }

    private void fillWithOnes() {
        for (int i = 0; i < fractions.length; i++) {
            fractions[i] = 1;
        }
    }

    private void fillFractionsForStreets() {
        for (AgeRange ageRange : ageRanges.values()) {
            SoftEnumSampleSpace<ContextType> sampleSpace = new SoftEnumSampleSpace<>(contextTypes);
            int ageBin = ageRange.ordinal();
            for (ContextType streetContext : contextTypes.streetContexts()) {
                int distance = ageBin - (streetContext.ordinal() - contextTypes.STREET_00.ordinal());
                int distanceSquared = distance * distance;
                sampleSpace.changeOutcome(streetContext, (float) Math.exp( - distanceSquared / 2.0));
            }
            sampleSpace.normalize();

            for (ContextType streetContext : contextTypes.streetContexts()) {
                int idx = idxFor(ageRange, streetContext);
                fractions[idx] = sampleSpace.getProbability(streetContext);
            }
        }
    }

}
