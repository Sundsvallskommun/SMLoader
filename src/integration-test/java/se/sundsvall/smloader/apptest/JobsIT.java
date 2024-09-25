package se.sundsvall.smloader.apptest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.smloader.Application;
import se.sundsvall.smloader.integration.db.CaseMappingRepository;
import se.sundsvall.smloader.integration.db.CaseRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.CREATED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.FAILED;
import static se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.PENDING;

/**
 * JobsIT tests.
 */
@WireMockAppTestSuite(files = "classpath:/JobsIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class JobsIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/jobs";
	@Autowired
	private CaseRepository repository;

	@Autowired
	private CaseMappingRepository caseMappingRepository;

	@Test
	void test01_import() {

		// Assert that we don't have the records we are going to import.
		assertThat(repository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, MUNICIPALITY_ID).stream()
			.filter(caseEntity -> caseEntity.getExternalCaseId().equals("123456") || caseEntity.getExternalCaseId().equals("234567") ||
				caseEntity.getExternalCaseId().equals("111111"))
			.toList()).isEmpty();

		// Call
		setupCall()
			.withServicePath(PATH + "/caseimporter?from=2024-06-30T12:00:00&to=2024-07-02T12:00:00")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.andVerifyThat(() -> repository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, MUNICIPALITY_ID).stream()
				.filter(caseEntity -> caseEntity.getExternalCaseId().equals("123456") || caseEntity.getExternalCaseId().equals("234567") ||
					caseEntity.getExternalCaseId().equals("111111"))
				.toList().size() == 3);
	}

	@Test
	void test02_export() {

		// Assert that we have records with status PENDING.
		assertThat(repository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, MUNICIPALITY_ID)).isNotEmpty();

		// Call
		setupCall()
			.withServicePath(PATH + "/caseexporter")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.andVerifyThat(() -> repository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(PENDING, MUNICIPALITY_ID).isEmpty());
	}

	@Test
	void test03_clean_db() {

		// Assert that we have records with status CREATED and FAILED.
		assertThat(repository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(CREATED, MUNICIPALITY_ID)).isNotEmpty();
		assertThat(repository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(FAILED, MUNICIPALITY_ID)).isNotEmpty();
		assertThat(caseMappingRepository.findAll()).isNotEmpty();

		// Call
		setupCall()
			.withServicePath(PATH + "/dbcleaner?from=2024-07-31T12:00:00")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.andVerifyThat(() -> repository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(CREATED, MUNICIPALITY_ID).isEmpty())
			.andVerifyThat(() -> repository.findAllByDeliveryStatusAndCaseMetaDataEntityMunicipalityId(FAILED, MUNICIPALITY_ID).isEmpty())
			.andVerifyThat(() -> caseMappingRepository.findAll().isEmpty());
	}
}
