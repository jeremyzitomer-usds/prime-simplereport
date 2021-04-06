package gov.cdc.usds.simplereport.db.repository;

import gov.cdc.usds.simplereport.db.model.Facility;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.Person;
import gov.cdc.usds.simplereport.db.model.TestEvent;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

public interface TestEventRepository extends AuditedEntityRepository<TestEvent> {

  public static final String BASE_ORG_QUERY =
  "from #{#entityName} q "
      + "where q.organization = :org "
      + "and q.organization.isDeleted = false "
      + "and q.patient.isDeleted = false ";
  public static final String FACILITY_QUERY = BASE_ORG_QUERY + " and q.facility = :facility ";

  @Query("FROM #{#entityName} e WHERE e.patient = :p and e.facility in :facilities")
  public List<TestEvent> findAllByPatientAndFacilities(Person p, Collection<Facility> facilities);

  public List<TestEvent> findAllByOrganizationOrderByCreatedAtDesc(Organization o);

  public List<TestEvent> findAllByOrganizationAndFacility(Organization o, Facility f);

  public TestEvent findFirst1ByPatientOrderByCreatedAtDesc(Person p);

  @EntityGraph(attributePaths = {"patient", "order"})
  public TestEvent findByOrganizationAndInternalId(Organization o, UUID id);

  // Need to control how this query is built. "between" is too vague.
  // This is across all Orgs/facilities because datahub uploader users
  @Query(
      "FROM #{#entityName} q WHERE q.createdAt > :before AND q.createdAt <= :after ORDER BY q.createdAt")
  public List<TestEvent> queryMatchAllBetweenDates(Date before, Date after, Pageable p);

  @Query(
      value =
          "WITH FILTEREDEVENTS AS ("
              + " SELECT DISTINCT ON (test_order_id) * "
              + " FROM {h-schema}test_event te "
              + " WHERE te.facility_id = :facilityId "
              + " ORDER BY test_order_id, te.created_at desc"
              + ") "
              + " SELECT * FROM FILTEREDEVENTS "
              // moving this filter into the CTE makes this query significantly
              // more efficient (like 75% faster in one case), but then when we
              // make it more complicated somebody will probably break it
              + " ORDER BY created_at DESC ",
      countQuery = "SELECT count(*) FROM FILTEREDEVENTS",
      nativeQuery = true)
  public List<TestEvent> getTestEventResults(UUID facilityId, Pageable pageable);

  @Query(
      value =
          "WITH FILTEREDEVENTS AS ("
              + " SELECT DISTINCT ON (test_order_id) * "
              + " FROM {h-schema}test_event te "
              + " WHERE te.facility_id = :facilityId) "
              + " SELECT count(*) FROM FILTEREDEVENTS ",
      nativeQuery = true)
  public int getTestResultsCount(UUID facilityId);

  // @Query("FROM #{#entityName} q WHERE q.facility = :facility and q.createdAt >
  // :newerThanDate
  // ORDER BY q.createdAt DESC")
  // @EntityGraph(attributePaths = {"patient", "order", "order.patientLink"})
  // public List<TestEvent> getTestEventResults(Facility facility, Date
  // newerThanDate);

@Query(FACILITY_QUERY + 
  " and q.patient in :ps and q.createdAt >= :since")
public List<TestEvent> findByPatientsAndFacilityAndDate(
  Organization org,
  Facility facility,
  Collection<Person> ps,
  Date since);

@Query(FACILITY_QUERY + " and q.patient in :ps and (q.createdAt is null and q.createdAt is null)")
public List<TestEvent> findByPatientsAndFacility(
  Organization org,
  Facility facility,
  Collection<Person> ps);
}
