package ch.ethz.covspectrum.controller.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;


@RestController
@RequestMapping("/resource/region")
public class RegionResourceController {

    @GetMapping("")
    public List<String> getAllRegions() {
        return Arrays.asList(
                "Africa",
                "Asia",
                "Europe",
                "North America",
                "South America",
                "Oceania"
        );
    }

}
