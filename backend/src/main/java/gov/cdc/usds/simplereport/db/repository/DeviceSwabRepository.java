package gov.cdc.usds.simplereport.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import gov.cdc.usds.simplereport.db.model.DeviceSwab;

public interface DeviceSwabRepository extends EternalEntityRepository<DeviceSwab> {

    @Override
    @EntityGraph(attributePaths = { "deviceType", "swabType" })
    @Query(BASE_QUERY + " and e.deviceType.isDeleted = false and e.swabType.isDeleted = false")
    public List<DeviceSwab> findAll();

}
