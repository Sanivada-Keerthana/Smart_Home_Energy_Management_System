package com.srems.srems.model;
import jakarta.persistence.*;

@Entity
public class CommonArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Long commonAreaId;

    @Column(nullable = false, unique = true)
    private String areaName;

    // constructors
    public CommonArea() {}
    public CommonArea(String areaName) {
        this.areaName = areaName;
    }

    // getters & setters

    public Long getCommonAreaId() {
        return commonAreaId;
    }
    public String getAreaName() {
        return areaName;
    }
    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    
}
