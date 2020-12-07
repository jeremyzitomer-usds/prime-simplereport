package gov.cdc.usds.simplereport.api.model;

import java.util.List;
import java.util.UUID;

import gov.cdc.usds.simplereport.db.model.DeviceType;
import gov.cdc.usds.simplereport.db.model.Facility;
import gov.cdc.usds.simplereport.db.model.auxiliary.StreetAddress;

public class ApiFacility {

	private Facility wrapped;

	public ApiFacility(Facility wrapped) {
		super();
		this.wrapped = wrapped;
	}

	public UUID getId() {
		return wrapped.getInternalId();
	}

	public String getName() {
		return wrapped.getFacilityName();
	}

	public String getCliaNumber() {
		return wrapped.getCliaNumber();
    }

	public ApiProvider getOrderingProvider() {
		return new ApiProvider(wrapped.getOrderingProvider());
	}

	public DeviceType getDefaultDeviceType() {
		return wrapped.getDefaultDeviceType();
	}

	public List<DeviceType> getDeviceTypes() {
		return wrapped.getDeviceTypes();
	}

	public StreetAddress getAddress() {
		return wrapped.getAddress();
	}

	public String getStreet() {
		return wrapped.getAddress().getStreetOne();
	}

	public String getStreetTwo() {
		return wrapped.getAddress().getStreetTwo();
	}

	public String getCity() {
		return wrapped.getAddress().getCity();
	}

	public String getCounty() {
    return wrapped.getAddress().getCounty();
	}

	public String getState() {
		return wrapped.getAddress().getState();
	}

	public String getZipCode() {
		return wrapped.getAddress().getPostalCode();
	}

	public String getPhone() {
		return wrapped.getTelephone();
	}
}
