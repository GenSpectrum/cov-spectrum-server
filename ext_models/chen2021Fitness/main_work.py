from typing import Dict
import dataclasses
from typing import Dict
import numpy as np
from statsmodels.stats.proportion import proportion_confint
from base_classes import WithPredictionRequest, WithoutPredictionRequest, WithPredictionResult, PlotProportion, PointData
import model

def process_with_prediction(request_object: Dict) -> Dict:
    input = WithPredictionRequest(
        alpha=request_object["alpha"],
        generation_time=request_object["generationTime"],
        reproduction_number_wildtype=request_object["reproductionNumberWildtype"],
        data_t=np.array(request_object["data"]["t"]),
        data_n=np.array(request_object["data"]["n"]),
        data_k=np.array(request_object["data"]["k"]),
        plot_start_t=request_object["plotStartT"],
        plot_end_t=request_object["plotEndT"],
        initial_wildtype=request_object["initialWildtype"],
        initial_variant=request_object["initialVariant"]
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


def process_without_prediction(request_object: Dict) -> Dict:
    input = WithoutPredictionRequest(
        alpha=request_object["alpha"],
        generation_time=request_object["generationTime"],
        reproduction_number_wildtype=request_object["reproductionNumberWildtype"],
        data_t=np.array(request_object["data"]["t"]),
        data_n=np.array(request_object["data"]["n"]),
        data_k=np.array(request_object["data"]["k"])
    )
    growth, fitted_model = model.fit(input.alpha, input.data_t, input.data_k, input.data_n,
                                     input.generation_time, input.reproduction_number_wildtype)
    return dataclasses.asdict(growth)


def result_to_serializable_dict(result: WithPredictionResult) -> Dict:
    d = dataclasses.asdict(result)
    d["plot_absolute_numbers"]["t"] = d["plot_absolute_numbers"]["t"].tolist()
    d["plot_absolute_numbers"]["wildtype_cases"] = d["plot_absolute_numbers"]["wildtype_cases"].tolist()
    d["plot_absolute_numbers"]["variant_cases"] = d["plot_absolute_numbers"]["variant_cases"].tolist()

    d["plot_proportion"]["t"] = d["plot_proportion"]["t"].tolist()
    d["plot_proportion"]["proportion"] = d["plot_proportion"]["proportion"].tolist()
    d["plot_proportion"]["ci_lower"] = d["plot_proportion"]["ci_lower"].tolist()
    d["plot_proportion"]["ci_upper"] = d["plot_proportion"]["ci_upper"].tolist()

    d["daily"]["t"] = d["daily"]["t"].tolist()
    d["daily"]["proportion"] = d["daily"]["proportion"].tolist()
    d["daily"]["ci_lower"] = d["daily"]["ci_lower"].tolist()
    d["daily"]["ci_upper"] = d["daily"]["ci_upper"].tolist()
    return d

