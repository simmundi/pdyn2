package pl.edu.icm.pdyn2.behaviour;

import net.snowyhollows.bento.annotation.WithFactory;

public class TravelConfig {
    private float probabilityOfTravel;
    private float probabilityOfEndingTravel;

    @WithFactory
    public TravelConfig(float probabilityOfTravel, float probabilityOfEndingTravel) {
        this.probabilityOfTravel = probabilityOfTravel;
        this.probabilityOfEndingTravel = probabilityOfEndingTravel;
    }

    public float getProbabilityOfTravel() {
        return probabilityOfTravel;
    }

    public float getProbabilityOfEndingTravel() {
        return probabilityOfEndingTravel;
    }

    public void setProbabilityOfTravel(float probabilityOfTravel) {
        this.probabilityOfTravel = probabilityOfTravel;
    }

    public void setProbabilityOfEndingTravel(float probabilityOfEndingTravel) {
        this.probabilityOfEndingTravel = probabilityOfEndingTravel;
    }
}
