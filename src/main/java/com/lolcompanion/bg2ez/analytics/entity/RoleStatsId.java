package com.lolcompanion.bg2ez.analytics.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class RoleStatsId implements Serializable {
    private String puuid;
    private String role;
}