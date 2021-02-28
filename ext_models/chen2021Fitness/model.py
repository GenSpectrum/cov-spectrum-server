from typing import Tuple

import numpy as np
from scipy.stats import norm
import statsmodels.api as sm
from statsmodels.discrete.discrete_model import BinaryResultsWrapper

from base_classes import GrowthNumbers, ValueWithCi, ValueWithCi2, PlotAbsoluteNumbers


def fit(
        alpha,
        t,
        k,
        n,
        generation_time,
        reproduction_number
) -> Tuple[GrowthNumbers, BinaryResultsWrapper]:
    # adapt the data to the required format
    x = np.array([])
    y = np.array([])
    for i in range(0, len(t)):
        y = np.concatenate(
            (np.concatenate((y, np.array([True] * k[i]))), np.array([False] * (n[i] - k[i]))))
        x = np.concatenate((x, np.array([t[i]] * n[i])))
    # add a column of 1's to be multiplied by the intercept
    X = sm.add_constant(x)

    # estimate the model
    model = sm.Logit(y, X).fit(disp=0)

    # The following cov matrix returned by the method is the same as invFI
    cov = model.cov_params()

    # We transform these two parameters for our parameterization of interest
    # and use a delta method to get the correct variance

    # take the MLE of the parameters as the functions of the MLE of beta0, beta
    beta0, beta1 = model.params
    t0, a, fd, fc = -beta0 / beta1, \
                    beta1, \
                    np.exp(beta1 * generation_time) - 1, \
                    beta1 * generation_time / reproduction_number

    # Build the Jacobian matrix with first derivatives of, in this order, t0, a, fd, fc
    Jac = np.array([-1 / beta1, beta0 / (beta1 ** 2),
                    0, 1,
                    0, generation_time * np.exp(beta1 * generation_time),
                    0, generation_time / reproduction_number]) \
        .reshape(4, 2)

    # New Sigma is obtained through the delta method
    Sigma = np.dot(np.dot(Jac, cov), Jac.T)

    # And now we can look for the CI with the level of interest
    q = norm.ppf(1 - ((1 - alpha) / 2))
    delta_t0 = np.sqrt(Sigma[0, 0]) * q
    delta_a = np.sqrt(Sigma[1, 1]) * q
    delta_fd = np.sqrt(Sigma[2, 2]) * q
    delta_fc = np.sqrt(Sigma[3, 3]) * q

    return GrowthNumbers(
        a=ValueWithCi(a, a - delta_a, a + delta_a),
        t0=ValueWithCi(t0, t0 - delta_t0, t0 + delta_t0),
        fd=ValueWithCi(fd, fd - delta_fd, fd + delta_fd),
        fc=ValueWithCi(fc, fc - delta_fc, fc + delta_fc)
    ), model


def predict(
        model: BinaryResultsWrapper,
        t: np.ndarray,
        alpha: float
) -> ValueWithCi2:
    X = sm.add_constant(t)
    proba = model.predict(X)
    cov = model.cov_params()

    # we need to estimate confidence interval for predicted probabilities using the delta method as well

    # the proportion is a function of the parameters
    # applied to the MLE, it is simply "proba".
    # and the Jacobian matrix taken at the different time points turns out to be
    D = (X.T * proba * (1 - proba)).T

    # We thus get the following standard errors
    std_errors = np.array([np.sqrt(np.dot(np.dot(d, cov), d)) for d in D])

    # And it is Gaussian, so we take the desired quantile
    q = norm.ppf(1 - ((1 - alpha) / 2))
    lower = np.maximum(0, np.minimum(1, proba - std_errors * q))
    upper = np.maximum(0, np.minimum(1, proba + std_errors * q))
    return ValueWithCi2(proba, lower, upper)


def predict_case_numbers(
        t,
        initial_wildtype,
        initial_variant,
        reproduction_number_wildtype,
        reproduction_number_variant,
        generation_time
) -> PlotAbsoluteNumbers:
    return PlotAbsoluteNumbers(
        t,
        np.round(initial_wildtype * (reproduction_number_wildtype ** (t / generation_time))).astype(int),
        np.round(initial_variant * (reproduction_number_variant ** (t / generation_time))).astype(int)
    )
