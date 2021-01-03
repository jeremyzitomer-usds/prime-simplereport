package gov.cdc.usds.simplereport.db.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import gov.cdc.usds.simplereport.db.model.auxiliary.StreetAddress;

@Entity
public class Facility extends OrganizationScopedEternalEntity {

	@Column(nullable = false, unique = false) // unique within an organization only
	private String facilityName;

	// these are common to all the children of the immediate base class, but ...
	@Embedded
	private StreetAddress address;
	@Column
	private String telephone;

	@Column
	private String cliaNumber;

	@ManyToOne(optional = true)
	@JoinColumn(name = "default_device_type_id")
	private DeviceType defaultDeviceType;

	@OneToOne(optional=false)
	@JoinColumn(name = "ordering_provider_id", nullable = false)
	private Provider orderingProvider;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name = "facility_device_type",
		joinColumns = @JoinColumn(name="facility_id"),
		inverseJoinColumns = @JoinColumn(name="device_type_id")
	)
	private Set<DeviceType> configuredDevices = new HashSet<>();

    @ManyToOne(optional = true)
    @JoinColumn(name = "default_device_swab_id")
    private DeviceSwab defaultDeviceSwab;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "facility_device_swab",
            joinColumns = @JoinColumn(name = "facility_id"),
            inverseJoinColumns = @JoinColumn(name = "device_swab_id")
    )
    private Set<DeviceSwab> configuredDeviceSwabs = new HashSet<>();

    protected Facility() {/* for hibernate */}

	public Facility(Organization org, String facilityName, String cliaNumber, Provider orderingProvider) {
		super(org);
		this.facilityName = facilityName;
		this.cliaNumber = cliaNumber;
		this.orderingProvider = orderingProvider;
	}

	public Facility(Organization org,
			String facilityName,
			String cliaNumber,
			Provider orderingProvider,
			DeviceType defaultDeviceType) {
		this(org, facilityName, cliaNumber, orderingProvider);
		this.defaultDeviceType = defaultDeviceType;
		if (defaultDeviceType != null) {
			this.configuredDevices.add(defaultDeviceType);
		}
	}

	public Facility(
			Organization org,
			String facilityName,
			String cliaNumber,
			Provider orderingProvider,
			DeviceType defaultDeviceType,
			Collection<DeviceType> configuredDevices) {
		this(org, facilityName, cliaNumber, orderingProvider, defaultDeviceType);
		this.configuredDevices.addAll(configuredDevices);
	}

	public Facility(Organization org, String facilityName, String cliaNumber,
			StreetAddress facilityAddress, String phone,
			Provider orderingProvider, 
			DeviceType defaultDeviceType,
			List<DeviceType> configuredDeviceTypes) {
		this(org, facilityName, cliaNumber, orderingProvider, defaultDeviceType, configuredDeviceTypes);
		this.address = facilityAddress;
		this.telephone = phone;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public DeviceType getDefaultDeviceType() {
		return defaultDeviceType;
	}

	public void setDefaultDeviceType(DeviceType defaultDeviceType) {
		this.defaultDeviceType = defaultDeviceType;
	}

	public List<DeviceType> getDeviceTypes() {
		// this might be better done on the DB side, but that seems like a recipe for weird behaviors
		return configuredDevices.stream()
			.filter(e -> !e.isDeleted())
			.collect(Collectors.toList());
	}

	public void addDeviceType(DeviceType newDevice) {
		configuredDevices.add(newDevice);
	}

	public void addDefaultDeviceType(DeviceType newDevice) {
		this.defaultDeviceType = newDevice;
		addDeviceType(newDevice);
	}

	public void removeDeviceType(DeviceType existingDevice) {
		configuredDevices.remove(existingDevice);
		if (defaultDeviceType != null && existingDevice.getInternalId().equals(defaultDeviceType.getInternalId())) {
			defaultDeviceType = null;
		}
	}

	public String getCliaNumber() {
		return this.cliaNumber;
	}

	public void setCliaNumber(String cliaNumber) {
		this.cliaNumber = cliaNumber;
	}

	public Provider getOrderingProvider() {
		return orderingProvider;
	}

	public void setOrderingProvider(Provider orderingProvider) {
		this.orderingProvider = orderingProvider;
	}

	public StreetAddress getAddress() {
		return address;
	}

	public void setAddress(StreetAddress address) {
		this.address = address;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
}
