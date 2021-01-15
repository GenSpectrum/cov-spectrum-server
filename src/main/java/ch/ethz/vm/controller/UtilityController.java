package ch.ethz.vm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.extra.YearWeek;


@RestController
@RequestMapping("/utils")
public class UtilityController {

    @GetMapping("/current-week")
    public int getCurrentWeek() {
        return YearWeek.now().getWeek();
    }

}
