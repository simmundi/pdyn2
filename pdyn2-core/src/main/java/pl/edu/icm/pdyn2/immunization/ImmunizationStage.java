package pl.edu.icm.pdyn2.immunization;

public enum ImmunizationStage {

    LATENTNY(0),
    OBJAWOWY(1),
    HOSPITALIZOWANY_BEZ_OIOM(2),
    HOSPITALIZOWANY_PRZED_OIOM(3);

    public final int id;

    ImmunizationStage(int id) {
        this.id = id;
    }
}
