package ch.ethz.covspectrum.controller.computed;

import ch.ethz.covspectrum.entity.api.CountAndProportionWithCI;
import ch.ethz.covspectrum.entity.api.Distribution;
import ch.ethz.covspectrum.entity.core.AAMutation;
import ch.ethz.covspectrum.entity.core.DataType;
import ch.ethz.covspectrum.entity.core.Variant;
import ch.ethz.covspectrum.entity.model.chen2021fitness.ApiResponse;
import ch.ethz.covspectrum.entity.model.chen2021fitness.Request;
import ch.ethz.covspectrum.entity.model.chen2021fitness.Response;
import ch.ethz.covspectrum.service.DatabaseService;
import ch.ethz.covspectrum.util.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/computed/model/chen2021Fitness")
public class Chen2021FitnessController {

    public final String ENDPOINT = "http://cov-spectrum-model-chen2021Fitness:7070";

    private final DatabaseService databaseService;


    public Chen2021FitnessController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping
    public Optional<ApiResponse> compute(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String country,
            @RequestParam String mutations,
            @RequestParam(defaultValue = "1") float matchPercentage,
            @RequestParam(required = false) DataType dataType,
            @RequestParam(defaultValue = "0.95") float alpha,
            @RequestParam(defaultValue = "4.8") float generationTime,
            @RequestParam(defaultValue = "1") float reproductionNumberWildtype,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plotStartDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plotEndDate,
            @RequestParam(defaultValue = "1000") int initialWildtypeCases,
            @RequestParam(defaultValue = "100") int initialVariantCases
    ) throws IOException, SQLException {
        // Get input data for the model
        Set<AAMutation> aaMutations = Arrays.stream(mutations.split(","))
                .map(AAMutation::new)
                .collect(Collectors.toSet());
        Variant variant = new Variant(aaMutations);
        var dailyTimeDistribution = databaseService.getDailyTimeDistribution(
                variant, region, country, matchPercentage, dataType, plotStartDate, plotEndDate);
        if (dailyTimeDistribution.size() < 3) {
            return Optional.empty();
        }

        // We need a mapping from calendar days to t. I.e., we need a day that is t=0. This shall be the minimum of
        // plotStartDate (if provided) and earliest date in the data.
        LocalDate t0Date = dailyTimeDistribution.stream()
                .map(Distribution::getX)
                .min(Comparator.comparing(LocalDate::toEpochDay))
                .get();
        if (plotStartDate.isBefore(t0Date)) {
            t0Date = plotStartDate;
        }

        // The plot will start at the plotStartDate
        int plotStartT = (int) ChronoUnit.DAYS.between(t0Date, plotStartDate);

        // Build data object
        List<Integer> t = new ArrayList<>();
        List<Integer> n = new ArrayList<>();
        List<Integer> k = new ArrayList<>();
        for (Distribution<LocalDate, CountAndProportionWithCI> distr : dailyTimeDistribution) {
            t.add((int) ChronoUnit.DAYS.between(t0Date, distr.getX()));
            n.add(distr.getY().getTotal());
            k.add(distr.getY().getCount());
        }

        // Build request JSON to the model
        Request request = new Request(
                new Request.InnerData(t, n, k),
                alpha,
                generationTime,
                reproductionNumberWildtype,
                plotStartT,
                (int) ChronoUnit.DAYS.between(t0Date, plotEndDate),
                initialWildtypeCases,
                initialVariantCases
        );
        String json = new ObjectMapper().writeValueAsString(request);

        // Send request
        String responseString = Utils.postRequest(ENDPOINT, json);
        Response response = new ObjectMapper().readValue(responseString, Response.class);

        // Return result
        ApiResponse apiResponse = new ApiResponse(
                new ApiResponse.Daily(
                        t2LocalDate(response.getDaily().getT(), t0Date),
                        response.getDaily().getProportion(),
                        response.getDaily().getCi_lower(),
                        response.getDaily().getCi_upper()
                ),
                new ApiResponse.Params(
                        new ApiResponse.ValueWithCI(
                                response.getParams().getA().getValue(),
                                response.getParams().getA().getCi_lower(),
                                response.getParams().getA().getCi_upper()
                        ),
                        new ApiResponse.ValueWithCI(
                                response.getParams().getT0().getValue(),
                                response.getParams().getT0().getCi_lower(),
                                response.getParams().getT0().getCi_upper()
                        ),
                        new ApiResponse.ValueWithCI(
                                response.getParams().getFc().getValue(),
                                response.getParams().getFc().getCi_lower(),
                                response.getParams().getFc().getCi_upper()
                        ),
                        new ApiResponse.ValueWithCI(
                                response.getParams().getFd().getValue(),
                                response.getParams().getFd().getCi_lower(),
                                response.getParams().getFd().getCi_upper()
                        )
                ),
                new ApiResponse.PlotAbsoluteNumbers(
                        t2LocalDate(response.getPlot_absolute_numbers().getT(), t0Date),
                        response.getPlot_absolute_numbers().getVariant_cases(),
                        response.getPlot_absolute_numbers().getWildtype_cases()
                ),
                new ApiResponse.PlotProportion(
                        t2LocalDate(response.getPlot_proportion().getT(), t0Date),
                        response.getPlot_proportion().getProportion(),
                        response.getPlot_proportion().getCi_lower(),
                        response.getPlot_proportion().getCi_upper()
                )
        );
        return Optional.of(apiResponse);
    }


    private List<LocalDate> t2LocalDate(List<Integer> t, LocalDate t0Date) {
        return t.stream().map(t0Date::plusDays).collect(Collectors.toList());
    }

}
