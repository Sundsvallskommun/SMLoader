package se.sundsvall.smloader.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.DeliveryStatus;
import se.sundsvall.smloader.integration.db.model.Instance;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Transactional(isolation = READ_COMMITTED)
@CircuitBreaker(name = "CaseRepository")
public interface CaseRepository extends JpaRepository<CaseEntity, String> {
	List<CaseEntity> findAllByDeliveryStatus(DeliveryStatus deliveryStatus);
	boolean existsByOpenECaseIdAndInstance(String openECaseId, Instance instance);
}
