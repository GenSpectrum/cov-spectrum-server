import dataclasses
from typing import Dict
import numpy as np
from statsmodels.stats.proportion import proportion_confint
from base_classes import WithPredictionRequest, WithPredictionResult, PlotProportion, PointData
import model


def result_to_serializable_dict(result: WithPredictionResult) -> Dict:
    d = dataclasses.asdict(result)
    result = {
        "params": {
            "a": value_with_ci_to_json_dict(d["params"]["a"]),
            "t0": value_with_ci_to_json_dict(d["params"]["t0"]),
            "fc": value_with_ci_to_json_dict(d["params"]["fc"]),
            "fd": value_with_ci_to_json_dict(d["params"]["fd"])
        },
        "estimatedAbsoluteNumbers": {
            "t": d["plot_absolute_numbers"]["t"].tolist(),
            "wildtypeCases": d["plot_absolute_numbers"]["wildtype_cases"].tolist(),
            "variantCases": d["plot_absolute_numbers"]["variant_cases"].tolist()
        },
        "estimatedProportions": {
            "t": d["plot_proportion"]["t"].tolist(),
            "proportion": d["plot_proportion"]["proportion"].tolist(),
            "ciLower": d["plot_proportion"]["ci_lower"].tolist(),
            "ciUpper": d["plot_proportion"]["ci_upper"].tolist()
        }
    }
    return result


def value_with_ci_to_json_dict(valueWithCi):
    return {
        "value": valueWithCi["value"],
        "ciLower": valueWithCi["ci_lower"],
        "ciUpper": valueWithCi["ci_upper"]
    }


def process(request_object: Dict) -> Dict:
    input = WithPredictionRequest(
        alpha=request_object["config"]["alpha"],
        generation_time=request_object["config"]["generationTime"],
        reproduction_number_wildtype=request_object["config"]["reproductionNumberWildtype"],
        data_t=np.array(request_object["data"]["t"]),
        data_n=np.array(request_object["data"]["n"]),
        data_k=np.array(request_object["data"]["k"]),
        plot_start_t=request_object["config"]["tStart"],
        plot_end_t=request_object["config"]["tEnd"],
        initial_wildtype=request_object["config"]["initialCasesWildtype"],
        initial_variant=request_object["config"]["initialCasesVariant"]
    )
    growth, fitted_model = model.fit(input.alpha, input.data_t, input.data_k, input.data_n,
                                     input.generation_time, input.reproduction_number_wildtype)
    prediction_t = np.array(range(input.plot_start_t, input.plot_end_t + 1))
    predicted_proportions = model.predict(fitted_model, prediction_t, input.alpha)
    daily_ci = proportion_confint(input.data_k, input.data_n, alpha=1 - input.alpha, method="wilson")
    result = WithPredictionResult(
        growth,
        model.predict_case_numbers(prediction_t, input.initial_wildtype, input.initial_variant,
                                   input.reproduction_number_wildtype,
                                   (1 + growth.fc.value) * input.reproduction_number_wildtype,
                                   input.generation_time),
        PlotProportion(
            prediction_t,
            predicted_proportions.value,
            predicted_proportions.ci_lower,
            predicted_proportions.ci_upper
        ),
        PointData(
            input.data_t,
            input.data_k / input.data_n,
            daily_ci[0],
            daily_ci[1]
        )
    )
    return result_to_serializable_dict(result)
