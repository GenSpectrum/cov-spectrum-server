###########################################################
# Based on https://github.com/cevo-public/Quantification-of-the-spread-of-a-SARS-CoV-2-variant/blob/main/code/Re/estimate_Re.R
# by JS Huisman
###########################################################
library(tidyverse)
library(lubridate)
library(viridis)
library(EpiEstim)

# This code assumes you have cloned the git repository
# https://github.com/covid-19-Re/shiny-dailyRe in the location
# app_dir
app_dir <- './shiny-dailyRe'
data_dir <- './data'
plot_dir <- './figures'

source(paste0(app_dir,'/app/otherScripts/2_utils_getInfectionIncidence.R'))
source(paste0(app_dir,'/app/otherScripts/3_utils_doReEstimates.R'))
###########################################################
source('Re_functions.R')
###########################################################

calculate <- function (data) {
  ## Deconvolve Data ####
  variant_deconv <- deconvolveIncidence(data, incidence_var = 'cases',
                                        getCountParams('incubation'),
                                        getCountParams('confirmed'),
                                        smooth_param = TRUE, n_boot = 1000)

  ## Estimate Re ####
  variant_Re <- getReBootstrap(variant_deconv, estimate_types = c("slidingWindow"))
  result <- variant_Re %>%
    select(date, median_R_mean, median_R_highHPD, median_R_lowHPD) %>%
    rename(
      re = median_R_mean,
      re_low = median_R_lowHPD,
      re_high = median_R_highHPD
    )

  return(result)
}
