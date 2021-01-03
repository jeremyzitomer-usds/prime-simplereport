package gov.cdc.usds.simplereport.db.model;

import javax.persistence.Column;

import gov.cdc.usds.simplereport.validators.RequiredNumericCode;

/**
 * A SNOMED-registered swab type that can be used by one or more
 * {@link DeviceType}s.
 */
public class SwabType extends EternalEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @RequiredNumericCode
    private String typeCode;

    @Column(nullable = false)
    @RequiredNumericCode
    private String siteCode;

}
