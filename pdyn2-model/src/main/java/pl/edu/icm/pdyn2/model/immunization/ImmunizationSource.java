package pl.edu.icm.pdyn2.model.immunization;

import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.trurl.ecs.annotation.MappedCollection;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static pl.edu.icm.trurl.util.Pair.of;

@WithMapper
public class ImmunizationSource {
    @MappedCollection(margin = 5)
    private List<TransmissionSource> sourcesList = new ArrayList<TransmissionSource>(5);

    public float getHouseholdSource() {
        for (int i = 0; i < sourcesList.size(); ++i) {
            if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.HOUSEHOLD) {
                return sourcesList.get(i).getProbability();
            }
        }
        return 0.0f;
    }

    public void setHouseholdSource(float householdSource) {
        if (householdSource > 0) {
            for (int i = 0; i < sourcesList.size(); ++i) {
                if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.HOUSEHOLD) {
                    sourcesList.set(i,
                            new TransmissionSource(TransmissionSource.SOURCES.HOUSEHOLD, householdSource));
                    return;
                }
            }
            sourcesList.add(new TransmissionSource(TransmissionSource.SOURCES.HOUSEHOLD, householdSource));
        }
    }

    public float getWorkplaceSource() {
        for (int i = 0; i < sourcesList.size(); ++i) {
            if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.WORKPLACE) {
                return sourcesList.get(i).getProbability();
            }
        }
        return 0.0f;
    }

    public void setWorkplaceSource(float workplaceSource) {
        if (workplaceSource > 0) {
            for (int i = 0; i < sourcesList.size(); ++i) {
                if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.WORKPLACE) {
                    sourcesList.set(i,
                            new TransmissionSource(TransmissionSource.SOURCES.WORKPLACE, workplaceSource));
                    return;
                }
            }
            sourcesList.add(new TransmissionSource(TransmissionSource.SOURCES.WORKPLACE, workplaceSource));
        }
    }

    public float getKindergartenSource() {
        for (int i = 0; i < sourcesList.size(); ++i) {
            if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.KINDERGARTEN) {
                return sourcesList.get(i).getProbability();
            }
        }
        return 0.0f;
    }

    public void setKindergartenSource(float kindergartenSource) {
        if (kindergartenSource > 0) {
            for (int i = 0; i < sourcesList.size(); ++i) {
                if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.KINDERGARTEN) {
                    sourcesList.set(i,
                            new TransmissionSource(TransmissionSource.SOURCES.KINDERGARTEN, kindergartenSource));
                    return;
                }
            }
            sourcesList.add(new TransmissionSource(TransmissionSource.SOURCES.KINDERGARTEN, kindergartenSource));
        }
    }

    public float getSchoolSource() {
        for (int i = 0; i < sourcesList.size(); ++i) {
            if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.SCHOOL) {
                return sourcesList.get(i).getProbability();
            }
        }
        return 0.0f;
    }

    public void setSchoolSource(float schoolSource) {
        if (schoolSource > 0) {
            for (int i = 0; i < sourcesList.size(); ++i) {
                if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.SCHOOL) {
                    sourcesList.set(i,
                            new TransmissionSource(TransmissionSource.SOURCES.SCHOOL, schoolSource));
                    return;
                }
            }
            sourcesList.add(new TransmissionSource(TransmissionSource.SOURCES.SCHOOL, schoolSource));
        }
    }

    public float getUniversitySource() {
        for (int i = 0; i < sourcesList.size(); ++i) {
            if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.UNIVERSITY) {
                return sourcesList.get(i).getProbability();
            }
        }
        return 0.0f;
    }

    public void setUniversitySource(float universitySource) {
        if (universitySource > 0) {
            for (int i = 0; i < sourcesList.size(); ++i) {
                if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.UNIVERSITY) {
                    sourcesList.set(i,
                            new TransmissionSource(TransmissionSource.SOURCES.UNIVERSITY, universitySource));
                    return;
                }
            }
            sourcesList.add(new TransmissionSource(TransmissionSource.SOURCES.UNIVERSITY, universitySource));
        }
    }

    public float getBigUniversitySource() {
        for (int i = 0; i < sourcesList.size(); ++i) {
            if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.BIG_UNIVERSITY) {
                return sourcesList.get(i).getProbability();
            }
        }
        return 0.0f;
    }

    public void setBigUniversitySource(float bigUniversitySource) {
        if (bigUniversitySource > 0) {
            for (int i = 0; i < sourcesList.size(); ++i) {
                if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.BIG_UNIVERSITY) {
                    sourcesList.set(i,
                            new TransmissionSource(TransmissionSource.SOURCES.BIG_UNIVERSITY, bigUniversitySource));
                    return;
                }
            }
            sourcesList.add(new TransmissionSource(TransmissionSource.SOURCES.BIG_UNIVERSITY, bigUniversitySource));
        }
    }

    public float getStreetSource() {
        for (int i = 0; i < sourcesList.size(); ++i) {
            if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.STREET) {
                return sourcesList.get(i).getProbability();
            }
        }
        return 0.0f;
    }

    public void setStreetSource(float streetSource) {
        if (streetSource > 0) {
            for (int i = 0; i < sourcesList.size(); ++i) {
                if (sourcesList.get(i).getSource() == TransmissionSource.SOURCES.STREET) {
                    sourcesList.set(i,
                            new TransmissionSource(TransmissionSource.SOURCES.STREET, streetSource));
                    return;
                }
            }
            sourcesList.add(new TransmissionSource(TransmissionSource.SOURCES.STREET, streetSource));
        }
    }
}
