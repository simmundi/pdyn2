package pl.edu.icm.pdyn2.model.context;

public enum ContextType {
    HOUSEHOLD(ContextInfectivityClass.HOUSEHOLD),
    WORKPLACE(ContextInfectivityClass.WORKPLACE),
    KINDERGARTEN(ContextInfectivityClass.KINDERGARTEN),
    SCHOOL(ContextInfectivityClass.SCHOOL),
    UNIVERSITY(ContextInfectivityClass.UNIVERSITY),
    BIG_UNIVERSITY(ContextInfectivityClass.BIG_UNIVERSITY),
    STREET_00(ContextInfectivityClass.STREET),
    STREET_10(ContextInfectivityClass.STREET),
    STREET_20(ContextInfectivityClass.STREET),
    STREET_30(ContextInfectivityClass.STREET),
    STREET_40(ContextInfectivityClass.STREET),
    STREET_50(ContextInfectivityClass.STREET),
    STREET_60(ContextInfectivityClass.STREET),
    STREET_70(ContextInfectivityClass.STREET),
    STREET_80(ContextInfectivityClass.STREET),
    STREET_90(ContextInfectivityClass.STREET);

    private final ContextInfectivityClass infectivityClass;

    ContextType(ContextInfectivityClass infectivityClass) {
        this.infectivityClass = infectivityClass;
    }

    public ContextInfectivityClass getInfectivityClass() {
        return infectivityClass;
    }

    public static ContextType[] streetContexts() {
        return new ContextType[]{
                ContextType.STREET_00, ContextType.STREET_10,
                ContextType.STREET_20, ContextType.STREET_30,
                ContextType.STREET_40, ContextType.STREET_50,
                ContextType.STREET_60, ContextType.STREET_70,
                ContextType.STREET_80, ContextType.STREET_90,
        };
    }
}
