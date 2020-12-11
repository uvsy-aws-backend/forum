package app.uvsy.controllers.report;

import app.uvsy.controllers.report.payload.ReportCommentPayload;
import app.uvsy.controllers.report.payload.ReportPublicationPayload;
import app.uvsy.service.ReportService;
import org.github.serverless.api.annotations.HttpMethod;
import org.github.serverless.api.annotations.handler.Handler;
import org.github.serverless.api.annotations.parameters.BodyParameter;

public class ReportController {

    private final ReportService reportService;

    public ReportController(){
        this(new ReportService());
    }
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Handler(method = HttpMethod.POST, resource = "/v1/reports/publication")
    public void createPublicationReport(@BodyParameter ReportPublicationPayload payload) {
        reportService.reportPublication(payload.getPublicationId(), payload.getUserId());
    }

    @Handler(method = HttpMethod.POST, resource = "/v1/reports/comment")
    public void createCommentReport(@BodyParameter ReportCommentPayload payload) {
        reportService.reportComment(payload.getCommentId(), payload.getUserId());
    }

}
