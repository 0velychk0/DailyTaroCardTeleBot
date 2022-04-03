package com.ovelychko.webhookbotspring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ViberUserData {
    private String id;
    private String country;
    private String language;
    private Integer apiVersion;
    private String name;
    private String avatar;
}
