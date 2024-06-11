package se.sundsvall.smloader.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.smloader.service.AsyncExecutorService;

import java.time.LocalDateTime;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;

@RestController
@Validated
@Tag(name = "Jobs", description = "Jobs resource")
@RequestMapping("/jobs")
public class JobsResource {

	private final AsyncExecutorService asyncExecutorService;

	public JobsResource(AsyncExecutorService asyncExecutorService) {
		this.asyncExecutorService = asyncExecutorService;
	}

	@PostMapping(path = "/caseexporter", produces = APPLICATION_PROBLEM_JSON_VALUE)
	@Operation(summary = "Triggers export of errands (to SupportManagement) job.", description = "Triggers export errands (to SupportManagement) job.")
	@ApiResponse(responseCode = "204", description = "Successful operation", content = @Content(mediaType = ALL_VALUE, schema = @Schema(implementation = Void.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> caseeexporter() {
		asyncExecutorService.exportCases();
		return noContent().build();
	}

	@PostMapping(path = "/caseimporter", produces = APPLICATION_PROBLEM_JSON_VALUE)
	@Operation(summary = "Triggers import of cases (from OpenE) job.", description = "Triggers import of cases (from OpenE) job.")
	@ApiResponse(responseCode = "204", description = "Successful operation", content = @Content(mediaType = ALL_VALUE, schema = @Schema(implementation = Void.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> caseimporter(
		@Parameter(description = "From date for the cases to import", example = "2024-01-01T12:00:00") @NotNull @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime from,
		@Parameter(description = "To date for the cases to import", example = "2024-01-31T12:00:00") @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime to) {
		asyncExecutorService.importCases(from, to);
		return noContent().build();
	}

	@PostMapping(path = "/dbcleaner", produces = APPLICATION_PROBLEM_JSON_VALUE)
	@Operation(summary = "Triggers database cleaning job.", description = "Triggers database cleaning job.")
	@ApiResponse(responseCode = "204", description = "Successful operation", content = @Content(mediaType = ALL_VALUE, schema = @Schema(implementation = Void.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> dbcleaner(@Parameter(description = "From date for cleaning older cases", example = "2024-01-01T12:00:00") @NotNull @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime from) {
		asyncExecutorService.databaseCleanerExecute(from);
		return noContent().build();
	}
}
