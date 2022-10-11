package pl.edu.icm.pdyn2;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.administration.TestingConfig;
import pl.edu.icm.pdyn2.isolation.IsolationConfig;
import pl.edu.icm.pdyn2.transmission.TransmissionConfig;

public class EpidemicConfig {
    private final TransmissionConfig transmissionConfig;
    private final TestingConfig testingConfig;
    private final IsolationConfig isolationConfig;

    @WithFactory
    public EpidemicConfig(TransmissionConfig transmissionConfig,
                          TestingConfig testingConfig,
                          IsolationConfig isolationConfig) {
        this.transmissionConfig = transmissionConfig;
        this.testingConfig = testingConfig;
        this.isolationConfig = isolationConfig;
    }

    public EpidemicConfig workplace(float v) {
        transmissionConfig.setWorkplaceWeight(v);
        return this;
    }

    public EpidemicConfig household(float v) {
        transmissionConfig.setHouseholdWeight(v);
        return this;
    }

    public EpidemicConfig kindergarten(float v) {
        transmissionConfig.setKindergartenWeight(v);
        return this;
    }

    public EpidemicConfig school(float v) {
        transmissionConfig.setSchoolsWeight(v);
        return this;
    }

    public EpidemicConfig university(float v) {
        transmissionConfig.setUniversityWeight(v);
        return this;
    }

    public EpidemicConfig bigUniversity(float v) {
        transmissionConfig.setBigUniversityWeight(v);
        return this;
    }

    public EpidemicConfig street(float v) {
        transmissionConfig.setStreetWeight(v);
        return this;
    }

    public EpidemicConfig isolation(float v) {
        isolationConfig.setSelfIsolationWeight(v);
        return this;
    }

    @Override
    public String toString() {
        return String.format("Workplace: %.3f   Household: %.3f    Kindergarten: %.3f    School: %.3f    Uni: %.3f    BigUni: %.3f    Street: %.3f   Isolation: %.3f",
                transmissionConfig.getWorkplaceWeight(),
                transmissionConfig.getHouseholdWeight(),
                transmissionConfig.getKindergartenWeight(),
                transmissionConfig.getSchoolsWeight(),
                transmissionConfig.getUniversityWeight(),
                transmissionConfig.getBigUniversityWeight(),
                transmissionConfig.getStreetWeight(),
                isolationConfig.getSelfIsolationWeight());
    }
}
