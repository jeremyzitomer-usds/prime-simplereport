package gov.cdc.usds.simplereport.db.model;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * A valid combination of device and swab types. Can be soft-deleted.
 */
public class DeviceSwab extends EternalEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_type_id", nullable = false)
    private DeviceType deviceType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "swab_type_id", nullable = false)
    private SwabType swabType;
}
