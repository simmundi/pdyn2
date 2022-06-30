package pl.edu.icm.pdyn2.sowing;

import pl.edu.icm.board.agesex.AgeSex;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class ImmunizationRecordFromCsv {

    private AgeSex ageSex;
    private int days;
    private Load load;
    private String areaCode;
    private int recordCount;

    public AgeSex getAgeSex() {
        return ageSex;
    }

    public void setAgeSex(AgeSex ageSex) {
        this.ageSex = ageSex;
    }

    public void setAgeSex(String ageSex) {
        if (!ageSex.equals("")) {
            this.ageSex = AgeSex.valueOf(ageSex);
        }
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public void setDays(String days) {
        if (days.equals("")) {
            this.days = -1;
        } else {
            this.days = Integer.parseInt(days);
        }
    }

    public Load getLoad() {
        return load;
    }

    public void setLoad(Load load) {
        this.load = load;
    }

    public void setLoad(String load) {
        if (!load.equals("")) {
            this.load = Load.valueOf(load);
        }
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }
}
